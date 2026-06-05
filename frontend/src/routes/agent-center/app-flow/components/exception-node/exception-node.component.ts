/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { AppFlowService } from '../../app-flow.service';
import type { IMessageNode } from '../../node.type';
import { NodeService } from '../../node.service';
import { NodeBaseComponent } from '../base/node-base.component';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeDependencies } from '../modules';

@Component({
  selector: 'meta-exception-node',
  templateUrl: './exception-node.component.html',
  styleUrls: ['../common-styles.less', './exception-node.component.less'],
  standalone: true,
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ExceptionNodeComponent extends NodeBaseComponent implements OnInit {
  @Input('nodeInfo') nodeInfo: IMessageNode;

  icon = WORKFLOW_SVGS.StructuredMessagesException;
  isWarn = false;
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

  override ngOnChanges(changes: SimpleChanges): void {
    try {
      const passibleObj = JSON.parse(this.nodeInfo.configs?.template as string);
      if (typeof passibleObj !== 'object' || Array.isArray(passibleObj)) {
        this.isWarn = true;
      } else {
        this.isWarn = false;
      }
    } catch {
      this.isWarn = true;
    }
  }
}
