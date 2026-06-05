import {
  Component,
  EventEmitter,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { I18nNamespace } from '@i18n';
import { KbUtils } from '@routes/knowledge-center/kb.utils';
import { MODULES } from '@shared/modules';
import { CommonValidation } from '@shared/validation/commonValidation';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { UploadImageComponent } from '@routes/knowledge-center/components/upload-image/upload-image.component';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'knowledge-base-edit',
  templateUrl: './knowledge-base-edit.component.html',
  styleUrls: ['./knowledge-base-edit.component.less'],
  standalone: true,
  imports: [
    MODULES,
    UploadImageComponent,
    NzDrawerModule,
    NzFormModule,
    NzInputModule,
    NzButtonModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.KNOWLEDGE,
      ],
    },
  ],
})
export class KnowledgeBaseEditComponent implements OnInit {
  @Output() getList = new EventEmitter();
  @ViewChild('uploadImage') uploadImage: any;

  isVisible = false;
  knowledgeBaseData: any;
  iconName = '';

  formGroup = new UntypedFormGroup({
    name: new UntypedFormControl('', [
      Validators.required,
      CommonValidation.customVerify(
        /^[A-Za-z0-9\u4e00-\u9fa5][A-Za-z0-9\u4e00-\u9fa5_-]{0,49}$/,
        this.i18n.transform('knowledge_name_validation'),
      ),
    ]),
    description: new UntypedFormControl('', [Validators.required]),
  });

  constructor(
    private knowledgeRepoServ: KnowledgeRepoService,
    private i18n: I18NextEagerPipe,
    private message: NzMessageService
  ) {}

  ngOnInit(): void {}

  // 打开编辑知识库弹窗
  public openEditKnowledgeBase(data): void {
    this.knowledgeBaseData = data;
    this.formGroup.setValue({
      name: this.knowledgeBaseData.name,
      description: this.knowledgeBaseData.description,
    });
    this.isVisible = true;
    if (this.uploadImage) {
      this.uploadImage.imagePreview = data.icon;
    }
  }

  clearValue() {
    this.iconName = '';
    this.isVisible = false;
  }

  public formValidate() {
    if (this.formGroup.valid) {
      return true;
    }
    Object.values(this.formGroup.controls).forEach(control => {
      if (control.invalid) {
        control.markAsDirty();
        control.updateValueAndValidity({ onlySelf: true });
      }
    });
    return false;
  }

  async onImageChanged(file: any) {
    if (!file) {
      return;
    }
    try {
      KbUtils.readFileAsBase64(file).then(res => {
        this.iconName = res;
      })
    } finally {
    }
  }

  handleConfirm() {
    if (!this.formValidate()) {
      return;
    }
    let params: any = {
      display_name: this.formGroup.get('name').value,
      description: this.formGroup.get('description').value,
    };
    if (this.iconName !== '') {
      params.icon = this.iconName;
    }
    this.knowledgeRepoServ
      .editKnowledgeBase(this.knowledgeBaseData.knowledge_base_id, params)
      .then(() => {
        this.message.success(this.i18n.transform('edit_success_1'), { nzDuration: 3000 });
        this.getList.emit();
        this.clearValue();
      });
  }
}
