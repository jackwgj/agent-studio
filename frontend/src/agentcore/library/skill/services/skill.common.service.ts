import { Injectable } from '@angular/core';

import { BehaviorSubject, catchError, map, of } from 'rxjs';

import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';

import { SkillApi } from '@agentcore/api/skill.api';
import { I18nService } from '@agentcore/core/i18n.service';
import { IMappings } from '@routes/agent-center/types/common.types';

@Injectable({
  providedIn: 'root',
})
export class SkillCommonService {
  refreshSkillSub = new BehaviorSubject<boolean>(false);
  refreshSkillSub$ = this.refreshSkillSub.asObservable();
  obsEndpoint = '';

  constructor(
    private nzModal: NzModalService,
    private message: NzMessageService,
    private _skillApi: SkillApi,
    private i18n: I18nService,
  ) {}

  ngOnDestroy() {
    this.refreshSkillSub.complete();
  }

  public async downloadFile(url: string) {
    try {
      if (!url) {
        return;
      }
      const link = document.createElement('a');
      link.href = url;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      this.message.error(`${JSON.stringify(err)}`);
      throw err;
    }
  }

  onDeleteSkill(params: { skill_id: string; name: string; query? }) {
    this._skillApi
      .queryReference(params.skill_id, 0, 10)
      .pipe(
        map((res: any) => {
          const relations = res.relations || [];
          return relations;
        }),
        catchError(() => of(false)),
      )
      .subscribe(
        (relations) => {
          this.openDeleteModal(params, relations);
        },
        () => {
          this.openDeleteModal(params, []);
        },
      );
  }

  private openDeleteModal(params: { skill_id: string; name: string }, relations: any[]) {
    if (relations.length > 0) {
      this.nzModal.confirm({
        nzTitle: this.i18n.transform('skill.list.button.delete.title'),
        nzContent: this.i18n.transform('skill.list.button.delete.list.title'),
        nzOkText: this.i18n.transform('skill.list.button.delete.confirm'),
        nzOkType: 'primary',
        nzOkDanger: true,
        nzOnOk: () => this.deleteSkill(params.skill_id),
        nzWidth: 600,
      });
    } else {
      this.nzModal.confirm({
        nzTitle: this.i18n.transform('skill.list.button.delete.title'),
        nzContent: this.i18n.transform('skill.list.button.delete.tip1', { name: params.name }),
        nzOkText: this.i18n.transform('common.confirm'),
        nzOkType: 'primary',
        nzOkDanger: true,
        nzOnOk: () => this.deleteSkill(params.skill_id),
      });
    }
  }

  deleteSkill(skillId: string) {
    this._skillApi.deleteSkill(skillId).subscribe(() => {
      this.refreshSkillSub.next(true);
      this.message.success(this.i18n.transform('skill.list.deleted.successfully'));
    });
  }

  initObsEndpoint() {
  }

    // 定义表格列配置
    private getDeleteTableColumns(): any[] {
      return [
        {
          headerName: this.i18n.transform('skill.list.button.delete.name'),
          field: 'app_name',
          sortable: false,
          cellRenderer: "gridRouterLinkRender",
          cellRendererParams: (params) => ({
            routerLink: '/home/agent-center/app-agent/detail',
            queryParams: {
              agentId: params.data.app_id,
            },
          }),
        },
         {
          headerName: this.i18n.transform('skill.detail.reference.header.appVersion'),
          field: 'app_version',
          sortable: false,
          valueGetter: (params) => params.data.app_version ?? this.i18n.transform('skill.agentVersion.notExist'),
        },
        {
          headerName: this.i18n.transform('skill.list.button.delete.type'),
          field: 'status',
          sortable: false,
          cellRenderer: "gridStatusRender",
          cellRendererParams: (params: any): any => ({
            ...params,
            showLabel: this.i18n.transform('skill.list.button.delete.type.single.agent'),
          }),
        },
      ];
    }
  
    // 复杂删除配置
    private createComplexDeleteConfig(params, relations): any {
      return {
        id: 'delete-customize-second-confirm-demo',
        title: this.i18n.transform('skill.list.button.delete.title'),
        confirmText: this.i18n.transform('skill.list.button.delete.list.title'),
        resource: {
          colDefs: this.getDeleteTableColumns(),
          rowsDataUniqueKey: 'name',
          srcData: relations,
          isSupportedPagination: true,
        },
        deleteText: {
          type: 'warn',
          closeIcon: false,
          content: this.i18n.transform('skill.list.button.delete.tip'),
        },
        showSimpleSecondConfirm: true,
        secondConfirmConfig: {
          label: this.i18n.transform('skill.list.button.delete.confirm'),
          confirmWords: 'DELETE',
          supportQuickInput: true,
        },
        modalWidth: "middle",
        confirm: () => {
          this.deleteSkill(params.skill_id);
        },
      };
    }
  
    // 简单删除配置
    private createSimpleDeleteConfig(params: { name: string; skill_id: string }): any {
      return {
        id: 'delete-basic-demo',
        title: this.i18n.transform('skill.list.button.delete.title'),
        confirmText: this.i18n.transform('skill.list.button.delete.tip1'),
        resourceLabelKey: 'name',
        resource: [{ name: params.name }],
        modalWidth: "small",
        animation: false,
        cancel: () => {},
        confirm: () => this.deleteSkill(params.skill_id),
      };
    }
}
