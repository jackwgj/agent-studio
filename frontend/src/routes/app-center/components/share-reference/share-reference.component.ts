import { Component, ViewChild, Input } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { MCPService } from '@services/agent-center/mcp.service';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { ActivatedRoute } from '@angular/router';
import { HttpService } from '@services/http.service';
import { ShareUsageTableComponent } from '../share-usage-table/share-usage-table.component';
import { SHARE_TOOL_TYPE } from '../edit-share-modal/edit-share-modal.config';
import { NzTabsModule } from 'ng-zorro-antd/tabs';

@Component({
  selector: 'share-reference',
  templateUrl: './share-reference.component.html',
  styleUrl: './share-reference.component.scss',
  standalone: true,
  imports: [MODULES, NzTabsModule, ShareUsageTableComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT],
    },
  ],
})
export class ShareReferenceComponent {
  @Input() toolType: SHARE_TOOL_TYPE = SHARE_TOOL_TYPE.workflow;
  @Input() toolId = '';
  @Input() versionId = '';
  @Input() shareInfo: any;
  @ViewChild('shareUsageTable') shareUsageTable: ShareUsageTableComponent;

  // 工作流 可以被多智能体，单智能体 工作流引用
  // 插件 可以被单智能体 工作流引用
  // 多智能体 可以被多智能体引用
  // 单智能体 目前没有引用的地方
  public tabObj = {
    agent: {
      id: SHARE_TOOL_TYPE.agent,
      title: this.i18n.transform('agent_app'),
    },
    workflow: {
      id: SHARE_TOOL_TYPE.workflow,
      title: this.i18n.transform('workflow_app'),
    },
  }
  public tabs = [];
  SHARE_TOOL_TYPE = SHARE_TOOL_TYPE;

  public activeTab: SHARE_TOOL_TYPE;
  public activeTabIndex = 0;

  constructor(
    private i18n: I18NextEagerPipe,
    private readonly mcpRepoServe: MCPService,
    private route: ActivatedRoute,
    private readonly http: HttpService,
  ) {
  }

  ngOnInit(): void {
    switch (this.toolType) {
      case SHARE_TOOL_TYPE.agent: {
        this.tabs = [this.tabObj.agent];
        break;
      }
      case SHARE_TOOL_TYPE.multipleAgent: {
        this.tabs = [this.tabObj.agent];
        break;
      }
      case SHARE_TOOL_TYPE.workflow: {
        this.tabs = [this.tabObj.agent, this.tabObj.workflow];
        break;
      }
      case SHARE_TOOL_TYPE.plugin: {
        this.tabs = [this.tabObj.agent, this.tabObj.workflow];
        break;
      }
      default: {
        this.tabs = [this.tabObj.agent, this.tabObj.workflow];
      }
    }
    this.activeTab = this.tabs[0].id;

  }

  ngAfterViewInit(): void {
    this.shareUsageTable.init(this.activeTab);
  }


  public onTabChange(index: number): void {
    this.activeTabIndex = index;
    this.activeTab = this.tabs[index].id;
    this.shareUsageTable.init(this.activeTab);
  }
}
