import { Component, OnInit, ChangeDetectionStrategy, Input, inject, Inject, EventEmitter, Output } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import * as angularI18next from 'angular-i18next';
import { PromptEditorService } from '@services/prompt-editor.service';
import { IPromptChunk } from '@interfaces/prompt/prompt-editor.interface';
import { PromptEditorComponent } from '@shared/components/prompt-editor/prompt-editor.component';
import { TmplVariantTagComponent } from '@shared/components/tmpl-variant-tag/tmpl-variant-tag.component';
import { NzSelectModule } from 'ng-zorro-antd/select';
import * as candidateTemplateListInterface from '@interfaces/prompt/candidate-template-list.interface';
import { TaskService } from '@services/task.service';
import { FormBuilder, FormControl, FormGroup, ValidationErrors, AbstractControl, Validators } from '@angular/forms';
import { CommonValidation } from '@shared/validation/commonValidation';
import { RouteParamsService } from '@services/routeParams.service';
import { isNil } from 'lodash';
import { CommonUtils } from 'src/utils/common.util';
import { ConsoleFrameworkService } from '@core/services';
import { PromptService } from '@services/prompt.service';
import { NzModalRef, NZ_MODAL_DATA } from 'ng-zorro-antd/modal';

@Component({
  selector: 'meta-save-tmpl-library-modal',
  templateUrl: './save-tmpl-library-modal.component.html',
  styleUrls: ['./save-tmpl-library-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [MODULES, PromptEditorComponent, TmplVariantTagComponent, NzSelectModule],
  providers: [
    {
      provide: angularI18next.I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PROMPT_PLATFORM],
    },
  ],
})
export class SaveTmplLibraryModalComponent implements OnInit {
  currentTmpl: candidateTemplateListInterface.ICandidateTmplData;
  @Output() modalClose = new EventEmitter<any>();
  readonly modalRef = inject(NzModalRef);
  public templateName = '';

  public modalContent: IPromptChunk[];

  public tags: string[];

  public industrys: string[];

  public formName: FormGroup;

  public lang = CommonUtils.getLanguage();

  public labelOptions = [];

  public industryOptions = [];

  public language = this.consoleFrameworkService?.dataService?.getLocale() ?? 'zh-cn';

  constructor(
    private promptEditorServ: PromptEditorService,
    private taskServ: TaskService,
    fb: FormBuilder,
    private routeParamsServe: RouteParamsService,
    private i18NextEagerPipe: angularI18next.I18NextEagerPipe,
    private readonly consoleFrameworkService: ConsoleFrameworkService,
    private service: PromptService,
    @Inject(NZ_MODAL_DATA) public nzData: any
  ) {
    this.currentTmpl = this.nzData.currentTmpl;
    this.formName = fb.group({
      templateName: new FormControl(this.routeParamsServe.getTemplateName(), [
        CommonValidation.templateNameVerify('请输入以中文、字母开头，以中文、字母、数字结尾，长度2~20的字符。只允许输入中文、字母、数字、下划线、中划线等字符'),
      ]),
      tags: new FormControl([], []),
      industrys: new FormControl([], this.templateindustrysVerify()),
    });
  }

  public templateindustrysVerify() {
    return (control: AbstractControl) => {
      if (!this.currentTmpl?.is_workflow) {
        return null;
      }

      const v: string = control.value || '';
      return v && v.length > 0
        ? null
        : {
            nameChAllow: {
              tiErrorMessage: this.i18NextEagerPipe.transform('select_placeholder'),
            },
          };
    };
  }

  showFromErrorByName(form, validatorName): boolean {
    let res = true;
    const keysList = Object.keys(form.controls);
    if (keysList) {
      for (let i = 0; i < keysList.length; i++) {
        form.controls[keysList[i]].markAsDirty();
        form.controls[keysList[i]].updateValueAndValidity({ onlySelf: false });
        if (validatorName?.length > 0) {
          for (let j = 0; j < validatorName.length; j++) {
            if (keysList[i].indexOf(validatorName[j]) === 0) {
              if (form.controls[keysList[i]].errors) {
                res = false;
              }
            }
          }
        } else {
          if (form.controls[keysList[i]].errors) {
            res = false;
          }
        }
      }
    }

    return res;
  }

  ngOnInit(): void {
    this.modalContent = this.promptEditorServ.processVar(this.currentTmpl.content);
    this.getLabelsByTaskId();
    this.getOptionsList();
  }

  public checkAll(): boolean {
    const res= this.showFromErrorByName( this.formName,[]);
    return res;
  }

  public submit() {
    const isPass = this.checkAll();
    if (isPass) {
      this.close();
      if (this.nzData?.modalClose) {
        this.nzData.modalClose(this.formName);
      }
      this.modalRef.destroy();
    }
  }

  public close(): void {}

  public dismiss(): void {
    this.modalRef.destroy();
  }

  private getLabelsByTaskId() {
    this.tags = this.taskServ.getTask().tags?.map((item: any) => (this.lang === 'zh-cn' ? item.name : item.name_en || item.name));
  }

  public isNotNil(val: any): boolean {
    return !isNil(val);
  }

  public getOptionsList = () => {
    let params = {
      limit: 1000,
      offset: 0,
    };
    this.service.getTagList(params).subscribe({
      next: res => {
        this.labelOptions = res.data.map(tag => {
          return {
            value: tag.id,
            label: this.language === 'en-us' ? tag.nameEn || tag.name : tag?.name,
          };
        });
      },
    });
    this.service.getIndustryList().subscribe({
      next: data => {
        this.industryOptions = data?.map(industry => {
          return {
            value: industry.id,
            label: this.language === 'en-us' ? industry.name_en || industry.name : industry.name,
          };
        });
      },
    });
  };
}
