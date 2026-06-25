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
      // 开关（enable_retrieve/enable_extract）是即时配置，弹窗无论以何种方式
      // 关闭（确定/取消/点 X）都同步到父组件 conversationState，保证关掉再打开
      // 之前的值仍存在（未刷新页面/未关工作流详情期间生效）。
      // 记忆内容编辑的持久化只在 template 的 close()（点确定）里执行，此处不重复。
      if (instance) {
        const data = instance.getData();
        this.confirm.emit(data);
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
