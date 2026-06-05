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
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { AppFlowService } from '../../app-flow.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeService } from '../../node.service';
import type {
  IRefContentType,
  ISetVariableNode,
  IWorkflowField,
} from '../../node.type';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { takeUntil } from 'rxjs';
import { FlowUtils } from '../../utils/flow-utils';
import { ValueWarnDirective } from '@shared/directives/value-warn.directive';
import { isNil } from 'lodash';

@Component({
  selector: 'meta-set-variable-node',
  templateUrl: './set-variable-node.component.html',
  styleUrls: ['../common-styles.less', './set-variable-node.component.less'],
  standalone: true,
  imports: [NodeDependencies, ValueWarnDirective],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class SetVariableNodeComponent
  extends NodeBaseComponent
  implements OnInit
{
  @Input('nodeInfo') nodeInfo: ISetVariableNode;

  icon = WORKFLOW_SVGS.SetVariable;

  isValueMap = {
    increment: this.i18n.transform('increment'),
    decrement: this.i18n.transform('decrement'),
    empty: 'null',
    empty_str: this.i18n.transform('empty_data_label'),
    empty_arr: '[]',

  }

  constructor(
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    protected override cdr: ChangeDetectorRef,
    protected override elementRef: ElementRef<HTMLDivElement>,
    private i18n: I18NextEagerPipe,
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

  protected override getFieldName(param: IWorkflowField): string {
    const name =
      (param?.value?.content as IRefContentType)?.ref_var_name
        ?.split('.')
        ?.pop();
    return name;
  }


  getFieldTypeN(param: IWorkflowField) {
    let value = param?.value;
    if(value?.operator) {
      return this.i18n.transform('operator');
    }
    else if(value?.type === 'ref') {
      return this.i18n.transform('ref');
    } else {
      return this.i18n.transform('literal');
    }
  }

  protected override getFieldContentValue(param: IWorkflowField) {
    if (param?.value?.type === 'ref') {
      if(param?.value?.operator) {
        return this.isValueMap[param.value.operator];
      } else {
        return (
          (param?.value?.content as IRefContentType).ref_var_name ||
          i18next.t('not_selected')
        );
      }
    }

    if (isNil(param?.value?.content) || param?.value?.content === '') {
      return i18next.t('not_configured');
    }

    return param.value.content;
  }
}
