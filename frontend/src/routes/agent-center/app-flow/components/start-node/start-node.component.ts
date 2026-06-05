/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import { ChangeDetectorRef, Component, ElementRef, Input } from '@angular/core';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { NodeService } from '../../node.service';
import type { IStartNode } from '../../node.type';
import { NodeBaseComponent } from '../base/node-base.component';
import { AppFlowService } from '../../app-flow.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeDependencies } from '../modules';
import { FlowUtils } from '../../utils/flow-utils';
import { takeUntil } from 'rxjs';

@Component({
  selector: 'meta-start-node',
  templateUrl: './start-node.component.html',
  styleUrls: ['../common-styles.less', './start-node.component.less'],
  standalone: true,
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class StartNodeComponent extends NodeBaseComponent {
  @Input('nodeInfo') nodeInfo: IStartNode;

  icon = WORKFLOW_SVGS.Start;

  get removeSysFromNode() {
    return this.nodeInfo.outputs.filter(
      (param) => param.name !== 'sys' && param.name !== 'env',
    );
  }

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
