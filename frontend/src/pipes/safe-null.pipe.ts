import { Pipe, PipeTransform } from '@angular/core';
import { isNil } from 'lodash';

@Pipe({
  name: 'safeNull',
  standalone: true,
})
export class SafeNullPipe implements PipeTransform {
  transform<T>(value: T, replace = '--'): T | string {
    if (isNil(value)) {
      return replace;
    }
    return value;
  }
}
