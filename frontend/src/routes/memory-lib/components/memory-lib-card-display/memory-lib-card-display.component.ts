import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, input, linkedSignal, OnInit, output, signal, untracked } from '@angular/core';
import { Router } from '@angular/router';

import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';

import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';

import { MEMORY_STRATEGY_MAP } from '@routes/memory-lib/memory-lib-constants';
import { I18nNamespace } from '@i18n';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { IMemoryLibItem, IMemoryStrategy, MemoryLibCardDisplayConfig } from '@routes/memory-lib/memory-lib-interfaces';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { MODULES } from '@shared/modules';

import { PipesModule } from '../../../../pipes/pipes.module';

// 引入 nz 所需模块
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'memory-lib-card-display',
  templateUrl: './memory-lib-card-display.component.html',
  styleUrls: ['./memory-lib-card-display.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    PipesModule,
    NzTypographyModule,
    NzTagModule,
    NzCheckboxModule,
    NzDropDownModule,
    NzIconModule,
    NzButtonModule,
    NzMenuModule,
    FormsModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MEMORY_LIB],
    },
  ],
})
export class MemoryLibCardDisplayComponent implements OnInit {
  readonly i18n = inject(I18NextEagerPipe);
  readonly configServ = inject(AgentConfigService);
  readonly router = inject(Router);
  readonly nzMessage = inject(NzMessageService); // 替代 TiMessageService
  readonly knowledgeRepoService = inject(KnowledgeRepoService);
  readonly nzModal = inject(NzModalService); // 替代 TpHalfmodalService

  readonly displayConfig = input<MemoryLibCardDisplayConfig | null>();
  readonly memoryLibs = input<IMemoryLibItem[]>([]);
  readonly selectedMemoryLibs = input<IMemoryLibItem[]>([]);

  readonly createMemoryLib = output();
  readonly selectChange = output<IMemoryLibItem[]>();

  /** 内部二次处理后的列表 */
  _memoryLibs = signal<IMemoryLibItem[]>([]);

  /** 选中的记忆库，副作用为传值 */
  selectedLibs = linkedSignal<IMemoryLibItem[]>(() => this.selectedMemoryLibs());

  /** 选中的记忆库ids */
  selectedIds = computed<string[]>(() => this.selectedLibs().map(lib => lib.memory_repo_id));

  /** 可选上限 */
  quantityLimit = signal(1);

  /** 是否到上限 */
  isEqualLimit = computed(() => this.selectedIds().length === this.quantityLimit());

  operationMap = new Map<string, (memoryLib: IMemoryLibItem) => void>([]);

  _selectChangeEffect = effect(() => {
    const currentSelectedIds = this.selectedIds();
    const currentMemoryLibs = this.memoryLibs();

    untracked(() => {
      this._memoryLibs.set(
        currentMemoryLibs.map(lib => ({
          ...lib,
          selected: currentSelectedIds.includes(lib.memory_repo_id),
        }))
      );
    });
  });

  get isShowOperationContainer() {
    const operationEnable = this.displayConfig()?.operationConfig?.enable;
    if (this.isValAvailable(operationEnable)) {
      return operationEnable;
    } else {
      return false;
    }
  }

  constructor() {}

  ngOnInit(): void {}

  getCardWidth() {
    if (this.displayConfig()?.layoutConfig?.layout === 'flex') {
      const flexQuantities = this.displayConfig()?.layoutConfig?.flexQuantities ?? 3;
      return `calc((100% - 16px * ${flexQuantities - 1}) / ${flexQuantities})`;
    }
    return null;
  }

  public isPermitSelect(): boolean {
    return !this.isEqualLimit();
  }

  isValAvailable(val: any) {
    return val !== null && val !== undefined;
  }

  isMemoryLibSelected(memoryLib: IMemoryLibItem) {
    return memoryLib.selected;
  }

  clickMenu(e: Event) {
    e.stopPropagation();
  }

  cardClick(memoryLib: IMemoryLibItem) {
    if (this.displayConfig()?.operationConfig?.enable) {
      if (this.isPermitSelect() || memoryLib.selected) {
        this.toggleSelect(memoryLib);
      }
    }
  }

  selectModelChange(memoryLib: IMemoryLibItem) {
    this.toggleSelect(memoryLib);
  }

  toggleSelect(memoryLib: IMemoryLibItem) {
    if (this.selectedIds().includes(memoryLib.memory_repo_id)) {
      this.selectedLibs.update(current => current.filter(lib => lib.memory_repo_id !== memoryLib.memory_repo_id));
    } else {
      this.selectedLibs.update(current => [...current, memoryLib]);
    }
    this.selectChange.emit(this.selectedLibs());
  }

  menuOperation(menuItem: any, memoryLib: IMemoryLibItem) {
    const { id } = menuItem;
    this.operationMap.get(id)?.(memoryLib);
  }

  resolveStrategyTags(memoryLib: IMemoryLibItem) {
    return memoryLib.long_term_memory_strategies
      .map((strategy: IMemoryStrategy) => this.i18n.transform(MEMORY_STRATEGY_MAP.get(strategy.type)?.name ?? ''))
      .filter(Boolean);
  }
}
