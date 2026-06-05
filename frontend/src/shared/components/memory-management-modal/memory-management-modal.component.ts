import { Component, EventEmitter, Input, Output } from '@angular/core';

import { NzModalService } from 'ng-zorro-antd/modal';

import { MemoryManagementService } from '@services/memory-management.service';
import { MemoryManagementTemplateComponent } from '@shared/components/memory-management-modal/memory-management-template/memory-management-template.component';
import {
  type ConversationState,
  IMemoryInfo,
  IMemoryManagementData,
  ModuleResolveFn,
} from '@shared/components/memory-management/memory-management.interface';
import { MODULES } from '@shared/modules';

@Component({
  selector: 'memory-management-modal',
  template: '',
  standalone: true,
  styles: [''],
  imports: [MODULES],
  providers: [],
})
export class MemoryManagementModalComponent {
  @Input({ required: false }) updatedMemoryInfo: IMemoryInfo[];

  @Input() conversationState: ConversationState;

  @Input() memoryLibId: string;

  @Output() confirm = new EventEmitter<IMemoryManagementData>();

  constructor(
    private nzModalService: NzModalService,
    private memoryManagementService: MemoryManagementService
  ) {}

  showModal() {
    const modalRef = this.nzModalService.create({
      nzTitle: undefined,
      nzFooter: null,
      nzClosable: true,
      nzWidth: '700px',
      nzCentered: false,
      nzMaskClosable: false,
      nzContent: MemoryManagementTemplateComponent,
    });

    const instance = modalRef.componentInstance as MemoryManagementTemplateComponent;
    instance.updatedMemoryInfo = this.updatedMemoryInfo;
    instance.memoryLibId = this.memoryLibId;
    instance.conversationState = this.conversationState;

    modalRef.afterClose.subscribe(reason => {
      if (reason) {
        const data: IMemoryManagementData = instance.getData();
        instance.loading = true;
        this.modifyPersonaData(data).then(res => {
          if (res) {
            this.confirm.emit(data);
            modalRef.close(reason);
          } else {
            instance.loading = false;
          }
        });
      } else {
        modalRef.close(reason);
      }
    });
  }

  modifyPersonaData(data: IMemoryManagementData): ModuleResolveFn {
    const { memories } = data;
    return new Promise(resolve => {
      if (data && memories.length) {
        this.memoryManagementService
          .changeMemoryContent(
            {
              memories,
            },
            { memory_repo_id: this.memoryLibId }
          )
          .then(() => {
            resolve(true);
          })
          .catch(() => {
            resolve(false);
          });
      } else {
        resolve(true);
      }
    });
  }
}
