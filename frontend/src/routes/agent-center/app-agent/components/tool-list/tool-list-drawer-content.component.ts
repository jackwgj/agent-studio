import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { ToolListComponent } from './tool-list.component';

@Component({
  selector: 'tool-list-drawer-content',
  template: `
    <tool-list
      [toolAdded]="toolAdded"
      [noDataText]="noDataText"
      [isFlowReadonly]="isFlowReadonly"
      (collapseRegionFlag)="collapseRegionFlag.emit($event)"
      (addBtnClickedFlag)="addBtnClickedFlag.emit($event)"
      (deleteTrigger)="deleteTrigger.emit($event)"
    ></tool-list>
  `,
  imports: [MODULES, ToolListComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ToolListDrawerContentComponent {
  @Input() toolAdded: any;
  @Input() noDataText: string = '';
  @Input() isFlowReadonly: boolean = false;

  @Output() collapseRegionFlag = new EventEmitter<any>();
  @Output() addBtnClickedFlag = new EventEmitter<any>();
  @Output() deleteTrigger = new EventEmitter<any>();
}
