import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewContainerRef } from "@angular/core";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { v4 as uuidV4 } from 'uuid';
import { NzMessageService } from 'ng-zorro-antd/message';
import { MCPService } from '@services/agent-center/mcp.service';
import { MODULES } from '@shared/modules';
import { ToolCardComponent } from '@shared/components/tool-card/tool-card.component';
import { I18nNamespace } from '@i18n';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { agentCommonLogic } from '../../common-logic-agent';
import { getMcpIcon } from '../../../utils';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzSpinModule } from 'ng-zorro-antd/spin';

@Component({
  selector: 'meta-add-tools',
  standalone: true,
  imports: [
    MODULES,
    ToolCardComponent,
    NzButtonModule,
    NzIconModule,
    NzPaginationModule,
    NzSpinModule
  ],
  templateUrl: './add-tools.component.html',
  styleUrl: './add-tools.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ]
})
export class AddToolsComponent implements OnInit {
  @Input() agentId = '';
  @Input() mcp: any = null;
  public selectedTools = [];
  public tools = [];
  public displayed = [];
  public currentPage = 1;
  public pageSize = 10;
  public loading = true;
  public btnLoading = false;
  public mcpServers = [];
  public getMcpIcon = getMcpIcon;
  public get selectedNum() {
    return this.tools.reduce((acc, item) => acc += item.selected ? 1 : 0, 0);
  }

  constructor(
    private i18n: I18NextEagerPipe,
    private cdr: ChangeDetectorRef,
    private mcpService: MCPService,
    private appAgentServe: AppAgentRepoService,
    private commonLogic: agentCommonLogic,
    private messageService: NzMessageService,
  ) {
  }

  async ngOnInit() {
    await this.getSelectedTools();
    this.getMcpTools();
  }

  private async getSelectedTools() {
    const agentRes = await this.appAgentServe.fetchAgentDetail(this.agentId);
    this.mcpServers = agentRes.mcp_servers ?? [];
  }

  private getMcpTools() {
    if (!this.mcp?.server_id) {
      return;
    }
    this.mcpService.getMcpToolInfo(this.mcp.server_id).then((mcpRes) => {
      let mcp = this.mcpServers.find(item => item.mcp_server_id === this.mcp.server_id);
      let selectedTool = new Set([]);
      if (mcp) {
        selectedTool = new Set(mcp.mcp_choose_tools ?? mcpRes.tools.map(tool => tool.name));
      }
      this.tools = mcpRes.tools.map(tool => ({
        id: uuidV4(),
        name: tool.name,
        description: tool.description,
        selected: selectedTool.has(tool.name),
        canAdd: true,
      }));
      this.updateDisplayed();
      this.loading = false;
      this.cdr.detectChanges();
    }).catch((error) => {
      this.messageService.error(error.error_msg);
      this.loading = false;
    });
  }

  public selectAll() {
    this.tools.forEach(tool => tool.selected = true);
    let start = (this.currentPage - 1) * this.pageSize;
    for (let i = 0; i < this.displayed.length; i++) {
      let tool = {...this.tools[i + start], id: uuidV4()};
      this.tools[i + start] = tool;
      this.displayed[i] = tool;
    }
  }

  public pageUpdated(page: number) {
    this.currentPage = page;
    this.updateDisplayed();
  }

  private updateDisplayed() {
    const start = (this.currentPage - 1) * this.pageSize;
    this.displayed = this.tools.slice(start, start + this.pageSize);
  }

  public async confirm() {
    this.btnLoading = true;
    const mcpChooseTools: Record<string, string[]> = {};
    this.mcpServers.forEach(item => {
      if (item.mcp_server_id === this.mcp.server_id) {
        mcpChooseTools[item.mcp_server_id] = this.tools.filter(item => item.selected).map(item => item.name);
      } else {
        mcpChooseTools[item.mcp_server_id] = item.mcp_choose_tools;
      }
    });

    this.commonLogic.doTriggerSave(this.agentId, {
      data: {
        mcp_servers: this.mcpServers.map(item => item.mcp_server_id),
        mcp_choose_tools: mcpChooseTools,
      },
      successCb: () => {
        this.dismiss();
        this.messageService.success(this.i18n.transform('select_tool_success'));
      },
      finallyCb: () => {
        this.btnLoading = false;
      },
    });
  }

  public dismiss() {}
}
