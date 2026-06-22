import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  QueryList,
  SimpleChanges,
  ViewChildren
} from "@angular/core";
import { I18nNamespace } from "@i18n";
import { AgentConfigService } from "@routes/agent-center/agent-config.service";
import { ADD_TYPE } from "@routes/app-center/app-center-management/app-center-management.config";
import { ShareService } from "@routes/app-center/components/edit-share-modal/share.service";
import { AgentDataService } from "@services/agent-center/agent-data.service";
import { AppFlowRepoService } from "@services/agent-center/app-flow-repo.service";
import { AppLibraryRepoService } from "@services/app-library/app-library-repo.service";
import { NewCommonNoDataWithBtnComponent } from "@shared/components/new-common-no-data-with-btn/new-common-no-data-with-btn.component";
import { NoDataIconComponent } from "@shared/components/no-data-icon/no-data-icon.component";
import { MODULES } from "@shared/modules";
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { Subject, takeUntil } from "rxjs";
import { CommonUtils } from "src/utils/common.util";
import { agentCommonLogic } from "../../../app-agent/common-logic-agent";
import { PAGINATION_LIMIT_NUM, WORKFLOW_NORMAL_STATUS } from "../../../types/my-workspace.types";

@Component({
  selector: "add-workflows",
  templateUrl: "./add-workflows.component.html",
  styleUrls: ["./add-workflows.component.scss"],
  standalone: true,
  imports: [MODULES, NoDataIconComponent, NewCommonNoDataWithBtnComponent, NzInputModule, NzSelectModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT]
    }
  ]
})
export class AddWorkflowsComponent {
  @Input() workflows = [];

  @Output() workflowAdded = new EventEmitter<any[]>();

  @Output() workflowReduced = new EventEmitter<any[]>();
  @Output() workflowCreate = new EventEmitter<any[]>();
  @Input() showAddBtn: boolean;
  @Output() showAddBtnChange = new EventEmitter<any>();
  @ViewChildren("descTipHost") descTipHost!: QueryList<ElementRef>;

  public emptyPlaceholder = this.i18n.transform("search_and_filter");

  public workflowSelected: any = [];
  public workflowCardSelected: any = []; // 专门设置一个与select-group双向绑定的存放workflowid的内容

  // 知识库对应的数据与分页
  public workflowData: any = {
    list: [],
    isLoading: false
  };

  public currentPage = 1;

  public totalNumber = 0;

  public searchValue: string = "";

  private destroy$ = new Subject<void>();

  get searchNameIsEmpty(): boolean {
    return this.searchValue.length === 0;
  }

  public workflowTips = this.i18n.transform("workflow_not_published");

  public addTypeOptions = [
    {
      title: this.i18n.transform("addchildflowmodal_1"),
      id: ADD_TYPE.CURRENT_SPACE,
      active: true
    },
    {
      title: this.i18n.transform("addchildflowmodal_2"),
      id: ADD_TYPE.SHARE,
      active: false
    }
  ];
  public activeTabId = this.addTypeOptions[0].id;
  public shareSearchKey = "";

  get isShareTab() {
    return this.activeTabId === ADD_TYPE.SHARE;
  }

  public lang = CommonUtils.getLanguage();

  get isZH(): boolean {
    return this.lang === "zh-cn";
  }

  constructor(
    private appFlowServe: AppFlowRepoService,
    private agentDataServe: AgentDataService,
    private agentCommonLogic: agentCommonLogic,
    private cdr: ChangeDetectorRef,
    private configServ: AgentConfigService,
    private i18n: I18NextEagerPipe,
    private shareService: ShareService,
    private service: AppLibraryRepoService
  ) {
    this.agentDataServe
      .clickFlagWorkflowUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe((clickFlag: string) => {
        this.clearSearchboxValue();
        if (clickFlag === "add") {
          // 添加工作流后
          this.initData();
          /** 点击外部的添加工作流按钮，以父组件的@Input属性为准*/
          this.initSelectedWorkflow(this.workflows);
          this.addTypeOptions.forEach(v => v.active = false);
          this.addTypeOptions[0].active = true;
          this.activeTabId = this.addTypeOptions[0].id;

          this.loadWorkflowList();
        }
        this.cdr.markForCheck();
      });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.workflows) {
      // 创建副本，避免同步修改父组件中的workflowAdded.list
      this.initSelectedWorkflow(changes.workflows.currentValue);
    }
  }

  getActiveTabIndex() {
    return this.addTypeOptions.findIndex(itm => itm.active);
  }

  initSelectedWorkflow(workflows) {
    this.workflowSelected = [...workflows];
    this.workflowCardSelected = this.workflowSelected.map(item => item.workflow_id); //需要更新双向绑定的workflowId
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get flowLimit() {
    return this.configServ.getConfigs()?.agent_workflow_bound_limit ?? 5;
  }

  ngModelChange(workflowIds) {
    // 找出新增的id
    const addedIds = workflowIds.filter(id =>
      !this.workflowSelected.some(item => item.workflow_id === id)
    );
    // 找出新增的项
    const newAddItem = this.workflowData.list.find(item => addedIds[0] === item.workflow_id);

    // 找出删除的项
    const removeItems = this.workflowSelected
      .filter(item => !workflowIds.includes(item.workflow_id));
    if (newAddItem) {
      this.addOneWorkflow(newAddItem);
    } else if (removeItems) {
      this.reduceOneWorkflow(removeItems[0]);
    }
  }

  public addOneWorkflow(item: any) {
    this.workflowSelected.push(item);
    this.workflowAdded.emit(this.workflowSelected);
  }

  public reduceOneWorkflow(item: any) {
    const index = this.workflowSelected.findIndex(
      (workflowItem: any) => workflowItem.workflow_id === item.workflow_id
    );
    if (index > -1) {
      this.workflowSelected.splice(index, 1);
    }
    this.workflowReduced.emit(this.workflowSelected);
  }

  public toggleWorkflowSelection(item: any, checked: boolean): void {
    if (checked) {
      if (!this.workflowCardSelected.includes(item.workflow_id)) {
        this.addOneWorkflow(item);
        this.workflowCardSelected.push(item.workflow_id);
      }
    } else {
      if (this.workflowCardSelected.includes(item.workflow_id)) {
        this.reduceOneWorkflow(item);
        this.workflowCardSelected = this.workflowCardSelected.filter(id => id !== item.workflow_id);
      }
    }
  }


  /** 是否显示暂无数据。返回为真，表示知识库工作流列表非空 */
  public isShowNoData() {
    return !this.workflowData.isLoading && this.workflowData.list?.length;
  }

  public handleIconClick(type: string, item: any, event: any) {
    event.stopPropagation();
    if (type === "add") {
      this.addOneWorkflow(item);
    } else {
      this.reduceOneWorkflow(item);
    }
  }

  public loadWorkflowList(type?: string) {
    if ("search" === type) {
      this.currentPage = 1;
    }
    const params: any = {
      offset: ((this.currentPage || 1) - 1) * PAGINATION_LIMIT_NUM,
      limit: PAGINATION_LIMIT_NUM,
    };
    if (this.searchValue) {
      params.name = this.searchValue;
    }

    this.workflowData.isLoading = true;
    this.appFlowServe.getFlows(params).then(
      (res: any) => {
        this.processTip();
        this.workflowData.isLoading = false;
        this.workflowData.list = res.workflow_list.map(item => ({ ...item, disabled: item.status !== WORKFLOW_NORMAL_STATUS }));
        this.totalNumber = res?.count;
        this.cdr.markForCheck();
      },
      () => {
        this.workflowData.isLoading = false;
        this.cdr.markForCheck();
      }
    );
  }

  public addExtraWorkflow(info) {
    let item = this.workflowData.list.find(ele => ele.workflow_id = info.id);
    if (!item) {
      item = info;
      item.workflow_id = info.id;
    }
    this.addOneWorkflow(item);
  }

  private initData() {
    this.currentPage = 1;
    this.totalNumber = 0;
    this.workflowData = {
      list: [],
      isLoading: false
    };
    this.cdr.markForCheck();
  }

  /** 清空搜索框的输入值 */
  private clearSearchboxValue() {
    this.searchValue = "";
  }

  /** 通过服务生成的方式，生成超长的知识库卡片描述tip */
  private processTip() {
    setTimeout(() => {
      this.agentCommonLogic.createTip(
        this.descTipHost.toArray(),
        null
      );
    });
  }

  handleClickClearSearch(): void {
    this.init();
  }

  createWorkflow() {
    this.workflowCreate.emit();
  }

  public tabChange(tab: any): void {
    if (tab.id === ADD_TYPE.CURRENT_SPACE) {
      this.addTypeOptions[0].active = true;
      this.addTypeOptions[1].active = false;
    } else {
      this.addTypeOptions[1].active = true;
      this.addTypeOptions[0].active = false;
    }
    this.activeTabId = tab.id;
    this.searchValue = "";
    this.shareSearchKey = "";
    if (this.activeTabId !== this.addTypeOptions[0].id) {
      this.showAddBtn = false;
    } else {
      this.showAddBtn = true;
    }
    this.showAddBtnChange.emit(this.showAddBtn);
    this.initList();
  }

  public init($event?: any) {
    this.searchValue = "";
    this.currentPage = 1;
  }

  public initList($event?: any) {
    this.init();
    if (this.isShareTab) {
      this.initShareList();
    } else {
      this.loadWorkflowList();
    }
  }

  public initShareList() {
    let params: any = {
      offset: ((this.currentPage || 1) - 1) * PAGINATION_LIMIT_NUM,
      limit: PAGINATION_LIMIT_NUM,
      resource_type: "workflow"
      // 增加区分预置和当前的参数
    };
    if (this.shareSearchKey) {
      params.resourceName = this.shareSearchKey;
    }

    this.workflowData.isLoading = true;
    this.shareService.getShareList(params).then(
      (res: any) => {
        this.processTip();
        this.workflowData.isLoading = false;
        const list = res?.resource_list || [];
        list.forEach((v) => {
          v.name = v.resource_name_ch;
          v.description = v.resource_desc;
          v.status = WORKFLOW_NORMAL_STATUS;
          v.disabled = false;
          v.workflow_id = v.resource_id;
          v.last_version_id = v.version_info_list[0].version_id;
          v.avatar = v.resource_icon;
          v.share = true;
        });
        this.workflowData.list = list;
        this.totalNumber = res?.count;
        this.cdr.markForCheck();
      },
      () => {
        this.workflowData.isLoading = false;
        this.cdr.markForCheck();
      }
    );
  }

  visionClick(event) {
    event?.stopPropagation();
  }
}
