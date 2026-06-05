/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  SimpleChanges,
} from '@angular/core';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { AppFlowService } from '../../app-flow.service';
import type { ILLMNode, IWorkflowField } from '../../node.type';
import { NodeService } from '../../node.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { takeUntil } from 'rxjs';
import { FlowUtils } from '../../utils/flow-utils';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

interface LlmAction {
  id: string;
  label: string;
  icon: any;
}

@Component({
  selector: 'meta-llm-node',
  templateUrl: './llm-node.component.html',
  styleUrls: ['../common-styles.less', './llm-node.component.less'],
  standalone: true,
  imports: [NodeDependencies, NzButtonModule, NzDropDownModule, NzIconModule, NzToolTipModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class LlmNodeComponent extends NodeBaseComponent {
  @Input('nodeInfo') nodeInfo: ILLMNode;

  public inputParams: IWorkflowField[] = [];

  public llmIconUrl = WORKFLOW_SVGS.LLM;

  constructor(
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    protected override cdr: ChangeDetectorRef,
    protected override elementRef: ElementRef<HTMLDivElement>,
  ) {
    super(nodeServ, appFlowServ, cdr, elementRef);
  }

  override ngOnInit(): void {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();
    this.appFlowServ.nodeRefChange$().pipe(takeUntil(this.destroy$)).subscribe((data: any) => {
        FlowUtils.handelNodeRefChange(data, this.nodeInfo?.inputs);
    });
  }

  override ngOnChanges(changes: SimpleChanges) {
    this.filterNormalInput();
    super.ngOnChanges(changes);
  }

  filterNormalInput() {
    const visionList: string[] = this.nodeInfo.configs?.vision ?? [];
    this.inputParams = this.nodeInfo.inputs.filter((i) => {
      return !visionList.includes(i.name);
    });
  }
}
