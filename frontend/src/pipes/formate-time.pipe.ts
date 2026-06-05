import { Pipe, PipeTransform } from '@angular/core';
import moment from 'moment';

@Pipe({
  name: 'formateTime',
  standalone: false
})
export class FormateTimePipe implements PipeTransform {
  transform(value: any, format?: string): string {
    if (value) {
      return moment(value).format(format || 'YYYY/MM/DD HH:mm:ss');
    }
    return '--';
  }
}
