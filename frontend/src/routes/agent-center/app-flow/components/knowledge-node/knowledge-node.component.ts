/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnInit,
} from '@angular/core';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { AppFlowService } from '../../app-flow.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeService } from '../../node.service';
import { type IKnowledgeRepo } from '../../node.type';
import { NodeBaseComponent } from '../base/node-base.component';
import { ReadonlyParamsComponent } from '../readonly-params/readonly-params.component';
import { NodeDependencies } from '../modules';
import { takeUntil } from 'rxjs';
import { FlowUtils } from '../../utils/flow-utils';
import { ValueWarnDirective } from '@shared/directives/value-warn.directive';

@Component({
  selector: 'meta-knowledge-node',
  templateUrl: './knowledge-node.component.html',
  styleUrls: ['../common-styles.less', './knowledge-node.component.less'],
  standalone: true,
  imports: [NodeDependencies, ReadonlyParamsComponent, ValueWarnDirective],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class KnowledgeNodeComponent
  extends NodeBaseComponent
  implements OnInit
{
  @Input('nodeInfo') nodeInfo: IKnowledgeRepo;

  icon = WORKFLOW_SVGS.KnowledgeRepo;

  get getReposTxt() {
    return this.nodeInfo.configs.repos.map((repo) => repo.name).join(', ');
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
     this.appFlowServ.nodeRefChange$().pipe(takeUntil(this.destroy$)).subscribe((data: any) => {
      FlowUtils.handelNodeRefChange(data, this.nodeInfo?.inputs);
    });
  }

  public forceDetectChanges() {
    this.cdr.detectChanges();
  }
}
