import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from "@angular/core";
import { MODULES } from "@shared/modules";
import { cdnAssetUrl } from "src/single-spa/assets-url";
import { AssetIntelligentAddComponent } from "../assets/asset-intelligent-add/asset-intelligent-add.component";
import { ContextService } from "@services/context.service";
import { getCardBgColor } from "@routes/agent-center/app-agent/common-logic-agent";
import { I18NEXT_NAMESPACE } from "angular-i18next";
import { I18nNamespace } from "@i18n";
import { AGENT_MODE_CODE } from "@routes/agent-center/app-agent/agent-bot-page/agent-bot-page.constant";

export interface IToolAdded {
  title: string;
  collapsed: boolean;
  workflowSwitchEnabled?: boolean;
  list: any[];
}

@Component({
  selector: "skill-list",
  templateUrl: "./skill-list.component.html",
  styleUrls: ["./skill-list.component.scss"],
  standalone: true,
  imports: [MODULES, AssetIntelligentAddComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER]
    }
  ]
})
export class SkillListComponent implements OnInit {
  @Input() toolAdded: IToolAdded = {
    title: "",
    collapsed: false,
    list: []
  };
  @Input() type = "";
  @Input() loading = false;
  @Input() noDataText = "";
  @Input() flowActionsVisible: boolean = true;
  @Input() defaultIcon = "";
  @Input() isFlowReadonly = false;
  @Input() curAgentModeCode = "";

  @Output() collapseRegionFlag = new EventEmitter<any>();
  @Output() addBtnClickedFlag = new EventEmitter<void>();
  @Output() selectTool = new EventEmitter<any>();
  @Output() deleteFlow = new EventEmitter<any>();
  @Output() configPlugin = new EventEmitter<any>();
  @Output() autoAddFlow = new EventEmitter<void>();
  @Output() onConfirmUpdateFlow = new EventEmitter<void>();

  public changeUrl = cdnAssetUrl;
  public isF1 = this.ctxServ.isF1ReqList();
  public hoverToolId: string = "";

  constructor(private ctxServ: ContextService) {
  }

  ngOnInit() {
  }

  public changeCollapseEvent(tool: any) {
    this.collapseRegionFlag.emit(tool);
  }

  public configPluginEvent(item: any) {
    this.configPlugin.emit(item);
  }

  public addBtnClickedEvent() {
    this.addBtnClickedFlag.emit();
  }

  public toSelectTool(item: any) {
    this.selectTool.emit(item);
  }

  public deleteOneFlow(item: any) {
    this.deleteFlow.emit(item);
  }

  public autoAddClickedEvent() {
    this.autoAddFlow.emit();
  }

  public getBgColor(item: { valid?: boolean; hover?: boolean }) {
    return getCardBgColor(item);
  }

  public onConfirmUpdate(item) {
    this.onConfirmUpdateFlow.emit(item);
  }

  public isShowDescClassName(item: any) {
    return !this.isFlowReadonly && this.type === "workflow" && item.app_last_version_obj && !item.app_last_version_obj.valid;
  }

  public isShowUpdateIcon(item: any) {
    return !this.isFlowReadonly &&
      this.type === "workflow" &&
      item.app_last_version_obj &&
      item.app_last_version_obj.valid &&
      item.resource_version &&
      item.app_last_version_obj.resource_latest_version !== item.resource_version;
  }

  get getParams() {
    const result = [];
    let filterData = this.toolAdded.list.filter(item => item.hover);
    if (this.hoverToolId && filterData.length <= 0) {
      this.initHoverStatus(filterData);
      return filterData;
    }
    if (filterData.length <= 0) {
      return [];
    }
    for (let index = 0; index < filterData.length; index++) {
      if (this.type === "mcp") {
        this.hoverToolId = filterData[index].server_id;
        const params = JSON.parse(filterData[index].mcp_parameter);
        for (let i = 0; i < params.length; i++) {
          result.push(params[i]);
        }
      } else if (this.type === "workflow") {
        this.hoverToolId = filterData[index].workflow_id;
        const params = JSON.parse(filterData[index].workflow_parameter);
        for (let i = 0; i < params.length; i++) {
          result.push(params[i]);
        }
      }
    }
    return result;
  }

  private initHoverStatus(filterData: any[]) {
    if (this.type === "mcp") {
      filterData = this.toolAdded.list.filter(item => item.server_id === this.hoverToolId);
      if (filterData.length <= 0) {
        this.toolAdded.list.forEach(item => {
          item.isHover = false;
        });
      } else {
        filterData[0].isHover = true;
      }
    }
    if (this.type === "workflow") {
      filterData = this.toolAdded.list.filter(item => item.workflow_id === this.hoverToolId);
      if (filterData.length <= 0) {
        this.toolAdded.list.forEach(item => {
          item.isHover = false;
        });
      } else {
        filterData[0].isHover = true;
      }
    }
  }

  public getParamsType(data: string): string {
    if (!data) {
      return "";
    }
    if (data === "ref" || data === "input_ref") {
      return "reference";
    }
    return "";
  }

  // 用户移入tip元素内的事件
  public handleHoverEnterTipContent() {
    for (let index = 0; index < this.toolAdded.list.length; index++) {
      if (this.type === "mcp" && this.toolAdded.list[index].server_id === this.hoverToolId) {
        this.toolAdded.list[index].hover = true;
      }
      if (this.type === "workflow" && this.toolAdded.list[index].workflow_id === this.hoverToolId) {
        this.toolAdded.list[index].hover = true;
      }
    }
  }

  // 用户移出tip元素内的事件
  public handleHoverLeaveTipContent() {
    for (let index = 0; index < this.toolAdded.list.length; index++) {
      if (this.type === "mcp" && this.toolAdded.list[index].server_id === this.hoverToolId) {
        this.toolAdded.list[index].hover = false;
      }
      if (this.type === "workflow" && this.toolAdded.list[index].workflow_id === this.hoverToolId) {
        this.toolAdded.list[index].hover = false;
      }
    }
    this.hoverToolId = "";
  }

  // 获取默认值
  public getDefaultValue(value: any) {
    if (value?.type === "literal") {
      return value?.content || "--";
    }
    if (value?.type === "ref" && value?.content[0].variable_key) {
      return value?.content[0].variable_key + " " + value?.content[0].default_value;
    }
    if (value?.type === "input_ref" && value?.content[0].variable_key && value?.content[0].default_value) {
      return value?.content[0].variable_key + " " + value?.content[0].default_value;
    }
    return "--";
  }

  protected readonly AGENT_MODE_CODE = AGENT_MODE_CODE;
}
