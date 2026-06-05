import { Pipe, PipeTransform } from '@angular/core';
import {
  INodeType,
  IWorkflowFieldType,
} from '@routes/agent-center/app-flow/node.type';
import { capitalize } from 'lodash';

@Pipe({
  name: 'paramLabel',
  standalone: true,
})
export class ParamLabelPipe implements PipeTransform {
  transform(paramType: IWorkflowFieldType | INodeType): string {
    if (!paramType) {
      return 'String';
    }

    const typeArr = paramType.split('/');

    if (typeArr.length > 1) {
      if (typeArr[0] === 'array<file') {
        const [mainType, subType] = typeArr[0].split('<');
        return `${capitalize(mainType)}<${capitalize(subType)}/${capitalize(
          typeArr[1],
        )}`;
      }
      return capitalize(typeArr[1]);
    }

    const type = typeArr[0] as IWorkflowFieldType;
    if (type.startsWith('array')) {
      const itemType = type.match(/array<(.+)>/)[1];
      return `Array<${capitalize(itemType)}>`;
    }

    return capitalize(type);
  }
}
