import { Component, Inject, OnInit } from '@angular/core';
import { MODULES } from '@shared/modules';
import { NzDrawerRef } from 'ng-zorro-antd/drawer';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { cdnAssetUrl } from '../../../single-spa/assets-url';
import { AppPluginRepoService } from '@services/agent-center/app-plugin-repo.service';
import { CommonUtils } from '../../../utils/common.util';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { FormsModule } from '@angular/forms';

enum mapKeys {
  auth_info = 'auth_info',
  auth_keys = 'auth_keys'
}

@Component({
  selector: 'add-search-half-modal-component',
  templateUrl: './add-search-half-modal.component.html',
  styleUrls: ['./add-search-half-modal.component.less'],
  standalone: true,
  imports: [
    MODULES,
    NzIconModule,
    NzButtonModule,
    NzInputModule,
    FormsModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
    NzMessageService,
  ],
})
export class AddSearchHalfModalComponent implements OnInit {
  agentId: string = '';
  searchEngine: any[] = [];

  protected readonly changeUrl = cdnAssetUrl;
  public loadingAuth: boolean = false;
  public searchList = [];
  public currentPage = 1;
  public lang: string = '';

  constructor(
    private i18n: I18NextEagerPipe,
    private pluginMarketService: AppPluginRepoService,
    private drawerRef: NzDrawerRef,
    private message: NzMessageService,
  ) {
    this.lang = CommonUtils.getLanguage();
  }

  async ngOnInit() {
    await this.getSearchList();
  }

  initSearchList() {
    for (let index = 0; index < this.searchList.length; index++) {
      this.searchList[index].isNeedAuth = this.searchList[index].credential_status === 'UNAUTHENTICATED';
      this.searchList[index].isAdded = false;
      this.searchList[index].openPayment = false;
      this.searchList[index].API_KEY = '';
    }
  }

  async getSearchList() {
    const params = {
      offset: 0,
      limit: 100,
      type: 'inner',
      label: 'websearch',
      published: 1,
    };
    const res = await this.pluginMarketService.getPluginList(params);
    this.searchList.length = 0;
    if (res && res.plugin_list && Array.isArray(res.plugin_list)) {
      for (let index = 0; index < res.plugin_list.length; index++) {
        res.plugin_list[index].isNeedAuth = res.plugin_list[index].credential_status === 'UNAUTHENTICATED';
        res.plugin_list[index].isAdded = false;
        res.plugin_list[index].openPayment = false;
        res.plugin_list[index].API_KEY = '';
        res.plugin_list[index].tool_id = res.plugin_list[index].input_schema[0].tool_id;
        this.searchList.push(res.plugin_list[index]);
      }
    }
    if (this.searchEngine && this.searchEngine.length > 0) {
      const ids = Array.from(this.searchEngine, (item) => item.plugin_id);
      for (let index = 0; index < this.searchList.length; index++) {
        if (ids.indexOf(this.searchList[index].plugin_id) !== -1) {
          this.searchList[index].isAdded = true;
        }
      }
    }
  }

  get totalNumber(): number {
    return this.searchList.length;
  }

  handleClickAddSearchCard(data: any) {
    if (data.isNeedAuth) {
      if (data.openPayment) {
        return;
      }
      data.openPayment = !data.openPayment;
      return;
    }
    let initAddStatus = data.isAdded;
    for (let index = 0; index < this.searchList.length; index++) {
      this.searchList[index].isAdded = false;
    }
    if (initAddStatus) {
      data.isAdded = false;
      this.searchEngine.length = 0;
      return;
    }
    data.isAdded = !data.isAdded;
    if (data.isAdded) {
      this.searchEngine.length = 0;
      this.searchEngine.push(data);
    }
  }

  dismiss() {
    this.drawerRef.close();
  }

  close() {
    this.drawerRef.close();
  }

  isZH(): boolean {
    return this.lang === 'zh-cn';
  }

  handleClickCardBtn(event: any, data: any, type: string) {
    if (event && event.stopPropagation) {
      event.stopPropagation();
    }
    if (type === 'cancel') {
      data.openPayment = false;
      return;
    }
    if (type === 'save') {
      this.loadingAuth = true;
      let auth_info = data[mapKeys.auth_info];
      auth_info[mapKeys.auth_keys][0].auth_key = data.API_KEY;
      let params = {
        auth_keys: auth_info[mapKeys.auth_keys],
      };
      this.pluginMarketService.setPluginCredentials(data.plugin_id, params)
        .then(() => {
          data.openPayment = false;
          data.isNeedAuth = false;
          this.message.success(this.i18n.transform('auth_success'));
        })
        .catch(() => {
          this.message.error(this.i18n.transform('auth_fail'));
        })
        .finally(() => {
          this.loadingAuth = false;
        })
    }
    return;
  }
}
