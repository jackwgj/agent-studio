import { Injectable, ViewChild } from '@angular/core';

@Injectable()
export class Index {
  I18n: any;
  maskShow: boolean = false;
  @ViewChild('alert') alert: any;
  onHistoryBack($event: Event): void {
    $event.preventDefault();
    history.back();
  }
}
