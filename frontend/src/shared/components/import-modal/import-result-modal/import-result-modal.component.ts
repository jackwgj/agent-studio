import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input, ViewChild, inject
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MessageComponent } from '@shared/services/cfdata.service';
import { MODULES } from "@shared/modules";
import { I18nNamespace } from "@i18n";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { NzModalService, NzModalRef } from "ng-zorro-antd/modal";
import { NoDataIconComponent } from "../../no-data-icon/no-data-icon.component";
import { AppFlowRepoService } from "@services/agent-center/app-flow-repo.service";
import { cdnAssetUrl } from "src/single-spa/assets-url";
import { AgentCenterHomeService } from "@services/agent-center/agent-center-home.service";
import { AppPluginRepoService } from "@services/agent-center/app-plugin-repo.service";
import { AppAgentRepoService } from "@services/agent-center/app-agent-repo.service";
import { AssetArrowIconComponent } from "../../assets/asset-arrow-icon/asset-arrow-icon.component";
import {
  NonEmptyValidatorDirective,
  ValueValidityValidatorDirective
} from "@shared/directives/variable-name-validator.directive";
import { ModelAuthModalComponent } from "@shared/components/import-modal/model-auth-modal/model-auth-modal.component";
import { McpAuthModalComponent } from "@shared/components/import-modal/mcp-auth-modal/mcp-auth-modal.component";
import { PluginAuthModalComponent } from "@shared/components/import-modal/plugin-auth-modal/plugin-auth-modal.component";
import { ApplicationType } from "@enums/agent-center.enum";

@Component({
  selector: "import-result-modal",
  templateUrl: "./import-result-modal.component.html",
  styleUrls: ["./import-result-modal.component.scss"],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MODULES
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.MODEL_ACCESS
      ]
    }
  ]
})
export class ImportResultModalComponent {
  readonly modalRef = inject(NzModalRef);
  @Input() importResult: any;
  @Input() importToolType = ApplicationType.SINGLE_AGENT;
  @ViewChild("pluginConfigModal") pluginConfigModal: any;
  public alertContent = "";

  public isLoading = false;
  public alertStr = "";
  public warnTipList = []; // 导入后的异常提示
  public errorTip = "";
  public bottomTip = ""; // 表格下方提示
  public collapsed = false;

  public cdnUrl = cdnAssetUrl;

  displayedData: Array<any> = [];
  srcData: any = {
    data: [],
    state: undefined
  };

  public columns: Array<any> = [
    {
      title: this.i18n.transform("agent_name"),
      width: "28%",
      key: "name"
    },
    {
      title: this.i18n.transform("description"),
      width: "28%",
      key: "resource_desc"
    },
    {
      title: this.i18n.transform("result"),
      width: "28%",
      key: "result"
    },
    {
      title: this.i18n.transform("paramextractionlogcomponent_375"),
      width: "16%",
      key: "operate"
    }
  ];

  public radioList: Array<{ label: string; id: string; tip?: string; show?: boolean }> = [
    {
      label: this.i18n.transform("radio_list.no_authentication_required", { ns: I18nNamespace.MODEL_ACCESS }),
      id: "NO_AUTH"
    },
    {
      label: this.i18n.transform("radio_list.api_key", { ns: I18nNamespace.MODEL_ACCESS }),
      id: "API_KEY",
      tip: this.i18n.transform("Api_key_tip", { ns: I18nNamespace.MODEL_ACCESS })
    },
    {
      label: this.i18n.transform("radio_list.AK_SK", { ns: I18nNamespace.MODEL_ACCESS }),
      id: "AK_SK",
      tip: this.i18n.transform("AK_SK_tip", { ns: I18nNamespace.MODEL_ACCESS })
    },
    {
      label: this.i18n.transform("radio_list.App-code", { ns: I18nNamespace.MODEL_ACCESS }),
      id: "APP_CODE",
      tip: this.i18n.transform("APP_code_tip", { ns: I18nNamespace.MODEL_ACCESS })
    },
    {
      label: this.i18n.transform("radio_list.CUSTOM_APIKEY", { ns: I18nNamespace.MODEL_ACCESS }),
      id: "CUSTOM_APIKEY",
      tip: this.i18n.transform("custom_tip", { ns: I18nNamespace.MODEL_ACCESS })
    },
    {
      label: this.i18n.transform("radio_list.CUSTOM_IAM", { ns: I18nNamespace.MODEL_ACCESS }),
      id: "CUSTOM_IAM",
      tip: this.i18n.transform("IAM_tip", { ns: I18nNamespace.MODEL_ACCESS })
    }
  ];


  // 解析出的依赖type字段与中文名称的映射关系
  private typeMapping = {
    workflow: this.i18n.transform("workflow"),
    agent: this.i18n.transform("single_agent"),
    controller: this.i18n.transform("multiple_agent"),
    model: this.i18n.transform("model"),
    tool: this.i18n.transform("插件"),
    repo: "repo",
    strategy: this.i18n.transform("route_policy"),
    provider: this.i18n.transform("model_supplier"),
    card: this.i18n.transform("Card"),
    sql: "sql",
    functiongraph: "FunctionGraph"
  };

  constructor(
    private appFlowRepoServe: AppFlowRepoService,
    private appPluginRepoServe: AppPluginRepoService,
    private ppServ: AgentCenterHomeService,
    private service: AppAgentRepoService,
    private cdr: ChangeDetectorRef,
    private i18n: I18NextEagerPipe,
    private nzModal: NzModalService
  ) {
  }

  ngOnInit() {
    this.warnTipList = [];
    this.srcData.data = this.importResult?.import_list || [];
    const type = this.srcData.data[0].type;
    const name = this.typeMapping[type];
    if (this.srcData.data?.length) {
      if (this.importResult?.failed_len === 0 && this.importResult?.succeed_len !== 0) {
        this.alertStr = this.i18n.transform("import_success_total_tip", { num: this.importResult?.succeed_len, name });
      }
      if (this.importResult?.succeed_len === 0 && this.importResult?.failed_len !== 0) {
        this.errorTip = this.i18n.transform("import_fail_total_tip", { failNum: this.importResult?.failed_len, name });
      }
      if (this.importResult?.succeed_len !== 0 && this.importResult?.failed_len !== 0) {
        this.errorTip = this.i18n.transform("import_result_total_tip", { successNum: this.importResult?.succeed_len, failNum: this.importResult?.failed_len, name });
      }
    }

    let isExitConfig = false;
    let isExitFailed = false;
    this.srcData.data.forEach(item => {
      item.dependencies?.forEach(childItem => {
        if (childItem.config_display) {
          isExitConfig = true;
        }
        if (childItem.status === "FAILED") {
          isExitFailed = true;
        }
      });
    });
    if (isExitFailed) {
      this.warnTipList.push(this.i18n.transform("import_exit_tip"));
    }
    if (isExitConfig) {
      this.warnTipList.push(this.i18n.transform("import_auth_tip"));
    }
    if (this.importResult?.failed_len === 0) {
      this.bottomTip = this.i18n.transform("import_total_tip", { total: this.importResult?.count });
    } else {
      this.bottomTip = this.i18n.transform("import_total_fail_tip", { total: this.importResult?.count, failNum: this.importResult?.failed_len, name });
    }
    if (this.importToolType === ApplicationType.WORKFLOW) {
      this.columns[0].title = this.i18n.transform("workflow_name");
    }
    if (this.importToolType === ApplicationType.MULTI_AGENT) {
      this.columns[0].title = this.i18n.transform("controller_name");
    }
  }

  public dismiss() {
    this.modalRef.close();
  }

  public beforeToggle(row: any): void {
    if (row.showDetails) {
      row.showDetails = false;
    } else {
      row.showDetails = true;
    }
  }

  /** 获取type字段对应的中文名称 */
  public getTypeName(type: string): string {
    return this.typeMapping[type] || type;
  }


  public authenteConfig(childItem) {
    if (childItem.type === "tool") {
      const thisNzModal: any = this.nzModal.create({
        nzContent: PluginAuthModalComponent,
        nzWidth: "600px"
      });
      const instance = thisNzModal.getContentComponent();
      instance.id = childItem.id;
      instance.saveSuccess.subscribe(() => this.authSuccess(childItem));
    }
    if (childItem.type === "provider") {
      const thisNzModal: any = this.nzModal.create({
        nzContent: ModelAuthModalComponent,
        nzWidth: "600px"
      });
      const instance = thisNzModal.getContentComponent();
      instance.id = childItem.id;
      instance.saveSuccess.subscribe(() => this.authSuccess(childItem));
    }
    if (childItem.type === "mcp") {
      const thisNzModal: any = this.nzModal.create({
        nzContent: McpAuthModalComponent,
        nzWidth: "600px"
      });
      const instance = thisNzModal.getContentComponent();
      instance.id = childItem.id;
      instance.saveSuccess.subscribe(() => this.authSuccess(childItem));
    }
  }

  public authSuccess(childItem) {
    MessageComponent.showSuccess(this.i18n.transform("auth_success"), 3000);
    childItem.config_display = false;
    this.cdr.markForCheck();
  }

  public change(): void {
    this.collapsed = !this.collapsed;
  }

}
