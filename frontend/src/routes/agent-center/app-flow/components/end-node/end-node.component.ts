/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import { ChangeDetectorRef, Component, ElementRef, Input } from '@angular/core';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import type { IEndNode } from '../../node.type';
import { NodeService } from '../../node.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeBaseComponent } from '../base/node-base.component';
import { AppFlowService } from '../../app-flow.service';
import { NodeDependencies } from '../modules';
import { takeUntil } from 'rxjs';
import { cloneDeep } from 'lodash';
import { FlowUtils } from '../../utils/flow-utils';

@Component({
  selector: 'meta-end-node',
  templateUrl: './end-node.component.html',
  styleUrls: ['../common-styles.less', 'end-node.component.less'],
  standalone: true,
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class EndNodeComponent extends NodeBaseComponent {
  @Input('nodeInfo') nodeInfo: IEndNode;

  public icon = WORKFLOW_SVGS.End;

  get filterResponseContent() {
    return (this.nodeInfo.outputs || []).filter(
      (param) => param.name !== 'response_content',
    );
  }

  constructor(
    protected override nodeServ: NodeService,
    protected override appFlowServ: AppFlowService,
    protected override elementRef: ElementRef<HTMLDivElement>,
    protected override cdr: ChangeDetectorRef,
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
