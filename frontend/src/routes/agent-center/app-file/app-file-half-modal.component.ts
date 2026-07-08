import { Component, ChangeDetectorRef, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { NzUploadFile, NzUploadXHRArgs } from "ng-zorro-antd/upload";
import { MODULES } from "@shared/modules";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { I18nNamespace } from "@i18n";
import { agentCommonLogic } from "@routes/agent-center/app-agent/common-logic-agent";
import { NzAlertModule } from "ng-zorro-antd/alert";
import { NzButtonModule } from "ng-zorro-antd/button";
import { NzIconModule } from "ng-zorro-antd/icon";
import { NzUploadModule } from "ng-zorro-antd/upload";
import { NzDrawerRef } from "ng-zorro-antd/drawer";
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
  fileList: NzUploadFile[] = [];

  constructor(
    private i18n: I18NextEagerPipe,
    private appAgentRepo: AppAgentRepoService,
    private deepResearchServ: DeepResearchService,
    private drawerRef: NzDrawerRef,
    private cdr: ChangeDetectorRef
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
    this.fileList = [];
    if (this.selectedType === "direct") {
      this.uploadNameDir = !this.fileNameDir ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
    } else {
      this.uploadName = !this.fileName ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
    }
  }

  handleUploadError(file: NzUploadFile): void {
    MessageComponent.showError(this.tips2);
  }

  beforeUpload = (file: NzUploadFile): boolean => {
    this.fileList = [file];
    this.handleFileUpload(file as unknown as File);
    return false;
  };

  handleFileUpload(file: File) {
    this.informationTemplateFile = file;
    const formData = new FormData();
    formData.append("file", file);
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
    this.fileList = this.fileList.map(f => {
      f.status = 'done';
      return f;
    });
    if (this.selectedType === "extract") {
      this.fileUrl = data.url;
      this.fileName = data.file_name;
      this.uploadName = !this.fileName ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
      this.uploadFileEnv.emit({ fileName: this.fileName, fileUrl: this.fileUrl, templateType: this.selectedType });
      this.cdr.detectChanges();
      return;
    }
    this.fileUrlDir = data.url;
    this.fileNameDir = data.file_name;
    this.uploadNameDir = !this.fileNameDir ? this.i18n.transform("upload-file-1") : this.i18n.transform("upload-file-2");
    this.uploadFileEnv.emit({ fileName: this.fileNameDir, fileUrl: this.fileUrlDir, templateType: this.selectedType });
    this.cdr.detectChanges();
  }

  private setUploadDataWithError() {
    this.fileList = this.fileList.map(f => {
      f.status = 'error';
      return f;
    });
    if (this.selectedType === "extract") {
      this.fileUrl = "";
      this.fileName = "";
    } else {
      this.fileUrlDir = "";
      this.fileNameDir = "";
    }
  }

  runtimeUploadFile(formData: any) {
    formData.append("is_template", String(this.selectedType === "direct"));
    this.deepResearchServ.uploadFileTemplate(formData)
      .then((res) => {
        this.fileList = this.fileList.map(f => { f.status = 'done'; return f; });
        MessageComponent.showSuccess(this.i18n.transform("upload_success"));
        if (this.selectedType === "direct") {
          this.fileNameDir = this.informationTemplateFile.name;
          this.fileUrlDir = res.url || "";
        } else {
          this.fileName = this.informationTemplateFile.name;
          this.fileUrl = res.url || "";
        }
        this.uploadFileEnv.emit({ fileName: this.selectedType === "direct" ? this.fileNameDir : this.fileName, id: res.data });
        this.cdr.detectChanges();
      })
      .catch(() => {
        this.fileList = this.fileList.map(f => { f.status = 'error'; return f; });
        if (this.selectedType === "direct") {
          this.fileNameDir = "";
        } else {
          this.fileName = "";
        }
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
    this.drawerRef.close(true);
  }

  dismiss() {
    this.drawerRef.close(false);
  }

  get confirmBtnDisabledStatus() {
    if (this.selectedType === "extract" && !this.fileName) {
      return true;
    }
    return this.selectedType === "direct" && !this.fileNameDir;
  }
}
