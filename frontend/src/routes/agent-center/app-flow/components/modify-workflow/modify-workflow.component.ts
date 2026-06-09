/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable @typescript-eslint/restrict-template-expressions */
/* eslint-disable @typescript-eslint/no-unsafe-call */
/* eslint-disable @typescript-eslint/no-unsafe-member-access */
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DoCheck,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { CommonValidation } from '@shared/validation/commonValidation';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { handleCommonReqError } from 'src/utils/utils';
import type { IWorkflowInfo } from '../../app-flow.types';
import { AppFlowService } from '../../app-flow.service';
import { IconUploadComponent } from '@routes/knowledge-center/knowledge/icon-upload.component';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { generateFlowCode } from "@routes/agent-center/utils";

enum mapKey {
  code = 'code',
}

@Component({
  selector: 'modify-workflow',
  templateUrl: './modify-workflow.component.html',
  styleUrls: ['./modify-workflow.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [MODULES, IconUploadComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT],
    },
  ],
})
export class ModifyWorkflowComponent implements OnInit, DoCheck {
  @Input() public workflowDetail: IWorkflowInfo;

  @Input() public type: string;

  @Output() workflowDetailChange = new EventEmitter<IWorkflowInfo>();

  public avatar = '';

  public modalTitle: string = this.i18n.transform('modify_workflow');

  public loading = false;

  MAXLENGHT = 1024;

  // 表单label
  public createAppFormLabel = {
    name: this.i18n.transform('name'),
    code: this.i18n.transform('name'),
    description: this.i18n.transform('description'),
    namePlaceholder: this.i18n.transform('workflow_name_placeholder'),
    descriptionPlaceholder: this.i18n.transform('workflow_desc_placeholder'),
  };

  public groupFormControl: FormGroup;

  constructor(
    public i18n: I18NextEagerPipe,
    private modalRef: NzModalRef,
    private cd: ChangeDetectorRef,
    private appFlowRepoServ: AppFlowRepoService,
    private multiAgentServ: AppAgentRepoService,
    private appFlowServe: AppFlowService,
    private fb: FormBuilder,
    private elementRef: ElementRef,
  ) {
  }

  ngOnInit() {
    this.avatar =
      this.type === 'multi'
        ? this.workflowDetail?.icon
        : this.workflowDetail?.avatar;

    if (this.type === 'multi') {
      this.MAXLENGHT = 256;
    } else {
      this.MAXLENGHT = 1024;
    }

    if (this.type === 'multi') {
      this.modalTitle = this.i18n.transform(
        this.i18n.transform('modify_application'),
      );
      this.createAppFormLabel.name = this.i18n.transform(
        this.i18n.transform('name'),
      );
      this.createAppFormLabel.description = this.i18n.transform(
        this.i18n.transform('description'),
      );
    }

    this.groupFormControl = this.fb.group({
      name: new FormControl(this.workflowDetail.name, [
        Validators.required,
        CommonValidation.wfNameVerify(
          this.i18n.transform('variable_validator5'),
        ),
      ]),
      description: new FormControl(this.workflowDetail.description, [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.minLength(1),
        Validators.maxLength(this.MAXLENGHT),
      ]),
    });

    if (this.type === 'multi') {
    }
  }

  ngDoCheck(): void {
    this.cd.markForCheck();
  }

  // 表单整体校验
  checkGroup(): boolean {
    if (this.groupFormControl.invalid) {
      Object.values(this.groupFormControl.controls).forEach(control => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({onlySelf: true});
        }
      });
      const firstControlName = Object.keys(this.groupFormControl.controls).find(
        key => this.groupFormControl.get(key)?.invalid
      );
      if (firstControlName) {
        this.elementRef.nativeElement
          .querySelector(`[formControlName=${firstControlName}]`)
          ?.focus();
      }

      return false;
    }

    return true;
  }

  public getFormData(): {
    name: string;
    description: string;
    code?: string;
    avatar?: string;
    icon?: string;
  } {
    if (this.type === 'multi') {
      const form: {
        name: string;
        description: string;
        icon?: string;
      } = {
        name:
          (this.groupFormControl.controls.name.value as string).trim() ?? '',
        description:
          (this.groupFormControl.controls.description.value as string).trim() ??
          '',
        icon: this.avatar,
      };

      if (this.avatar === this.workflowDetail?.avatar) {
        delete form.icon;
      }

      return form;
    } else {
      const form = this.groupFormControl.value;
      form[mapKey.code] = generateFlowCode(form?.name)
      if (this.avatar === this.workflowDetail?.avatar) {
        return form;
      }

      return {
        ...form,
        avatar: this.avatar,
      };
    }
  }

  async submit() {
    const isPass = this.checkGroup();
    if (isPass) {
      this.loading = true;
      try {
        const response =
          this.type === 'multi'
            ? await this.multiAgentServ.modifyMultiAgent({
              ...this.getFormData(),
              details: this.workflowDetail.details,
              project_id: this.workflowDetail.project_id,
              type: 'controller',
              agent_id: this.workflowDetail.agent_id,
            })
            : await this.appFlowRepoServ.modifyFlow({
              ...this.getFormData(),
              workflow_id: this.workflowDetail.workflow_id,
              update_time: this.appFlowServe.getVersionInfo(),
            });

        if (this.type === 'multi') {
          this.workflowDetail.icon = response.icon;
          this.workflowDetail.avatar = response.icon;
        } else {
          // 设置version信息
          const {update_time} = response;
          this.appFlowServe.setVersionInfo(update_time);
          this.workflowDetail.code = this.getFormData().code.trim();
          this.workflowDetail.avatar = response.avatar;
        }

        document.title = response?.name;
        this.workflowDetail.name = this.getFormData().name.trim();
        this.workflowDetail.description = this.getFormData().description.trim();
        this.workflowDetailChange.emit(this.workflowDetail);
        this.appFlowServe.setFlowInfo({
          name: response?.name,
          avatar: this.type === 'multi' ? response.icon : response.avatar,
        });

        this.close();
      } finally {
        this.loading = false;
      }
    }
  }

  dismiss() {
    this.modalRef.close();
  }

  close(): void {
    this.modalRef.close();
  }
}
