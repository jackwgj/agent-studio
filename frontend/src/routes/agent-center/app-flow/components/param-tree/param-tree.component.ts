import { Component, Input } from '@angular/core';
import { MODULES } from '@shared/modules';
import { ParamLabelPipe } from 'src/pipes/param-label.pipe';
import type { INodeType, IParamRef, IWorkflowFieldType } from '../../node.type';
import { NodeUtils } from '../utils';

@Component({
  selector: 'meta-param-tree',
  templateUrl: './param-tree.component.html',
  styleUrls: ['./param-tree.component.less'],
  standalone: true,
  imports: [MODULES, ParamLabelPipe],
})
export class ParamTreeComponent {
  @Input() item: IParamRef;

  cantClick = NodeUtils.cantClick;

  type2Img(type: INodeType | IWorkflowFieldType) {
    return NodeUtils.type2Img(type as INodeType);
  }

  getTreeNodeParamName(name: string) {
    return name?.split('.')?.pop() ?? '';
  }
}
