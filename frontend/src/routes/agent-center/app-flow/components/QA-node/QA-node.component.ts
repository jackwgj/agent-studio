import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnInit,
} from '@angular/core';
import { NodeDependencies } from '../modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { NodeBaseComponent } from '../base/node-base.component';
import { WORKFLOW_SVGS } from '../../flow.const';
import { AppFlowService } from '../../app-flow.service';
import { NodeService } from '../../node.service';
import { ReadonlyParamsComponent } from '../readonly-params/readonly-params.component';
import type { IQANode } from '../../node.type';
import { takeUntil } from 'rxjs';
import { FlowUtils } from '../../utils/flow-utils';

@Component({
  selector: 'meta-QA-node',
  templateUrl: './QA-node.component.html',
  styleUrls: ['../common-styles.less', './QA-node.component.scss'],
  standalone: true,
  imports: [NodeDependencies, ReadonlyParamsComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class QANodeComponent extends NodeBaseComponent implements OnInit {
  @Input('nodeInfo') nodeInfo: IQANode;

  public icon = WORKFLOW_SVGS.QA;

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
}
