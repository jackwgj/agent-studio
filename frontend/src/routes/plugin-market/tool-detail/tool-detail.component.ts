import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { I18nNamespace } from '@i18n';
import { AppPluginRepoService } from '@services/agent-center/app-plugin-repo.service';
import { MODULES } from '@shared/modules';
import { Subject, takeUntil } from 'rxjs';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { CommonUtils } from 'src/utils/common.util';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { SetSidebarVisibilityService } from '@shared/services/set-sidebar-visibility.service';
import { IPluginWithTools, IRequestArgsView, IResponseArgsView, IToolInfo } from '@routes/agent-center/app-plugin/app-plugin.interface';
import { PluginHeaderNewComponent } from '@routes/agent-center/app-plugin/plugin-header-new/plugin-header.component';
import { coverToolInfo, getFlatData } from '@routes/agent-center/app-plugin/utils';
import { PipesModule } from 'src/pipes/pipes.module';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzCardModule } from 'ng-zorro-antd/card';

@Component({
  selector: 'meta-plugin-market-tool-detail',
  templateUrl: './tool-detail.component.html',
  styleUrls: ['./tool-detail.component.less'],
  imports: [MODULES, NzEmptyModule, NzIconModule, NzCardModule, PluginHeaderNewComponent, PipesModule],
  standalone: true,
  providers: [
    NzModalService,
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.AGENT_CENTER, I18nNamespace.TOOL],
    },
  ],
})
export class PluginMarketToolDetailComponent implements OnInit {
  public toolId = '';
  public pluginId = '';
  public isLoading = false;

  public name = '--';
  public toolInfo: IToolInfo;
  public plugin: IPluginWithTools = {} as IPluginWithTools;

  public lang = CommonUtils.getLanguage();

  public authInfo: {
    type: 'no_auth' | 'API Key';
    location?: string;
    params?: {
      name: string;
      value: string;
    }[];
  } = {
    type: 'no_auth',
  };

  public authTbd: { data: any[] } = {
    data: [],
  };

  public requestCol: any[] = [
    {
      title: this.i18n.transform('param_name'),
    },
    {
      title: this.i18n.transform('param_type'),
    },
    {
      title: this.i18n.transform('req_loc'),
    },
    {
      title: this.i18n.transform('default_v'),
    },
    {
      title: this.i18n.transform('description'),
    },
    {
      title: this.i18n.transform('required'),
    },
  ];

  public responseCol: any[] = [
    {
      title: this.i18n.transform('param_name'),
    },
    {
      title: this.i18n.transform('description'),
    },
    {
      title: this.i18n.transform('param_type'),
    },
  ];

  public requestDisplayedData: any[] = [];

  public requestSrcData: { data: any[] } = {
    data: [],
  };

  public responseDisplayedData: any[] = [];

  public responseSrcData: { data: any[] } = {
    data: [],
  };

  public req: IRequestArgsView[] = [];

  public res: IResponseArgsView[] = [];

  public previewVersionId = '';

  public previewVersionName = '';

  public isPluginMarket = true;

  private destroy$ = new Subject<void>();

  public isShare = false;
  // 这里的引用插件查看的 分享人是所有引用。使用人是当前分享被自己引用的

  constructor(
    private i18n: I18NextEagerPipe,
    private route: ActivatedRoute,
    private appPluginRepoServ: AppPluginRepoService,
    private router: Router,
    private agentDataServe: AgentDataService,
    private agentRepoServe: AppAgentRepoService,
    private setSidebarVisibilityService: SetSidebarVisibilityService,
    private nzModalService: NzModalService
  ) {
    this.toolId = this.route.snapshot.queryParams.tool_id;
    this.pluginId = this.route.snapshot.queryParams.plugin_id;
    this.isShare = this.route.snapshot.queryParams.pageType === 'share';
    this.isPluginMarket = this.router.url.indexOf('plugin-market/detail') > -1;
    this.agentDataServe
      .getPreviewVersion()
      .pipe(takeUntil(this.destroy$))
      .subscribe(versionInfo => {
        this.previewVersionId = versionInfo?.version_id;
        this.previewVersionName = versionInfo?.version_name;

        if (versionInfo?.version_id) {
          this.getPluginVersion(versionInfo.version_id);
        }
      });
  }

  exitViewHistory() {
    this.agentDataServe.setPreviewVersion(undefined);
  }

  async ngOnInit() {
    this.setSidebarVisibilityService.setSidebarsVisibilityByState('init');
    if (this.isShare) {
      const visionSelected = this.route.snapshot.queryParams.releaseVersion;
      this.getPluginVersion(visionSelected);
    } else {
      await this.getPluginDetails();
    }
  }

  ngOnDestroy(): void {
    this.agentDataServe.setPreviewVersion(undefined);
    this.setSidebarVisibilityService.setSidebarsVisibilityByState('destroy');
    this.destroy$.next();
    this.destroy$.complete();
  }

  getPluginVersion(version_id) {
    this.agentRepoServe.getPluginVersion(this.pluginId, version_id).then(res => {
      this.plugin = res.data || {};
      this.coverToolInfo();
    });
  }

  coverToolInfo() {
    coverToolInfo(this);
  }

  async getPluginDetails() {
    try {
      this.isLoading = true;
      let value;

      if (this.isPluginMarket) {
        value = await this.appPluginRepoServ.getPluginById(this.pluginId);
        this.plugin = value.data;
        this.coverToolInfo();
      } else {
        value = await this.appPluginRepoServ.getPluginList({
          offset: 0,
          limit: 1,
          id: this.pluginId,
          published: 1,
          type: 'inner',
        });

        if (value.plugin_list.length) {
          this.plugin = value.plugin_list[0];
          this.coverToolInfo();
        }
      }
    } finally {
      this.isLoading = false;
    }
  }

  private getFlatData(nodes: any[], level?: number): any[] {
    return getFlatData(nodes, level);
  }

  toggle(node: any, type: 'req' | 'res'): void {
    node.expand = !node.expand;

    if (type === 'req') {
      this.requestSrcData.data = this.getFlatData(this.req);
    } else {
      this.responseSrcData.data = this.getFlatData(this.res);
    }
  }

  getLevelStyle(node: any): { 'padding-left': string } {
    return {
      'padding-left': `${node.level * 24 + 10}px`,
    };
  }
}
