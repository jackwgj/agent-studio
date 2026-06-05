/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnInit,
} from '@angular/core';
import i18next from 'i18next';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import type {
  IBranchCondition,
  IBranchNode,
  IRefContentType,
} from '../../node.type';
import { AppFlowService } from '../../app-flow.service';
import { NodeService } from '../../node.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { FlowUtils } from '../../utils/flow-utils';
import { takeUntil } from 'rxjs';
import { ValueWarnDirective } from '@shared/directives/value-warn.directive';
import { cloneDeep } from 'lodash';

@Component({
  selector: 'meta-branch-node',
  templateUrl: './branch-node.component.html',
  styleUrls: ['../common-styles.less', './branch-node.component.less'],
  standalone: true,
  imports: [NodeDependencies, ValueWarnDirective],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class BranchNodeComponent extends NodeBaseComponent implements OnInit {
  @Input('nodeInfo') nodeInfo: IBranchNode;

  public branchImgUrl = WORKFLOW_SVGS.Branch;

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
    this.appFlowServ.nodeRefChange$().pipe(takeUntil(this.destroy$)).subscribe((data: any) => {
      const { outputsMap, memory } = data;
      if (!outputsMap) {
        return;
      } else {
        this.nodeInfo?.branches?.forEach((branch: any) => {
          branch?.configs?.conditions?.forEach(v => {
            FlowUtils.handelNodeRefChange(data, [v.left]);
            FlowUtils.handelNodeRefChange(data, [v.right]);
          })
        });
      }
    });
  }

  getBlockTitle(id: string) {
    if (id === 'default') {
      return 'ELSE';
    }

    return id.toUpperCase();
  }

  getLeftVal(con: IBranchCondition) {
    return (
      (con.left.value.content as IRefContentType).ref_var_name ||
      ''
    );
  }

  getRightValColor(con: IBranchCondition) {
    return this.getRightVal(con) === i18next.t('not_configured')
      ? '#595959'
      : '';
  }

  getRightVal(con: IBranchCondition) {
    if (!con?.right) {
      return '';
    }

    if (con.right?.value?.type === 'ref') {
      return (
        (con.right.value.content as IRefContentType).ref_var_name || ''
      );
    }

    return con.right?.value?.content === ''
      ? ''
      : con.right?.value?.content;
  }

  getOp(con: IBranchCondition) {
    if (
      !con.left?.type ||
      ['boolean', 'string', 'array', 'object'].includes(con.left.type)
    ) {
      return `acf_${con.operator}`;
    }

    if (['integer', 'number'].includes(con.left.type)) {
      if (con.operator.includes('longer')) {
        return `acf_${con.operator.replace('longer', 'larger')}`;
      }

      if (con.operator.includes('shorter')) {
        return `acf_${con.operator.replace('shorter', 'smaller')}`;
      }

      return `acf_${con.operator}`;
    }

    return `acf_${con.operator}`;
  }
}
