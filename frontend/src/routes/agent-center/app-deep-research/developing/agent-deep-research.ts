import { CommonModule } from "@angular/common";
import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import * as angularI18next from "angular-i18next";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { Subscription } from "rxjs";
import { MessageComponent } from '@shared/services/cfdata.service';
import { KnowledgeRepoStatus } from "@enums/knowledge-repo.enum";
import { NzTooltipDirective } from "ng-zorro-antd/tooltip";
import { I18nNamespace } from "@i18n";
import { AgentConfigService } from "@routes/agent-center/agent-config.service";
import { agentCommonLogic, getCardBgColor } from "@routes/agent-center/app-agent/common-logic-agent";
import { ConfigToolsComponent } from "@routes/agent-center/app-agent/components/config-tools/config-tools.component";
import {
  type IKbParam,
  KbConfComponent
} from "@routes/agent-center/app-agent/components/config-tools/kb-conf.component";
import { InfoAndPromptBuilderComponent } from "@routes/agent-center/app-agent/components/info-and-prompt-builder/info-and-prompt-builder.component";
import { ModelConfigTipComponent } from "@routes/agent-center/app-agent/components/model-config-tip/model-config-tip.component";
import { ModelConfigTipService } from "@routes/agent-center/app-agent/components/model-config-tip/model-config-tip.service";
import { AgentDeepResearchRuntime } from "@routes/agent-center/app-deep-research/runtime/agent-deep-research-runtime";
import { AppFileHalfModalComponent } from "@routes/agent-center/app-file/app-file-half-modal.component";
import { LLMSelectComponent } from "@routes/agent-center/app-flow/components/llm-select/llm-select.component";
import {
  AGENT_DEFAULT_ICON_BASE64_STR,
  KB_DEFAULT_ICON_BASE64_STR
} from "@routes/agent-center/app-knowledge/create-knowledge/default-icon-base64";
import { AddSearchHalfModalComponent } from "@routes/agent-center/app-search/add-search-half-modal.component";
import * as myWorkspaceTypes from "@routes/agent-center/types/my-workspace.types";
import { KnowledgeBaseCreationComponent } from "@routes/knowledge-center/components/knowledge-base-creation/knowledge-base-creation.component";
import { KnowledgeBaseSelectorComponent } from "@routes/knowledge-center/components/knowledge-base-selector/knowledge-base-selector.component";
import { NzDrawerRef, NzDrawerService } from "ng-zorro-antd/drawer";
import { KbAbilitiesService } from "@services/knowledge-center/kb-abilities.service";
import { ModelManagementService } from "@services/repositories/model-management-new";
import { WindowResizeService } from "@shared/base/window-resize.service";
import { MODULES } from "@shared/modules";
import { SetSidebarVisibilityService } from "@shared/services/set-sidebar-visibility.service";
import { cdnAssetUrl } from "../../../../single-spa/assets-url";
import { AppAgentRepoService } from "@services/agent-center/app-agent-repo.service";

@Component({
  selector: "agent-deep-research",
  templateUrl: "./agent-deep-research.html",
  styleUrls: ["./agent-deep-research.less"],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    InfoAndPromptBuilderComponent,
    ModelConfigTipComponent,
    LLMSelectComponent,
    KbConfComponent,
    AgentDeepResearchRuntime,
    KnowledgeBaseCreationComponent
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
export class AgentDeepResearch implements OnInit, OnDestroy {
  @Input() knowledge_retrieve_policy: IKbParam = {
    top_k: 3,
    recall_threshold: 0.5,
    search_mode: "",
    faq_threshold: 0.9,
    need_extras_faq_search: false,
    extra_params: []
  };
  @Input() modelConfig: any = {};
  @Input() availableModels: any[] = [];
  @Input() modelConfigTipService: ModelConfigTipService;
  @Input() knowledge = []; // 知识库
  @Input() templates: any[] = []; // 写作模板
  @Output() onClickEvent = new EventEmitter<boolean>();
  @Output() isDeepLoading = new EventEmitter<boolean>();
  @Output() updateSearchEngine = new EventEmitter<number>(); // 搜索引擎的数量
  @ViewChild("kbCreation") kbCreation: KnowledgeBaseCreationComponent;
  @ViewChild("configToos") configToolComponent: ConfigToolsComponent;
  @ViewChild("kbConfRef") kbConfRef: NzTooltipDirective;

  @HostListener("document:click", ["$event"])
  public onClick(event?: MouseEvent): void {
    const target = event?.target as HTMLElement;
    if (target?.closest(".kb-conf") || target?.closest(".ant-tooltip")) {
      return;
    }
    this.showModelConfigTip = false;
    if (this.showKbConfRef) {
      setTimeout(() => {
        this.showKbConfRef = false;
        this.kbConfRef?.hide();
      });
    }
    this.onClickEvent.emit(true);
  }

  public agentId: string = "";
  public capabilityConfig = [
    {
      id: "model",
      collapsed: true,
      name: this.i18n.transform("model"),
      data: [],
      configs: {},
      description: this.i18n.transform("model_desc")
    },
    {
      id: "search",
      collapsed: true,
      name: this.i18n.transform("search"),
      data: [],
      description: this.i18n.transform("search_desc")
    },
    {
      id: "knowledge",
      collapsed: true,
      name: this.i18n.transform("knowledge"),
      data: [],
      description: this.i18NextEagerPipe.transform("kbLimit", {
        number: this.kbLimit
      })
    },
    {
      id: "template",
      collapsed: true,
      name: this.i18n.transform("template"),
      data: [],
      description: this.i18n.transform("template_desc")
    }
  ];
  public availableModelList = [];
  public modelSelected: any; // 选择的模型整个数据
  public showModelConfigTip: boolean = false; // 控制模型配置的布尔值
  public showKbConfRef: boolean = false; // 控制知识库配置的布尔值
  public isFlowReadonly = false;
  protected readonly changeUrl = cdnAssetUrl;
  public tempTemplateData: any = {
    data: "",
    type: "", // 选择的模式
    url: ""
  };

  // 搜索引擎
  public searchHalfModal: NzDrawerRef;
  // 写作模板
  public templateHalfModal: any;
  public kbHalfModal: any;
  public kbId: string = "";
  public kbParams: IKbParam = {
    top_k: 3,
    recall_threshold: 0.5,
    faq_threshold: 0.9,
    search_mode: "",
    need_extras_faq_search: false,
    extra_params: []
  };

  // 监听浏览器宽度，做自适应弹框宽度
  public windowWidth: number;
  private resizeSub: Subscription;
  public kbDefaultIcon = KB_DEFAULT_ICON_BASE64_STR;
  public agentInfo = {
    icon: "",
    name: "",
    description: "",
    agentType: "",
    agentSubType: "",
    id: "",
    model_deployment_id: ""
  };

  // 选择的搜索引擎
  public searchEngine: any[] = [];
  private copySearchEngine: any[] = [];
  // 左右两块区域的样式
  leftAndRightAreaStyle = {
    left: {
      width: "50%",
      padding: "24px 0 24px 24px",
      marginRight: "auto"
    },
    right: {
      width: "50%",
      marginLeft: "24px"
    }
  };
  // 模板
  isTemplate: boolean = false;
  writeTemplate: string = "";

  constructor(
    private setSidebarVisibilityService: SetSidebarVisibilityService,
    private route: ActivatedRoute,
    private i18n: I18NextEagerPipe,
    private nzDrawerService: NzDrawerService,
    public kbAbilitiesService: KbAbilitiesService,
    private commonLogic: agentCommonLogic,
    private windowResizeService: WindowResizeService,
    private service: AppAgentRepoService,
    private configServ: AgentConfigService,
    private readonly i18NextEagerPipe: angularI18next.I18NextEagerPipe,
    private cdr: ChangeDetectorRef,
    private modelManagementService: ModelManagementService
  ) {
    this.route.queryParams.subscribe((params) => {
      this.agentId = params.agentId;
    });
  }

  get kbLimit() {
    // 深度研究模式前端限制最大一个
    return 1;
  }

  async ngOnInit() {
    this.setSidebarVisibilityService.setSidebarsVisibilityByState("init");
    this.resizeSub = this.windowResizeService
      .getWindowWidth()
      .subscribe((width: any) => {
        this.windowWidth = width;
      });
    await this.initAgentInfo(); // 先从接口中获取Agent的名称和描述

    //此处不调保存，有此方法 页面渲染会先get 再put 导致数据有问题，在切换模式处做了处理
    // this.doTriggerSave({
    //   data: {
    //     type: this.agentInfo.agentSubType,
    //   },
    // });
  }

  private async initAgentInfo() {
    this.isDeepLoading.emit(true);
    const res: any = await this.service.fetchAgentDetail(this.agentId);
    this.agentInfo = {
      name: res.name || "",
      description: res.description || "",
      icon: res.icon || "",
      agentType: res.type || "",
      agentSubType: "deepresearch",
      id: this.agentId,
      model_deployment_id: res.model_deployment_id || ""
    };
    this.isTemplate = res.is_template;
    this.writeTemplate = res.writing_template || "";
    if (this.writeTemplate) {
      this.initTemplate();
    }
    this.modelConfig.enable_thinking = res.model_config.enable_thinking;
    this.modelConfig.frequency_penalty = res.model_config.frequency_penalty;
    this.modelConfig.history_size = res.model_config.history_size;
    this.modelConfig.max_tokens = res.model_config.max_tokens;
    this.modelConfig.output_format = res.model_config.output_format;
    this.modelConfig.temperature = res.model_config.temperature;
    this.modelConfig.top_p = res.model_config.top_p;
    this.modelSelected = res.model_deployment_id;
    this.initModel();
    this.initSearch(res);
    this.initKnowledge(res);
    this.initCollapseStatus();
    await this.initModelList();
    this.isDeepLoading.emit(false);
  }


  private initModel() {
    this.capabilityConfig[0].data.length = 0;
    this.capabilityConfig[0].data.push(this.modelSelected);
  }

  private initSearch(data: any) {
    if (data.search_engine && data.search_engine.tool_id) {
      const splitStr = data.search_engine.tool_id.split("#");
      this.capabilityConfig[1].data.push({
        name: data.search_engine.plugin_chinese_name,
        description: data.search_engine.desc,
        icon: data.search_engine.tool_icon,
        plugin_id: splitStr[0],
        tool_id: splitStr[1]
      });
      this.searchEngine = JSON.parse(JSON.stringify(this.capabilityConfig[1].data));
    }
    this.updateSearchEngine.emit(this.searchEngine.length);
  }

  private initTemplate() {
    let fileName = "";
    const index = this.writeTemplate.lastIndexOf("/");
    if (index === -1) {
      fileName = this.writeTemplate;
    } else {
      fileName = this.writeTemplate.substring(
        index + 1,
        this.writeTemplate.length
      );
    }
    this.tempTemplateData.data = this.getTemplateType(fileName);
    this.tempTemplateData.type = this.isTemplate ? "direct" : "extract";
    this.tempTemplateData.url = this.writeTemplate;
    this.capabilityConfig[3].data.length = 0;
    if (this.tempTemplateData.data && typeof this.tempTemplateData.data === 'object') {
      this.capabilityConfig[3].data.push(this.tempTemplateData.data);
    }
  }

  private initKnowledge(data: any) {
    if (data && !data.knowledge_repos) {
      return;
    }
    this.capabilityConfig[2].data.length = 0;
    for (let index = 0; index < data.knowledge_repos.length; index++) {
      this.capabilityConfig[2].data.push({
        knowledge_repo_name: data.knowledge_repos[index].knowledge_repo_name,
        icon: data.knowledge_repos[index].knowledge_repo_icon,
        id: data.knowledge_repos[index].knowledge_repo_id
      });
    }
  }

  private initCollapseStatus() {
    for (let index = 0; index < this.capabilityConfig.length; index++) {
      this.capabilityConfig[index].collapsed = this.capabilityConfig[index].data.length <= 0;
    }
  }

  private async initModelList() {
    if (this.availableModelList.length > 0) {
      return;
    }
    try {
      const res = await this.modelManagementService.getAvailableModelList({
        model_type: "LLM,IMAGE-TO-TEXT",
        with_router: true
      });
      this.availableModelList = res.data.map((item: any) => ({
        ...item,
        merge_name: item.model_name,
        model_name: item.model_name,
        model_nickname: item.model_name,
        model_type: item.model_type,
        model_deployment_id: item.id,
        model_provider: item.icon
      }));
    } catch (e) {
      this.availableModelList.length = 0;
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.agentInfo && changes.agentInfo.currentValue) {
      const { icon } = changes.agentInfo.currentValue;
      if (!icon) {
        changes.agentInfo.currentValue.icon = AGENT_DEFAULT_ICON_BASE64_STR;
      }
    }
    if (changes.availableModels && changes.availableModels.currentValue) {
      this.initAvailableModelList(changes.availableModels.currentValue);
    }
    if (changes.modelConfig && changes.modelConfig.currentValue) {
      this.initModelConfigs(changes.modelConfig.currentValue);
    }
    if (changes.knowledge && changes.knowledge.currentValue) {
      this.capabilityConfig[2].data = changes.knowledge.currentValue;
    }
    if (changes.knowledge_retrieve_policy) {
      this.kbParams = changes.knowledge_retrieve_policy.currentValue;
    }
  }

  private initAvailableModelList(models: any[]) {
    this.availableModelList = models.map((item: any) => ({
      ...item,
      merge_name: item.model_name
    }));
  }

  private initModelConfigs(modelConfig: any) {
    this.modelConfig.isUpdated = modelConfig.isUpdated;
  }

  ngOnDestroy() {
    this.resizeSub?.unsubscribe();
  }

  onUpdateName(data: any) {
    this.agentInfo.name = data;
  }

  onUpdateLogo(logo: string) {
    this.agentInfo.icon = logo;
  }

  handleClickCollapseIcon(data: any) {
    data.collapsed = !data.collapsed;
  }

  handleClickAddData(event: any, data: any) {
    if (event) {
      event.stopPropagation();
    }
    this.onClick();
    if (data.id === "knowledge") {
      this.handleClickAddKnowledgeModal(data);
      return;
    }
    if (data.id === "search") {
      this.handleClickAddSearchModal(data);
      return;
    }
    if (data.id === "template") {
      this.handleClickAddTemplateModal(data);
      return;
    }
  }

  handleClickConfigData(event: any, data: any) {
    if (event) {
      event.stopPropagation();
    }
    this.onClick();
    if (data.id === "model") {
      this.showModelConfigTip = !this.showModelConfigTip;
      return;
    }
    if (data.id === "knowledge") {
      return;
    }
  }

  isShowCapabilityConfigDesc(data: any, index: number) {
    if (data.id === "model") {
      return false;
    }
    return this.capabilityConfig[index].data.length === 0;
  }

  // 切换模型
  planChangeModelSelected(data: any) {
    this.modelSelected = data.id;
    const modelConfigSelected = this.availableModelList.find(
      (item) => item.model_deployment_id === this.modelSelected
    );

    if (!modelConfigSelected || !this.modelSelected) {
      return;
    }

    const { model_name, model_version, model_type, model } =
      modelConfigSelected;

    let modelData = {
      model_name,
      model_deployment_id: this.modelSelected,
      model_version,
      model_type,
      model,
      type: this.agentInfo.agentSubType
    };
    this.isDeepLoading.emit(true);
    this.doTriggerSave({
      data: modelData,
      successCb: () => {
        MessageComponent.showSuccess(
          this.i18n.transform("update_model_success")
        );
      },
      failCb: () => {
        MessageComponent.showError(this.i18n.transform("update_model_fail"));
      },
      finallyCb: () => {
        this.isDeepLoading.emit(false);
      }
    });
  }

  private handleClickAddSearchModal(data: any) {
    this.searchEngine = JSON.parse(JSON.stringify(this.copySearchEngine));
    const originalSearchEngine = JSON.stringify(this.copySearchEngine);
    const drawerRef = this.nzDrawerService.create({
      nzTitle: undefined,
      nzContent: AddSearchHalfModalComponent,
      nzWidth: this.halfModalWidth,
      nzPlacement: "right",
      nzMask: true,
      nzMaskClosable: true,
      nzCloseOnNavigation: true,
      nzContentParams: {
        agentId: this.agentId,
        searchEngine: this.searchEngine
      }
    });
    drawerRef.afterOpen.subscribe(() => {
      const comp = drawerRef.getContentComponent();
      if (comp) {
        comp.agentId = this.agentId;
        comp.searchEngine = this.searchEngine;
        if (this.searchEngine?.length > 0) {
          const ids = this.searchEngine.map((item) => item.plugin_id);
          for (const item of comp.searchList) {
            if (ids.includes(item.plugin_id)) {
              item.isAdded = true;
            }
          }
        }
      }
    });
    drawerRef.afterClose.subscribe(() => {
      if (JSON.stringify(this.searchEngine) === originalSearchEngine) {
        return;
      }
      this.isDeepLoading.emit(true);
      this.doTriggerSave({
        data: {
          type: this.agentInfo.agentSubType,
          search_engine: this.searchEngine.length <= 0 ? {} : {
            id:
              this.searchEngine[0].plugin_id +
              "#" +
              this.searchEngine[0].tool_id
          }
        },
        successCb: () => {
          this.capabilityConfig[1].data.length = 0;
          for (let index = 0; index < this.searchEngine.length; index++) {
            this.capabilityConfig[1].data.push({
              name: this.searchEngine[index].plugin_chinese_name,
              description: this.searchEngine[index].plugin_desc,
              icon: this.searchEngine[index].icon,
              plugin_id: this.searchEngine[index].plugin_id,
              tool_id: this.searchEngine[index].tool_id
            });
          }
          this.copySearchEngine = this.searchEngine;
          this.searchEngine = JSON.parse(JSON.stringify(this.copySearchEngine));
          this.updateSearchEngine.emit(this.searchEngine.length);
          MessageComponent.showSuccess(
            this.i18n.transform("update_success")
          );
        },
        finallyCb: () => {
          this.isDeepLoading.emit(false);
          this.capabilityConfig[1].collapsed = false;
        }
      });
    });
    this.searchHalfModal = drawerRef;
  }

  private handleClickAddTemplateModal(data: any) {
    const filterTemplateData = this.capabilityConfig.filter(item => item.id === "template");
    let selectedTemplateData = "";
    if (filterTemplateData.length > 0) {
      selectedTemplateData = filterTemplateData[0].data.length > 0 ? filterTemplateData[0].data[0].name : "";
    }
    const originalTemplateData = JSON.stringify(this.tempTemplateData);
    const drawerRef = this.nzDrawerService.create({
      nzTitle: undefined,
      nzContent: AppFileHalfModalComponent,
      nzWidth: this.halfModalWidth,
      nzPlacement: "right",
      nzMask: true,
      nzMaskClosable: true,
      nzCloseOnNavigation: true,
      nzContentParams: {
        agentId: this.agentId,
        isTemplate: this.isTemplate,
        selectedTemplate: selectedTemplateData
      }
    });
    drawerRef.afterOpen.subscribe(() => {
      const comp = drawerRef.getContentComponent();
      if (comp) {
        comp.agentId = this.agentId;
        comp.isTemplate = this.isTemplate;
        comp.selectedTemplate = selectedTemplateData;
        comp.uploadFileEnv.subscribe((result: any) => {
          if (result) {
            this.tempTemplateData.url = result.fileUrl || result.fileName;
            this.tempTemplateData.type = result.templateType || "extract";
            this.tempTemplateData.data = this.getTemplateType(result.fileName);
          }
        });
      }
    });
    drawerRef.afterClose.subscribe((confirmed: boolean) => {
      if (!confirmed) {
        return;
      }
      this.isDeepLoading.emit(true);
      this.doTriggerSave({
        data: {
          writing_template: this.tempTemplateData.url,
          is_template: this.tempTemplateData.type === "direct",
          type: this.agentInfo.agentSubType
        },
        successCb: () => {
          this.capabilityConfig[3].data.length = 0;
          this.isTemplate = this.tempTemplateData.type === "direct";
          if (this.tempTemplateData.data && typeof this.tempTemplateData.data === 'object') {
            this.capabilityConfig[3].data.push(this.tempTemplateData.data);
          }
          MessageComponent.showSuccess(
            this.i18n.transform("update_success")
          );
        },
        failCb: () => {
          MessageComponent.showSuccess(this.i18n.transform("update_fail"));
        },
        finallyCb: () => {
          this.isDeepLoading.emit(false);
          this.capabilityConfig[3].collapsed = false;
        }
      });
    });
  }

  private getTemplateType(templateName: string) {
    if (templateName && templateName.toLowerCase().endsWith(".jsx")) {
      return {
        type: "jsx",
        icon: "assets/agent-center/images/JSX.svg",
        name: templateName,
        id: this.capabilityConfig[3].data.length
      };
    }
    if (templateName && templateName.toLowerCase().endsWith(".md")) {
      return {
        type: "md",
        icon: "assets/agent-center/images/MD.svg",
        name: templateName,
        id: this.capabilityConfig[3].data.length
      };
    }
    if (
      templateName &&
      (templateName.toLowerCase().endsWith(".doc") ||
        templateName.toLowerCase().endsWith(".docx"))
    ) {
      return {
        type: "word",
        icon: "assets/agent-center/images/word.svg",
        name: templateName,
        id: this.capabilityConfig[3].data.length
      };
    }
    if (templateName && templateName.toLowerCase().endsWith(".pdf")) {
      return {
        type: "word",
        icon: "assets/agent-center/images/pdf.svg",
        name: templateName,
        id: this.capabilityConfig[3].data.length
      };
    }
    return {
      type: "file",
      icon: "assets/agent-center/images/pdf.svg",
      name: templateName,
      id: this.capabilityConfig[3].data.length
    };
  }

  private handleClickAddKnowledgeModal(config: any) {
    const drawerRef = this.nzDrawerService.create({
      nzTitle: undefined,
      nzContent: KnowledgeBaseSelectorComponent,
      nzWidth: this.halfModalWidth,
      nzPlacement: "right",
      nzMask: true,
      nzMaskClosable: true,
      nzCloseOnNavigation: true,
      nzData: {
        existedKbs: config.data.map((kb: any) =>
          this.kbAbilitiesService.getKbId(kb)
        )
      }
    });
    let confirmed = false;
    drawerRef.afterOpen.subscribe(() => {
      const comp = drawerRef.getContentComponent();
      if (comp) {
        comp.addKB?.subscribe(() => {
          confirmed = true;
        });
      }
    });
    drawerRef.afterClose.subscribe(() => {
      if (!confirmed) {
        return;
      }
      this.isDeepLoading.emit(true);
      const comp = drawerRef.getContentComponent();
      const selectedKbIds = comp?.selectedIds ?? [];
      const selectedKbs = comp?.selectedKbs ?? [];
      const data = {
        knowledge_repos: selectedKbIds,
        type: this.agentInfo.agentSubType
      };
      this.doTriggerSave({
        data,
        successCb: () => {
          config.data = selectedKbs.map((item) => {
            return {
              desc: item.description,
              icon: item.icon,
              knowledge_repo_id: item.id,
              knowledge_repo_name: item.name,
              status: item.status
            };
          });
          this.capabilityConfig[2].collapsed = false;
        },
        finallyCb: () => {
          this.isDeepLoading.emit(false);
        }
      });
    });
  }

  // 保存Agent的数据
  private doTriggerSave(
    config: myWorkspaceTypes.IUpdateSingleAgentConfig = {
      data: {},
      successCb: () => {
      },
      failCb: () => {
      },
      finallyCb: () => {
      }
    }
  ) {
    this.commonLogic.doTriggerSave(this.agentId, config);
  }

  // 更新知识库的配置信息
  public updateKbConfig(configData: IKbParam) {
    this.kbParams = { ...configData };

    this.doTriggerSave({
      data: {
        knowledge_retrieve_policy: {
          ...configData
        },
        type: this.agentInfo.agentSubType
      }
    });
  }

  // 展开对话框区域
  public isExpandRuntimeArea(data: boolean) {
    if (data) {
      // 展开runtime区域
      this.leftAndRightAreaStyle.left.width = "0%";
      this.leftAndRightAreaStyle.left.padding = "";
      this.leftAndRightAreaStyle.right.width = "100%";
      this.leftAndRightAreaStyle.right.marginLeft = "auto";
    } else {
      this.leftAndRightAreaStyle.left.width = "50%";
      this.leftAndRightAreaStyle.left.padding = "24px 0 24px 24px";
      this.leftAndRightAreaStyle.right.width = "50%";
    }
  }

  // 查看deepResearch产物
  public viewArticleArea(data: boolean) {
    if (data) {
      // 查看article
      this.leftAndRightAreaStyle.left.width = "0%";
      this.leftAndRightAreaStyle.left.padding = "";
      this.leftAndRightAreaStyle.right.width = "100%";
    } else {
      this.leftAndRightAreaStyle.left.width = "0%";
      this.leftAndRightAreaStyle.left.padding = "";
      this.leftAndRightAreaStyle.right.width = "100%";
    }
  }

  public kbTipCtx = {
    param: this.kbParams,
    kbId: "",
    selectedNum: 0,
    outputs: {
      updatedParam: ($event: IKbParam) => {
        this.kbParams = { ...$event };
        this.showKbConfRef = false;
        this.kbConfRef?.hide();
        this.doTriggerSave({
          data: {
            knowledge_retrieve_policy: {
              ...$event
            },
            type: this.agentInfo.agentSubType
          }
        });
      }
    }
  };

  onClickKbConf(event) {
    event.stopPropagation();

    if (this.isFlowReadonly || this.kbAbilitiesService?.isResourceDisabled || !this.capabilityConfig[2]?.data?.length) {
      return;
    }

    setTimeout(() => {
      this.showKbConfRef = !this.showKbConfRef;
      if (this.showKbConfRef) {
        this.kbTipCtx.param = this.kbParams;
        this.kbTipCtx.kbId = this.kbAbilitiesService.getKbId(
          this.capabilityConfig[2].data?.[0]
        );
        this.kbTipCtx.selectedNum = this.capabilityConfig[2].data?.length || 0;
        this.kbConfRef?.show();
      } else {
        this.kbConfRef?.hide();
      }
    });
  }

  public onShowKbConfTip(event: boolean | MouseEvent) {
    if (typeof event === "boolean") {
      this.showKbConfRef = event;
      if (event) {
        if (this.isFlowReadonly || this.kbAbilitiesService.isResourceDisabled) {
          this.showKbConfRef = false;
          return;
        }
        this.kbTipCtx.param = this.kbParams;
        this.kbTipCtx.kbId = this.kbAbilitiesService.getKbId(
          this.capabilityConfig[2].data?.[0]
        );
        this.kbTipCtx.selectedNum = this.capabilityConfig[2].data?.length || 0;
      }
    } else {
      event.stopPropagation();
    }
  }

  get halfModalWidth() {
    if (this.windowWidth > 1920) {
      return "800px";
    } else {
      return "600px";
    }
  }

  public getBgColor(item, type) {
    switch (type) {
      case "kb": {
        const { status, hover } = item;
        const valid = KnowledgeRepoStatus.CLOSE !== status;
        return getCardBgColor({ valid, hover });
      }
      case "plugin": {
        return getCardBgColor(item);
      }
      default: {
        return getCardBgColor(item);
      }
    }
  }

  /** 删除一个知识库 */
  public deleteOneKnowledge(knowledgeItem: any) {
    if (this.kbAbilitiesService.isResourceDisabled) {
      return;
    }
    this.isDeepLoading.emit(true);
    const config = this.capabilityConfig[2];
    const knowledgeList = config.data.filter(
      (item: any) =>
        this.kbAbilitiesService.getKbId(item) !==
        this.kbAbilitiesService.getKbId(knowledgeItem)
    );
    const kbIds =
      knowledgeList?.map((kb) => this.kbAbilitiesService.getKbId(kb)) ?? [];
    const data = {
      knowledge_repos: kbIds,
      type: this.agentInfo.agentSubType
    };

    this.doTriggerSave({
      data,
      successCb: () => {
        config.data = knowledgeList;
        MessageComponent.showSuccess(
          this.i18n.transform("successfully_deleted_knowledge"),
          3000
        );
        this.cdr.markForCheck();
        this.kbParams.show_source = false;
        this.kbTipCtx.outputs.updatedParam(this.kbParams);
      },
      finallyCb: () => {
        this.isDeepLoading.emit(false);
        this.cdr.markForCheck();
      }
    });
  }

  /** 删除数据 **/
  public deleteData(config: any) {
    if (config.id === "template") {
      this.handleClickDeleteTemplate();
    } else if (config.id === "search") {
      this.handleClickDeleteSearch();
    }
  }

  // 删除模板数据
  private handleClickDeleteTemplate() {
    this.isDeepLoading.emit(true);
    this.doTriggerSave({
      data: {
        is_template: false,
        writing_template: "",
        type: this.agentInfo.agentSubType
      },
      successCb: () => {
        MessageComponent.showSuccess(
          this.i18n.transform("deleted_successfully")
        );
        this.capabilityConfig[3].data.length = 0;
        this.isTemplate = false;
      },
      failCb: () => {
        MessageComponent.showError(this.i18n.transform("deletion_failed"));
      },
      finallyCb: () => {
        this.isDeepLoading.emit(false);
      }
    });
  }

  // 删除搜索引擎数据
  private handleClickDeleteSearch() {
    this.isDeepLoading.emit(true);
    this.doTriggerSave({
      data: {
        search_engine: {},
        type: this.agentInfo.agentSubType
      },
      successCb: () => {
        MessageComponent.showSuccess(
          this.i18n.transform("deleted_successfully")
        );
        this.capabilityConfig[1].data.length = 0;
        this.searchEngine = [];
        this.updateSearchEngine.emit(this.searchEngine.length);
        this.copySearchEngine = this.searchEngine;
      },
      failCb: () => {
        MessageComponent.showError(this.i18n.transform("deletion_failed"));
      },
      finallyCb: () => {
        this.isDeepLoading.emit(false);
      }
    });
  }
}
