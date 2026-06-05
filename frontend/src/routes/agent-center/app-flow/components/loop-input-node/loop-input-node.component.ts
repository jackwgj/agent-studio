import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnInit,
} from '@angular/core';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import type { NodeInfo } from '../../node.type';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeDependencies } from '../modules';
import { NodeBaseComponent } from '../base/node-base.component';
import { AppFlowService } from '../../app-flow.service';
import { NodeService } from '../../node.service';

@Component({
  selector: 'meta-loop-input-node',
  templateUrl: './loop-input-node.component.html',
  styleUrls: ['../common-styles.less', 'loop-input-node.component.less'],
  standalone: true,
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class LoopInputNodeComponent
  extends NodeBaseComponent
  implements OnInit
{
  @Input('nodeInfo') nodeInfo: NodeInfo;

  public icon = WORKFLOW_SVGS.Start;

  constructor(
    protected override elementRef: ElementRef<HTMLDivElement>,
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    protected override cdr: ChangeDetectorRef,
  ) {
    super(nodeServ, appFlowServ, cdr, elementRef);
  }

  override ngOnInit(): void {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();
  }
}
