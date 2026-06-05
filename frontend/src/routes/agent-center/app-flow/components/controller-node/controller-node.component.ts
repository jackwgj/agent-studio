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
import type { IControllerNode } from '../../node.type';
import { NodeService } from '../../node.service';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { WORKFLOW_SVGS } from '../../flow.const';
import { cdnAssetUrl } from 'src/single-spa/assets-url';

@Component({
  selector: 'meta-controller-node',
  templateUrl: './controller-node.component.html',
  styleUrls: ['../common-styles.less', './controller-node.component.scss'],
  standalone: true,
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ControllerNodeComponent
  extends NodeBaseComponent
  implements OnInit
{
  @Input('nodeInfo') nodeInfo: IControllerNode;

  public icon = WORKFLOW_SVGS.Controller;

  public changeUrl = cdnAssetUrl;
  public showUpdatedIcon: boolean = false;
  public updateFlowTip: string = '';

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

  get normalFlows() {
    return this.nodeInfo.configs.workflows?.filter(
      (item) => item?.type === 'Normal',
    );
  }

  get startFlow() {
    return this.nodeInfo.configs.workflows?.filter(
      (item) => item?.type === 'Start',
    );
  }

  get intentDetectionFlow() {
    return this.nodeInfo.configs.workflows?.filter(
      (item) => item?.type === 'IntentIdentification',
    );
  }

  get endFlow() {
    return this.nodeInfo.configs.workflows?.filter(
      (item) => item?.type === 'End',
    );
  }
}
