/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable no-restricted-syntax */
/* eslint-disable @typescript-eslint/no-non-null-assertion */
import { Pipe, PipeTransform } from '@angular/core';
import { CommonUtils } from 'src/utils/common.util';

export type ByteUnit = 'B' | 'kB' | 'KB' | 'MB' | 'GB' | 'TB';

@Pipe({
  name: 'bytes',
  standalone: true,
})
export class BytesPipe implements PipeTransform {
  static formats: { [key: string]: { max: number; prev?: ByteUnit } } = {
    B: { max: 1024 },
    kB: { max: 1024 ** 2, prev: 'B' },
    KB: { max: 1024 ** 2, prev: 'B' },
    MB: { max: 1024 ** 3, prev: 'kB' },
    GB: { max: 1024 ** 4, prev: 'MB' },
    TB: { max: Number.MAX_SAFE_INTEGER, prev: 'GB' },
  };

  transform(
    source: any,
    decimal = 0,
    from: ByteUnit = 'B',
    to?: ByteUnit,
  ): string {
    if (
      !(
        CommonUtils.isNumberFinite(source) &&
        CommonUtils.isNumberFinite(decimal) &&
        CommonUtils.isInteger(decimal) &&
        CommonUtils.isPositive(decimal)
      )
    ) {
      return source as string;
    }

    let bytes = source;
    let unitItem = from;
    while (unitItem !== 'B') {
      bytes *= 1024;
      unitItem = BytesPipe.formats[unitItem].prev!;
    }

    if (to) {
      const format = BytesPipe.formats[to];

      const result = CommonUtils.toDecimal(
        BytesPipe.calculateResult(format, bytes),
        decimal,
      );

      return BytesPipe.formatResult(result, to);
    }

    for (const key in BytesPipe.formats) {
      if (Object.prototype.hasOwnProperty.call(BytesPipe.formats, key)) {
        const format = BytesPipe.formats[key];
        if (bytes < format.max) {
          const result = CommonUtils.toDecimal(
            BytesPipe.calculateResult(format, bytes),
            decimal,
          );

          return BytesPipe.formatResult(result, key);
        }
      }
    }

    return '';
  }

  static formatResult(result: number, unit: string): string {
    return `${result} ${unit}`;
  }

  static calculateResult(
    format: { max: number; prev?: ByteUnit },
    bytes: number,
  ) {
    const prev = format.prev ? BytesPipe.formats[format.prev] : undefined;
    return prev ? bytes / prev.max : bytes;
  }
}
