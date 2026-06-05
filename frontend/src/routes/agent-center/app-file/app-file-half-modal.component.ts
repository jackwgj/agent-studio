import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { MODULES } from "@shared/modules";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { I18nNamespace } from "@i18n";
import { agentCommonLogic } from "@routes/agent-center/app-agent/common-logic-agent";
import { NzUploadFile } from "ng-zorro-antd/upload";
import { NzAlertModule } from "ng-zorro-antd/alert";
import { NzButtonModule } from "ng-zorro-antd/button";
import { NzIconModule } from "ng-zorro-antd/icon";
import { NzUploadModule } from "ng-zorro-antd/upload";
import { MessageComponent } from '@shared/services/cfdata.service';
import { AppAgentRepoService } from "@services/agent-center/app-agent-repo.service";
import { DeepResearchService } from "@services/agent-center/deep-research.service";

@Component({
  selector: "add-file-half-modal-component",
  templateUrl: "./app-file-half-modal.component.html",
  styleUrls: ["./app-file-half-modal.component.less"],
  imports: [
    CommonModule,
    FormsModule,
    MODULES,
    NzAlertModule,
    NzButtonModule,
    NzIconModule,
    NzUploadModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.AGENT,
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.COMMON
      ]
    },
    agentCommonLogic
  ]
})
export class AppFileHalfModalComponent implements OnInit {
  @Input() agentId?: string = "";
  @Input() isTemplate?: boolean = false;
  @Input() selectedTemplate: string = "";
  @Output() uploadFileEnv = new EventEmitter<any>();

  public selectedType: string = "extract";
  public tips1: string = this.i18n.transform("template_tip_1");
  public tips2: string = this.i18n.transform("template_tip_2");
  public tips3: string = this.i18n.transform("template_tip_3");
  public openAlert: boolean = true;

  public fileUrl: string = "";
  public fileName: string = "";
  public uploadName: string = "";
  public isShowOldFile: boolean = true;

  public fileUrlDir: string = "";
  public fileNameDir: string = "";
  public uploadNameDir: string = "";
  public isShowOldFileDir: boolean = true;

  filters: any[] = [
    {
      name: "type",
      fn: (fileList: File[]) => {
        const allowedTypes = [".md", ".html", ".doc", ".docx", ".pdf"];
        const filtered = fileList.filter(f => {
          const ext = "." + f.name.split(".").pop()?.toLowerCase();
          return allowedTypes.includes(ext);
        });
        if (filtered.length !== fileList.length) {
          return false;
        }
        return true;
      }
    },
    {
      name: "maxSize",
      fn: (fileList: File[]) => {
        const maxSize = 10485760;
        const filtered = fileList.filter(f => f.size <= maxSize);
        return filtered.length === fileList.length;
      }
    }
  ];

  filter2: any[] = [
    {
      name: "type",
      fn: (fileList: File[]) => {
        const filtered = fileList.filter(f => {
          const ext = "." + f.name.split(".").pop()?.toLowerCase();
          return ext === ".md";
        });
        return filtered.length === fileList.length;
      }
    },
    {
      name: "maxSize",
      fn: (fileList: File[]) => {
        const maxSize = 10485760;
        const filtered = fileList.filter(f => f.size <= maxSize);
        return filtered.length === fileList.length;
      }
    }
  ];

  informationTemplateFile: any;
  isHoveUploadBtn: boolean = false;

  constructor(
    private i18n: I18NextEagerPipe,
    private appAgentRepo: AppAgentRepoService,
    private deepResearchServ: DeepResearchService
  ) {
  }

  ngOnInit() {
    if (this.isTemplate) {
      this.selectedType = "direct";
      this.fileNameDir = this.selectedTemplate;
      this.uploadNameDir = !this.fileNameDir ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
    } else {
      this.selectedType = "extract";
      this.fileName = this.selectedTemplate;
      this.uploadName = !this.fileName ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
    }
  }

  handleClickChangeTemplateType(type: string): void {
    this.selectedType = type;
    if (this.selectedType === "direct") {
      this.uploadNameDir = !this.fileNameDir ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
    } else {
      this.uploadName = !this.fileName ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
    }
  }

  handleUploadError(file: NzUploadFile): void {
    MessageComponent.showError(this.tips2);
  }

  onAddFileSuccess(fileData: any): void {
    if (!fileData) {
      return;
    }
    this.informationTemplateFile = fileData._file;
    const formData = new FormData();
    formData.append("file", this.informationTemplateFile);
    this.selectedType === "extract" ? this.isShowOldFile = false : this.isShowOldFileDir = false;
    if (this.agentId) {
      this.appAgentRepo.uploadFileDeepResearch(this.agentId, formData)
        .then((res) => {
          MessageComponent.showSuccess(this.i18n.transform("upload_success"));
          this.setUploadData(res);
        })
        .catch(() => {
          this.setUploadDataWithError();
          MessageComponent.showError(this.i18n.transform("upload_fail"));
        });
    } else {
      this.runtimeUploadFile(formData);
    }
  }

  private setUploadData(data: any) {
    if (this.selectedType === "extract") {
      this.fileUrl = data.url;
      this.fileName = data.file_name;
      this.uploadName = !this.fileName ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
      this.uploadFileEnv.emit({ fileName: this.fileName, fileUrl: this.fileUrl, templateType: this.selectedType });
      return;
    }
    this.fileUrlDir = data.url;
    this.fileNameDir = data.file_name;
    this.uploadNameDir = !this.fileNameDir ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
    this.uploadFileEnv.emit({ fileName: this.fileNameDir, fileUrl: this.fileUrlDir, templateType: this.selectedType });
  }

  private setUploadDataWithError() {
    if (this.selectedType === "extract") {
      this.fileUrl = "";
      this.fileName = "";
    } else {
      this.fileUrlDir = "";
      this.fileNameDir = "";
    }
  }

  runtimeUploadFile(formData: any) {
    formData.append("is_template", this.selectedType === "direct");
    this.deepResearchServ.uploadFileTemplate(formData)
      .then((res) => {
        MessageComponent.showSuccess(this.i18n.transform("upload_success"));
        this.fileName = this.informationTemplateFile.name;
        this.uploadFileEnv.emit({ fileName: this.fileName, id: res.data });
      })
      .catch(() => {
        this.fileName = "";
        MessageComponent.showError(this.i18n.transform("upload_fail"));
      });
  }

  onRemoveItems(fileData: any): void {
    if (this.selectedType === "extract") {
      this.isShowOldFile = false;
      this.fileUrl = "";
      this.fileName = "";
      this.uploadName = !this.fileName ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
    } else {
      this.fileUrlDir = "";
      this.isShowOldFileDir = false;
      this.fileNameDir = "";
      this.uploadNameDir = !this.fileNameDir ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
    }
  }

  close() {
  }

  dismiss() {
  }

  get confirmBtnDisabledStatus() {
    if (this.selectedType === "extract" && !this.fileName) {
      return true;
    }
    return this.selectedType === "direct" && !this.fileNameDir;
  }
}
