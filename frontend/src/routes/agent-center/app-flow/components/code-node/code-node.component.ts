/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import { ChangeDetectorRef, Component, ElementRef, Input } from '@angular/core';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import type { ICodeNode } from '../../node.type';
import { AppFlowService } from '../../app-flow.service';
import { NodeService } from '../../node.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { takeUntil } from 'rxjs';
import { FlowUtils } from '../../utils/flow-utils';

@Component({
  selector: 'meta-code-node',
  templateUrl: './code-node.component.html',
  styleUrls: ['../common-styles.less', './code-node.component.less'],
  standalone: true,
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class CodeNodeComponent extends NodeBaseComponent {
  @Input('nodeInfo') nodeInfo: ICodeNode;

  public codeImgUrl = WORKFLOW_SVGS.Code;

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
