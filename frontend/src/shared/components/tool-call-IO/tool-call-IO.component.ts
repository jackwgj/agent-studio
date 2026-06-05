import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { isArray, isEmpty, isPlainObject, isString, toPairs } from 'lodash';

@Component({
  selector: 'tool-call-IO',
  templateUrl: './tool-call-IO.component.html',
  styleUrls: ['./tool-call-IO.component.scss'],
  standalone: true,
  imports: [CommonModule, MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ToolCallIOComponent {
  @Input() toolCallIO: any;

  public convertItemToString(item: any): string {
    return JSON.stringify(item);
  }

  public isEmptyObject(value: any) {
    return isPlainObject(value) && isEmpty(value);
  }

  public isString(value: any) {
    return isString(value);
  }

  public isObject(value: any): boolean {
    return isPlainObject(value);
  }

  public isArray(value: any): boolean {
    return isArray(value);
  }

  public objectKeys(obj: any): string[] {
    return Object.keys(obj);
  }

  public isEmptyArray(value: any): boolean {
    return isArray(value) && isEmpty(value);
  }

  public convertToIterable(value: any): any[] {
    if (this.isArray(value)) {
      return value;
    }
    if (this.isObject(value)) {
      return toPairs(value);
    }
    return [];
  }
}
