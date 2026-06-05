import { Injectable } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { MCPService } from '@services/agent-center/mcp.service';
import { I18NextEagerPipe } from 'angular-i18next';
import { IMappings, IPage } from '@routes/agent-center/types/common.types';
import { DeleteRefsModalComponent } from '@routes/datasource-management/components/delete-refs-modal/delete-refs-modal.component';

export interface IDeleteRefsModalInfos {
  title: string;
  alertText: string;
  secondConfirmLabel: string;
  tiMsgTitle: string;
  tiMsgContent: string;
  isShowChinese?: boolean;
}

@Injectable()
export class DeleteRefsService {
  constructor(
    private readonly mcpRepoServe: MCPService,
    private nzModal: NzModalService,
    private i18n: I18NextEagerPipe
  ) {}

  public onDeleteRefs(params: { id: string; query? }, options: IDeleteRefsModalInfos, onConfirm: () => void) {
    if (params.query.resource_type === 'high') {
      this.nzModal.info({
        nzTitle: options.tiMsgTitle,
        nzContent: options.tiMsgContent,
        nzOnOk: () => {
          onConfirm();
        },
      });
      return;
    }
    this.mcpRepoServe
      .getReferenceList(params.id, { limit: 10, offset: 0, ...params.query })
      .then((res: IMappings) => {
        const { relations, count } = res;
        if (relations.length) {
          const thisNzModal: any = this.nzModal.create({
            nzContent: DeleteRefsModalComponent,
            nzWidth: '800px',
          });
          const instance = thisNzModal.getContentComponent();
          instance.title = options.title;
          instance.alertText = options.alertText;
          instance.srcData = { data: relations, state: undefined };
          instance.totalNumber = count;
          instance.secondConfirmConfig = {
            supportQuickInput: true,
            label: options.secondConfirmLabel,
          };
          instance.confirm.subscribe(() => {
            onConfirm();
          });
          instance.pageInfo.subscribe((info: IPage) => {
            const { limit, offset } = info;
            this.mcpRepoServe
              .getReferenceList(params.id, {
                limit,
                offset: (offset - 1) * limit,
                ...params.query,
              })
              .then((res1: IMappings) => {
                instance.srcData = {
                  data: res1.relations,
                  state: {
                    searched: false,
                    sorted: false,
                    paginated: true,
                  },
                };
                instance.totalNumber = res1.count;
              });
          });
        } else {
          this.nzModal.info({
            nzTitle: options.tiMsgTitle,
            nzContent: options.tiMsgContent,
            nzOnOk: () => {
              onConfirm();
            },
          });
        }
      })
      .catch(() => {
        this.nzModal.info({
          nzTitle: options.tiMsgTitle,
          nzContent: options.tiMsgContent,
          nzOnOk: () => {
            onConfirm();
          },
        });
      });
  }
}
