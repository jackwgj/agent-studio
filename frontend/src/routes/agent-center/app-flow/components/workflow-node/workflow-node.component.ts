import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  NgZone,
  OnInit, SimpleChanges,
} from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { AppFlowService } from '../../app-flow.service';
import type { IViewNodeData, IWorkflowField, IWorkflowNode } from '../../node.type';
import { NodeService } from '../../node.service';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { WORKFLOW_SVGS } from '../../flow.const';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { Clipboard } from '@angular/cdk/clipboard';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { HttpService } from '@services/http.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'meta-workflow-node',
  templateUrl: './workflow-node.component.html',
  styleUrls: ['../common-styles.less', './workflow-node.component.scss'],
  standalone: true,
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class WorkflowNodeComponent extends NodeBaseComponent implements OnInit {
  @Input('nodeInfo') nodeInfo: IWorkflowNode;

  public icon = WORKFLOW_SVGS.WorkflowMulti;

  public changeUrl = cdnAssetUrl;

  override actions = [
    {
      id: 'detail',
      label: this.i18n.transform('workflow_details'),
      disabled: false,
    },
    {
      id: 'copyId',
      label: this.i18n.transform('copy_workflow_id'),
      disabled: false,
    },
  ];

  public updateFlowTip = '';

  public showUpdatedIcon: string = 'init';

  private childFlowInfo: any;

  description = '';
  workspaceId = '';
  public apiImgUrl = WORKFLOW_SVGS.Workflow;

  constructor(
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    protected override cdr: ChangeDetectorRef,
    protected override elementRef: ElementRef<HTMLDivElement>,
    private clipboard: Clipboard,
    private ngZone: NgZone,
    private nzMessage: NzMessageService,
    private nzModal: NzModalService,
    private appAgentRepoServe: AppAgentRepoService,
    private i18n: I18NextEagerPipe,
    private configServ: AgentConfigService,
    private readonly http: HttpService,
    private activatedRoute: ActivatedRoute,
  ) {
    super(nodeServ, appFlowServ, cdr, elementRef);
  }

  override ngOnChanges(changes: SimpleChanges) {
    this.workspaceId = this.http.getWorkspaceId()
    super.ngOnChanges(changes);
    this.description = this.nodeInfo.configs?.intent?.description
      ? this.nodeInfo.configs?.intent?.description
      : this.nodeInfo.configs?.description;
  }

  override async ngOnInit(): Promise<void> {

    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();

    const { ref_workflows } = this.isChildFlowsUpdated || {};
    this.updateFlowTip = this.i18n.transform('update_flow_tip', {
      versionName: ref_workflows?.last_version_name,
    });

    if (this.isChildFlowsUpdated?.showIcon) {
      const value = await this.appAgentRepoServe.rollbackFlowVersion(
        ref_workflows?.workflow_id,
        ref_workflows?.last_version_id,
      );
      this.childFlowInfo = value;

      if (this.configServ.getConfigs().son_workflow_level_limit === false) {
        this.showUpdatedIcon = 'show';
        return;
      }
      if (value?.ref_workflows && !value.ref_workflows.length) {
        this.showUpdatedIcon = 'show';
      }
      if (value?.ref_workflows && value.ref_workflows.length) {
        this.showUpdatedIcon = 'nested';
      }
    }
    const parentId = this.getSourceNodeIdController(this.nodeInfo.id);
    const parentTypeIsSub = this.getParentInfoIsSub(parentId);
    this.actions[0].disabled = (parentTypeIsSub && this.nodeInfo.configs?.fromShare) || this.activatedRoute.snapshot.queryParams.fromShare === 'true';
  }

  public get versionId() {
    return (
      this.nodeInfo?.configs?.version_id ||
      this.isChildFlowsUpdated?.configs?.version_id ||
      ''
    );
  }

  public onConfirmUpdateFlow() {
    const { configs, ref_workflows } = this.isChildFlowsUpdated || {};
    this.updateWorkflowDesc();
    this.ngZone.run(() => {
      this.nzModal.confirm({
        nzTitle: this.i18n.transform('update_flow_modal_title'),
        nzContent: configs?.version_name
          ? this.i18n.transform('update_flow_modal_content', {
            versionName: configs.version_name,
            latestVersionName: ref_workflows?.last_version_name,
          })
          : this.i18n.transform('workflow_node_1', { last_version_name: ref_workflows?.last_version_name }),
        nzOkText: this.i18n.transform('update_flow_modal_btn'),
        nzOnOk: () => {
          const startNode = this.childFlowInfo?.workflow_details?.nodes?.filter(
            (node) => node.type === 'Start',
          );
          const endNode = this.childFlowInfo?.workflow_details?.nodes?.filter(
            (node) => node.type === 'End',
          );

          const oldInputs = this.nodeInfo.inputs;
          const newIputs = startNode?.[0]?.outputs;
          const updatedInputs = this.fillPreviousInputs(newIputs, oldInputs);
          const { workflow_id, name, description, code } = this.childFlowInfo;
          const bodyParams: IWorkflowNode = {
            id: this.nodeBase?.id,
            inputs: updatedInputs,
            outputs: endNode?.[0]?.outputs,
            type: 'Workflow',
            name,
            configs: {
              id: workflow_id,
              name,
              description: configs.description || description,
              version_id: ref_workflows?.last_version_id,
              version_name: ref_workflows?.last_version_name,
              code,
              type: this.childFlowInfo?.type,
              intent: this.nodeInfo?.configs.intent
            },
          };
          this.appFlowServ.setChildFlowPutBody(bodyParams);
        },
      });
    });
  }

  private updateWorkflowDesc() {
    if (this.childFlowInfo?.workflow_id === this.isChildFlowsUpdated?.configs?.id) {
      if (this.isChildFlowsUpdated.configs && this.isChildFlowsUpdated.configs?.description) {
        this.isChildFlowsUpdated.configs.description = this.childFlowInfo.description;
      }
    }
  }

  private fillPreviousInputs(
    newIputs: IWorkflowField[],
    oldInputs: IWorkflowField[],
  ) {
    return newIputs
      ?.map((item) => {
        const matchingItem = oldInputs?.find(
          (inputItem) =>
            inputItem.name === item.name && inputItem.type === item.type,
        );
        return matchingItem ?? item;
      })
      .filter((input) => input.name !== 'sys');
  }

  override onClickAction(action: any) {
    switch (action.id) {
      case 'detail': {
        this.onClickWorkflow();
        break;
      }
      case 'copyId': {
        this.onCopyWorkflowId();
        break;
      }
      default: {
        break;
      }
    }
  }

  public onClickWorkflow() {
    const config = this.nodeInfo.configs;
    const prefixUrl = window.location.href?.split('/home')[0];
    // fromShare 添加的是分享出来的工作流，进只读页面
    const queryParams = `?id=${config.id}&versionId=${config.version_id}&versionName=${config.version_name}&workspace_id=${this.workspaceId}&fromShare=${config.fromShare || ''}`;
    const newurl = `${prefixUrl}/home/agent-center/app-flow/flow${queryParams}`;
    window.open(newurl, '_blank');
  }

  public onCopyWorkflowId() {
    const config = this.nodeInfo.configs;
    this.clipboard.copy(config.id);
    this.nzMessage.success(
      this.i18n.transform('copy_workflow_id_successfully'),
    );
  }

  getSourceNodeIdController(targetId) {
    const allEdges = this.appFlowServ.getGraph().getEdges();
    const edge = allEdges.find((ed) => {
      return ed.getTargetCellId() === targetId;
    });
    return edge?.getSourceCellId();
  }

  getParentInfoIsSub(id) {
    const nodes = this.appFlowServ.getGraph().getNodes();
    const parent = nodes?.find(v => v.id === id);

    const parentInfo = (parent?.getData() as IViewNodeData)?.ngArguments.nodeInfo;
    return parentInfo?.type === 'SubController';
  }
}
