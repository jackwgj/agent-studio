/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { cloneDeep } from 'lodash';
import { AppFlowService } from '../../app-flow.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeService } from '../../node.service';
import type {
  IAggregationNode,
  IRefContentType,
  IWorkflowField,
} from '../../node.type';
import { IAggGroup } from '../aggregation-modal/aggregation-modal.component';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { NodeUtils } from '../utils';
import { ValueWarnDirective } from '@shared/directives/value-warn.directive';
import { FlowUtils } from '../../utils/flow-utils';
import { takeUntil } from 'rxjs';

@Component({
  selector: 'meta-aggregation-node',
  templateUrl: './aggregation-node.component.html',
  styleUrls: ['../common-styles.less', './aggregation-node.component.less'],
  standalone: true,
  imports: [NodeDependencies, ValueWarnDirective],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class AggregationNodeComponent
  extends NodeBaseComponent
  implements OnInit, OnChanges {
  @Input('nodeInfo') nodeInfo: IAggregationNode;

  icon = WORKFLOW_SVGS.Aggregation;

  public groups: IAggGroup[] = [];

  public getType = NodeUtils.getFieldTypeView;

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
      FlowUtils.handelNodeRefChange(data, this.nodeInfo.inputs);
      this.handelGroups();
    });
  }

  override ngOnChanges(changes: SimpleChanges): void {
    super.ngOnChanges(changes);

    if (changes.nodeInfo.currentValue) {
      this.handelGroups();
    }
  }

  handelGroups() {
    this.groups = this.nodeInfo.outputs.map((o) => {
      const fields = this.nodeInfo.configs.groups[o.name]
        .map((group) => {
          const field = cloneDeep(
            this.nodeInfo.inputs.find((input) => input.name === group.name),
          );

          return field;
        })
        .filter(Boolean);

      return {
        name: o.name,
        type: null,
        fields,
      };
    });
  }

  getFieldRefName(field: IWorkflowField) {
    if (field?.value) {
      const refName = (field.value.content as IRefContentType).ref_var_name
        .split('.')
        .pop();

      return refName;
    }

    return '';
  }
}
