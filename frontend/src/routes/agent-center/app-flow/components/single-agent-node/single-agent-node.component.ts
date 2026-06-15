import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { AppFlowService } from '../../app-flow.service';
import type { ISingleAgentNode } from '../../node.type';
import { NodeService } from '../../node.service';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { WORKFLOW_SVGS } from '../../flow.const';

@Component({
  selector: 'meta-single-agent-node',
  templateUrl: './single-agent-node.component.html',
  styleUrls: ['../common-styles.less', './single-agent-node.component.scss'],
  standalone: true,
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class SingleAgentNodeComponent
  extends NodeBaseComponent
  implements OnInit
{
  @Input('nodeInfo') nodeInfo!: ISingleAgentNode;

  public icon = WORKFLOW_SVGS.SingleAgent;

  public description = '';

  constructor(
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    protected override cdr: ChangeDetectorRef,
    protected override elementRef: ElementRef<HTMLDivElement>,
    private i18n: I18NextEagerPipe,
  ) {
    super(nodeServ, appFlowServ, cdr, elementRef);
  }

  override ngOnChanges(changes: SimpleChanges) {
    super.ngOnChanges(changes);
    this.description = this.nodeInfo.configs?.intent?.description
      ? this.nodeInfo.configs?.intent?.description
      : this.nodeInfo.configs?.description;
  }

  override ngOnInit(): void {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();
  }

  public get versionId() {
    return this.nodeInfo?.configs?.version_id || '';
  }

  override actions = [
    {
      id: 'detail',
      label: this.i18n.transform('agent_detail'),
    },
    {
      id: 'copyId',
      label: this.i18n.transform('copy_agent_id'),
    },
  ];
}
