import { Injectable, Renderer2, RendererFactory2 } from '@angular/core';
import { Router } from '@angular/router';
import { MaskComponent } from '@shared/services/cfdata.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { AppLibraryRepoService } from '@services/app-library/app-library-repo.service';
import { I18NextEagerPipe } from 'angular-i18next';

@Injectable({
  providedIn: 'root',
})
export class CardOperatorService {
  constructor(
    private appLibraryRepoServe: AppLibraryRepoService,
    private router: Router,
    private i18n: I18NextEagerPipe,
    private nzModal: NzModalService,
  ) {}

  public copyToWorkbench(
    appId: string,
    type: string,
  ): void {
    MaskComponent.show();
    this.appLibraryRepoServe
      .copyToWorkbench(appId, type)
      .then((res) => {
        if (res.resource_id) {
          const title =
            type === 'workflow'
              ? this.i18n.transform('copy_tip1')
              : this.i18n.transform('copy_tip2');
          const modalInstance = this.nzModal.confirm({
            nzTitle: title,
            nzCancelText: this.i18n.transform('cancel'),
            nzOkText: this.i18n.transform('confirm'),
            nzOnOk: (): void => {
              if (type === 'workflow') {
                this.router.navigate(['/home/agent-center/app-flow/flow'], {
                  queryParams: {
                    id: res.resource_id,
                    from: 'featured',
                  },
                });
              } else {
                this.router.navigate(
                  ['/home/agent-center/app-agent/detail'],
                  {
                    queryParams: {
                      agentId: res.resource_id,
                      from: 'featured',
                    },
                  },
                );
              }
              modalInstance.destroy();
            },
          });
        }
      })
      .finally(() => {
        MaskComponent.hide();
      });
  }
}
