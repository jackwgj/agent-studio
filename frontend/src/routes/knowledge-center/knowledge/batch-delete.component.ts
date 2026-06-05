import {
  Component,
  EventEmitter,
  Output,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { FormsModule } from '@angular/forms';
import { NzModalRef, NzModalService, NzModalModule } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzTableModule } from 'ng-zorro-antd/table';

@Component({
  selector: 'batch-delete',
  template: `
    <ng-template #deleteModalTitle>
      <div class="text-[18px] leading-[28px]">
        {{ 'delete' | i18nextEager }}
      </div>
    </ng-template>
    <ng-template #deleteModalContent>
      <nz-alert nzType="warning" [nzMessage]="alert | i18nextEager" [nzCloseable]="false" class="!w-full"></nz-alert>
      <div class="mb-2 mt-4 text-[12px] font-bold leading-[18px]">
        {{ 'delete_list' | i18nextEager }}
      </div>
      <nz-table #basicTable [nzData]="tableData" nzSize="small" [nzFrontPagination]="false" [nzShowPagination]="false" [nzScroll]="{ y: '250px' }">
        <thead>
          <tr>
            <th *ngFor="let col of columns">{{ col.headerName || col.title }}</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let row of basicTable.data">
            <td *ngFor="let col of columns">
               <span class="truncate block w-full" [title]="row[col.field]">{{ row[col.field] }}</span>
            </td>
          </tr>
        </tbody>
      </nz-table>
      <div class="mb-2 mt-4 text-[12px]">
        <span>{{ 'batch_delete_tip' | i18nextEager }}</span>
        <span class="ml-1 mr-4 font-bold">DELETE</span>
        <button
          nz-button
          nzType="link"
          class="!p-0 !h-auto"
          (click)="autoInput()"
        >
          {{ 'auto_input' | i18nextEager }}
        </button>
      </div>
      <input
        nz-input
        class="w-full"
        type="text"
        [(ngModel)]="confirmText"
        placeholder="DELETE"
      />
    </ng-template>
    <ng-template #deleteModalFooter>
      <button nz-button nzType="default" (click)="closeModal()">
        {{ 'cancel' | i18nextEager }}
      </button>
      <button
        nz-button
        nzType="primary"
        nzDanger
        [nzLoading]="confirmLoading"
        [disabled]="disableConfirm"
        (click)="handleConfirm()"
      >
        {{ 'ok' | i18nextEager }}
      </button>
    </ng-template>
  `,
  standalone: true,
  imports: [
    MODULES,
    FormsModule,
    NzModalModule,
    NzButtonModule,
    NzInputModule,
    NzAlertModule,
    NzTableModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.KNOWLEDGE],
    },
    NzModalService
  ],
})
export class BatchDeleteComponent {
  @Output() confirm = new EventEmitter();
  @ViewChild('deleteModalTitle', { static: true }) deleteModalTitle!: TemplateRef<any>;
  @ViewChild('deleteModalContent', { static: true }) deleteModalContent!: TemplateRef<any>;
  @ViewChild('deleteModalFooter', { static: true }) deleteModalFooter!: TemplateRef<any>;
  public alert = 'batch_delete_alert';
  public confirmText = '';
  public confirmLoading = false;
  public columns: any[] = [];
  public tableData: any[] = [];

  public get disableConfirm() {
    return 'DELETE' !== this.confirmText;
  }

  public modalRef: NzModalRef;

  constructor(private nzModal: NzModalService) {}

  public autoInput() {
    this.confirmText = 'DELETE';
  }

  public openModal(fields: any[] = [], data: any[] = [], alert?: string) {
    if (alert) {
      this.alert = alert;
    }
    this.confirmText = '';

    this.columns = fields;
    this.tableData = data;

    this.modalRef = this.nzModal.create({
      nzTitle: this.deleteModalTitle,
      nzContent: this.deleteModalContent,
      nzFooter: this.deleteModalFooter,
      nzWidth: 700,
    });
  }

  public closeModal() {
    this.confirmLoading = false;
    if (this.modalRef) {
      this.modalRef.destroy();
    }
  }

  public handleConfirm() {
    this.confirmLoading = true;
    this.confirm.emit(this);
  }
}
