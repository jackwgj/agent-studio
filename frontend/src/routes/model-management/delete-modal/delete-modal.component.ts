import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { ModelManagementService } from '@services/repositories/model-management-new';
import { NZ_MODAL_DATA, NzModalRef } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';

@Component({
  selector: 'meta-delete-modal',
  imports: [
    CommonModule,
    FormsModule,
    MODULES,
    NzButtonModule,
    NzInputModule,
    NzIconModule
  ],
  templateUrl: './delete-modal.component.html',
  styleUrls: ['./delete-modal.component.scss'],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MODEL_ACCESS],
    },
  ],
})
export class DeleteModalComponent implements OnInit {
  title = '';
  tip = '';
  id = '';
  fnName = '';
  configWord = '';
  confirmText = 'DELETE';
  loading = false;

  constructor(
    private modelManagementService: ModelManagementService,
    private modalRef: NzModalRef,
    @Inject(NZ_MODAL_DATA) public nzData: any
  ) {}

  ngOnInit() {
    if (this.nzData && this.nzData.context) {
      this.title = this.nzData.context.title;
      this.tip = this.nzData.context.tip;
      this.id = this.nzData.context.id;
      this.fnName = this.nzData.context.fnName;
    }
  }

  delete() {
    this.loading = true;
    this.modelManagementService[this.fnName](this.id)
      .then(() => {
        this.close();
      })
      .finally(() => {
        this.loading = false;
      });
  }

  dismiss(): void {
    this.modalRef.destroy();
  }

  close(): void {
    if (this.nzData?.context && typeof this.nzData.context.close === 'function') {
      this.nzData.context.close();
    } else {
      this.modalRef.destroy();
    }
  }
}
