import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnInit,
  Output,
  QueryList,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { NgForm, Validators } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { batchBindFlowsComponent } from '@shared/components/batch-bind-flows-tip';
import { NoDataIconComponent } from '@shared/components/no-data-icon/no-data-icon.component';
import { RefSelectedRequireDirective } from '@shared/directives/common-validator.directive';
import { NonEmptyValidatorDirective } from '@shared/directives/variable-name-validator.directive';
import { MODULES } from '@shared/modules';
import { CommonValidation } from '@shared/validation/commonValidation';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { cloneDeep, isNil, isObject, isString } from 'lodash';
import { debounceTime, filter, Subject, Subscription, takeUntil } from 'rxjs';
import { AppFlowService } from '../../app-flow.service';
import { LAZY_LOAD_LIMIT, WORKFLOW_SVGS } from '../../flow.const';
import { NodeService } from '../../node.service';
import type { IParamRef, IWorkflowField } from '../../node.type';
import { type IIntentContainerNode } from '../../node.type';
import { AccBlockComponent } from '../acc-block/acc-block.component';
import { ModalBaseComponent } from '../base/modal-base.component';
import { NodeDependencies } from '../modules';
import { ParamTreeComponent } from '../param-tree/param-tree.component';
import { NodeUtils } from '../utils';
import { FlowSelectService } from '@shared/services/flow-select.service';
import { ActivatedRoute } from '@angular/router';
import { IntentPackageService } from '@routes/intent-package/intent-package.service';
import { EditNameComponent } from '@routes/agent-center/app-flow/components/edit-name/edit-name.component';
import { NodeDescriptionComponent } from '../node-description/node-description.component';
import { InputTreeSelect } from 'src/routes/agent-center/app-flow/components/input-tree-select/input-tree-select';
interface IFlowIdAndVersion {
  workflow_id: string;
  version_id: string;
}

@Component({
  selector: 'meta-intent-container-modal',
  templateUrl: './intent-container-modal.component.html',
  styleUrls: [
    './intent-container-modal.component.scss',
    '../common-styles.less',
  ],
  standalone: true,
  imports: [
    MODULES,
    NodeDependencies,
    AccBlockComponent,
    RefSelectedRequireDirective,
    NoDataIconComponent,
    NonEmptyValidatorDirective,
    ParamTreeComponent,
    EditNameComponent,
    NodeDescriptionComponent,
    InputTreeSelect,
    batchBindFlowsComponent
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class IntentContainerModalComponent
  extends ModalBaseComponent
  implements OnInit
{
  @Input('names') names: string[];

  @Input('nodeInfo') nodeInfo: IIntentContainerNode;

  @Input('workflowType') workflowType: string = '';

  @Output('confirm') confirm = new EventEmitter<any>();

  @Input() outputs: {
    confirm?: (data: any) => void;
    closeDrawer?: () => void;
  } = {};

  @ViewChild('inputForm') inputForm: NgForm;

  @ViewChildren('boundFlowListForm') boundFlowListForm: QueryList<NgForm>;

  public icon = WORKFLOW_SVGS.Aggregation;

  public validationRules = [Validators.required];

  public validation: any = {
    type: 'change',
  };

  public requestValidation: any = {
    errorMessage: {
      required: this.i18n.transform('please_select_workflow'),
    },
    type: 'changeAlert',
  };

  public steps = [
    {
      label: this.i18n.transform('action_configuration'),
    },
    {
      label: this.i18n.transform('params_info'),
    },
  ];

  public curStepIndex = 0;

  public branchList: any[] = [];

  public isLoading = false;

  public isFlowsLoading = false;

  public inputParams: IWorkflowField[] = [];

  public sourceOptions = [
    { label: this.i18n.transform('ref'), value: 'ref' },
    { label: this.i18n.transform('literal'), value: 'literal' },
  ];

  public nameRefOptions: IParamRef[] = [];

  // 已绑定的所有工作流进行去重后的输入参数列表
  public allInputParams = [];

  public searchBranchName = '';

  public showBatchBindRef = false;

  private searchValueSubject$ = new Subject<string>();

  private subscription: Subscription;

  private branchNameRes = null;

  private workflowId = '';

  get branchListDynamic() {
    return this.searchBranchName ? this.branchNameRes : this.branchList;
  }

  public batchBindTipCtx = {
    workflowType: this.workflowType,
    outputs: {
      flowInfoSelected: (flowId: string) => {
        const selectedBranches = this.branchList.filter(
          (item) => item.selected,
        );
        if (this.isObject(flowId) && !isNil(flowId)) {
          selectedBranches.forEach((param) => {
            param.workflow_id = flowId;
          });
          this.showBatchBindRef = false;
          this.cdr.markForCheck();
        }
      },
    },
  };

  public isFlowListLoading = false;
  public flowSearchWord = '';

  public onBeforeOpenFlowList(isOpen: boolean) {
    if (!isOpen) return;
    this.isFlowListLoading = true;
    this.flowSearchWord = '';
    this.appFlowRepoServe
      .getPublishedFlows({ offset: 0, limit: LAZY_LOAD_LIMIT })
      .then(result => {
        this.flowSelecServe.updateFlowList(
          result?.workflow_version_list.filter(
            (item) => item.workflow_id !== this.workflowId,
          ) ?? []
        );
        this.isFlowListLoading = false;
        this.cdr.markForCheck();
      })
      .catch(() => {
        this.flowSelecServe.updateFlowList([]);
        this.isFlowListLoading = false;
        this.cdr.markForCheck();
      });
  }

  public onBeforeSearchFlow(searchValue: string) {
    this.flowSearchWord = searchValue;
    this.isFlowListLoading = true;
    this.appFlowRepoServe
      .getPublishedFlows({ offset: 0, limit: LAZY_LOAD_LIMIT, name: searchValue })
      .then(result => {
        this.flowSelecServe.updateFlowList(
          result?.workflow_version_list.filter(
            (item) => item.workflow_id !== this.workflowId,
          ) ?? []
        );
        this.isFlowListLoading = false;
        this.cdr.markForCheck();
      })
      .catch(() => {
        this.flowSelecServe.updateFlowList([]);
        this.isFlowListLoading = false;
        this.cdr.markForCheck();
      });
  }

  public loadMoreFlows() {
    const currentList = this.flowList || [];
    if (currentList.length >= (this.flowSelecServe as any).totalIntentBranches) return;
    this.isFlowListLoading = true;
    this.appFlowRepoServe
      .getPublishedFlows({
        offset: currentList.length,
        limit: LAZY_LOAD_LIMIT,
        name: this.flowSearchWord,
      })
      .then(result => {
        const merged = [...currentList, ...result.workflow_version_list].filter(
          (item) => item.workflow_id !== this.workflowId,
        );
        this.flowSelecServe.updateFlowList(merged);
        this.isFlowListLoading = false;
        this.cdr.markForCheck();
      })
      .catch(() => {
        this.isFlowListLoading = false;
        this.cdr.markForCheck();
      });
  }

  public compareFlow = (a: any, b: any): boolean => {
    if (!a && !b) return true;
    if (!a || !b) return false;
    if (typeof a === 'string' || typeof b === 'string') return a === b;
    return a?.workflow_id === b?.workflow_id;
  };

  public get flowList() {
    return this.flowSelecServe.getFlowList;
  }

  public get flowSelectOptions() {
    return (this.flowList || []).map(item => ({
      label: isObject(item) ? (item as any).name ?? '' : String(item),
      value: item,
    }));
  }

  constructor(
    protected override nodeServ: NodeService,
    protected override appFlowServ: AppFlowService,
    private appFlowRepoServe: AppFlowRepoService,
    private i18n: I18NextEagerPipe,
    private flowSelecServe: FlowSelectService,
    private route: ActivatedRoute,
    private intentRepoServe: IntentPackageService,
    private cdr: ChangeDetectorRef,
  ) {
    super(nodeServ, appFlowServ);

    this.route.queryParams.subscribe((params) => {
      this.workflowId = params.id;
    });
  }

  override async ngOnInit() {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();
    this.subNodeActions();

    this.validationRules.push(
      CommonValidation.nameUniquenessVerify(
        this.names,
        this.i18n.transform('name_uniqueness'),
        this.nodeInfo.name,
      ),
    );

    const parentNode = this.getParentNodeInfo(this.appFlowServ.getGraph());
    if (parentNode) {
      this.getLoopInnerNodeRefs(parentNode, { strOnly: true }).subscribe(
        (info) => {
          this.onRefUpdate(info);
        },
      );
    } else {
      this.getSelfRefs({ strOnly: true }).subscribe((info) => {
        this.onRefUpdate(info);
      });
    }

    if (this.nodeInfo?.branches?.length > 0) {
      // 已配置过容器节点，回填分支和已绑定的工作流信息
      await this.getBranchList();
      this.branchList = this.branchList.map((item: any) => {
        const matchBranch = this.nodeInfo.branches.find((branch: any) => {
          const branchKey = `${item.branch_id}`;
          const configKey = `${branch.configs.branch_id}`;
          return branchKey === configKey;
        });

        if (matchBranch) {
          return {
            branch_index: item.branch_index,
            name: matchBranch.configs.category,
            description: matchBranch.configs.description,
            intent_id: matchBranch.configs.intent_id,
            branch_id: matchBranch.configs.branch_id,
            workflow_id:
              matchBranch.configs.workflow_id !== ''
                ? {
                    name: matchBranch.configs.workflow_name,
                    workflow_id: matchBranch.configs.workflow_id,
                    version_id: matchBranch.configs.workflow_version_id,
                  }
                : '',
          };
        } else {
          return { ...item, workflow_id: '' };
        }
      });
      this.cdr.markForCheck();
    } else {
      // 未配置过容器节点，查询意图分支列表
      this.getBranchList().then(() => this.cdr.markForCheck());
    }

    // 搜索意图分支
    this.mountDebounceSearch();

    this.flowSelecServe.flowIdSetter = this.workflowId;
    this.flowSelecServe.flowTypeSetter = this.workflowType;

    this.isFlowListLoading = true;
    this.appFlowRepoServe
      .getPublishedFlows({ offset: 0, limit: LAZY_LOAD_LIMIT })
      .then(result => {
        this.flowSelecServe.updateFlowList(
          result?.workflow_version_list.filter(
            (item) => item.workflow_id !== this.workflowId,
          ) ?? []
        );
        this.isFlowListLoading = false;
        this.cdr.markForCheck();
      })
      .catch(() => {
        this.flowSelecServe.updateFlowList([]);
        this.isFlowListLoading = false;
        this.cdr.markForCheck();
      });
  }

  override ngOnDestroy() {
    this.subscription?.unsubscribe();
  }

  dismiss(): void {
    this.appFlowServ.setNodeModalCloseMonitor({ id: this.nodeInfo.id });
    this.outputs?.closeDrawer?.();
  }

  close(): void {
    this.outputs?.closeDrawer?.();
  }

  onConfirm(): void {
    this.inputForm?.form?.markAllAsTouched();
    if (this.inputForm?.form?.invalid) {
      return;
    }

    const inputs = cloneDeep(this.inputParams);
    inputs.forEach((param) => {
      if (param.value.type === 'ref') {
        const contentArr = (param.value.content as IParamRef[])?.[0];
        if (contentArr) {
          const { ref_node_id, ref_var_name, source } = contentArr;
          param.value.content = {
            ref_node_id,
            ref_var_name,
            source,
          };
        }
      }
      delete param?.refs;
    });
    const branches = this.getBranches(this.branchList);
    const response_content = this.getGroupsOfConfigs();

    this.appFlowServ.setNodeSaveMonitor({
      nodeData: {
        ...this.nodeInfo,
        inputs,
        branches,
        configs: {
          groups: {
            response_content,
          },
          model: 'first-non-null',
        },
      },
    });
    this.appFlowServ.setNodeModalCloseMonitor({ id: this.nodeInfo.id });
    this.outputs?.closeDrawer?.();
  }

  /** 是否存在选中的分支。若存在，批量按钮可点击 */
  public hasSelectedBranch(): boolean {
    return this.branchList.some((item) => item.selected);
  }

  public openBatchBindModal(): void {
    this.showBatchBindRef = true;
    this.batchBindTipCtx.workflowType = this.workflowType;
  }

  public batchUnbindFlows() {
    const selectedBranches = this.branchList.filter((item) => item.selected);
    selectedBranches.forEach((param) => {
      param.workflow_id = '';
    });
  }

  public nextStep(): void {
    this.boundFlowListForm.forEach((item) => {
      Object.values(item.controls).forEach((control) => control.markAsTouched());
    });

    const allFormsValid = this.boundFlowListForm
      .toArray()
      .every((item) => item.valid);
    if (allFormsValid) {
      const boundFlows = this.getBranchFlowNames();

      this.getBoundFlowsInfo(boundFlows);

      this.curStepIndex += 1;
      this.showBatchBindRef = false;
      this.cdr.markForCheck();
    }
  }

  public lastStep(): void {
    this.curStepIndex -= 1;
    this.cdr.markForCheck();
  }

  public subNodeActions() {
    this.appFlowServ.nodeActionUpdate$().pipe(
      filter((action) => action && action.id === this.nodeInfo.id),
      takeUntil(this.destroy$),
    );
  }

  public onRefUpdate(info: IParamRef[]) {
    this.nameRefOptions = info.filter(
      (item) => item.type !== 'IntentDetection',
    );
    // 若准备好输入参数后，只需调用reSelectRefsWithNewOps函数
    if (this.inputParams) {
      NodeUtils.reSelectRefsWithNewOps(this.inputParams, this.nameRefOptions);
    }
  }

  public onTypeChange(row: IWorkflowField) {
    row.value.content = NodeUtils.getChangeContent(row.value.type);
  }

  public searchBranches(event: string) {
    this.searchValueSubject$.next(event);
  }

  public isObject(value): boolean {
    return isObject(value);
  }

  /** 获取高级意图节点配置的意图包里的分支列表 */
  private async getBranchList() {
    this.isLoading = true;

    try {
      const params = {
        offset: 0,
        limit: 1000,
      };
      let result = await this.intentRepoServe.getIntentBranchesList(
        params,
        this.nodeInfo.intentId,
      );

      this.branchList = [
        {
          branch_id: 'branch_0',
          intent_id: this.nodeInfo.intentId,
          project_id: '',
          branch_index: 'branch_0',
          name: this.i18n.transform('other_intentions'),
          description: '',
          creator_id: '',
          examples: [],
          workflow_id: '',
        },
        ...result.branches.map((item) => ({
          ...item,
          selected: false,
          workflow_id: '',
        })),
      ];
    } finally {
      this.isLoading = false;
      this.cdr.markForCheck();
    }
  }

  /** 组装高级意图节点dsl的configs.groups字段值 */
  private getGroupsOfConfigs() {
    return this.branchList.map((item) => {
      return {
        name: item.branch_index,
        description: '',
        required: true,
        reflection: false,
        value: {
          type: 'ref',
          content: {
            ref_node_id: item.branch_index,
            ref_var_name: 'response_content',
            source: 'system',
          },
          hint: '',
        },
        source: 'user',
        type: 'string',
      };
    });
  }

  /** 组装高级意图节点dsl的branches字段值 */
  private getBranches(branchList: any[]) {
    return branchList.map((item) => {
      return {
        id: item.branch_index,
        configs: {
          category: item.name,
          description: item.description,
          index: item.branch_index,
          branch_id: item.branch_id,
          intent_id: item.intent_id,
          workflow_id: item?.workflow_id?.workflow_id ?? '',
          workflow_version_id: item?.workflow_id?.version_id ?? '',
          workflow_name: item?.workflow_id?.name ?? '',
          workflow_version_name: item?.workflow_id?.version_name ?? '',
        },
      };
    });
  }

  /**
   * 获取意图分支绑定的工作流id和版本号列表
   * @returns {IFlowIdAndVersion[]} 包含工作流id和版本号的对象列表
   */
  private getBranchFlowNames(): IFlowIdAndVersion[] {
    return this.branchList
      .map((param) => {
        const { workflow_id } = param;
        // 删除valueKey后，并且将ngModel替换为绑定workflow_id，所以workflow_id键值是完整对象
        if (this.isObject(workflow_id)) {
          return {
            workflow_id: param?.workflow_id?.workflow_id,
            version_id: param?.workflow_id?.version_id,
          };
        }
        return null;
      })
      .filter(
        (i) => !isNil(i) && i?.workflow_id !== '' && i?.version_id !== '',
      );
  }

  /**
   * 查询已绑定的所有工作流的dsl信息
   * @param boundFlows 已绑定的工作流id和版本号的列表
   */
  private async getBoundFlowsInfo(boundFlows: IFlowIdAndVersion[]) {
    this.isFlowsLoading = true;
    let inputList: any[] = [];

    try {
      const uniqueBoundFlows = Array.from(
        new Map(boundFlows.map((item) => [item.workflow_id, item])).values(),
      );

      const promises = uniqueBoundFlows
        .filter(
          (item) =>
            this.isNotEmptyStr(item.workflow_id) &&
            this.isNotEmptyStr(item.version_id),
        )
        .map((item) =>
          this.appFlowRepoServe.getFlowVersionInfo(
            item.workflow_id,
            item.version_id,
          ),
        );

      // 等待所有工作流dsl查询完成
      const results = await Promise.allSettled(promises);
      results.forEach((item) => {
        if (item.status === 'fulfilled') {
          const rawInputParams = this.appFlowServ.childFlow2ApiNode(
            item.value,
          ).inputs;
          inputList.push(rawInputParams);
        }
      });

      inputList = inputList.flat();
      this.allInputParams = Array.from(
        new Map(inputList.map((item) => [item.name, item])).values(),
      );
      // 输入参数列表准备好后，初始化输入参数inputParams
      this.inputParams = NodeUtils.initInputs(
        this.allInputParams,
        this.nameRefOptions,
      ).map((item) => ({
        ...item,
        refs: item.refs?.filter((ref) => ref.type !== 'IntentDetection') || [],
      }));
      this.updateInputs();
    } finally {
      this.isFlowsLoading = false;
      this.cdr.markForCheck();
    }
  }

  private mountDebounceSearch() {
    this.subscription = this.searchValueSubject$
      .pipe(debounceTime(300))
      .subscribe((name) => {
        this.isLoading = true;
        if (name) {
          const lowerCaseName = name.toLowerCase();
          this.branchNameRes = this.branchList.filter((item: any) =>
            item.name.toLowerCase().includes(lowerCaseName),
          );
          this.isLoading = false;
        } else {
          this.branchNameRes = null;
          this.isLoading = false;
        }
      });
  }

  /** 若已配置过容器节点，回填同名输入变量的值 */
  private updateInputs() {
    this.inputParams.forEach((item) => {
      const matchingSubItem = this.nodeInfo.inputs.find(
        (subItem) => subItem.name === item.name && subItem.type === item.type,
      );
      if (matchingSubItem) {
        Object.assign(item, matchingSubItem);
      }
    });
    // 注意：需要调用initInputs，初始化引用，否则回填后选不到引用
    this.inputParams = NodeUtils.initInputs(
      this.inputParams,
      this.nameRefOptions,
    ).map((item) => ({
      ...item,
      refs: item.refs?.filter((ref) => ref.type !== 'IntentDetection') || [],
    }));
  }

  private isNotEmptyStr(val) {
    return isString(val) && val.trim() !== '';
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.showBatchBindRef = false;
  }

  /**
   * 切换浏览器页签时，tiny tip会自动关闭，但没有修改 this.show 值
   * 规避先去别的页签，再切回来，要点击两次示例按钮，才能展示tip的问题
   */
  @HostListener('window:visibilitychange', ['$event'])
  onVisibilityChange() {
    if (document.hidden) {
      this.showBatchBindRef = false;
    }
  }

  @HostListener('scroll', ['$event'])
  onScroll() {
    this.showBatchBindRef = false;
  }
}
