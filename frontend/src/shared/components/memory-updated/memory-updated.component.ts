import { Component, inject, Input, OnChanges, SimpleChanges } from '@angular/core';

import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { v4 as uuidV4 } from 'uuid';

import { I18nNamespace } from '@i18n';
import { MemoryManagementModalComponent } from '@shared/components/memory-management-modal/memory-management-modal.component';
import {
  type ConversationState,
  IMemoryInfo,
  type MemoryUpdatedSSE,
  MemUpdatedOperationType,
} from '@shared/components/memory-management/memory-management.interface';
import { MemoryManagementUtils } from '@shared/components/memory-management/memory-management.utils';
import { MODULES } from '@shared/modules';

@Component({
  selector: 'memory-management-updated',
  templateUrl: 'memory-updated.component.html',
  standalone: true,
  styleUrls: ['./memory-updated.component.less'],
  imports: [MODULES, MemoryManagementModalComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class MemoryUpdatedComponent implements OnChanges {
  @Input() answers: MemoryUpdatedSSE;

  @Input() chatId: string;

  @Input() conversationState: ConversationState;

  @Input() memoryLibId: string;

  readonly i18n = inject(I18NextEagerPipe);

  memorySSE: MemoryUpdatedSSE;
  memoryInfo: IMemoryInfo[] = [];
  memUpdateOperationMap = new Map<
    MemUpdatedOperationType,
    {
      name: string;
      color: string;
    }
  >([
    [
      MemUpdatedOperationType.UPDATE,
      {
        name: this.i18n.transform('mem_operation_update'),
        color: 'green',
      },
    ],
    [
      MemUpdatedOperationType.DELETE,
      {
        name: this.i18n.transform('mem_operation_del'),
        color: 'grey',
      },
    ],
  ]);

  protected readonly memoryManagementUtils = MemoryManagementUtils;

  get isShowMemoryManageButton() {
    return this.updatedMemory.length > 0;
  }

  get updatedMemory() {
    return this.memoryInfo.filter(mem => mem.updatedType !== MemUpdatedOperationType.DELETE);
  }

  ngOnChanges(changes: SimpleChanges) {
    this.memorySSE = changes.answers?.currentValue ?? null;
    this.setMemoryInfo();
  }

  /**
   * convert to same data struct
   */
  setMemoryInfo() {
    this.memoryInfo = [];
    if (this.memorySSE?.data) {
      const memories = (this.memorySSE.data.memories || []).map(mem => ({
        content: mem.content,
        updatedType: MemUpdatedOperationType[mem.operation as keyof typeof MemUpdatedOperationType],
        id: mem.id,
        fakeId: uuidV4(),
      }));

      this.memoryInfo = [...memories];
    }
  }
}
