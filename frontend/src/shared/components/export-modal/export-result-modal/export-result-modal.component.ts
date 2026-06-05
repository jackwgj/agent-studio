import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  Input
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MODULES } from "@shared/modules";
import { I18nNamespace } from "@i18n";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { NzTableModule } from "ng-zorro-antd/table";
import { NzIconModule } from "ng-zorro-antd/icon";
import { NzAlertModule } from "ng-zorro-antd/alert";
import { NzButtonModule } from "ng-zorro-antd/button";
import { NzToolTipModule } from "ng-zorro-antd/tooltip";
import { NzMessageService } from "ng-zorro-antd/message";
import { cdnAssetUrl } from "src/single-spa/assets-url";
import { NZ_MODAL_DATA, NzModalRef } from "ng-zorro-antd/modal";

interface ChildResult {
  status: string;
  details?: any;
  resource_name?: string;
  resource_type?: string;
  resource_desc?: string;
  reason?: string;
}

interface ExportItemResult {
  resource_name: string;
  resource_desc?: string;
  description: string;
  result: string;
  status: "SUCCESS" | "FAILED" | string;
  resource_type: "Plugin" | "workflow" | "agent" | "controller" | "model" | string;
  child_results: ChildResult[];
  showDetails?: boolean;
  reason?: string;
}

export interface ExportResult {
  export_result: ExportItemResult[];
  download_url?: string;
}

@Component({
  selector: "export-result-modal",
  templateUrl: "./export-result-modal.component.html",
  styleUrls: ["./export-result-modal.component.scss"],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MODULES,
    NzTableModule,
    NzIconModule,
    NzAlertModule,
    NzButtonModule,
    NzToolTipModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.AGENT_CENTER
      ]
    }
  ]
})
export class ExportResultModalComponent {
  @Input() exportResult: ExportResult;

  public alertContent = "";

  public isLoading = false;
  public alertStr = "";
  public errorTip = "";
  public isHaveFails = false;
  public isShowCopyUrl = false;
  public cdnUrl = cdnAssetUrl;

  displayedData: ExportItemResult[] = [];
  public srcData: ExportItemResult[] = [];

  public columns: Array<{ title: string; width: string; key: string }> = [
    {
      title: this.i18n.transform("agent_name"),
      width: "28%",
      key: "resource_name"
    },
    {
      title: this.i18n.transform("description"),
      width: "28%",
      key: "resource_desc"
    },
    {
      title: this.i18n.transform("result"),
      key: "result",
      width: "28%"
    },
    {
      title: this.i18n.transform("paramextractionlogcomponent_375"),
      width: "16%",
      key: "operate"
    }
  ];

  // 解析出的依赖type字段与中文名称的映射关系
  private typeMapping = {
    workflow: this.i18n.transform("workflow"),
    agent: this.i18n.transform("single_agent"),
    controller: this.i18n.transform("multiple_agent"),
    model: this.i18n.transform("model"),
    tool: this.i18n.transform("plugin"),
    repo: "repo",
    strategy: this.i18n.transform("route_policy"),
    provider: this.i18n.transform("model_supplier"),
    sql: "sql",
    card: this.i18n.transform("Card"),
    functiongraph: "FunctionGraph"
  };

  constructor(
    private i18n: I18NextEagerPipe,
    private message: NzMessageService,
    private modalRef: NzModalRef,
    @Inject(NZ_MODAL_DATA) private modalData: any
  ) {
    this.exportResult = modalData.exportResult;

  }

  ngOnInit() {
    this.srcData = this.exportResult?.export_result || [];
    if (this.srcData?.length) {
      const type = this.srcData[0].resource_type;
      const name = this.typeMapping[type];
      const successNum = this.srcData.filter(item => item.status === "SUCCESS")?.length;
      const failNum = this.srcData.filter(item => item.status !== "SUCCESS")?.length;
      if (failNum === 0) {
        this.alertStr = this.i18n.transform("export_success_total_tip", { num: successNum, name });
      } else if (successNum === 0) {
        this.errorTip = this.i18n.transform("export_fail_total_tip", { failNum, name });
      } else {
        this.alertStr = this.i18n.transform("export_result_total_tip", { successNum, failNum, name });
      }
    }
    this.isHaveFails = this.srcData.some(item =>
      item.child_results?.some(child => child.status !== "SUCCESS")
    );
    this.downloadFn();
  }

  public dismiss() {
    this.modalRef.close();
  }

  public beforeToggle(row: ExportItemResult): void {
    row.showDetails = !row.showDetails;
  }

  public downloadFn() {
    if (!this.exportResult?.download_url) {
      return;
    }
    const downloadUrlObj = new URL(this.exportResult?.download_url);
    this.isShowCopyUrl = downloadUrlObj.protocol !== window.location.protocol;
    if (this.isShowCopyUrl) {
      return; // 不同协议的话就显示复制下载链接按钮
    }
    const link = document.createElement("a");
    link.href = this.exportResult?.download_url;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  /** 获取type字段对应的中文名称 */
  public getTypeName(type: string): string {
    return this.typeMapping[type] || type;
  }

  public copyUrl() {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(this.exportResult?.download_url).then(() => {
        this.message.success(
          this.i18n.transform("copied_to_clipboard", { ns: I18nNamespace.PROMPT_PLATFORM })
        );
      });
    }
  }
}
