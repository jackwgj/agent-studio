/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  SimpleChanges,
} from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { AppFlowService } from '../../app-flow.service';
import type {
  IParamExtractionNode,
  IWorkflowField,
  IWorkflowNode,
} from '../../node.type';
import { NodeService } from '../../node.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { takeUntil } from 'rxjs';
import { FlowUtils } from '../../utils/flow-utils';
import { ValueWarnDirective } from '@shared/directives/value-warn.directive';

@Component({
  selector: 'meta-param-extraction-node',
  templateUrl: './param-extraction-node.component.html',
  styleUrls: [
    '../common-styles.less',
    './param-extraction-node.component.less',
  ],
  standalone: true,
  imports: [NodeDependencies, ValueWarnDirective],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ParamExtractionNodeComponent extends NodeBaseComponent {
  @Input('nodeInfo') nodeInfo: IParamExtractionNode;

  public inputParams: IWorkflowField[] = [];
  public domainParams: IWorkflowField[] = [];

  public iconUrl = WORKFLOW_SVGS.ParamExtraction;

  isShowException = false;

  public updateFlowTip = '';

  public showUpdatedIcon: string = 'init';

  constructor(
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    protected override cdr: ChangeDetectorRef,
    protected override elementRef: ElementRef<HTMLDivElement>,
    private i18n: I18NextEagerPipe,
  ) {
    super(nodeServ, appFlowServ, cdr, elementRef);
  }

  override ngOnInit(): void {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();
    this.updateFlowTip = this.i18n.transform('param_update_flow_tip');
    this.appFlowServ.nodeRefChange$().pipe(takeUntil(this.destroy$)).subscribe((data: any) => {
      FlowUtils.handelNodeRefChange(data, this.nodeInfo?.inputs);
    });
  }

  override ngOnChanges(changes: SimpleChanges) {
    this.filterNormalInput();
    this.isShowException = (
      this.nodeInfo as any
    )?.configs?.exception_config?.exception_condition?.conditions?.length;
    super.ngOnChanges(changes);
  }

  filterNormalInput() {
    this.inputParams = this.nodeInfo.inputs.filter(
      (input) =>
        input.name !== 'intermediate_loop_var' &&
        input.name !== 'domain_objects' &&
        input.name !== 'model_output_var',
    );
    const domainObject: any = this.nodeInfo.inputs.filter(
      (input) => input.name === 'domain_objects',
    );
    this.domainParams = domainObject[0]?.schema ?? [];
  }
}
