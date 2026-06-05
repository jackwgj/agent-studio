import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { MODULES } from '@shared/modules';
import { ModelManagementService } from '@services/repositories/model-management-new';
import { CommonUtils } from '../../../utils/common.util';
import { DeleteRefsService } from '@shared/services/delete-refs.service';
import { NewCommonNoDataWithBtnComponent } from "@shared/components/new-common-no-data-with-btn/new-common-no-data-with-btn.component";
import { HandleCertificationComponent } from "@routes/model-management/certification/components/handle-certification/handle-certification.component";
import { CommonService } from "@services/common.service";
import { HttpService } from "@services/http.service";
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'model-certification',
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    NewCommonNoDataWithBtnComponent,
    NzButtonModule,
    NzAlertModule,
    NzTableModule,
    NzModalModule
  ],
  templateUrl: './certification.component.html',
  styleUrls: ['./certification.component.scss'],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MODEL_ACCESS],
    },
    DeleteRefsService,
    NzModalService
  ],
})
export class CertificationComponent implements OnInit {
  subscribeBtnStatus = this.commonService.getSubscribeStatus();
  displayedData: Array<any> = [];
  srcData: { data: any[], state?: any } = {
    data: [],
    state: undefined,
  };

  columns: Array<any> = [
    {
      title: this.i18n.transform('name'),
    },
    {
      title: this.i18n.transform('description'),
    },
    {
      title: this.i18n.transform('create_time'),
      width: '150px',
    },
    {
      title: this.i18n.transform('action'),
      width: '240px',
      fixed: 'right',
    },
  ];

  public descandbuyConfig: any = {
    helptip: {
      label: this.i18n.transform('my_credentials'),
    },
    buttons: [],
  };

  public searchName: string = '';
  public loading = false;
  public currentPage = 1;
  public totalNumber = 0;
  public pageSize = {
    options: [10, 15, 20, 50],
    size: 10,
  };

  constructor(
    private i18n: I18NextEagerPipe,
    private nzModal: NzModalService,
    private modelManagementService: ModelManagementService,
    private readonly commonService: CommonService,
    private readonly http: HttpService,
    private message: NzMessageService,
  ) {
    this.http.getCurrentUserResourceStatus().subscribe(res => {});
  }

  ngOnInit() {
    this.getApiAuthList();
  }

  public onSearchContentChange(): void {
    this.currentPage = 1;
    this.getApiAuthList();
  }

  getApiAuthList(isDelete = false) {
    this.loading = true;

    if (isDelete && this.currentPage > 1) {
      if (
        Math.ceil((this.totalNumber - 1) / this.pageSize.size) <
        this.currentPage
      ) {
        this.currentPage -= 1;
      }
    }

    const param = {
      page_num: this.currentPage,
      page_size: this.pageSize.size,
    };
    return this.modelManagementService
      .getApiAuthLists(param)
      .then((res: any) => {
        const result = res.data;
        for (const item of result) {
          item.created_date = CommonUtils.getDateToString(item.created_date);
        }
        this.srcData.data = result;
        this.totalNumber = res.total;
      })
      .finally(() => {
        this.loading = false;
      });
  }

  // 修改点1：直接使用 nzModal.confirm，原生删除交互体验更好
  public deleteModal(item: any) {
    this.nzModal.confirm({
      nzTitle: this.i18n.transform('del_platform_api_key'),
      nzContent: this.i18n.transform('confirm_delete_api_key', { name: item.api_key_name }),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        return this.modelManagementService.deleteApiAuth(item.api_key_id).then(() => {
          this.message.success(this.i18n.transform('api_key_del_successfully'));
          this.getApiAuthList(true);
        });
      }
    });
  }

  addCertification() {
    const modal = this.nzModal.create({
      nzTitle: this.i18n.transform('add_platform_api_key'),
      nzContent: HandleCertificationComponent,
      nzFooter: null,
      nzWidth: 500,
      nzData: {
        type: 'create',
        close: () => {
          modal.destroy();
          this.getApiAuthList();
        }
      }
    });
  }
}
