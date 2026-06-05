import { Component, inject, input, output, signal, TemplateRef, viewChild } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { IFile } from '@routes/agent-center/app-knowledge/knowledge.types';
import { TagSelectComponent } from '@routes/knowledge-center/components/tag-select/tag-select.component';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { cloneDeep } from 'lodash';
import { FormsModule } from '@angular/forms';
import { NzModalService, NzModalRef, NzModalModule } from 'ng-zorro-antd/modal';
import { NzDrawerService, NzDrawerRef, NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';

@Component({
  selector: 'kb-file-tag-edit',
  templateUrl: './kb-file-tag-edit.component.html',
  styleUrls: ['./kb-file-tag-edit.component.less'],
  standalone: true,
  imports: [
    MODULES,
    TagSelectComponent,
    FormsModule,
    NzModalModule,
    NzDrawerModule,
    NzFormModule,
    NzTableModule,
    NzButtonModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.KNOWLEDGE, I18nNamespace.COMMON],
    },
    NzModalService,
    NzDrawerService,
    NzMessageService
  ],
})
export class KbFileTagEditComponent {
  private readonly nzModalService = inject(NzModalService);
  private readonly nzDrawerService = inject(NzDrawerService);
  private readonly nzMessageService = inject(NzMessageService);

  private readonly kbApiService = inject(KnowledgeRepoService);
  private readonly i18n = inject(I18NextEagerPipe);

  singleTagEdit = viewChild<TemplateRef<any>>('singleTagEdit');
  singleTagEditFooter = viewChild<TemplateRef<any>>('singleTagEditFooter');
  multiTagEdit = viewChild<TemplateRef<any>>('multiTagEdit');
  multiTagEditFooter = viewChild<TemplateRef<any>>('multiTagEditFooter');

  kbId = input.required<string>();
  success = output();
  files = signal<IFile[]>([]);

  isConfirmLoading = false;
  private modalRef: NzModalRef | null = null;
  private drawerRef: NzDrawerRef<any> | null = null;

  constructor() {}

  showModal(files: IFile[], isMulti = true) {
    this.files.set(cloneDeep(files));

    if (isMulti) {
      this.drawerRef = this.nzDrawerService.create({
        nzTitle: this.i18n.transform('edit_tag'),
        nzContent: this.multiTagEdit(),
        nzFooter: this.multiTagEditFooter(),
        nzWidth: 700,
      });
    } else {
      this.modalRef = this.nzModalService.create({
        nzTitle: this.i18n.transform('edit_tag'),
        nzContent: this.singleTagEdit(),
        nzFooter: this.singleTagEditFooter(),
      });
    }
  }

  handleSingleTagSubmit() {
    this.isConfirmLoading = true;
    this.kbApiService.editFileInfo(this.kbId(), this.files()[0].file_id, { tags: this.files()[0].file_tags })
      .then(() => {
        this.nzMessageService.success(this.i18n.transform('tag_edit_success'));
        this.isConfirmLoading = false;
        this.modalRef?.destroy();
        this.success.emit();
      })
      .catch(() => {
        this.isConfirmLoading = false;
      });
  }

  closeModal() {
    this.modalRef?.destroy();
  }

  closeDrawer() {
    this.drawerRef?.close();
  }
}
