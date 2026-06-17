import { Component, Input, SimpleChanges, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { isNaN, isNil } from 'lodash';
import { Subject } from 'rxjs';

import { I18nNamespace } from '@i18n';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import type { IWorkflow } from '@routes/agent-center/app-flow/app-flow.types';
import { DEFAULT_FLOW_SEND_MSG_MAX_LEN } from '@routes/agent-center/app-flow/flow.const';
import { IStartNode, IWorkflowField, IWorkflowFieldType } from '@routes/agent-center/app-flow/node.type';
import { CHAT_ASYNC_FILTER_KEYS, IFlowChatItem } from '@routes/agent-center/types/common.types';
import { isSystemTypeWithNames } from '@routes/agent-center/utils';
import { FlowTaskService } from '@services/agent-center/flow-task.service';
import { AgentChatContainerComponent } from '@shared/components/agent-chat-container/agent-chat-container.component';
import { DynamicNodeParamsComponent } from '@shared/components/dynamic-node-params/dynamic-node-params.component';
import { DynamicNodeParam } from '@shared/components/dynamic-node-params/dynamic-node-params.interface';
import { SendData, SenderComponent } from '@shared/components/sender/sender.component';
import { MODULES } from '@shared/modules';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import type { FlowTaskDetailRes, FlowTaskHistory } from '../../flow-async-test.interface';
import { FlowRunTaskReq, FlowTaskStatus } from '../../flow-async-test.interface';
import { FlowAsyncTestService } from '../../flow-async-test.service';
import { ModelChatAsyncItemComponent } from '../chat-item/chat-item.component';
import { FlowTaskBoardService } from './flow-task-board.service';

@Component({
  selector: 'flow-task-board',
  templateUrl: './flow-task-board.component.html',
  styleUrl: './flow-task-board.component.scss',
  standalone: true,
  imports: [
    MODULES,
    DynamicNodeParamsComponent,
    AgentChatContainerComponent,
    ModelChatAsyncItemComponent,
    SenderComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class FlowTaskBoardComponent {
  @Input() currWorkflow!: IWorkflow;
  @Input() conversationId!: string;
  @Input() taskInfo: FlowTaskDetailRes;

  @ViewChild('dynamicNodeParams') dynamicNodeParams!: DynamicNodeParamsComponent;
  @ViewChild('pluginForm') pluginForm: NgForm;

  public changeUrl = cdnAssetUrl;

  public dialogHistory: FlowTaskHistory[];
  public chatLoop: IFlowChatItem[] = [];

  /** 开始节点的输出参数列表 */
  public startNodeParams: DynamicNodeParam[] = [];
  public timeout = 3600000;

  /** 是否询问请求响应中 */
  public isQuerying = false;
  public userQueryLimit = DEFAULT_FLOW_SEND_MSG_MAX_LEN;

  private workflowId = '';
  private destroy$ = new Subject<void>();

  public get isShowStartParam(): boolean {
    const otherParam = this.startNodeParams.filter(param => param.name !== 'query');
    return this.taskInfo?.status === 'PENDING' && this.dialogHistory?.length === 0
      && Boolean(otherParam.length);
  }

  constructor(
    private i18n: I18NextEagerPipe,
    private flowTaskService: FlowTaskService,
    private flowAsyncTesService: FlowAsyncTestService,
    private agentConfigService: AgentConfigService,
    private flowTaskBoardService: FlowTaskBoardService,
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    this.isQuerying = this.taskInfo?.status === FlowTaskStatus.RUNNING;

    if (changes.conversationId?.previousValue !== changes.conversationId?.currentValue) {
      this.dialogHistory = [];
      this.chatLoop = [];
    }
    if (changes.conversationId?.currentValue || changes.taskInfo?.currentValue) {
      this.getExecutionsDetail(this.conversationId);
    }

    if (changes.currWorkflow?.currentValue) {
      this.workflowId = this.currWorkflow.workflow_id;
      this.initStartInputParams(this.currWorkflow);
    }
  }

  ngOnInit(): void {
    this.userQueryLimit = this.agentConfigService.getConfigs().user_query_limit ?? DEFAULT_FLOW_SEND_MSG_MAX_LEN;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  public runWorkflow(): void {
    if (!this.checkStartParam()) {
      return;
    }

    let startInputs = this.dynamicNodeParams?.getDynamicParams() || {};
    startInputs = Object.entries(startInputs).reduce((acc, [key, value]) => {
      if (!(isNil(value) || value === '' || isNaN(value))) {
        acc[key] = value;
      }
      return acc;
    }, {});
    const params: FlowRunTaskReq = {
      inputs: { ...startInputs },
      timeout: this.timeout
    };

    this.runTask(params);
  }

  public runTask(params: FlowRunTaskReq): void {
    this.isQuerying = true;
    this.flowTaskService.runTask(
      this.currWorkflow.workflow_id, this.taskInfo.id, params
    ).then((res) => {
      // 任务名为默认名称时，自动改名为 query
      if (this.taskInfo.name === this.i18n.transform('flow_async_new_task')) {
        this.taskInfo.name = this.truncateName(params.inputs.query);
        this.flowTaskService.putTask(this.currWorkflow.workflow_id, this.taskInfo.id, this.taskInfo)
          .finally(() => {
            this.flowAsyncTesService.setTaskStatusChange(true);
          });
      } else {
        this.flowAsyncTesService.setTaskStatusChange(true);
      }
    });
  }

  public onMsgSend(msg: SendData): void {
    const params: FlowRunTaskReq = {
      inputs: {
        query: msg.content
      },
      timeout: this.timeout
    };
    this.runTask(params);
  }

  public onMsgStop(): void {
    this.flowTaskService.cancelTask(this.workflowId, this.taskInfo.id).then(() => {
      this.flowAsyncTesService.setTaskStatusChange(true);
    });
  }

  public onContinue(inputData: Object): void {
    this.runTask({ inputs: { query: this.getFormattedData(inputData) } });
  }

  private getExecutionsDetail(conversationId: string): void {
    this.flowTaskService.getExecutionsDetail(this.workflowId, conversationId).then((res) => {
      this.dialogHistory = res;
      this.chatLoop = this.flowTaskBoardService.parseExecHistory(res, this.taskInfo);
      this.isQuerying = this.chatLoop[this.chatLoop.length - 1]?.thinkLoading;
    });
  }

  private checkStartParam(): boolean {
    // 检查开始节点基础类型入参
    let dynamicNodeParamsPass = this.dynamicNodeParams
      ? this.dynamicNodeParams.validatePrimitiveInputs()
      : true;

    // 检查开始节点复杂类型入参
    const validationResults = this.dynamicNodeParams?.validateJsonSchemas() || [];
    validationResults.forEach(item => {
      if (item.type === 'object' || item.type.startsWith('array')) {
        if (item.isError || (item.required && item.isEmpty)) {
          dynamicNodeParamsPass = false;
        }
      }
    });

    return dynamicNodeParamsPass;
  }

  /** 更新开始节点的入参列表 */
  private initStartInputParams(workflowDetailValue: IWorkflow) {
    const { nodes } = workflowDetailValue?.workflow_details;

    const startNode = nodes.find((node) => node.type === 'Start') as IStartNode;
    if (!startNode) {
      return;
    }

    this.startNodeParams = startNode.outputs.filter((item) =>
      !isSystemTypeWithNames(item, CHAT_ASYNC_FILTER_KEYS)
    ).map(param => {
      if (param.type === 'array' && param.schema) {
        const schemaType = (param.schema as IWorkflowField).type;
        param.type = `${param.type}<${schemaType}>` as IWorkflowFieldType;
      }
      return { ...param };
    });
    this.startNodeParams = this.addControlValueField(this.startNodeParams);
  }

  /** 为开始节点的每个输入参数增加 controlValue 字段，用于记录输入参数的控件值 */
  private addControlValueField(params: DynamicNodeParam[]): any[] {
    return params.map((item) => {
      if (
        item.type === 'string' ||
        item.type === 'integer' ||
        item.type === 'number' ||
        item.type === 'boolean'
      ) {
        item.controlValue = this.dynamicNodeParams?.getControlValue(item.name);
      } else {
        item.controlValue = this.dynamicNodeParams?.getEditorDefaultValueByName(
          item.name,
        );
      }
      return {
        ...item,
      };
    });
  }

  /** 获取输入节点的表单key-value，并转换为包含:和\n的字符串，作为run接口的入参 */
  private getFormattedData(inputData: Object) {
    const entries = Object.entries(inputData ?? {}).map(([key, value]) => {
      const formattedValue = value === null ? '' : value;
      return `${key}:${formattedValue}`;
    });
    return entries.join('\n');
  }

  private truncateName(name: string): string {
    const maxLength = 32;
    if (name.length > maxLength) {
      return name.substring(0, maxLength - 3) + '...';
    }
    return name;
  }
}
