import { Component, Input, SimpleChanges } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'json-viewer',
  templateUrl: './json-viewer.component.html',
  styleUrls: ['./json-viewer.component.less'],
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class JsonViewerComponent {
  @Input() jsonData: { [key: string]: any } | Array<any> | string;

  @Input() keyIndex?: number | string;

  @Input() preOpen?: boolean = true;

  public dataType: string;

  public showData: { [key: string]: any } | Array<any> | string;

  public localOpen: boolean;

  public keyIndexType: string | number | undefined;

  constructor() {
    this.localOpen = this.preOpen;
  }

  isObjectItem(
    item: { [key: string]: any } | Array<any> | string
  ): item is { [key: string]: any } {
    return typeof item === 'object' && item !== null;
  }

  isArrayItem(
    item: { [key: string]: any } | Array<any> | string
  ): item is Array<any> {
    return typeof item === 'object' && item !== null;
  }

  ngOnChanges(changes: SimpleChanges): void {
    const data = changes.jsonData?.currentValue;
    const preOpen = changes.preOpen?.currentValue;
    const keyIndex = changes.keyIndex?.currentValue;

    if (data && typeof data === 'object' && !Array.isArray(data)) {
      this.dataType = 'object';
      this.showData = data;
    } else if (Array.isArray(data)) {
      this.dataType = 'array';
      this.showData = data;
    } else if (data === null) {
      this.dataType = 'null';
      this.showData = String(data);
    } else if (typeof data === 'string') {
      this.dataType = 'string';
      this.showData = keyIndex !== undefined ? `"${data}"` : data;
    } else {
      this.dataType = typeof data;
      this.showData = String(data);
    }

    if (preOpen !== undefined) {
      this.localOpen = preOpen;
    }

    if (keyIndex !== undefined) {
      this.keyIndexType = typeof keyIndex;
    }
  }

  public getObjectLength(data: { [key: string]: any }) {
    return Object.entries(data).length;
  }
}
