import { Clipboard } from '@angular/cdk/clipboard';
import { TextFieldModule } from '@angular/cdk/text-field';
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter, HostListener,
  Input,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { type Graph } from '@antv/x6';
import { MonacoEditorLoaderService } from '@materia-ui/ngx-monaco-editor';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { isNaN, isNil } from 'lodash';
import { takeUntil } from 'rxjs';
import { v4 as uuidV4 } from 'uuid';

import { I18nNamespace } from '@i18n';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { agentCommonLogic } from '@routes/agent-center/app-agent/common-logic-agent';
import {
  ModelChatAsyncItemComponent
} from "@routes/agent-center/app-flow/flow-async-test/components/chat-item/chat-item.component";
import { FlowAsyncTestService } from "@routes/agent-center/app-flow/flow-async-test/flow-async-test.service";
import { TASK_FILTER_KEYS } from '@routes/agent-center/types/common.types';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { AppPluginRepoService } from '@services/agent-center/app-plugin-repo.service';
import { FlowTaskService } from "@services/agent-center/flow-task.service";
import { WorkflowChatBaseComponent } from '@shared/base/workflow-chat-base.service';
import { AssetCopyIconComponent } from '@shared/components/assets/asset-copy-icon/asset-copy-icon.component';
import { AssetCopySuccessIconComponent } from '@shared/components/assets/asset-copy-success-icon/asset-copy-success-icon.component';
import { DynamicNodeParamsComponent } from '@shared/components/dynamic-node-params/dynamic-node-params.component';
import { JsonViewerComponent } from '@shared/components/json-viewer/json-viewer.component';
import { MODULES } from '@shared/modules';
import CommonLogic from '../../../../../app-center/common-logic-app-center';
import { AppFlowService } from '../../../app-flow.service';
import type { IWorkflow } from '../../../app-flow.types';
import { EViewType } from "../../../app-flow.types";
import flowCommonLogic from '../../../common-logic-workflow';
import type { IStartNode } from '../../../node.type';
import { CopyType } from '../../../node.type';
import type { FlowTaskDetailRes } from '../../flow-async-test.interface';
import { FlowTaskStatus } from '../../flow-async-test.interface';
import { FlowAsyncExecRole } from '../../flow-async-test.constant';

@Component({
  selector: 'flow-async-task',
  templateUrl: './flow-async-task.component.html',
  styleUrls: ['./flow-async-task.component.less'],
  standalone: true,
  imports: [
    MODULES,
    TextFieldModule,
    JsonViewerComponent,
    DynamicNodeParamsComponent,
    AssetCopyIconComponent,
    AssetCopySuccessIconComponent,
    ModelChatAsyncItemComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class FlowAsyncTaskComponent extends WorkflowChatBaseComponent {
  @Input() override currWorkflow!: IWorkflow;

  @Input() taskInfo: FlowTaskDetailRes;

  @Input() showRunModal = false;

  @Input() override graph!: Graph;

  @Input() conversationId = '';

  @Input() executionList: any = [];

  @Output('close') close = new EventEmitter<void>();

  @ViewChild('dynamicNodeParams')
  override dynamicNodeParams!: DynamicNodeParamsComponent;

  @ViewChild('chatContainerRef')
  override chatContainerRef!: ElementRef;

  @ViewChild('pluginForm') override pluginForm: NgForm;

  @ViewChild('pluginAreaRef') pluginAreaRef!: ElementRef;

  public tabs: any = [
    { title: this.i18n.transform('config_info'), active: true },
    { title: this.i18n.transform('running_results'), active: false },
    { title: this.i18n.transform('call_details'), active: false },
  ];

  public dialogHistory:any[]
  public flowInfo: any = {};
  /** 表示纵向滚动条的当前位置 */
  private lastScrollTop = 0;

  /** 工作流调试信息 */
  public workflowDebugInfo: any = {
    status: '',
    status_desc: '',
    list: [],
  };

  /** 调用链信息 */
  public callChainInfo: any = {
    title: '',
    list: [],
  };
  /** 消息框中，运行结果Tab是否存在试运行信息 */
  public isEmptyRunResult = true;

  public nodeIdClicked = '';

  public curNodeId = '';

  public crumb = [];

  // 时序图数据
  public timelineData = {};
  private version: number = 0;
  public timeout = 3600000;
  private childInvokes: any[] = [];
  constructor(
    protected override i18n: I18NextEagerPipe,
    protected override appFlowServe: AppFlowService,
    protected override commonLogic: agentCommonLogic,
    protected override appAgentServe: AppAgentRepoService,
    protected override monacoLoader: MonacoEditorLoaderService,
    protected override configServ: AgentConfigService,
    protected override cdr: ChangeDetectorRef,
    protected override agentDataServe: AgentDataService,
    protected override pluginRepoServe: AppPluginRepoService,
    private route: ActivatedRoute,
    private clipboard: Clipboard,
    private flowTaskService: FlowTaskService,
    private flowAsyncTesService: FlowAsyncTestService,
  ) {
    super(
      appFlowServe,
      appAgentServe,
      configServ,
      commonLogic,
      monacoLoader,
      i18n,
      cdr,
      agentDataServe,
      pluginRepoServe,
    );

    this.route.queryParams.subscribe((params) => {
      const { id, type, versionId } = params;
      this.workflow_id = id;
      this.type = type;
      this.versionId = versionId;
    });

    /** 订阅工作流的整体运行状态，成功或失败 */
    this.appFlowServe
      .flowStatusUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe((status: string) => {
        this.updateWorkflowStatus(status);
      });

  }

  ngOnChanges(changes: SimpleChanges) {
    // 每次点击试运行按钮，清空上一次的【运行结果】Tab页数据
    if (changes.conversationId?.previousValue !== changes.conversationId?.currentValue) {
      this.chatView = EViewType.CHAT_EMPTY;
      this.dialogHistory =[];
    }

    if (changes.conversationId?.currentValue || changes.taskInfo?.currentValue) {
      this.getExecutionsDetail(this.conversationId);
    }

    const graphValue = changes.graph ? changes.graph.currentValue : this.graph;
    const workflowDetailValue = changes.currWorkflow
      ? changes.currWorkflow.currentValue
      : this.currWorkflow;

    // 设置对话型工作流空状态时，展示的工作流icon和名称
    if (workflowDetailValue) {
      const { avatar, name, icon } = workflowDetailValue || {};
      this.flowInfo = {
        avatar: avatar || icon,
        name,
      };
    }
    this.updateInputParams(graphValue, workflowDetailValue);
    this.registerJSONValidationSchema();
  }

  override onParamsValid() {
    // 把上一次流式打印的【工作流】运行状态和时间、【调用链】和【大模型输出】信息，恢复默认值
    this.clearOutputs(true);
    let startInputsList =  this.getRunWorkflowParams() || {};
    // 试运行里，针对非必填参数，若不填，则body体不带。避免出现{ 可选变量名:null }或{ 可选变量名:'' }
    startInputsList = Object.entries(startInputsList).reduce(
      (acc, [key, value]) => {
        if (!(isNil(value) || value === '' || isNaN(value))) {
          acc[key] = value;
        }
        return acc;
      },
      {},
    );
    this.saveTask(startInputsList)
  }

  saveTask(startInputs) {
    const params ={
      inputs:{...startInputs},
      timeout:this.timeout
    }
    this.flowTaskService.runTask(this.currWorkflow.workflow_id, this.taskInfo.id, params).then((res) => {
      this.taskInfo.status = FlowTaskStatus.INIT;
      this.flowAsyncTesService.setTaskStatusChange(true);
    })

  }

  public runTask(params){
    this.flowTaskService.runTask(this.currWorkflow.workflow_id, this.taskInfo.id, params).then((res) => {
      this.flowAsyncTesService.setTaskStatusChange(true);
    })
  }

  public get runWorkflowButtonText(): string {
    return this.commonLogic.runFlowButtonText(
      this.isRetryRunning,
      this.isStreamFail,
      this.isRequesting,
    );
  }

  public onClose() {
    this.appFlowServe.setRunSettings(this.settingsList);
    this.close.emit();
  }

  /** 判断调用节点的输入和输出是否为空对象 或 空数组 或 undefined/null */
  public isIOEmpty(IO: any): boolean {
    if (isNil(IO)) { return true;}
    if (Array.isArray(IO)) { return IO.length === 0;}
    if (typeof IO === 'object') {
      return Object.keys(IO).length === 0;
    }
    return false;
  }

  public getNodeIcon(node_type: string) {
    return flowCommonLogic.getNodeIcon(node_type);
  }

  public handleCopy(data: any, target: CopyType) {
    const contentToCopy = target === 'input' ? data.inputs : data.outputs;

    if (typeof contentToCopy === 'string') {
      this.clipboard.copy(contentToCopy);
    } else {
      this.clipboard?.copy(JSON.stringify(contentToCopy) as string);
    }

    if (target === 'input') {
      data.inputsCopyIcon = true;
    } else {
      data.outputsCopyIcon = true;
    }

    setTimeout(() => {
      if (target === 'input') {
        data.inputsCopyIcon = false;
      } else {
        data.outputsCopyIcon = false;
      }
      this.cdr?.markForCheck();
    }, 3000);
  }

  public changePluginFormCollapse(pluginForm: any) {
    const collapsed = pluginForm.collapsed;
    pluginForm.collapsed = !collapsed;
    this.cdr.markForCheck();
  }

  public isEmptyObject(obj: any) {
    return obj && Object.keys(obj).length === 0;
  }


  /** 开始节点的输入参数列表通过校验后，点击【开始运行】，获取表单key-value，作为run接口的入参 */
  private getRunWorkflowParams() {
    return this.dynamicNodeParams.getDynamicParams();
  }


  /** 更新工作流状态和描述 */
  private updateWorkflowStatus(status: string) {
    this.workflowDebugInfo.status = status;
    this.workflowDebugInfo.status_desc =
      flowCommonLogic.getWorkflowStatusDesc(status);
    this.cdr.markForCheck();
  }

  /** 清空上一次流式接口的返回数据 */
  private clearOutputs(isRegenerate = false) {
    this.updateWorkflowStatus(isRegenerate ? 'loading' : '');
    this.workflowDebugInfo.list = [];
    this.callChainInfo = { title: '', list: [] };
    this.childInvokes = [];
    this.timelineData = {};
    this.index = 0;
    this.chatLoop = [
      CommonLogic.createFlowChatItem({
        query: '',
        dialogId: uuidV4(),
      }),
    ];
    this.appFlowServe.setTokenData({});
  }

  /** 更新开始节点的入参列表 */
  private updateInputParams(graphValue: any, workflowDetailValue: any) {
    const { nodes } = workflowDetailValue.workflow_details;
    this.version = workflowDetailValue.update_time;
    // 存储已填的开始节点入参，确保在自动更新时不会丢失数据，导致重复填写
    const startNode = nodes.find((node) => node.type === 'Start') as IStartNode;
    if (!startNode) {
      return;
    }

    this.startNodeParams = this.getDebugConfigParams(
      startNode.outputs,
      TASK_FILTER_KEYS,
    );
    this.startNodeParams = this.addControlValueField(this.startNodeParams);
  }

  public getChildWorkflow(node) {
    const { node_id, parent_node_id, node_name } = node;
    this.curNodeId = node_id;
    if (!this.crumb.length) {
      this.crumb = [
        { label: this.i18n.transform('parent'), id: parent_node_id},
        { label: node_name, id: node_id},
      ];
    } else {
      this.crumb.push({ label: node_name, id: node_id });
    }
  }

  @HostListener('scroll', ['$event.target'])
  onScroll(target: any) {
    const currentScrollTop = target.scrollTop;

    // 判断是否向上滚动
    if (currentScrollTop < this.lastScrollTop) {
      this.isUserScrolling = true;
    } else if (
      currentScrollTop + target?.clientHeight >=
      target.scrollHeight - 2
    ) {
      this.isUserScrolling = false; // 滚动条手动滚到底部，再次自动滚动
    }
    this.lastScrollTop = currentScrollTop - 3;
  }

  getExecutionsDetail(conversationId:string){
    this.flowTaskService.getExecutionsDetail(this.workflow_id, conversationId).then((res) => {
      if (res.length > 0) {
        this.chatView = EViewType.CHAT_NOT_EMPTY
        res.forEach((item: any, index) => {
          const isJson = this.isJsonString(item.content);
          if (!isJson) {
            return;
          }

          if (item.role === FlowAsyncExecRole.ASSISTANT) {
            item.inputList = JSON.parse(item.content).inputs;
            item.isShowInputParams = Array.isArray(item.inputList);
            item.inputList?.forEach(input => {
              input.uniqueId = input.name;
            });
            item.isConfirm = res.length - 1 === index && this.taskInfo.status === 'PENDING';
          } else {
            const contentObj = JSON.parse(item.content);
            item.content = contentObj.query;
          }
        });
        this.dialogHistory = res;
      } else if (this.taskInfo.status === 'INIT' || this.taskInfo.status === 'RUNNING') {
        this.chatView = EViewType.CHAT_NOT_EMPTY;
      } else {
        this.chatView = EViewType.CHAT_EMPTY;
      }
    })
  }

  public getNodeStatus(node_status: string) {
    return flowCommonLogic.getNodeStatus(node_status);
  }

  public changeCollapsedState(configInfo: any) {
    configInfo.collapsed = !configInfo.collapsed;
    const { node_id } = configInfo;
    this.appFlowServe.setNodeIdClicked(node_id);
  }

  private isJsonString(str: string): boolean {
    try {
      JSON.parse(str);
      return true;
    } catch {
      return false;
    }
  }
}
