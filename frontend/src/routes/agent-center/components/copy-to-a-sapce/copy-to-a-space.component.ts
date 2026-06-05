import { Component, inject, Input, OnDestroy, OnInit } from "@angular/core";
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { agentCommonLogic } from '@routes/agent-center/app-agent/common-logic-agent';
import { SpaceTeamManagementService } from '@services/space-team-management.service';
import { ContextService } from '@services/context.service';
import { StorageService } from '@shared/services/cfdata.service';
import { CommonService } from '@services/common.service';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { CommonUtils } from '../../../../utils/common.util';
import { NZ_MODAL_DATA, NzModalRef } from "ng-zorro-antd/modal";
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageService } from 'ng-zorro-antd/message';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'copy-to-a-space',
  templateUrl: './copy-to-a-space.component.html',
  styleUrls: ['./copy-to-a-space.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    FormsModule,
    NzSelectModule,
    NzFormModule,
    NzButtonModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.COMMON],
    },
    agentCommonLogic,
    NzMessageService,
  ],
})
export class CopyToASpaceComponent implements OnInit, OnDestroy {
  @Input('isFromDevelopSpace') isFromDevelopSpace: boolean = false;

  readonly modalRef = inject(NzModalRef);
  readonly nzModalData = inject<any>(NZ_MODAL_DATA);
  readonly message = inject(NzMessageService);

  btnLoading = false;
  space_options = [];
  space_value: string;
  subscribeBtnStatus = this.commonService.getSubscribeStatus();
  _cross_space_replication_enable = true;
  lang = CommonUtils.getLanguage();

  constructor(
    private i18n: I18NextEagerPipe,
    private spaceTeamManagementService: SpaceTeamManagementService,
    private ctxServ: ContextService,
    private commonService: CommonService,
    private service: AppAgentRepoService,
  ) {}
  async ngOnInit() {
    await this.getLicenseRueryFn();
    this.get_space();
  }
  ngOnDestroy(): void {}
  dismiss() {
    this.modalRef.destroy();
  }
  close() {
    this.modalRef.triggerOk();
  }
  submitFn() {
    if (!this.space_value) {
      this.message.error(this.i18n.transform('copytoaspacecomponent_1181'));
      return;
    }
    this.close();
  }

  async getLicenseRueryFn() {}

  async get_space() {
    await this.spaceTeamManagementService
      .getWorkspace()
      .then((res) => {
        res.workspaceList.forEach(ws=>{
          if(ws.type === 'PERSON'){
            ws.name = this.i18n.transform('personal_space')
          }
        })
        const cur_space_options = JSON.parse(
          StorageService.getSessionStorage('CUR_SPACE_OPTIONS'),
        );
        const _cross_space_replication_enable =
          StorageService.getSessionStorage('cross_space_replication_enable');
        if (this.isFromDevelopSpace) {
          this.space_options = res.workspaceList?.filter(
            (item) => item.type === 'PERSON',
          );
        } else if (!this._cross_space_replication_enable) {
          this.space_options = res.workspaceList?.filter(
            (item) => item.id === cur_space_options?.id,
          );
        } else {
          this.space_options = res.workspaceList;
        }
      })
      .finally(() => {});
  }
}
