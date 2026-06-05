import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { I18nNamespace } from '@i18n';
import { AppPluginRepoService } from '@services/agent-center/app-plugin-repo.service';
import { MODULES } from '@shared/modules';
import { Subject, takeUntil } from 'rxjs';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { CommonUtils } from 'src/utils/common.util';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { SetSidebarVisibilityService } from '@shared/services/set-sidebar-visibility.service';
import {
  IPluginWithTools,
  IRequestArgsView,
  IResponseArgsView
} from '@routes/agent-center/app-plugin/app-plugin.interface';
import { PluginHeaderNewComponent } from "@routes/agent-center/app-plugin/plugin-header-new/plugin-header.component";
import { PipesModule } from 'src/pipes/pipes.module';
import { ShareService } from '@routes/app-center/components/edit-share-modal/share.service';
import { ContextService } from '@services/context.service';
import { coverPluginInfo } from '@routes/agent-center/app-plugin/utils';
import { getPluginQuota } from '../../agent-center/utils';
import { AddPluginAuthComponent } from '@routes/plugin-market/add-plugin/add-plugin-auth.component';
import { CommonService } from '@services/common.service';

@Component({
  selector: 'meta-plugin-market-detail',
  templateUrl: './plugin-detail.component.html',
  styleUrls: ['./plugin-detail.component.less'],
  imports: [MODULES, PluginHeaderNewComponent, PipesModule],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.TOOL
      ],
    },
  ],
})
export class PluginMarketDetailComponent implements OnInit {
  displayedData: any[] = [];
  srcData = {
    data: [],
    state: undefined,
  };
  toolsColumns: any[] = [
    {
      title: this.i18n.transform('tool_name'),
    },
    {
      title: this.i18n.transform('tool_description'),
    },
    {
      title: this.i18n.transform('agent_reference_count'),
    },
    {
      title: this.i18n.transform('workflow_reference_count'),
    },
    {
      title: this.i18n.transform('operation'),
    },
  ];

  public toolId = '';

  public isLoading = false;

  public name = '--';

  public plugin: IPluginWithTools = {} as IPluginWithTools;

  public lang = CommonUtils.getLanguage();

  public authInfo: {
    type: 'API Key'|'no_auth'  ;
    location?: string;
    params?: {
      name: string;
      value: string;
    }[];
  } = {
      type: 'no_auth',
    };

  public authTbd: any[] = [];

  public responseSrcData: any[] = [];

  public requestSrcData: any[] = [];
  public req: IRequestArgsView[] = [];

  public res: IResponseArgsView[] = [];

  public previewVersionName = '';

  public previewVersionId = '';

  public isPluginMarket = true;

  private destroy$ = new Subject<void>();

  isShare = false;

  visionSelected = '';
  public authTip = null;

  constructor(

    private route: ActivatedRoute,
    private appPluginRepoServ: AppPluginRepoService,
    private router: Router,
    private i18n: I18NextEagerPipe,
    private agentDataServe: AgentDataService,
    private agentRepoServe: AppAgentRepoService,
    private setSidebarVisibilityService: SetSidebarVisibilityService,
    private nzModalService: NzModalService,
    private commonService: CommonService
  ) {
    this.toolId = this.route.snapshot.queryParams.id;
    this.isShare = this.route.snapshot.queryParams.pageType === 'share';
    this.visionSelected = this.route.snapshot.queryParams.releaseVersion;
    this.isPluginMarket = this.router.url.indexOf('plugin-market/market-detail') > -1
    this.agentDataServe
      .getPreviewVersion()
      .pipe(takeUntil(this.destroy$))
      .subscribe((versionInfo) => {

        this.previewVersionName = versionInfo?.version_name;
        this.previewVersionId = versionInfo?.version_id;
        if (versionInfo?.version_id) {
          this.getPluginVersion(versionInfo.version_id);
        }
      });
  }



  async ngOnInit() {
    this.setSidebarVisibilityService.setSidebarsVisibilityByState('init');
    if(this.isShare){
      this.getPluginVersion(this.visionSelected);
    } else{
      await this?.getPluginDetails();
    }
  }

  ngOnDestroy(): void {
    this.agentDataServe.setPreviewVersion(undefined);
    this.setSidebarVisibilityService?.setSidebarsVisibilityByState('destroy');
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setQuotaConfig(plugin) {
    const quota = getPluginQuota(plugin, false);
    if (quota === 0) {
      this.authTip = {
        type: 'error',
        text: `${this.i18n.transform('pre_set_plugin_tip', {time: plugin.limit})}`,
        quota,
      };
    } else {
      this.authTip = {
        type: 'prompt',
        text: `${this.i18n.transform('pre_set_plugin', {name: this.getPluginName(plugin)})}${this.plugin.auth_required && quota ===-1 ? this.i18n.transform('pre_set_plugin1') : ''}`,
        quota,
      };
    }
  }

  public handleClickAddAuthConfig() {
    const modalRef = this.nzModalService.create({
      nzTitle: this.i18n.transform('add_plugin_auth'),
      nzContent: AddPluginAuthComponent,
      nzFooter: null,
      nzWidth: 500,
    });
    modalRef.componentInstance.pluginInfo = this.plugin;
    modalRef.afterClose.subscribe(() => {
      this.getPluginDetails();
    });
  }

  exitViewHistory() {
    this.agentDataServe.setPreviewVersion(undefined);
  }

  coverPluginInfo() {
    coverPluginInfo(this);
    this.displayedData = this.srcData.data;
  }

  async getPluginDetails() {
    try {
      this.isLoading = true;
      let value;
      if (this.isPluginMarket) {
        const value = await this.appPluginRepoServ.getPluginById(this.toolId);
        this.plugin = value.data;
        this.setQuotaConfig(this.plugin);
        this.coverPluginInfo();
      } else {
        value = await this.appPluginRepoServ.getPluginList({
          offset: 0,
          limit: 1,
          id: this.toolId,
          published: 1,
          type: 'inner',
        });

        if (value?.plugin_list?.length) {
          this.plugin = value.plugin_list[0];
          this.setQuotaConfig(this.plugin);
          this.coverPluginInfo();
        }
      }
    } finally {
      this.isLoading = false;
    }
  }

  public getPluginName(plugin:IPluginWithTools): string {
    if (this.lang === 'zh-cn') {
      return plugin?.plugin_chinese_name
        ? `${plugin?.plugin_chinese_name} ( ${plugin?.plugin_display_name} )`
        : plugin?.plugin_display_name;
    } else {
      return plugin?.plugin_display_name;
    }
  }

  handleToolsAction(row: any) {
    this.router.navigate(['/home/plugin-market/market-tool-detail'], {
      queryParams: {
        plugin_id: this.plugin.plugin_id,
        tool_id: row.tool_info.tool_id,
        pageType: this.isShare ? 'share' : '',
        releaseVersion: this.visionSelected,
      },
    });
  }

  getPluginVersion(version_id) {
    this.agentRepoServe
      .getPluginVersion(this.toolId, version_id)
      .then((res) => {
        this.plugin = {...this.plugin, ...res.data};
        this.setQuotaConfig(this.plugin);
        this.coverPluginInfo();
      });
  }

  visionSelectedChange(id: string) {
    this.visionSelected = id;
    this.getPluginVersion(this.visionSelected);
  }
}
