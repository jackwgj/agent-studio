import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  OnDestroy,
  ChangeDetectorRef,
  ElementRef,
  AfterViewInit,
  isDevMode,
  Injector,
  Input,
  SimpleChanges,
  OnChanges,
  Renderer2,
} from '@angular/core';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { MODULES } from '@shared/modules';
import { Subject, takeUntil } from 'rxjs';
import { v4 as uuidV4 } from 'uuid';
import {
  AppFlowService,
  INodeHeightUpdate,
} from '@routes/agent-center/app-flow/app-flow.service';
import { Graph, MiniMap, Node, NodeProperties } from '@antv/x6';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { fillAdvancedBranches } from 'src/utils/utils';
import {
  EHalfmodalType,
  IWorkflow,
} from '@routes/agent-center/app-flow/app-flow.types';
import {
  FLOW_COLORS,
  MULTI_PORT_NODE,
} from '@routes/agent-center/app-flow/flow.const';
import { EnvironmentVariablesManagementService } from '@routes/platform-management/environment-variables-management/environment-variables-management.service';
import { debounce, throttle } from 'lodash';
import { NodeService } from '@routes/agent-center/app-flow/node.service';
import { FlowHelperService } from '@routes/agent-center/app-flow/utils/flow-helper.service';
import { AssetRunIconComponent } from '../../../shared/components/assets/asset-run-icon/asset-run-icon.component';
import { DialogHalfmodalComponent } from '../../agent-center/app-flow/components/dialog-halfmodal/dialog-halfmodal.component';
import { FlowLogModalComponent } from '@routes/agent-center/app-flow/components/flow-log-modal/flow-log-modal.component';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { FormBuilder } from '@angular/forms';
import { NzDrawerRef, NzDrawerService } from 'ng-zorro-antd/drawer';
import { Router } from '@angular/router';
import { CheckErrorWorkflowModal } from '@routes/agent-center/app-flow/components/check-error-workflow/check-error-workflow.component';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { NodeInfo } from '@routes/agent-center/app-flow/node.type';
import { FlowEventUtils } from '@routes/agent-center/app-flow/utils/flow-event-utils';
import { FlowNodeRegisterService } from '@routes/agent-center/app-flow/flow-node-register.service';

@Component({
  selector: 'ai-assist-workflow-preview',
  templateUrl: './flow-preview.component.html',
  styleUrls: ['./flow-preview.component.less'],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
  imports: [
    CommonModule,
    MODULES,
    NzProgressModule,
    NzAlertModule,
    NzToolTipModule,
    NzIconModule,
    NzDrawerModule,
    AssetRunIconComponent,
    DialogHalfmodalComponent,
    CheckErrorWorkflowModal,
  ],
})
export class AiAssistWorkflowPreviewComponent
  implements OnInit, OnDestroy, OnChanges, AfterViewInit
{
  @Input() flowRetryCount: number;
  @Input() isRunning: boolean;
  @Input() activeTab: string;
  @Input() currentNodeProcessData: any;
  @Input() workflowId: string;
  @Input() processLogs: any[] = [];
  @Input() isFullScreen: boolean;
  public rightMsgType: 'success' | 'error' | 'warning' | 'info' =
    'info';
  public rightMsgTitle: string;
  public rightMsgContent: string;
  public showGoEditBtn = false;
  // 试运行工作流接口所需的入参conversation_id
  public conversationId = uuidV4();
  public showRunModal = false;
  public isAllExpand = true;
  // 默认缩放百分数
  public defaultScale = 100;
  // 当前缩放百分数
  public localScale = '100';
  public showMiniMap = false;
  public previewGraph: Graph;
  public workflowDetail: IWorkflow;
  public validateWorkflowList = [];
  public checkErrorWorkflowHalfModalRef = false;
  public isLoadingMask = false;
  public testBtnLoading = false;
  public checkErrorTip: SafeHtml;
  protected previewGraphId = 'appFlowPreview';
  protected minimapGraphId = 'minimapContainer';
  private showLogModal = false;
  private logHalfModalRef: NzDrawerRef;
  private previewGraphContainer: HTMLDivElement;
  private envList: any = [];
  private destroy$ = new Subject<void>();
  // 所有节点对应的高度映射
  private nodeHeightMap: Map<string, number> = new Map();
  private clearMsgTimer: NodeJS.Timeout;
  private checkErrorCount = 0;
  private nodeModalTop: number;
  private addedNodes = new Set(); // 已添加节点ID
  private pendingEdges = {}; // 缓存未完成的连接: { targetId: [{ source, label }] }
  private lastNodePosition = {}; // 保留上一个节点的位置
  private edges = [];
  private loopIds = [];
  private miniMap: MiniMap;

  constructor(
    private cdr: ChangeDetectorRef,
    private i18n: I18NextEagerPipe,
    private elementRef: ElementRef,
    private appFlowServ: AppFlowService,
    private injector: Injector,
    private appFlowRepoServ: AppFlowRepoService,
    private envVariablesManagement: EnvironmentVariablesManagementService,
    private nodeServ: NodeService,
    private flowHelperServ: FlowHelperService,
    private agentDataServ: AgentDataService,
    private fb: FormBuilder,
    private renderer: Renderer2,
    private halfModalserv: NzDrawerService,
    private router: Router,
    private sanitizer: DomSanitizer,
    private nodeRegisterService: FlowNodeRegisterService,
    private nzMessageService: NzMessageService,
  ) {}

  ngOnInit(): void {
    this.nodeRegisterService.registerComponents(this.injector);
    this.appFlowServ
      .nodeHeightMapUpdate()
      .pipe(takeUntil(this.destroy$))
      .subscribe((val) => {
        // 收起节点卡片(即val.isExpand等于false) 或 isResetNodeHeightMap为true时，重置nodeHeightMap
        if (!val?.isExpand) {
          this.nodeHeightMap.clear();
        }

        if (
          this.flowHelperServ.isHeightNormal(val, this.localScale) &&
          val?.nodeType !== 'Loop'
        ) {
          const node = this?.previewGraph?.getCellById(
            val?.id,
          ) as Node<NodeProperties>;

          if (node) {
            const scale = Number(this.localScale) / 100;
            const size = node.size();

            const prevHeight = this.nodeHeightMap.get(val.id) || 0;
            // 只有当订阅到的新高度比之前的大时，才执行node.size
            if (val.height > prevHeight) {
              node.size(size.width, val.height / scale);
              this.nodeHeightMap.set(val.id, val.height);
            }
            if (MULTI_PORT_NODE.includes(val.nodeType)) {
              this.handleMultiPortNodeExpand(node, val);
            }
          }
        }
      });

    // 调试详情
    this.appFlowServ
      .nodeTestInfoActionUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe((action) => {
        if (action && action.id) {
          this.openLogHalfmodal([], action.id);
        }
      });
  }

  async ngOnChanges(changes: SimpleChanges): Promise<void> {
    if (
      changes.isFullScreen?.currentValue !==
        changes.isFullScreen?.previousValue ||
      changes.activeTab?.currentValue !== changes.activeTab?.previousValue
    ) {
      this.debounceWindowResize();
    }
    if (
      changes.flowRetryCount?.currentValue !==
      changes.flowRetryCount?.previousValue
    ) {
      await this.resetCacheData();
    }

    if (
      changes.currentNodeProcessData?.currentValue &&
      changes.currentNodeProcessData?.currentValue !==
        changes.currentNodeProcessData?.previousValue
    ) {
      if (!this.workflowId) {
        this.throttleInitGraphData();
      }
    }

    if (
      changes.workflowId?.currentValue &&
      changes.workflowId?.currentValue !== changes.workflowId?.previousValue
    ) {
      this.throttleInitGraphData();
    }
  }

  ngOnDestroy(): void {
    FlowHelperService.deleteDefineAmd();
    this.previewGraph?.off();
    this.destroy$.next();
    this.destroy$.complete();
    this.flowHelperServ.cleanupAppFlowService(
      this.appFlowServ,
      this.agentDataServ,
      this.nodeServ,
      this.fb,
    );
    this.nodeRegisterService.unregisterComponents();
    window.removeEventListener('resize', this.debounceWindowResize);
  }

  ngAfterViewInit(): void {}

  public clickCheckErrorWorkflowModal(nodeInfo): void {
    this.appFlowServ.setNodeIdClicked(nodeInfo?.id);
  }

  dismissCheckErrorWorkflowModal() {
    this.checkErrorWorkflowHalfModalRef = false;
    this.flowHelperServ.adjustRightForNodeModal('', 'left');
  }

  /** 放大画布 */
  public addCanvas() {
    this.previewGraph.zoom(0.1);
  }

  /** 缩小画布 */
  public supCanvas() {
    this.previewGraph.zoom(-0.1);
  }

  public onClickShowThumb() {
    this.showMiniMap = !this.showMiniMap;
  }

  public expandAllSwitch() {
    this.isAllExpand = !this.isAllExpand;
    this.nodeServ.setIsAllExpand(this.isAllExpand);
  }

  /** 定位到画布中间 */
  public onCenter() {
    this.previewGraph.centerContent();
  }

  public optimizeLayout() {
    if (!this.previewGraph) {
      return;
    }
    FlowEventUtils.optimizeDagreLayout(this.previewGraph);
    if (!this.workflowId) {
      this.resetLoopSizeAndPosition();
    }
    this.flowZoomToFit();
  }

  public async openRunHalfmodal() {
    if (this.testBtnLoading) {
      return;
    }
    this.testBtnLoading = true;
    setTimeout(async () => {
      this.testBtnLoading = false;
      await this.checkErrorWorkFlow();
      if (this.showRunModal) {
        this.appFlowServ.setRunBtnClicked(true);
        this.appFlowServ.setNodeClicked(EHalfmodalType.TEST_RUN);
        this.appFlowServ.setTokenData({});
        this.nodeServ.setCurrSelectedNode('');
      }
    }, 2000);
  }

  public dialogHalfModalClose() {
    this.showRunModal = false;
    const hfModalList: any = document.getElementsByTagName(
      'tp-halfmodalcontainer',
    );
    let right = 0;
    for (let i = 0; i < hfModalList.length; i++) {
      const reace = hfModalList[i]?.getBoundingClientRect();
      hfModalList[i]?.style?.setProperty('right', `${right}px`, 'important');
      right = right + reace?.width + 1;
    }
  }

  public clickErrorInfo(e) {
    this.openLogHalfmodal(['runTestHalfModel']);
  }

  public gotFlowDetail($event: Event): void {
    $event?.stopPropagation();
    FlowHelperService.toEditWorkflow(this.router, {
      id: this.workflowId,
    });
  }

  public openLogHalfmodal(exict?: Array<any>, openNodeId?: string) {
    this.closeAllWindow(exict);
    this.showLogModal = true;
    this.logHalfModalRef = this.halfModalserv.create({
      nzContent: FlowLogModalComponent,
      nzWidth: '600px',
      nzClosable: true,
      nzData: {
        openNodeId: openNodeId ?? '',
        isShowLog: this.showLogModal,
        graph: this.previewGraph,
        workflowDetail: this.workflowDetail,
        inputWorkflowId: this.workflowId,
      },
    });
    this.logHalfModalRef.afterClose.subscribe(() => {
      if (exict) {
        this.flowHelperServ.adjustRightForNodeModal('', 'left');
      }
      this.showLogModal = false;
    });
    this.adjustTopForNodeModal();
  }

  private adjustTopForNodeModal() {
    if (!this.nodeModalTop) {
      const container = document.getElementById(this.previewGraphId);
      this.nodeModalTop = container?.getBoundingClientRect()?.top ?? 113;
    }

    this.flowHelperServ.adjustTopForNodeModal(this.nodeModalTop, this.renderer);
  }

  public closeAllWindow(excludeList?: string[]) {
    const shouldExclude = (item: string) =>
      excludeList?.includes(item) ?? false;

    if (this.logHalfModalRef && !shouldExclude('flow-log-half-modal')) {
      this.logHalfModalRef.close();
    }

    if (!!shouldExclude('checkErrorWorkflowHalfModalRef')) {
      this.checkErrorWorkflowHalfModalRef = false;
    }

    if (!shouldExclude('runTestHalfModel')) {
      this.showRunModal = false;
    }
  }

  private initFlowPreviewGraph(): void {
    this.previewGraphContainer = this.elementRef.nativeElement.querySelector(
      `#${this.previewGraphId}`,
    ) as HTMLDivElement;

    this.previewGraph = this.appFlowServ.getNewGraph(
      this.previewGraphContainer,
      false,
    );
    this.previewGraph.options.interacting = false; // 设置为只读
    this.nodeServ.setIsReadonlyFlow(true);
    this.addGraphEventListeners();
    if (isDevMode()) {
      window.__x6_instances__ = [];
      window.__x6_instances__.push(this.previewGraph);
    }
  }

  private addGraphEventListeners(): void {
    this.previewGraph.on('translate', () => {
      this.debounceSetMiniMap();
    });

    this.previewGraph.on('resize', () => {
      this.debounceSetMiniMap();
    });

    this.previewGraph.on('scale', (e: any) => {
      if (e?.sx) {
        this.localScale = ((e.sx as number) * this.defaultScale).toFixed(0);
      }
      this.debounceSetMiniMap();
    });

    this.previewGraph.on('node:click', (data: any) => {
      if (!this.workflowId) {
        return;
      }
      this.rightMsgType = 'info';
      this.rightMsgTitle = '';
      this.showGoEditBtn = true;
      this.rightMsgContent = this.i18n.transform('preview_graph_tip');
      this.clearRightMsg();
    });

    this.previewGraph.on('render:done', () => {
      this.resetLoopSizeAndPosition();
    });

    window.removeEventListener('resize', this.debounceWindowResize);
    window.addEventListener('resize', this.debounceWindowResize);
  }

  private resetLoopSizeAndPosition(): void {
    //重新设置loop节点大小
    this.previewGraph.getNodes().forEach((node) => {
      const nodeType = (node.data.ngArguments.nodeInfo as NodeInfo).type;
      // 排除循环输入和输出
      if (nodeType === 'Loop') {
        FlowEventUtils.resetParentSize(node);
      }

      if (!this.workflowId) {
        const position = node.getPosition();
        this.lastNodePosition = { x: position.x, y: position.y };
      }
    });
  }

  private clearRightMsg(): void {
    // 清除之前的定时器，避免多个定时器同时存在
    if (this.clearMsgTimer) {
      clearTimeout(this.clearMsgTimer);
    }

    this.clearMsgTimer = setTimeout(() => {
      this.rightMsgContent = '';
      this.clearMsgTimer = null; // 清空后重置定时器引用
    }, 10000);
  }
  private async initGraphData(): Promise<void> {
    if (!this.previewGraph) {
      this.initFlowPreviewGraph();
    }
    const flowData = this.workflowId
      ? await this.appFlowRepoServ.getFlow(this.workflowId)
      : (this.getProcessNodeData(this.processLogs) as IWorkflow);
    if (this.workflowId) {
      this.workflowDetail = flowData;
      this.nodeHeightMap.clear();
    }
    fillAdvancedBranches(
      flowData.workflow_details.nodes,
      flowData.workflow_details.edges,
    );
    if (this.workflowId) {
      this.appFlowServ.setVersionInfo(flowData.update_time);
    }
    try {
      this.flowHelperServ.useDataPaintWorkflow(
        flowData,
        this.previewGraph,
        !this.workflowId,
        FLOW_COLORS.edgeActive,
      );
    } catch {
      await this.resetCacheData();
      this.miniMap?.dispose();
      this.miniMap = null;
      this.previewGraph.off();
      this.previewGraph = null;
      this.throttleInitGraphData();
      return;
    }
    this.setEnvList(flowData);
    this.setLogModalTop();
    this.debounceSetMiniMap();
    this.initLoop(flowData.workflow_details.nodes);
    // 手动触发循环体节点的 node:change:position 事件，自适应布局
    setTimeout(() => {
      this.previewGraph?.getNodes()?.forEach((node) => {
        if (!node.getParent()) return;
        const options = { skipParentHandler: false };
        this.previewGraph.trigger('node:change:position', { node, options });
      });
    });
    setTimeout(() => {
      this.optimizeLayout();
      this.debounceWindowResize();
    }, 300);
  }

  private async setEnvList(flowData): Promise<void> {
    this.envList = [];
    if (
      flowData.workflow_details.configs?.environment &&
      typeof flowData.workflow_details.configs?.environment === 'string'
    ) {
      this.envVariablesManagement
        .getEnvVariablesDetail(flowData.workflow_details.configs.environment)
        .then((res) => {
          this.envList = res?.variables;
        });
    }
  }

  private setLogModalTop(): void {
    const logModalTop = `${
      this.previewGraphContainer?.getBoundingClientRect()?.top ?? 113
    }px`;
    document.documentElement.style.setProperty('--modal-top', logModalTop);
  }

  private throttleInitGraphData = throttle(this.initGraphData.bind(this), 500);
  private debounceSetMiniMap = debounce(this.setMiniMap.bind(this), 200);
  private debounceWindowResize = debounce(this.resizeGraph.bind(this), 200);

  private resizeGraph(): void {
    if (this.activeTab !== 'preview') {
      return;
    }
    const element = document.getElementById('previewComponent');
    this.previewGraph?.resize(element.offsetWidth, element.offsetHeight);
    this.previewGraph?.centerContent();
    this.flowZoomToFit();
  }

  private setMiniMap() {
    const minimapContainer = this.elementRef.nativeElement.querySelector(
      `#${this.minimapGraphId}`,
    );
    if (minimapContainer && this.previewGraph) {
      this.miniMap = this.flowHelperServ.setMiniMap(
        minimapContainer,
        this.previewGraph,
        this.miniMap,
      );
      if(this.miniMap) {
        this.miniMap.name = 'minimap';
      }
    }
  }

  /** 根据浏览器视口大小，自适应缩放工作流 */
  private flowZoomToFit() {
    setTimeout(() => {
      this.flowHelperServ.flowZoomToFit(this.previewGraph);
    });
  }

  private initLoop(nodes) {
    // 工作流没有创建完成时，增量增加节点，所以传存储的LoopId
    const looNodesIds = this.workflowId
      ? nodes.filter((node) => node.type === 'Loop').map((node) => node.id)
      : this.loopIds;
    this.flowHelperServ.initLoop(looNodesIds, this.previewGraph);
  }

  private handleMultiPortNodeExpand(
    node: Node<NodeProperties>,
    nodeUpdate: INodeHeightUpdate,
  ) {
    this.flowHelperServ.handleMultiPortNodeExpand(node, nodeUpdate);
  }

  private getProcessNodeData(processList) {
    const edges = this.edges;
    const nodes = [];
    const layouts = {};
    processList.forEach((item) => {
      // 保留已布局节点的位置（新节点使用默认位置）
      const tempNode = item.details?.node || {};
      if (this.addedNodes.has(tempNode.id)) {
        return;
      }
      // 1. 添加节点（若未添加过）
      this.addedNodes.add(tempNode.id);
      const curNode = {
        ...tempNode,
        ...(tempNode?.parameters || {}),
        name: tempNode.id,
        isTemp: true,
      };
      if (curNode.configs) {
        curNode.configs.template_content = curNode.configs.user_prompt;
        curNode.configs.question_content = curNode.configs.prompt;
        if (curNode.type === 'Plugin') {
          curNode.configs.valid = true;
          curNode.configs.name = curNode.configs.tool_name;
        }
      }

      if (
        ['IntentDetection', 'Branch'].includes(curNode.type) ||
        curNode.conditions?.length
      ) {
        curNode.branches = curNode.conditions || [];
        curNode.branches.forEach((branch) => {
          if (curNode.type === 'IntentDetection') {
            branch.configs = {
              category: branch.expression,
            };
          }
          branch.id = branch.branch;
        });
      }

      layouts[curNode.id] = this.lastNodePosition;
      if (curNode.id) {
        nodes.push(curNode);
        if (curNode.type === 'Loop') {
          this.loopIds.push(curNode.id);
        }
      }

      // 2. 检查 pendingEdges：目标节点已存在时生成边
      if (this.pendingEdges[curNode.id]) {
        this.pendingEdges[curNode.id].forEach((edge) => {
          if (this.canAddEdges(edge.source, curNode.id)) {
            edges.push({
              source: edge.source,
              target: curNode.id,
              ...edge,
            });
          }
        });
        delete this.pendingEdges[curNode.id];
      }

      // 3. 处理当前节点的连接（next/conditions）
      if (curNode.conditions?.length) {
        for (const cond of curNode.conditions) {
          if (
            this.canAddEdges(curNode.id, cond.next) &&
            this.addedNodes.has(cond.next)
          ) {
            // 目标节点已存在，生成分支边
            edges.push({ source: curNode.id, target: cond.next });
          } else if (cond.next) {
            // 目标节点未存在，缓存
            if (!this.pendingEdges[cond.next]) {
              this.pendingEdges[cond.next] = [];
            }
            this.pendingEdges[cond.next].push({
              source: curNode.id,
              ...cond,
            });
          }
        }
      } else if (this.canAddEdges(curNode.id, curNode.next)) {
        if (this.addedNodes.has(curNode.next)) {
          // 目标节点已存在，立即生成边
          edges.push({ source: curNode.id, target: curNode.next });
        } else {
          // 目标节点未存在，缓存到 pendingEdges
          if (!this.pendingEdges[curNode.next]) {
            this.pendingEdges[curNode.next] = [];
          }
          this.pendingEdges[curNode.next].push({
            source: curNode.id,
          });
        }
      }
    });

    return {
      workflow_details: {
        edges,
        nodes,
        comments: [],
        layouts,
        configs: {
          nl2workflow_first: true,
        },
      },
    };
  }

  private canAddEdges(source, target): boolean {
    return source && target && target !== 'null';
  }

  private async checkErrorWorkFlow() {
    if (this.checkErrorCount > 0) {
      this.showWorkflowModal();
      return;
    }
    this.isLoadingMask = true;
    try {
      const validateRes = await this.appFlowRepoServ.validateFlow(
        this.workflowId,
      );
      const { success, errors } = validateRes;
      if (success) {
        this.checkErrorCount = 0;
        this.appFlowServ.testRunVerificationError = false;
        this.validateWorkflowList = [];
        this.checkErrorWorkflowHalfModalRef = false;
        this.showRunModal = true;
      } else {
        this.validateWorkflowList = errors;
        this.appFlowServ.testRunVerificationError = true;
        this.checkErrorCount = new Set(errors.map((error) => error.id)).size;
        if (this.checkErrorCount > 0) {
          this.showWorkflowModal();
        }
      }
      this.appFlowServ.setValidateWorkflowList(this.validateWorkflowList);
    } catch {
      const flowDetailLink = this.getFlowDetailLink();
      if (flowDetailLink) {
        this.nzMessageService.error(
          this.i18n.transform('run_errors', {
            url: flowDetailLink,
          }),
        );
      }
    } finally {
      this.isLoadingMask = false;
    }
  }

  private showWorkflowModal() {
    const flowDetailLink = this.getFlowDetailLink();
    if (flowDetailLink) {
      this.checkErrorTip = this.sanitizer.bypassSecurityTrustHtml(
        this.i18n.transform('validate_errors', {
          count: this.checkErrorCount,
          url: flowDetailLink,
        }),
      );
    }
    this.closeAllWindow();
    this.checkErrorWorkflowHalfModalRef = true;
    this.adjustTopForNodeModal();
  }

  private getFlowDetailLink(): string {
    FlowHelperService.deleteDefineAmd();
    let workFlowDetailUrl: string;
    const prefix = window.location.href?.split?.('/home')?.[0];
    if (prefix) {
      workFlowDetailUrl = `${prefix}/home/agent-center/app-flow/flow?id=${this.workflowDetail?.workflow_details?.id}`;
    }
    return workFlowDetailUrl;
  }

  private async resetCacheData() {
    await new Promise(resolve => setTimeout(resolve, (this.previewGraph?.getCells()?.length || 0) * 100));
    this.addedNodes.clear();
    this.pendingEdges = {};
    this.lastNodePosition = {};
    this.edges = [];
    this.nodeHeightMap.clear();
    this.previewGraph?.clearCells();
  }
}
