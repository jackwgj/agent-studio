import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnInit,
} from '@angular/core';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
// eslint-disable-next-line import/no-cycle
import { AppFlowService } from '../../app-flow.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeService } from '../../node.service';
import type { ILoopNode } from '../../node.type';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { takeUntil } from "rxjs";

@Component({
  selector: 'meta-loop-node',
  templateUrl: './loop-node.component.html',
  styleUrls: ['../common-styles.less', './loop-node.component.less'],
  standalone: true,
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class LoopNodeComponent extends NodeBaseComponent implements OnInit {
  @Input('nodeInfo') nodeInfo: ILoopNode;

  icon = WORKFLOW_SVGS.Loop;

  constructor(
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    protected override cdr: ChangeDetectorRef,
    protected override elementRef: ElementRef<HTMLDivElement>,
  ) {
    super(nodeServ, appFlowServ, cdr, elementRef);
  }

  nodeName: string = ''

  override ngOnInit() {
    this.nodeName = this.nodeInfo.name
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();
    this.appFlowServ.nodeNameChange().pipe(takeUntil(this.destroy$)).subscribe((info: any) => {
      if (info && info.name && info.id === this.nodeInfo.id) {
        this.nodeName = info.name
      }
    })
  }
}
