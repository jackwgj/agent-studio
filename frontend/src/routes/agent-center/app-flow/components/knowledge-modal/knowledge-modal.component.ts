import { ChangeDetectorRef, Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from "@angular/core";
import { NgForm } from "@angular/forms";
import { I18nNamespace } from "@i18n";
import { AgentConfigService } from "@routes/agent-center/agent-config.service";
import { type IKbParam, KbConfComponent } from "@routes/agent-center/app-agent/components/config-tools/kb-conf.component";
import { EditNameComponent } from "@routes/agent-center/app-flow/components/edit-name/edit-name.component";
import { KnowledgeCardUsed } from "@routes/agent-center/components/knowledge-card-used/knowledge-card-used.component";
import { KnowledgeBaseCreationComponent } from "@routes/knowledge-center/components/knowledge-base-creation/knowledge-base-creation.component";
import { KnowledgeBaseSelectorComponent } from "@routes/knowledge-center/components/knowledge-base-selector/knowledge-base-selector.component";
import { TagRuleBuilderComponent } from "@routes/knowledge-center/components/tag-rule-builder/tag-rule-builder.component";
import { KBScope, TagRule } from "@routes/knowledge-center/knowledge.types";
import { KnowledgeRepoService } from "@services/agent-center/knowledge.service";
import { KbAbilitiesService } from "@services/knowledge-center/kb-abilities.service";
import { RefSelectedRequireDirective } from "@shared/directives/common-validator.directive";
import { NonEmptyValidatorDirective } from "@shared/directives/variable-name-validator.directive";
import { MODULES } from "@shared/modules";
import { I18NEXT_NAMESPACE, I18NextPipe } from "angular-i18next";
import { cloneDeep } from "lodash";
import { NzDrawerService } from "ng-zorro-antd/drawer";
import { AppFlowService } from "../../app-flow.service";
import { WORKFLOW_SVGS } from "../../flow.const";
import { NodeService } from "../../node.service";
import { IKbConf, type IKnowledgeRepo, IParamRef, IWorkflowField } from "../../node.type";
import { AccBlockComponent } from "../acc-block/acc-block.component";
import { ModalBaseComponent } from "../base/modal-base.component";
import { NodeDescriptionComponent } from "../node-description/node-description.component";
import { ReadonlyParamsTreeComponent } from "../readonly-params/readonly-params-tree.component";
import { NodeUtils } from "../utils";
import { InputTreeSelect } from "src/routes/agent-center/app-flow/components/input-tree-select/input-tree-select";
import { NodeTypeTopic } from "@routes/agent-center/types/common.types";
import { HelpCenterService } from "@services/help-center.service";
import { CommonService } from "@services/common.service";
import { NzTooltipDirective } from "ng-zorro-antd/tooltip";

@Component({
  selector: "meta-knowledge-modal",
  templateUrl: "./knowledge-modal.component.html",
  styleUrls: ["./knowledge-modal.component.less", "../common-styles.less"],
  standalone: true,
  imports: [
    MODULES,
    AccBlockComponent,
    NonEmptyValidatorDirective,
    RefSelectedRequireDirective,
    ReadonlyParamsTreeComponent,
    KnowledgeCardUsed,
    KnowledgeBaseCreationComponent,
    EditNameComponent,
    NodeDescriptionComponent,
    TagRuleBuilderComponent,
    InputTreeSelect,
    KbConfComponent
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER]
    },
    NzDrawerService
  ]
})
export class KnowledgeModalComponent
  extends ModalBaseComponent
  implements OnInit, OnDestroy {
  @ViewChild("kbCreation") kbCreation: KnowledgeBaseCreationComponent;
  @ViewChild("settingIcon") settingIcon: ElementRef;
  @Output() openDefaultKnowledgeBase = new EventEmitter<string>();
  @Output() openThirdPartyKnowledgeBase = new EventEmitter<string>();
  @Output() openAccessKnowledgeBase = new EventEmitter<string>();
  @Input("names") names: string[];

  @Input("nodeInfo") nodeInfo: IKnowledgeRepo;

  @Output("confirm") confirm = new EventEmitter<any>();

  @ViewChild("inputForm") inputForm: NgForm;
  @ViewChild("kbConfRef") kbConfRef: NzTooltipDirective;

  isKbConfTipShow: boolean = false;

  public kbParams: IKbParam = {
    top_k: 3,
    recall_threshold: 0.5,
    faq_threshold: 0.9,
    search_mode: "",
    need_extras_faq_search: false,
    extra_params: []
  };

  public kbs: IKbConf[] = [];

  public icon = WORKFLOW_SVGS.KnowledgeRepo;

  public inputParams: IWorkflowField[] = [];

  public sourceOptions = [
    { label: this.i18n.transform("ref"), value: "ref" },
    { label: this.i18n.transform("literal"), value: "literal" }
  ];

  public nameRefOptions: IParamRef[] = [];

  tagRule: TagRule = {
    basic: null,
    advancedRules: null
  };

  needValid = false;

  knowledgeTip = `${this.i18n.transform("knowledge")}${this.i18n.transform("can_not_empty")}`;
  updateTimeout: any = null;
  drawerRef: any;
  private isConfigChange = false;

  constructor(
    private i18n: I18NextPipe,
    protected override nodeServ: NodeService,
    protected override appFlowServ: AppFlowService,
    private configServ: AgentConfigService,
    private drawerService: NzDrawerService,
    public kbAbilitiesService: KbAbilitiesService,
    private knowledgeRepoService: KnowledgeRepoService,
    private helpCenterService: HelpCenterService,
    protected commonService: CommonService,
    private cdr: ChangeDetectorRef
  ) {
    super(nodeServ, appFlowServ);
  }

  get kbLimit() {
    return this.configServ.getConfigs()?.agent_knowledge_bound_limit ?? 1;
  }

  public override ngOnInit() {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();

    const parentNode = this.getParentNodeInfo(this.appFlowServ.getGraph());
    if (parentNode) {
      this.getLoopInnerNodeRefs(parentNode, { strOnly: true }).subscribe(
        (info) => {
          this.onRefUpdate(info);
        }
      );
    } else {
      this.getSelfRefs({ strOnly: true }).subscribe((info) => {
        this.onRefUpdate(info);
      });
    }

    const {
      top_k,
      recall_threshold,
      search_mode,
      faq_threshold,
      repos,
      need_extras_faq_search,
      extra_params,
      show_source = false,
      retrieve_image = false,
      common_tags = null,
      advanced_tags = null
    } = this.nodeInfo.configs;

    this.kbs = cloneDeep(repos);
    this.batchKbDetails();
    this.kbParams = {
      top_k,
      recall_threshold,
      search_mode,
      faq_threshold,
      need_extras_faq_search,
      extra_params,
      show_source,
      retrieve_image
    };
    this.tagRule = {
      basic: common_tags,
      advancedRules: advanced_tags
    };
  }

  batchKbDetails() {
    if (this.kbs.length) {
      this.knowledgeRepoService.getKnowledgeBaseList({
        knowledge_base_ids: this.kbs.map(kb => this.kbAbilitiesService.getKbId(kb)),
        limit: 100,
        offset: 0,
        share_scope: KBScope.ALL
      }).then(res => {
        const kbItems = this.kbAbilitiesService.normalizeKBList(res.items);
        this.kbs = [...this.kbs.map(kb => {
          const actualKb = kbItems.find(kbRes => kbRes.id === kb.id);
          return actualKb ?? kb;
        })];
      });
    }
  }

  ngAfterViewInit(): void {
    if (this.appFlowServ.testRunVerificationError) {
      setTimeout(() => {
        this.validateNode();
      });
    }
  }

  public override ngOnDestroy() {
    super.ngOnDestroy();
    this.helpCenterService.hideHelpPanel();
    this.modelCloseSave();
    this.drawerRef?.close();
  }

  public onRefUpdate(info: IParamRef[]) {
    this.nameRefOptions = info;
    if (this.isInit) {
      this.inputParams = NodeUtils.initInputs(
        this.nodeInfo.inputs,
        this.nameRefOptions
      );
    } else {
      NodeUtils.reSelectRefsWithNewOps(this.inputParams, this.nameRefOptions);
    }
    this.isInit = false;
  }

  public onTypeChange(row: IWorkflowField) {
    row.value.content = NodeUtils.getChangeContent(row.value.type);
    this.onSave();
  }

  validateNode() {
    this.needValid = true;
  }

  getKbTagRule() {
    if (!this.kbs.length) {
      return {
        common_tags: null,
        advanced_tags: null
      };
    }
    return {
      common_tags: this.tagRule.basic,
      advanced_tags: this.tagRule.advancedRules
    };
  }

  public openAddKbModal() {
    if (this.kbAbilitiesService.isResourceDisabled) {
      return;
    }
    const existedKbs = this.kbs.map(kb => this.kbAbilitiesService.getKbId(kb));
    this.drawerRef = this.drawerService.create({
      nzTitle: undefined,
      nzPlacement: "right",
      nzWidth: "700px",
      nzClosable: true,
      nzMaskClosable: true,
      nzKeyboard: true,
      nzContent: KnowledgeBaseSelectorComponent,
      nzData: {
        existedKbs
      }
    });

    this.drawerRef.afterOpen.subscribe(() => {
      const instance = this.drawerRef.getContentComponent();
      instance.createKB.subscribe(() => {
        this.drawerRef.close();
        this.kbCreation.createKb();
      });
      instance.addKB.subscribe(() => {
        this.kbs = this.drawerRef.getContentComponent().selectedKbs ?? [];
        this.cdr.detectChanges();
        this.onSave();
      });
    });
  }

  public cleanKb(index: number) {
    this.kbs.splice(index, 1);
    this.handelSave();
  }

  public handleRetrievalSettings(e: Event) {
    if (this.drawerRef) {
      this.drawerRef.close();
      return;
    }

    this.isConfigChange = true;
    this.drawerRef = this.drawerService.create({
      nzTitle: undefined,
      nzPlacement: "right",
      nzWidth: 500,
      nzClosable: false,
      nzMaskClosable: true,
      nzKeyboard: true,
      nzContent: KbConfComponent,
      nzContentParams: {
        param: this.kbParams,
        kbId: this.kbs?.[0]?.id
      }
    });

    this.drawerRef.componentInstance.outputs = {
      updatedParam: (params) => {
        this.kbParams = params;
        this.drawerRef.close();
        this.drawerRef = null;
        this.isConfigChange = false;
        this.onSave();
      }
    };

    this.drawerRef.afterClose.subscribe(() => {
      this.drawerRef = null;
      this.isConfigChange = false;
    });
  }

  onKbConfTipClick(event) {
    setTimeout(()=>{
      this.isKbConfTipShow = !this.isKbConfTipShow;
      if (this.isKbConfTipShow) {
        this.kbConfRef.show();
      } else {
        this.kbConfRef.hide();
      }
    })

    this.isConfigChange = true;
  }

  updateKBConfTip(params) {
    if (this.isKbConfTipShow) {
      this.kbParams = params;
      this.kbConfRef.hide();
      this.isKbConfTipShow = false;
      this.isConfigChange = false;
      this.onSave();
    }
  }

  ruleComplete(tagRule: TagRule) {
    this.tagRule = tagRule;
    this.onSave();
  }

  dismiss(): void {
  }

  close(): void {
  }

  inputOnSave() {
    if (this.appFlowServ.testRunVerificationError) {
      this.handelSave();
    }
  }

  modelCloseSave() {
    if (!this.tagCompareNoChange()) {
      this.appFlowServ.setNodeModalCloseMonitor({ id: this.nodeInfo.id });
    }
    this.handelSave();
  }

  onSave() {
    this.changeUpdateTime();
    if (this.appFlowServ.testRunVerificationError) {
      this.handelSave();
    }
  }

  treeSelect() {
    setTimeout(() => {
      this.onSave();
    });
  }

  handelSave() {
    if (this.tagCompareNoChange()) {
      return;
    }
    const {
      top_k,
      recall_threshold,
      search_mode = "doc",
      faq_threshold = 0.9,
      need_extras_faq_search = false,
      extra_params = [],
      show_source = false,
      retrieve_image = false
    } = this.kbParams;
    this.appFlowServ.setNodeSaveMonitor({
      nodeData: {
        ...this.nodeInfo,
        inputs: NodeUtils.getDtoInputs(this.inputParams),
        configs: {
          repos: this.kbs.map(kb => {
            return {
              id: this.kbAbilitiesService.getKbId(kb),
              name: kb.name,
              description: kb.description
            };
          }),
          top_k: Number(top_k),
          recall_threshold: Number(recall_threshold),
          search_mode,
          faq_threshold,
          need_extras_faq_search,
          extra_params,
          show_source,
          retrieve_image,
          ...this.getKbTagRule()
        }
      }
    });
    if (this.updateTimeout) {
      clearTimeout(this.updateTimeout);
      this.updateTimeout = null;
    }
    this.updateTimeout = setTimeout(() => {
      this.updateChangeAndInitTime();
    }, 200);
  }
}
