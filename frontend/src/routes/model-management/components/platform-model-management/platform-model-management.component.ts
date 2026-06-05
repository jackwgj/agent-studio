import { Component, Input } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzDrawerService } from 'ng-zorro-antd/drawer';
import { NzMessageService } from 'ng-zorro-antd/message';

import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { ModelManagementService } from '@services/repositories/model-management-new';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonUtils } from '../../../../utils/common.util';
import { AddPublisherComponent } from '@routes/model-management/components/add-publisher/add-publisher.component';
import { statusMeta } from '@constants/status';
import { AuthModalComponent } from '@routes/model-management/components/auth-modal/auth-modal.component';
import { ModelType } from '@enums/jiuwen-model.enum';
import {
  NewCommonNoDataWithBtnComponent
} from "@shared/components/new-common-no-data-with-btn/new-common-no-data-with-btn.component";
import { CommonService } from "@services/common.service";
import { SendRequestService } from "@routes/subscription/send-request.service";
import { AgentConfigService } from "@routes/agent-center/agent-config.service";

export interface TiMenuItem {
  label: string;
  disabled?: boolean;
  type?: string;
  disIcon?: string;
  icon?: string;
}

export interface TpSearchboxItem {
  label: string;
  field: string;
  valueKey?: string;
  options?: any[];
  _value?: any;
}

export interface TpSearchboxTag {
  label?: string;
  field: string;
  value?: any;
  id?: any;
}

@Component({
  selector: 'platform-model-management',
  templateUrl: './platform-model-management.component.html',
  styleUrls: ['./platform-model-management.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MODULES,
    NzSpinModule,
    NzTagModule,
    NzButtonModule,
    NzToolTipModule,
    NzIconModule,
    NzPaginationModule,
    NzInputModule,
    NzSelectModule,
    NewCommonNoDataWithBtnComponent
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MODEL_ACCESS, I18nNamespace.AGENT, I18nNamespace.AGENT_CENTER],
    },
    NzModalService,
    NzDrawerService
  ],
})
export class PlatformModelManagementComponent {
  // 是否来自于开发者空间
  @Input() isFromDevelopSpace: boolean = true;
  public changeUrl = cdnAssetUrl;

  constructor(
    private readonly i18n: I18NextEagerPipe,
    private route: ActivatedRoute,
    private modelManagementService: ModelManagementService,
    private nzDrawer: NzDrawerService,
    private router: Router,
    private nzModal: NzModalService,
    private commonService: CommonService,
    private sendRequest: SendRequestService,
    private configServ: AgentConfigService,
    private message: NzMessageService,
  ) {}

  public currentPage = 1;
  public totalNumber = 0;

  public pageSize = 12;

  prefix: string = '/v1';
  public pageData = [];
  public provider_id = '';
  public loading = false;
  public basicData = {} as any;
  public activeName = 'PLATFORM';

  public iconEmpty = cdnAssetUrl('assets/images/model/empty.svg');
  public iconNoneEmpty = cdnAssetUrl('assets/model/default_model_detail.svg');
  public iconBasicNoneEmpty = cdnAssetUrl('assets/model/default_model.svg');

  public resourcesCurrentValue:number = 0;
  public itemsTab: TiMenuItem = {
    label: this.i18n.transform('auth_configuration'),
    disabled: false,
    type: 'authModal',
    disIcon: cdnAssetUrl('assets/model/dis_new_setting.svg'),
    icon: cdnAssetUrl('assets/images/model/setting.svg'),
  };

  public tagMap={
    'is_support_function':{
      name:this.i18n.transform('tag_tool'),
      color:'rgb(255,235,209)',
      text_color:'rgb(217,105,0)',
      icon: 'assets/images/tag/is_tool.svg',
    },
    'is_reasoning':{
      name:this.i18n.transform('tag_reasoning'),
      color:'rgb(244,224,252)',
      text_color:'rgb(131,47,214)',
      icon: 'assets/images/tag/is_think.svg',
    },
    'is_network':{
      name:this.i18n.transform('tag_network'),
      color:'rgb(222,236,255)',
      text_color:'rgb(20,118,255)',
      icon: 'assets/images/tag/is_network.svg',
    },
  }
  public authStatus:boolean = false
  public subscribeBtnStatus = this.commonService.getSubscribeStatus();

  public authTopModal(item) {
    const modal = this.nzModal.create({
      nzContent: AuthModalComponent,
      nzData: {
        id: item?.id,
        provider_info: item,
      },
      nzWidth: 'sm',
      nzFooter: null
    });
    modal.afterClose.subscribe(() => {
      // 刷新页面
      this.getDetail();
    });
  }

  public openTopPublisherHalfModel(provider?) {
    const drawer = this.nzDrawer.create({
      nzContent: AddPublisherComponent,
      nzWidth: '700px',
      nzData: {
        title: provider
          ? this.i18n.transform('edit-model-publisher',{ns: I18nNamespace.MODEL_ACCESS,})
          : this.i18n.transform('add-model-publisher',{ns: I18nNamespace.MODEL_ACCESS,}),
        source: this.activeName === 'PLATFORM' ? 'platform' : 'integration',
        provider_id: provider?.id,
      }
    });
    drawer.afterClose.subscribe(() => {
      this.getDetail();
    });
  }

  public pageloading = false;

  goModelTest(row) {
    this.router.navigate(['/home/development-configuration'], {
      queryParams: {
        activeName: this.activeName,
        providerId: this.provider_id,
        id: row?.id,
        modelType: row?.model_type,
        previousUrl: 'model-test',
      },
    });
  }

  public searchItems: TpSearchboxItem[] = [
    {
      label: this.i18n.transform('service_name'),
      field: 'service_name',

    },
    {
      label: this.i18n.transform('model_name'),
      field: 'model_name',
    },
    {
      label: this.i18n.transform('model_type'),
      field: 'model_type',
      valueKey: 'id',
      options: [
        {
          label: this.i18n.transform('LLM'),
          id:ModelType.LLM,
        },
        {
          label: this.i18n.transform('Text-Embedding'),
          id:ModelType.Text_Embedding,
        },
        {
          label: this.i18n.transform('RERANK'),
          id:ModelType.RERANK,
        },
        {
          label: this.i18n.transform('IMAGE-TO-TEXT'),
          id:ModelType.IMAGE_TO_TEXT,
        },
      ],
    },
    {
      label: this.i18n.transform('publish_status'),
      field: 'publish_status',
      valueKey: 'id',
      options: [
        {
          label:this.i18n.transform('published'),
          id:statusMeta.online,
        },
        {
          label: this.i18n.transform('not_published'),
          id:statusMeta.offline,
        }
      ],
    },
  ];
  public searchName: TpSearchboxTag[] = [];

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      if (params.from && params.from === 'develop-space') {
        this.isFromDevelopSpace = true;
      }
      this.activeName = params.activeName;
      this.provider_id = params.provider_id;
      this.getDetail();
    });
  }

  getLicenseQuery() {
    this.sendRequest
      .sendRequest(
        `${this.prefix}/common/license/query`,
        {},
        {headers: this.sendRequest.header,},
        'get',
      )
      .then((res: any) => {
        const llm_token_count = res.find((item) => item.attr_code === 'llm_token_count')
        this.resourcesCurrentValue =
          Number(llm_token_count?.max_value??0)-Number(llm_token_count?.current_value??0);
      });
  }

  getDetail(){
    this.provider_id = '100'
    const {platform_provider_id} = this.configServ.getConfigs();
    this.provider_id  = platform_provider_id || '100';
    this.modelManagementService
      .getModelPublisherInfo(
        this.provider_id,
        'platform',
      )
      .then((res) => {
        this.authStatus = res.auth_configs[0].auth_type!=='IAM'&& res.auth_config_status === 'available'

        res.tagStatus = {
          type:this.authStatus ? 'success' : 'warning',
          text: this.getText(res),
        };
        res.last_updated_date = CommonUtils.getDateToString(res.last_updated_date);
        res.created_date = CommonUtils.getDateToString(res.created_date);
        this.basicData = res;
        if (this.basicData.auth_config_status === 'available') {
          this.itemsTab.label = this.i18n.transform('clear_auth');
          this.itemsTab.icon = cdnAssetUrl('assets/model/new_delete.svg');
        } else {
          this.itemsTab.label = this.i18n.transform('auth_configuration');
          this.itemsTab.icon = cdnAssetUrl('assets/images/model/setting.svg');
        }
        this.getData();
      });
  }

  private getText(data) {
    if (this.authStatus) {
      return this.i18n.transform('auth_available');
    } else if (data.auth_config_status !== 'available') {
      return this.i18n.transform('auth_disable');
    } else {
      return this.i18n.transform('auth_disable');
    }
  }

  ngOnDestroy(): void {

  }

  onPageBack() {
    if (this.isFromDevelopSpace) {
      this.router.navigate(['/home/develop-space'],
        {
          state: {
            currentNavigator: 'model'
          }
        });
      localStorage.setItem('activeName',this.activeName)
      return;
    }
    this.router.navigate(['/home/model/management']);
    localStorage.setItem('activeName',this.activeName)
  }

  toDetail(row) {
    this.router.navigate(['/home/model/detail'], {
      queryParams: {
        id: row.id,
        from: this.isFromDevelopSpace? 'develop-space' : '',
      },
    });
  }


  validateUserInput(userInput: string): boolean {
    const regex = /^[\u4e00-\u9fa5a-zA-Z0-9:. _|-]{1,64}$/;
    return regex.test(userInput);
  }

  clickstopProp(e) {
    e.stopPropagation();
  }

  getData = () => {
    const params={};
    for(let item of this.searchName){
      if (item.field==="service_name" || item.field==="model_name"){
        if (!this.validateUserInput(item.value)) {
          this.message.warning(`${item.label || item.field}${this.i18n.transform('support_char_set')}`);
          this.loading = false;
          return;
        }
        params[item.field]=item.value;
      } else if(item.field==="model_type"){
        params[item.field]=item.id;
      } else if(item.field==="publish_status"){
        params[item.field]=item.id;
      }else{
        params[item.field]=item.value;
      }
    }
    const param = {
      provider_id: this.provider_id,
      page_num: this.currentPage,
      page_size: this.pageSize,
      ...params,
    };
    this.loading = true;
    this.modelManagementService
      .getModelList(param)
      .then((res) => {
        this.pageData = res.data ?? [];
        const data = new Date().getTime()
        this.pageData.forEach((item) => {
          item.showTag = data -  item?.last_updated_date <= 3 * 24 * 60 * 60 * 1000
          item.last_updated_date = CommonUtils.getDateToString(
            item.last_updated_date,
          );

          item.modelTags = [];
          if (item?.model_tags && item.model_tags.trim()) {
            item.modelTags = item?.model_tags.split(',');
          }
        });
        this.totalNumber = res.total;
      })
      .finally(() => {
        this.loading = false;
      });
  };

  getPublisherListFn() {
    this.getData();
  }

  freshData() {
    this.currentPage = 1;
    this.getPublisherListFn();
  }

  public onNzSearchChange(): void {
    this.searchName = this.searchItems
      .filter((i: any) => i._value !== undefined && i._value !== null && i._value !== '')
      .map((i: any) => ({
        label: i.label,
        field: i.field,
        value: i.options ? i.options.find((o: any) => o.id === i._value)?.label : i._value,
        id: i._value
      }));
    this.onSearchContentChange();
  }

  public onClearSearchContentChange(): void {
    this.searchName = [];
    this.searchItems.forEach((i: any) => i._value = null);
    this.onSearchContentChange();
  }


  public onSearchContentChange(): void {
    this.currentPage = 1;
    this.getPublisherListFn();
  }

  public onSelect(event, item,e?:any) {
    e.stopPropagation();
    if (event.disabled || (!this.subscribeBtnStatus && false)){
      return;
    }

    if (event.type === 'authModal') {
      this.authTopModal(this.basicData);
    }
    if (event.type === 'onlineTest') {
      //未配置鉴权 && 没有token 不让调测
      if (!this.authStatus && this.resourcesCurrentValue <= 0) {
        return;
      }
      this.goModelTest(item);
    }
  }


  handleClick(event: Event,url){
    window.open(url, '_black');
    event.stopPropagation();
  }
}
