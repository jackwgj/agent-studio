/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import { ChangeDetectorRef, Component, ElementRef, Input } from '@angular/core';
import i18next from 'i18next';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { AppFlowService } from '../../app-flow.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeService } from '../../node.service';
import type { IIntentBranch, IIntentNode } from '../../node.type';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { takeUntil } from 'rxjs';
import { FlowUtils } from '../../utils/flow-utils';

@Component({
  selector: 'meta-intent-node',
  templateUrl: './intent-node.component.html',
  styleUrls: ['../common-styles.less', './intent-node.component.less'],
  standalone: true,
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class IntentNodeComponent extends NodeBaseComponent {
  @Input('nodeInfo') nodeInfo: IIntentNode;

  public intentImgUrl = WORKFLOW_SVGS.IntentDetection;

  public hasOtherIntent = false;

  get intentList() {
    if (
      this.nodeInfo.branches.length > 0 &&
      this.nodeInfo.branches[0].id === 'branch_0'
    ) {
      this.hasOtherIntent = true;
      return this.nodeInfo.branches.slice(1);
    }

    return this.nodeInfo.branches;
  }

  get extraDivCount(): number {
    return Math.max(0, this.nodeInfo.branches.length - 3);
  }

  get extraDivs(): number[] {
    return Array(this.extraDivCount).fill(0) as number[];
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
    this.intentImgUrl = WORKFLOW_SVGS[this.nodeInfo.type]
    this.appFlowServ.nodeRefChange$().pipe(takeUntil(this.destroy$)).subscribe((data: any) => {
        FlowUtils.handelNodeRefChange(data, this.nodeInfo?.inputs);
    });
  }

  getCateColor(intent: IIntentBranch) {
    return intent?.configs?.category ? '' : '#ff8800';
  }

  getCateContent(intent: IIntentBranch) {
    return intent?.configs?.category;
  }
}
