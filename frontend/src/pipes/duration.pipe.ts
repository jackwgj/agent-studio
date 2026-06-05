/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable @typescript-eslint/no-unsafe-argument */
/* eslint-disable @typescript-eslint/no-unsafe-call */
/* eslint-disable @typescript-eslint/no-unsafe-member-access */
import { Pipe, PipeTransform } from '@angular/core';
import { fromPairs, isNil, toPairs } from 'lodash';
import { DateTime, Duration } from 'luxon';
import { ConsoleFrameworkService } from '@core/services';

@Pipe({
  name: 'duration',
  standalone: true,
})
export class DurationPipe implements PipeTransform {
  constructor(
    private readonly consoleFrameworkService: ConsoleFrameworkService,
  ) {}

  transform(estimateTimeSec: string | number | undefined): string {
    const ets = Number(estimateTimeSec ?? 0);
    const date1 = DateTime.now();
    const date2 = DateTime.fromMillis(date1.toMillis() + ets * 1000);
    const language =
      (this.consoleFrameworkService.dataService.getLocale() as string) ===
      'zh-cn'
        ? 'zh-CN'
        : 'en-US';
    const etsDurationObject = date2
      .diff(date1, ['months', 'days', 'hours', 'minutes'])
      .toObject();
    const etsDurationStr = Duration.fromObject(
      fromPairs(
        toPairs(etsDurationObject)
          .filter(([, v]) => v > 0)
          .map(([k, v]) => [k, Math.floor(v)]),
      ),
    )
      .reconfigure({ locale: language })
      .toHuman()
      .replace(/[、个]/g, '');
    return etsDurationStr === '' || isNil(etsDurationStr)
      ? ' -- '
      : etsDurationStr;
  }
}
