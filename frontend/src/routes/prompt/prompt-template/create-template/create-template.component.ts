import { Router } from '@angular/router';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  Input,
  ChangeDetectorRef,
  ElementRef,
} from '@angular/core';
import * as angularI18next from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { COMMON_MODULES, MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';

import {
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { PromptService } from '@services/prompt.service';
import { MessageComponent } from '@shared/services/cfdata.service';
import { CommonValidation } from '@shared/validation/commonValidation';

interface IDataType {
  template_id: string;
  template_name: string;
  industry?: { id: string; name: string };
  tag_list?: { id: string; name: string }[];
  content?: string;
  description?: string;
}

interface IResponseParamsType {
  name?: string;
  tag_list?: string[];
  industry_id: string;
  content?: string;
  source?: string;
  description?: string;
}

@Component({
  selector: 'create-template',
  templateUrl: './create-template.component.html',
  styleUrls: ['./create-template.component.scss'],
  standalone: true,
  imports: [COMMON_MODULES, MODULES],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PROMPT_PLATFORM],
    },
  ],
})
export class CreateTemplateComponent implements OnInit {
  @Input() modalTitle!: string;
  @Input() data!: IDataType;

  public groupFormControl: FormGroup;
  public industryList: Array<any> = [];

  // 服务名称
  public prompt = {
    name: this.i18NextEagerPipe.transform('base_name_2'),
    industry: this.i18NextEagerPipe.transform('base_industry'),
    tag: this.i18NextEagerPipe.transform('base_label'),
    content: this.i18NextEagerPipe.transform('base_prompt'),
    description: this.i18NextEagerPipe.transform('base_desc'),
  };
  public tagList: Array<any> = [];
  public getTagList = () => {
    let params = {
      limit: 1000,
      offset: 0,
    };
    this.service.getTagList(params).subscribe({
      next: (res) => {
        this.tagList = res.data.map((tag) => {
          return {
            id: tag.id,
            label: tag.name,
          };
        });
      },
    });
  };
  // 接口返回参数统一处理
  public responseParams!: IResponseParamsType;

  constructor(
    private readonly i18NextEagerPipe: angularI18next.I18NextEagerPipe,
    private cd: ChangeDetectorRef,
    fb: FormBuilder,
    private service: PromptService,
    private elementRef: ElementRef,
    private router: Router,
  ) {
    this.groupFormControl = fb.group({
      promptName: new FormControl('', [
        Validators.required,
        CommonValidation.templateNameVerify(
          this.i18NextEagerPipe.transform('prompt_writing_verifyNameInput'),
        ),
      ]),
      industry: new FormControl('', [Validators.required]),
      tag: new FormControl(''),
      content: new FormControl('', [
        Validators.maxLength(CommonValidation.CONTENT_LENGTH_LIMIT),
      ]),
      description: new FormControl('', [
        Validators.maxLength(CommonValidation.DESP_LENGTH_LIMIT),
      ]),
    });
  }

  ngOnInit(): void {
    this.initData();
  }

  ngDoCheck(): void {
    this.cd.markForCheck();
  }

  private initData() {
    this.getTagList();
    this.getIndustryList();
    if (this.data) {
      this.groupFormControl.controls?.promptName?.setValue(
        this.data?.template_name,
      );
      this.groupFormControl.controls?.industry?.setValue({
        id: this.data?.industry?.id,
        label: this.data?.industry?.name,
      });
      this.groupFormControl.controls?.tag?.setValue(
        this.data?.tag_list?.map((item) => {
          return {
            id: item.id,
            label: item.name,
          };
        }),
      );
      this.groupFormControl.controls?.content?.setValue(this.data?.content);
      this.groupFormControl.controls?.description?.setValue(
        this.data?.description,
      );
    }
  }
  // 表单整体校验
  checkGroup(): boolean {
    this.validateAllFormFields(this.groupFormControl);
    const errors = this.groupFormControl.status;
    if (errors === 'INVALID') {
      const firstError: any = Object.keys(this.groupFormControl.controls)[0];
      const element = this.elementRef.nativeElement.querySelector(`[formControlName=${firstError}]`);
      if (element) {
        element.focus();
      }
      return false;
    }
    return true;
  }

  validateAllFormFields(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach((field) => {
      const control = formGroup.get(field);
      if (control) {
        control.markAsDirty();
        control.updateValueAndValidity();
      }
    });
  }

  // 接口返回参数
  private getResponseParmas() {
    const form = this.groupFormControl.controls;
    this.responseParams = {
      name: form.promptName?.value,
      industry_id: form.industry.value?.id,
      tag_list: (form.tag?.value || []).map((tag) => {
        return tag.id;
      }),

      content: form.content?.value,
      description: form.description?.value,
    };
    return this.responseParams;
  }

  public getIndustryList = () => {
    this.service.getIndustryList().subscribe({
      next: (data) => {
        this.industryList = data?.map((industry) => {
          return {
            id: industry.id,
            label: industry.name,
          };
        });
      },
    });
  };

  submit() {
    const isPass = this.checkGroup();
    if (isPass) {
      let params = this.getResponseParmas();
      if (!this.data?.template_id) {
        this.service.createPromptTemplate(params).subscribe({
          next: (res) => {
            MessageComponent.showSuccess(
              this.i18NextEagerPipe.transform('create_prompt_success'),
            );
            this.close();
          },
        });
      } else {
        this.service
          .editPromptTemplate({ ...params, id: this.data.template_id })
          .subscribe({
            next: (res) => {
              MessageComponent.showSuccess(
                this.i18NextEagerPipe.transform('edit_prompt_success'),
              );
              this.close();
            },
          });
      }
    }
  }

  close() {}

  dismiss() {}
}
