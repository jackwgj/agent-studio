import { Component, EventEmitter, Input, Output } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { MemoryManagementModalComponent } from '@shared/components/memory-management-modal/memory-management-modal.component';
import { type ConversationState, IMemoryManagementData } from '@shared/components/memory-management/memory-management.interface';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'memory-management',
  templateUrl: 'memory-management.component.html',
  standalone: true,
  styleUrls: ['./memory-management.component.less'],
  imports: [
    MODULES,
    NzToolTipModule,
    MemoryManagementModalComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.COMMON, I18nNamespace.MEMORY_LIB],
    },
  ],
})
export class MemoryManagementComponent {
  @Input() conversationState: ConversationState;

  @Input() memoryLibId: string;

  @Input() titleMode: 'dev' | 'runtime' = 'dev';

  @Output() confirm = new EventEmitter<IMemoryManagementData>();

  public changeUrl = cdnAssetUrl;

  constructor() {
  }
}
