import { Component, QueryList, ViewChildren } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { Subject } from 'rxjs';
import { CommonModule } from '@angular/common';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { MarkdownComponent } from "ngx-markdown";
import { ActivatedRoute, Router } from "@angular/router";
import { CommonUtils } from "../../../utils/common.util";
import { ModelRouterStrategiesService } from "@services/repositories/model-router-strategies";
import { getNewServiceKeyList } from "./router-common";
import { MachineState } from "@routes/model-management/model-route/model-route.component";
import { DeleteModalComponent } from "@routes/model-management/delete-modal/delete-modal.component";
import { SetSidebarVisibilityService } from "@shared/services/set-sidebar-visibility.service";
import { CommonService } from '@services/common.service';
import { HttpService } from '@services/http.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzMessageService } from 'ng-zorro-antd/message';

export interface ActionMenuItem {
  label: string;
  disabled?: boolean;
  icon?: string;
  click?: (item: ActionMenuItem, row: any) => void;
}

@Component({
  selector: 'router-policy-detail',
  templateUrl: './router-policy-detail.html',
  styleUrls: ['./router-policy-detail.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    MarkdownComponent,
    NzDropDownModule,
    NzGridModule,
    NzIconModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.MODEL_ACCESS
      ],
    },
    NzModalService,
  ],
})
export class RouterPolicyDetail {
  private readonly destroy$ = new Subject<void>();
  public changeUrl = cdnAssetUrl;
  public modelId = '';
  public infoDetail = {
    service_name:'',
    service_key_list:'-',
    strategy_key:'',
    readme:'',
    strategy_name:'',
    last_updated_date:'',
    created_date:"",
    strategy_description:''
  };

  public menuItems: ActionMenuItem[] = [];
  public loading = false;
  public isOpAccount = false;
  subscribeBtnStatus = this.commonService.getSubscribeStatus();

  constructor(
    private readonly i18n: I18NextEagerPipe,
    private route: ActivatedRoute,
    private router: Router,
    private nzModal: NzModalService,
    private sidebarVisibilityServ: SetSidebarVisibilityService,
    private modelRouterStrategiesService: ModelRouterStrategiesService,
    private readonly commonService: CommonService,
    private readonly http: HttpService,
    private message: NzMessageService,
  ) {
    this.http.getCurrentUserResourceStatus().subscribe(res=>{})
  }

  dataToItemsFn: (data: any) => Array<ActionMenuItem> = (data) => {
    const that = this
    const items: Array<ActionMenuItem> = [
      {
        label: this.i18n.transform('model_test'),
        disabled: !this.subscribeBtnStatus,
        icon: cdnAssetUrl('assets/images/model/auto-setting.svg'),
        click(item: ActionMenuItem, row: MachineState): void {
          that.goModelTest(data)
        }
      },
      {
        label: this.i18n.transform('edit'),
        disabled: !this.subscribeBtnStatus,
        icon: cdnAssetUrl('assets/images/model/edit.svg'),
        click(item: ActionMenuItem, row: MachineState): void {
          that.addRoutePolicy(data);
        }
      },
      {
        label: this.i18n.transform('delete'),
        disabled: !this.subscribeBtnStatus,
        icon: cdnAssetUrl('assets/images/model/delete.svg'),
        click: (item: ActionMenuItem, row: MachineState) => {
          that.deleteModal(data);
        }
      }
    ];
    return items;
  };

  ngOnInit() {
    const _value ='_value'
    this.modelId = this.route.queryParams[_value].id;
    this.getDetail();
    this.sidebarVisibilityServ.setSidebarsVisibilityByState('init');
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.sidebarVisibilityServ.setSidebarsVisibilityByState('destroy');
  }

  onPageBack() {
    this.router.navigate(['/home/development-configuration'], {
      queryParams: {
        previousUrl: 'route-policy',
      }
    });
  }

  goModelTest(row) {
    this.router.navigate(['/home/development-configuration'], {
      queryParams: {
        id: row.id,
        modelType: 'LLM',
        source: 'ROUTE-POLICY',
        previousUrl: 'model-test',
      },
    })
  }

  public clickMenu(e: Event) {
    e.stopPropagation();
  }

  addRoutePolicy(data?: any) {
    this.router.navigate(['/home/model/create-route-policy'], {
      queryParams: {
        id: data.id
      },
    })
  }

  public deleteModal(item) {
    const modal = this.nzModal.create({
      nzContent: DeleteModalComponent,
      nzWidth: '400px',
      nzData: {
        id: item.id,
        title: this.i18n.transform('delete_route_policy'),
        tip: this.i18n.transform('confirm_delete_route_policy',{strategy_name:item.strategy_name}),
        fnName: 'deleteModelRouterStrategies'
      },
      nzFooter: null
    });

    modal.afterClose.subscribe((res) => {
      if (res) {
        this.message.success(this.i18n.transform('del_route_policy_success'));
        window.history.back()
      }
    });
  }

  getDetail() {
    this.modelRouterStrategiesService.getModelRouterStrategiesDetail(this.modelId).then((res: any) => {
      res.last_updated_date = CommonUtils.getDateToString(res.last_updated_date)
      res.created_date = CommonUtils.getDateToString(res.created_date)
      const _service_key_list = getNewServiceKeyList(res.service_list)
      res.service_key_list = _service_key_list.join(' | ')
      this.infoDetail = res ?? {};
      this.menuItems = this.dataToItemsFn(this.infoDetail);
    });
  }

}
