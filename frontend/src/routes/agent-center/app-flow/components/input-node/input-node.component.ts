import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnInit,
} from '@angular/core';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { AppFlowService } from '../../app-flow.service';
import type { IInputNode } from '../../node.type';
import { NodeService } from '../../node.service';
import { NodeBaseComponent } from '../base/node-base.component';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeDependencies } from '../modules';
import { NodeUtils } from '../utils';

@Component({
  selector: 'meta-input-node',
  standalone: true,
  templateUrl: './input-node.component.html',
  styleUrls: ['../common-styles.less', './input-node.component.scss'],
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class InputNodeComponent extends NodeBaseComponent implements OnInit {
  @Input('nodeInfo') nodeInfo: IInputNode;

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
  }
}
