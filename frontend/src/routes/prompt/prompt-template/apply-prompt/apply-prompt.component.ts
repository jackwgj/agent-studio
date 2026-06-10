import { Router } from '@angular/router';
import {
  Component,
  OnInit,
  ElementRef,
  Output,
  EventEmitter,
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
import {
  IBaseSelectItem,
  IBaseSelectResp,
} from '@interfaces/prompt/common.interface';
import { cloneDeep } from 'lodash';
import { IPaginationListResp } from '@interfaces/prompt/prompt-template.interface';
import { lastValueFrom } from 'rxjs';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzRadioModule } from 'ng-zorro-antd/radio';

interface CreateTypeItem {
  text: string;
}

@Component({
  selector: 'apply-prompt',
  templateUrl: './apply-prompt.component.html',
  styleUrls: ['./apply-prompt.component.less'],
  standalone: true,
  imports: [
    COMMON_MODULES,
    MODULES,
    NzSelectModule,
    NzInputModule,
    NzButtonModule,
    NzModalModule,
    NzFormModule,
    NzRadioModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PROMPT_PLATFORM],
    },
  ],
})
export class ApplyPromptComponent implements OnInit {
  @Output() callBack = new EventEmitter<string>();

  public createTypes: CreateTypeItem[] = [
    {
      text: this.i18NextEagerPipe.transform('prompt_tmpl_create_new'),
    },
    {
      text: this.i18NextEagerPipe.transform('prompt_tmpl_create_select'),
    },
  ];

  public createType = this.createTypes[0];

  public groupFormControl: FormGroup<{
    createType: FormControl<CreateTypeItem>;
    tasks: FormControl<IBaseSelectItem>;
    taskName: FormControl<string>;
    industry: FormControl<IBaseSelectItem>;
    tag: FormControl<IBaseSelectItem[]>;
    desp: FormControl<string>;
  }> = this.initFormControl();

  public task = {
    createType: this.i18NextEagerPipe.transform(
      'prompt_tmpl_create_task_label',
    ),
    task: this.i18NextEagerPipe.transform('prompt_tmpl_select_task_label'),
    name: this.i18NextEagerPipe.transform('prompt_tmpl_taskname_label'),
    industry: this.i18NextEagerPipe.transform('prompt_tmpl_industry_label'),
    tag: this.i18NextEagerPipe.transform('prompt_tmpl_tag_label'),
    desp: this.i18NextEagerPipe.transform('prompt_tmpl_desc_label'),
  };

  public tagList: IBaseSelectItem[] = [];

  public industryList: IBaseSelectItem[] = [];

  public tasks: IBaseSelectItem[] = [];

  constructor(
    private readonly i18NextEagerPipe: angularI18next.I18NextEagerPipe,
    private fb: FormBuilder,
    private service: PromptService,
    private elementRef: ElementRef<HTMLDivElement>,
    private router: Router,
  ) {}

  async ngOnInit(): Promise<void> {
    await this.getTagList();
    await this.getIndustryList();
    await this.getTasks();
    this.initSelectedTask();
  }

  public createTypeChange(type: CreateTypeItem): void {
    if (
      type.text === this.i18NextEagerPipe.transform('prompt_tmpl_create_select')
    ) {
      this.groupFormControl.get('industry').disable();
      this.groupFormControl.get('tag').disable();
      this.groupFormControl.get('desp').disable();
    } else {
      this.groupFormControl.get('industry').enable();
      this.groupFormControl.get('tag').enable();
      this.groupFormControl.get('desp').enable();
    }
  }

  public isAddToTask(): boolean {
    return (
      (this.groupFormControl.controls.createType?.value as CreateTypeItem)
        ?.text === this.i18NextEagerPipe.transform('prompt_tmpl_create_select')
    );
  }

  public submit() {
    const isPass = this.checkGroup();

    if (isPass) {
      if (
        this.isCreateNew(this.groupFormControl.get('createType').value?.text)
      ) {
        const params = this.getResponseParmas();
        this.service.createPromptTask(params).subscribe({
          next: (res: { id: string; [key: string]: any }) => {
            if (res.id) {
              this.callBack.emit(res.id);
              this.router.navigateByUrl(
                `/home/prompt/prompt-writing?taskId=${res.id}`,
              );
            }
            MessageComponent.showSuccess(
              this.i18NextEagerPipe.transform('base_create_project_success'),
            );
            this.close();
          },
        });
      } else {
        this.callBack.emit(this.groupFormControl.get('tasks').value.id);
        this.close();
      }
    }
  }

  private checkGroup(): boolean {
    const control = cloneDeep(this.groupFormControl);
    let errors: ValidationErrors | null;

    if (this.isAddToTask()) {
      control.removeControl('industry');
      control.removeControl('taskName');
      errors = this.validateAllFormFields(control);
    } else {
      control.removeControl('tasks');
      errors = this.validateAllFormFields(control);
    }

    if (errors) {
      const firstError = Object.keys(errors)[0] as string;

      (
        this.elementRef.nativeElement.querySelector(
          `[formControlName=${firstError}]`,
        ) as HTMLInputElement
      ).focus();

      return false;
    }

    return true;
  }

  private validateAllFormFields(formGroup: FormGroup): ValidationErrors | null {
    Object.keys(formGroup.controls).forEach((field) => {
      const control = formGroup.get(field);
      control?.markAsDirty();
      control?.updateValueAndValidity();
    });
    return formGroup.invalid ? formGroup.errors : null;
  }

  private initFormControl() {
    return this.fb.group({
      createType: new FormControl(this.createType, [Validators.required]),
      tasks: new FormControl(null, [Validators.required]),
      taskName: new FormControl('', [
        Validators.required,
        CommonValidation.datasetNameVerify(
          this.i18NextEagerPipe.transform('dataset_name_verify_tips'),
        ),
      ]),
      industry: new FormControl(null, [Validators.required]),
      tag: new FormControl([]),
      desp: new FormControl('', [
        Validators.maxLength(CommonValidation.DESP_LENGTH_LIMIT),
      ]),
    });
  }

  private initSelectedTask(): void {
    if (this.tasks.length) {
      this.groupFormControl.controls.tasks.setValue(this.tasks[0]);
    }
  }

  private getResponseParmas() {
    return {
      name: this.groupFormControl.get('taskName')?.value,
      industry_id: this.groupFormControl.get('industry')?.value?.id,
      tag_ids: (this.groupFormControl.get('tag')?.value || []).map(
        (tag) => tag.id,
      ),
      description: this.groupFormControl.get('desp')?.value,
    };
  }

  private getTagList = async () => {
    const res: IPaginationListResp = await lastValueFrom(
      this.service.getTagList({
        limit: 1000,
        offset: 0,
      }),
    );

    if (res?.data?.length) {
      this.tagList = res.data.map((tag) => {
        return {
          id: tag.id,
          label: tag.name,
        };
      });
    }
  };

  private getIndustryList = async () => {
    const data: IBaseSelectResp[] = await lastValueFrom(
      this.service.getIndustryList(),
    );

    if (data?.length) {
      this.industryList = data?.map((industry) => {
        return {
          id: industry.id,
          label: industry.name,
        };
      });
    }
  };

  private getTasks = async () => {
    const res: IPaginationListResp = await lastValueFrom(
      this.service.getPromptTaskList({}),
    );

    if (res?.data?.length) {
      this.tasks = res.data.map((item) => ({
        id: item.id,
        label: item.name,
      }));
    }
  };

  private isCreateNew(str: string): boolean {
    return str === this.i18NextEagerPipe.transform('prompt_tmpl_create_new');
  }

  close() {}

  dismiss() {}
}
