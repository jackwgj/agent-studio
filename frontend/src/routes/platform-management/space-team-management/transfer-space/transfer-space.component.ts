import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { agentCommonLogic } from '@routes/agent-center/app-agent/common-logic-agent';
import {AbstractControl, ValidationErrors} from '@angular/forms';
import {SpaceTeamManagementService} from "@services/space-team-management.service";
import {ContextService} from "@services/context.service";
import { PE_SESSION_KEY } from "@constants/exp-tmpl-config.const";
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { FormsModule } from '@angular/forms';
import {NzMessageService} from "ng-zorro-antd/message";
import {getLocalStorage} from "../../../../utils/utils";
import {Router} from "@angular/router";

@Component({
  selector: 'transfer-space',
  templateUrl: './transfer-space.component.html',
  styleUrls: ['./transfer-space.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    NzSelectModule,
    NzAlertModule,
    FormsModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PLATFORM_MANAGEMENT, I18nNamespace.COMMON],
    },
    agentCommonLogic,
  ],
})
export class TransferSpaceComponent implements OnInit, OnDestroy {
  btnLoading = false;
  memberOption = [];
  memberModel = "";
  userInfo={
    userId:''
  }

  constructor(
    public i18n: I18NextEagerPipe,
    private spaceTeamManagementService: SpaceTeamManagementService,
    private ctxServ: ContextService,
    private modalRef: NzModalRef,
    private message: NzMessageService,
    private router: Router,
  ) {
    this.userInfo = getLocalStorage(PE_SESSION_KEY);
    this.get_space_members()
  }
  ngOnInit() {}

  ngOnDestroy(): void {}

  transferFn() {
    const params = {
      workspace_id:this.ctxServ.currentWorkspaceId,
      next_owner_id:this.memberModel
    };
    this.spaceTeamManagementService.putOwnership(params).then(res=> {
      this.message.create('success', this.i18n.transform('transfer_success'));
      this.router.navigate(['/home/overview']);
      this.modalRef.destroy();
    });
  }

  close() {
    this.modalRef.destroy();
  }

  dismiss(){
    this.modalRef.destroy();
  }

  async get_space_members() {
    const params = {
      page_num: 0,
      page_size: 9999,
    };
    await this.spaceTeamManagementService.getSpaceMembers(params).then((res: any) => {
      this.memberOption = res?.workspaceList
        ?.filter(item => item.memberId !== this.userInfo.userId)
        ?.map(item => ({ label: item.memberName, value: item.memberId }));
    });
  }

  space_value_verify(){
    return (control: AbstractControl): ValidationErrors | null => {
      const _val = control.value as string;
      if(_val){
        return null
      }
      return {
        serviceName: {
          nzErrorTip:
            this.i18n.transform('pls_select_a_member')
        },
      };
    }
  }
}
