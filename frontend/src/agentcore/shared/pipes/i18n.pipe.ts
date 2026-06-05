import { I18nService } from '@agentcore/core/i18n.service';
import { Pipe, PipeTransform } from '@angular/core';
import { PipeOptions } from 'angular-i18next';

@Pipe({
  name: 'i18n',
  standalone: true,
})
export class I18nPipe implements PipeTransform {
  constructor(private i18n: I18nService) {}

  /**
   * 词条转化管道
   * @returns
   * @param key
   * @param options
   */
  public transform(key: string | string[], options: PipeOptions = {}): string {
    return this.i18n.transform(key, options);
  }
}
