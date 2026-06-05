import { ChangeDetectionStrategy, Component, inject, input, output } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { I18NEXT_NAMESPACE, I18NextEagerPipe, I18NextModule } from 'angular-i18next';

// 引入 NG-ZORRO 模块
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { I18nNamespace } from '@i18n';
import { MEMORY_STRATEGY_MAP } from '@routes/memory-lib/memory-lib-constants';
import { IMemoryLibItem, IMemoryStrategy } from '@routes/memory-lib/memory-lib-interfaces';
import { MemoryLibService } from '@routes/memory-lib/memory-lib.service';

@Component({
  selector: 'memory-lib-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    I18NextModule,
    NzTableModule,
    NzEmptyModule,
    NzButtonModule,
    NzToolTipModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.MEMORY_LIB],
    },
  ],
  templateUrl: './memory-lib-list.component.html',
  styleUrl: './memory-lib-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MemoryLibListComponent {
  readonly memoryLibService = inject(MemoryLibService);
  readonly i18n = inject(I18NextEagerPipe);
  readonly configServ = inject(AgentConfigService);
  readonly router = inject(Router);

  libs = input<IMemoryLibItem[]>();
  dataChange = output<void>();

  constructor() {}

  /**
   * 获取策略类型的多语言拼接文本
   * @param data 行数据
   */
  getStrategyText(data: IMemoryLibItem): string {
    return data.long_term_memory_strategies
      ?.map((strategy: IMemoryStrategy) => this.i18n.transform(MEMORY_STRATEGY_MAP.get(strategy.type)?.name ?? ''))
      .filter(Boolean)
      .join(' | ') || '';
  }

  deleteMemory(memoryLib: IMemoryLibItem) {
    this.memoryLibService.safeDeleteMemoryLib(memoryLib).then(isDeleted => {
      if (isDeleted) {
        this.dataChange.emit();
      }
    });
  }
}
