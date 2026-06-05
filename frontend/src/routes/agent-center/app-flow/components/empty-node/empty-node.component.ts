import {ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, Input} from '@angular/core';
import {NodeBaseComponent} from "@routes/agent-center/app-flow/components/base/node-base.component";
import {I18NEXT_NAMESPACE} from "angular-i18next";
import {I18nNamespace} from "@i18n";
import type {INodeBase, IStartNode} from "@routes/agent-center/app-flow/node.type";
import {WORKFLOW_SVGS} from "@routes/agent-center/app-flow/flow.const";
import {AppFlowService} from "@routes/agent-center/app-flow/app-flow.service";
import {NodeService} from "@routes/agent-center/app-flow/node.service";
import {NodeCardDescriptionComponent} from "@routes/agent-center/app-flow/components/node-card-description/node-card-description.component";
import {MODULES} from "@shared/modules";
import {LineClampDirective} from "@shared/directives/line-clamp.directive";
import {InputParamsComponent} from "@routes/agent-center/app-flow/components/node-param-cmp/input-params.component";
import {OutputParamsComponent} from "@routes/agent-center/app-flow/components/node-param-cmp/output-params.component";
import {ValueWarnDirective} from "@shared/directives/value-warn.directive";
import { EventBusService } from '@agentcore/core/event-bus.service';

@Component({
  selector: 'empty-node',
  templateUrl: './empty-node.component.html',
  styleUrls: ['../common-styles.less', './empty-node.component.less'],
  standalone: true,
  imports: [
    MODULES,
    LineClampDirective,
    InputParamsComponent,
    OutputParamsComponent,
    NodeCardDescriptionComponent,
    ValueWarnDirective,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class EmptyNodeComponent extends NodeBaseComponent {
  @Input('nodeInfo') nodeInfo: IStartNode;

  public icon = WORKFLOW_SVGS.Empty;
  public alertIcon = WORKFLOW_SVGS.ValidateAlert;

  constructor(
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    protected override cdr: ChangeDetectorRef,
    protected override elementRef: ElementRef<HTMLDivElement>,
    private eventBusService: EventBusService
  ) {
    super(nodeServ, appFlowServ, cdr, elementRef);
  }

  override ngOnInit(): void {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();
  }

  onChangeNode($event) {
    $event.stopPropagation();
    this.eventBusService.emit('empty-node-open-panel', {
      clientX: $event.clientX,
      clientY: $event.clientY,
      nodeInfo: this.nodeInfo
    });
  }
}
