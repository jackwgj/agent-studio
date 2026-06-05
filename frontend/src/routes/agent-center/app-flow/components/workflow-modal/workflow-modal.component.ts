import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { AppFlowService } from '../../app-flow.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeService } from '../../node.service';
import type { IWorkflowNode } from '../../node.type';
import { AccBlockComponent } from '../acc-block/acc-block.component';
import { ModalBaseComponent } from '../base/modal-base.component';
import { ReadonlyParamsTreeComponent } from '../readonly-params/readonly-params-tree.component';

interface WorkflowOutputs {
  confirm?: (data: IWorkflowNode) => void;
  closeDrawer?: () => void;
  editCodeEvent?: (data: any) => void;
}

@Component({
  selector: 'meta-workflow-modal',
  templateUrl: './workflow-modal.component.html',
  styleUrls: ['./workflow-modal.component.less', '.././common-styles.less'],
  standalone: true,
  imports: [
    MODULES,
    FormsModule,
    AccBlockComponent,
    ReadonlyParamsTreeComponent,
    NzInputModule,
    NzButtonModule,
    NzIconModule,
    NzDrawerModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class WorkflowModalComponent
  extends ModalBaseComponent
  implements OnInit, OnDestroy
{
  @Input('names') names: string[];

  @Input('nodeInfo') nodeInfo: IWorkflowNode;

  @Input('workflow_parent_node_type') workflow_parent_node_type: string;

  @Input() outputs: WorkflowOutputs = {};

  @Output('confirm') confirm = new EventEmitter<IWorkflowNode>();

  public iconUrl = WORKFLOW_SVGS.WorkflowMulti;

  public description = '';

  public source = 'controller';

  public intent_name:string = '';
  public intent_desc:string = '';


  constructor(
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
  ) {
    super(nodeServ, appFlowServ);
  }

  override ngOnInit() {

    this.source = this.nodeInfo.parent_node_type
      ? this.nodeInfo.parent_node_type
      : this.workflow_parent_node_type;
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();

    this.description = this.nodeInfo.configs.description;
    this.intent_name = this.nodeInfo.configs?.intent?.name??''
    this.intent_desc = this.nodeInfo.configs?.intent?.description??''
  }

  onConfirm(): void {
    const params = {
      ...this.nodeInfo,
      configs: {
        ...this.nodeInfo.configs,
        description: this.description,
        intent:{
          name:this.intent_name,
          description:this.intent_desc
        }
      },
    }

    this.appFlowServ.setNodeSaveMonitor({ nodeData: params });
    this.appFlowServ.setNodeModalCloseMonitor({ id: this.nodeInfo.id });
    this.outputs?.closeDrawer?.();
  }

  dismiss(): void {
    this.appFlowServ.setNodeModalCloseMonitor({ id: this.nodeInfo.id });
    this.outputs?.closeDrawer?.();
  }

  close(): void {
    this.outputs?.closeDrawer?.();
  }
}
