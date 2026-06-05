import { ChangeDetectorRef, ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from "@angular/core";
import { FormArray, FormBuilder, FormControl, Validators } from "@angular/forms";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { Subscription } from "rxjs";
import { v4 as uuidV4 } from "uuid";
import { MCPService } from "@services/agent-center/mcp.service";
import { MODULES } from "@shared/modules";
import { I18nNamespace } from "@i18n";
import { WindowResizeService } from "@shared/base/window-resize.service";
import { ToolCardComponent } from "@shared/components/tool-card/tool-card.component";
import { SKILL_TYPE } from "../../../../../knowledge-center/components/scenario-guide-modal/scenario-guide-modal.constant";
import { AppAgentRepoService } from "@services/agent-center/app-agent-repo.service";
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: "meta-skill-card",
  standalone: true,
  imports: [MODULES, ToolCardComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT]
    }
  ],
  templateUrl: "./skill-card.component.html",
  styleUrl: "./skill-card.component.scss",
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SkillCardComponent {
  @Input() type: string = SKILL_TYPE.MCP;
  @Input() showWorkspace = false;
  @Input() data!: any;
  @Input() showFormServerId = "";
  @Input() shareToolHander: Function;

  @Output() select = new EventEmitter();
  @Output() createMcp = new EventEmitter();
  @Output() updateTools = new EventEmitter();
  @Output() onRedeploy = new EventEmitter();
  @Output() showForm = new EventEmitter();

  public get selectedToolText() {
    let selectedNum = this.tools.reduce((acc, tool) => acc += tool.selected ? 1 : 0, 0);
    if (selectedNum === 0) {
      return `${this.tools.length}`;
    }
    return `${selectedNum}/${this.tools.length}`;
  }

  public get isNeed() {
    return this.type === SKILL_TYPE.MCP && "need" === this.data?.config_status;
  }

  public get isShowOtherForm() {
    return this.showFormServerId !== this.data?.server_id;
  }

  public get controls(): FormControl[] {
    return this.form.controls as FormControl[];
  }

  public loading = false;
  public form: FormArray;
  public isShowForm = false;
  public showSelectAll = false;
  public selectAllDisabled = false;
  public createLoading = false;
  private resizeSub: Subscription;
  public windowWidth: number;
  public toolCollapsed = true;
  public tools = [];
  public selectAllBtn: any = {
    text: this.i18n.transform("add_all"),
    class: []
  };

  constructor(
    private windowResizeService: WindowResizeService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private i18n: I18NextEagerPipe,
    private mcpService: MCPService,
    private appAgentServe: AppAgentRepoService,
    private messageServ: NzMessageService
  ) {
  }

  ngOnInit() {
    this.resizeSub = this.windowResizeService.getWindowWidth().subscribe((width) => {
      this.windowWidth = width;
    });
    this.getTools();
  }

  ngOnDestroy(): void {
    this.resizeSub?.unsubscribe();
  }

  public setSelectAllDisabled() {
    this.selectAllDisabled = this.tools.length > 0 && this.tools.every(item => !item.canAdd);
  }

  private getTools() {
    if (this.type === SKILL_TYPE.MCP) {
      this.getMcpTools();
    } else {
      this.tools = this.data?.tools || [];
      this.setSelectAllDisabled();
      this.updateAllAdded(this.isSelectedAll());
    }
  }

  private getMcpTools() {
    if (!this.data?.id || this.data?.fc_instance_status !== "success") {
      return;
    }
    this.loading = true;
    this.mcpService.getMcpToolInfo(this.data.id).then((mcp) => {
      this.tools = mcp.tools.map(tool => ({
        id: uuidV4(),
        name: tool.name,
        description: tool.description,
        selected: this.data.oldDataSelectAll || this.data?.selectedTools.has(tool.name),
        canAdd: true
      }));
      this.updateAllAdded(this.isSelectedAll());
      if (this.data.oldDataSelectAll) {
        this.select.emit({ data: this.data, tools: this.tools });
      }
      this.cdr.detectChanges();
    }).catch((error) => {
      this.messageServ.error(error.error_msg,{nzDuration:3000})
    }).finally(() => {
      this.loading = false;
    });
  }

  public onVersionClick(event: any) {
    event.stopPropagation();
  }

  public onVersionSelect(plugin) {
    this.toolCollapsed = false;
    this.loading = true;
    this.appAgentServe
      .getPluginVersion(this.data.id, this.data.last_version_id)
      .then((res) => {
        this.tools = this.shareToolHander(this.data.id, res?.data?.request_info?.tool_info || []);
      })
      .finally(() => {
        this.loading = false;
      });
  }

  public isSelectedAll() {
    return this.tools.every(tool => tool.selected);
  }

  public updateAllAdded(selectAll) {
    this.selectAllBtn.text = this.i18n.transform(selectAll ? "remove_all" : "add_all");
    if (selectAll) {
      this.selectAllBtn.class = ["remove-all"];
    } else if (this.selectAllDisabled) {
      this.selectAllBtn.class = ["all-btn-disabled"];
    } else {
      this.selectAllBtn.class = [];
    }
  }

  public selectTool(tool: any) {
    this.updateAllAdded(this.isSelectedAll());
    this.select.emit({ data: this.data, tools: [tool] });
  }

  public addAllTools() {
    const selectedAll = this.isSelectedAll();
    if (!selectedAll && this.selectAllDisabled) {
      return;
    }
    this.tools = this.tools.map(item => {
      if (!selectedAll && !item.canAdd) {
        return item;
      }
      return { ...item, selected: !selectedAll };
    });
    this.data.selectedTools = new Set(this.tools.filter(item => item.selected).map(item => item.name));
    this.updateAllAdded(this.isSelectedAll());
    this.select.emit({ data: this.data, tools: this.tools });
  }

  public redeploy() {
    this.onRedeploy.emit(this.data);
  }

  public toggleForm() {
    if (this.isShowForm) {
      this.handleCancel();
      return;
    }
    this.showMcpForm();
  }

  toggleCollapse() {
    this.toolCollapsed = !this.toolCollapsed;
  }

  public showMcpForm() {
    if (!this.form) {
      this.form = this.fb.array(
        this.data.auth.auth_keys.map(() => {
          return this.fb.control("", [
            Validators.required
          ]);
        })
      );
    }
    this.isShowForm = true;
    this.showForm.emit(this.data);
  }

  public async createMcpServer() {
    if (!this.form.valid) {
      this.form.markAllAsTouched();
      return;
    }
    try {
      this.createLoading = true;
      const res = await this.mcpService.provisionService({
        serverId: this.data.server_id,
        auth_keys: this.form.value.map((v, i) => {
          const origin = this.data.auth.auth_keys[i];
          return {
            ...origin,
            auth_key: v
          };
        })
      });
      this.createMcp.emit(res);
    } finally {
      this.createLoading = false;
    }
  }

  public handleCancel() {
    this.isShowForm = false;
  }
}
