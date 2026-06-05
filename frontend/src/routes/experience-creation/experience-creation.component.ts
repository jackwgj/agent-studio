import { Component, inject, Input, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { cdnAssetUrl } from '../../single-spa/assets-url';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonValidation } from '@shared/validation/commonValidation';
import { Router } from '@angular/router';
import { generateFlowCode } from "@routes/agent-center/utils";
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzModalRef,NZ_MODAL_DATA } from 'ng-zorro-antd/modal';

@Component({
  selector: 'experience-creation',
  imports: [CommonModule, MODULES, NzFormModule, NzInputModule, NzButtonModule],
  templateUrl: './experience-creation.component.html',
  styleUrls: ['./experience-creation.component.less'],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MODEL_ACCESS, I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ExperienceCreationComponent {
  @Input() modalRef: NzModalRef;
  readonly nzModalData = inject<any>(NZ_MODAL_DATA);
  constructor(
    private fb: FormBuilder,
    private i18n: I18NextEagerPipe,
    private flowService: AppFlowRepoService,
    private appService: AppAgentRepoService,
    private router: Router,
  ) {
    this.form = this.fb.group({
      name: [
        '',
        [
          Validators.required,
          CommonValidation.wfNameVerify(
            this.i18n.transform('variable_validator5'),
          ),
        ],
      ],
      description: [
        '',
        [Validators.maxLength(256)],
      ],
      icon: [''],
    });
  }

  public form: FormGroup;

  dismiss(): void {
    this.modalRef.destroy();
  }

  close(): void {
    if (this.nzModalData.type === 'agent') {
      this.experienceCreateAgent();
    } else {
      this.experienceCreateFlow();
    }
  }

  public get placeholders() {
    if (this.nzModalData.type === 'agent') {
      return ['app_center_cards_8', 'app_center_cards_9'];
    } else {
      return ['app_center_cards_10', 'app_center_cards_11'];
    }
  }
  loading = false;
  async experienceCreateAgent() {
    this.loading = true;
    if (!this.form.valid) {
      this.form.markAllAsTouched();
      this.loading = false;
      return;
    }

    try {
      const result = await this.appService.createAgent({
        name: this.form.get('name')?.value,
        description: this.form.get('description')?.value || this.form.get('name')?.value,
      });
      if (result.agent_id) {
        this.router.navigate(['/home/agent-center/app-agent/detail'], {
          queryParams: {
            agentId: result.agent_id,
            from:'experience-creation'
          },
        });
      }
    } catch (error) {
    } finally {
      this.loading = false;
      this.modalRef.destroy();
    }
  }

  async experienceCreateFlow() {
    this.loading = true;
    if (!this.form.valid) {
      this.form.markAllAsTouched();
      this.loading = false;
      return;
    }
    try {
      const result = await this.flowService.createFlow({
        name: this.form.get('name')?.value,
        description:
          this.form.get('description')?.value || this.form.get('name')?.value,
        code: generateFlowCode(this.form.get('name')?.value),
        type: 'chat',
      });
      if (result.workflow_id) {
        this.router.navigate([`/home/agent-center/app-flow/flow`], {
          queryParams: {
            id: result.workflow_id,
            from:'experience-creation'
          },
        });
      }
    } catch (error) {
    } finally {
      this.loading = false;
      this.modalRef.destroy();
    }
  }

  protected readonly changeUrl = cdnAssetUrl;
}
