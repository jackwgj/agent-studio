import { CommonModule } from '@angular/common';
import { Component, effect, inject, input, OnInit, output, signal, untracked, viewChild } from '@angular/core';
import { Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzDrawerService } from 'ng-zorro-antd/drawer';
import { I18nNamespace } from '@i18n';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { KnowledgeBaseEditComponent } from '@routes/knowledge-center/components/knowledge-base-edit/knowledge-base-edit.component';
import {
  KB_SCOPE_MAP,
  knowledgeBaseDeleteTitleMap,
  knowledgeBaseSourceMap,
  knowledgeBaseStatusMap,
  KnowledgeType,
} from '@routes/knowledge-center/knowledge-base-list/knowledge-base-list.map';
import {
  CardOperationEmit,
  KbCardDisplayConfig,
  KBCardOperation,
  KbCardOperationType,
  KbItem,
  KbOperationKey,
  KBStatus,
  KbTagType,
  KBType,
} from '@routes/knowledge-center/knowledge-base.interface';
import { KnowledgeFormDrawerComponent } from '@routes/knowledge-center/knowledge-form-drawer/knowledge-form-drawer.component';
import { KBRepoType } from '@routes/knowledge-center/knowledge.types';
import { ReferenceModalComponent } from '@routes/knowledge-center/knowledge/reference-modal/reference-modal.component';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { KbOperationFilterService } from '@services/knowledge-center/kb-operation-filter.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { PipesModule } from '../../../../pipes/pipes.module';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'kb-card-display',
  templateUrl: './kb-card-display.component.html',
  styleUrls: ['./kb-card-display.component.less'],
  styles: ``,
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    PipesModule,
    KnowledgeBaseEditComponent,
    NzDropDownModule,
    NzIconModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.AGENT_CENTER,
      ],
    },
  ],
})
export class KbCardDisplayComponent implements OnInit {
  public readonly i18n = inject(I18NextEagerPipe);

  public readonly kbAbilitiesService = inject(KbAbilitiesService);
  readonly displayConfig = input<KbCardDisplayConfig | null>();
  kbs = input<KbItem[]>([]);
  _kbs = signal<KbItem[]>([]);
  limit = input<number>();
  isMarket = input<boolean>(false);
  currentSelected = input<KbItem[]>([]);
  readonly createKb = output();
  readonly cardHandler = output<KBCardOperation>();
  readonly operationCallEmit = output<CardOperationEmit>();
  knowledgeBaseEdit = viewChild<KnowledgeBaseEditComponent>('knowledgeBaseEdit');
  kbQuantityLimit = signal(1);
  selectedKbs = signal<KbItem[]>([]);
  operationMap = new Map([
    [
      KbOperationKey.HIT_TEST, (kb: KbItem) => {
      this.toHitTest(kb);
    },
    ],
    [
      KbOperationKey.CANCEL_SHARE, (kb: KbItem) => {
      this.cancelShare(kb);
    },
    ],
    [
      KbOperationKey.CHANGE_STATUS, (kb: KbItem) => {
      this.changeKbStatus(kb);
    },
    ],
    [
      KbOperationKey.ADVANCED_SET, (kb: KbItem) => {
      this.setAdvancedSetting(kb);
    },
    ],
    [
      KbOperationKey.KB_REFERENCE, (kb: KbItem) => {
      this.showKbReference(kb);
    },
    ],
    [
      KbOperationKey.DELETE, (kb: KbItem) => {
      this.delKb(kb);
    },
    ],
    [
      KbOperationKey.EDIT, (kb: KbItem) => {
      this.knowledgeBaseEdit()?.openEditKnowledgeBase(kb);
    },
    ],
  ]);
  protected readonly knowledgeBaseSourceMap = knowledgeBaseSourceMap;
  protected readonly KbTagType = KbTagType;
  protected readonly knowledgeBaseStatusMap = knowledgeBaseStatusMap;
  private readonly configServ = inject(AgentConfigService);
  private readonly router = inject(Router);
  private readonly nzModalService = inject(NzModalService);
  private readonly knowledgeRepoService = inject(KnowledgeRepoService);
  private readonly nzDrawerService = inject(NzDrawerService);
  private readonly kbOperationFilterService = inject(KbOperationFilterService);

  constructor(
    private message: NzMessageService,
  ) {
    effect(() => {
      const currentSelected = this.currentSelected() ?? [];
      untracked(() => {
        this.selectedKbs.set(currentSelected);
        this._kbs().forEach(kb => {
          kb.selected = this.selectedIds.includes(kb.id);
        })
      });
    });
    effect(() => {
      const kbs = this.kbs();

      untracked(() => {
        this._kbs.set(kbs.map(kb => {
          kb.operations = this.kbOperationFilterService.getKbOperations(kb, this.isMarket());
          kb.selected = this.selectedIds.includes(kb.id);
          return kb;
        }));
      });
    });
  }

  get isEqualLimit() {
    return this.selectedKbs().length === this.kbQuantityLimit();
  };

  get isShowOperationContainer() {
    const operationEnable = this.displayConfig()?.operationConfig?.enable;
    if (this.isValAvailable(operationEnable)) {
      return operationEnable;
    } else {
      return false;
    }
  };

  get sharedKbLimitSelection() {
    const freeEditionKB = this.selectedKbs().find(kb => kb.repo_type === KBRepoType.SHARE);
    if (freeEditionKB) {
      return this.selectedKbs().length > 0;
    }
    return false;
  };

  get selectedIds() {
    return this.selectedKbs().map(kb => kb.id);
  };

  ngOnInit(): void {
    if (this.isValAvailable(this.limit())) {
      this.kbQuantityLimit.set(this.limit());
    } else {
      this.kbQuantityLimit.set(this.configServ.getConfigs()?.agent_knowledge_bound_limit ?? this.kbQuantityLimit());
    }
  }

  getInvalidTip(kb: KbItem) {
    if (this.tagShow(kb, KbTagType.INVALID)) {
      return this.isKbInvalid(kb) ? this.i18n.transform('knowledge_base_error_tip', { name: kb.name }) : '';
    }
    return '';
  }

  getCardWidth() {
    if (this.displayConfig()?.layoutConfig?.layout === 'flex') {
      const flexQuantities = this.displayConfig()?.layoutConfig?.flexQuantities ?? 3;
      return `calc((100% - 16px * ${flexQuantities - 1}) / ${flexQuantities})`;
    }
    return null;
  }

  public isPermitSelect(kb: KbItem): boolean {

    // 括号内为所有不可用状态枚举
    return !(this.#isClosed(kb) ||
      this.isEqualLimit ||
      !this.#isSameSource(kb) ||
      this.kbAbilitiesService.isResourceDisabled ||
      this.#isKbInvalid(kb) ||
      this.sharedKbLimitSelection ||
      this.#isDiffRepoType(kb));
  }

  tagShow(kb: KbItem, type: KbTagType) {
    const tagEnable = this.displayConfig()?.tagsConfig?.enable;
    if (this.isValAvailable(tagEnable)) {
      if (typeof tagEnable === 'boolean') {
        return tagEnable;
      }
      if (tagEnable instanceof Array) {
        return tagEnable.includes(type);
      }
      if (typeof tagEnable === 'function') {
        return tagEnable(kb, type);
      }
      return false;
    } else {
      return false;
    }
  }

  isKbInvalid(kb: KbItem) {
    return kb.type === KBType.EXTERNAL && !kb.source?.knowledge_base_connection_id;
  }

  isValAvailable(val: any) {
    return val !== null && val !== undefined;
  }

  isKBSelected(kb: KbItem) {
    return kb.selected;
  }

  clickMenu(e: Event) {
    e.stopPropagation();
  }

  cardClick(kb: KbItem) {
    if (this.displayConfig()?.detailLink?.enable) {
      this.toKbDetail(kb);
    }
    if (this.displayConfig()?.operationConfig?.enable) {
      if (this.isPermitSelect(kb) || kb.selected) {
        this.cardOperationHandler(kb, KbCardOperationType.SELECT_CHANGE);
      }
    }
  }

  selectModelChange(kb: KbItem) {
    this.cardOperationHandler(kb, KbCardOperationType.SELECT_CHANGE);
  }

  cardOperationHandler(kb: KbItem, type: KbCardOperationType, event?: MouseEvent) {
    event?.stopPropagation?.();
    this.cardHandler.emit({ kb, type })
  }

  toKbDetail(kb: KbItem) {
    if (this.displayConfig()?.detailLink?.enable && kb.type !== 'external') {
      this.router.navigate([`/home/knowledge-center/knowledge/${ this.kbAbilitiesService.getKbId(kb) }`], {
        queryParams: {
          isMarket: this.isMarket(),
          previousUrl: this.router.url,
        },
      });
    }
  }

  toHitTest(kb: KbItem) {
    this.router.navigate([`/home/knowledge-center/knowledge/${ this.kbAbilitiesService.getKbId(kb) }/hit-testing`], {
      queryParams: {
        display_name: kb.name,
        id: kb.knowledge_base_id,
        isMarket: this.isMarket,
      },
    });
  }

  cancelShare(kb: KbItem) {
    const scopeItem = KB_SCOPE_MAP.get(kb.share_scope);
    if (scopeItem) {
      this.nzModalService.confirm({
        nzTitle: this.i18n.transform('confirm_cancel_shared_app'),
        nzContent: this.i18n.transform('confirm_cancel_knowledge_base'),
        nzOkText: this.i18n.transform('ok'),
        nzCancelText: this.i18n.transform('cancel'),
        nzOnOk: () => {
          return this.knowledgeRepoService.changeKbScope(this.kbAbilitiesService.getKbId(kb), { share_scope: scopeItem.operationParam }).then(() => {
            this.message.success(`${ this.i18n.transform(scopeItem.buttonName) }${ this.i18n.transform('success') }`);
            this.operationCallEmit.emit({
              type: KbCardOperationType.MENU,
              data: {
                status: 'success',
                key: KbOperationKey.CANCEL_SHARE,
              },
            });
          });
        },
      });
    }
  }

  changeKbStatus(kb: KbItem) {
    if (kb.status === KBStatus.OPEN) {
      this.nzModalService.confirm({
        nzTitle: this.i18n.transform('confirm_disable_knowledge_base'),
        nzContent: this.i18n.transform('disable_knowledge_base_usage_hint'),
        nzOkText: this.i18n.transform('button_close'),
        nzCancelText: this.i18n.transform('cancel'),
        nzOnOk: () => {
          return this.knowledgeRepoService.stopKb(kb.knowledge_base_id).then(() => {
            this.operationCallEmit.emit({
              type: KbCardOperationType.MENU,
              data: {
                status: 'success',
                key: KbOperationKey.CHANGE_STATUS,
                beforeStatus: KBStatus.OPEN,
                currentStatus: KBStatus.CLOSE,
              },
            });
            this.message.success(this.i18n.transform('knowledge_close_success'));
          });
        },
      });
    } else {
      this.knowledgeRepoService.startKb(kb.knowledge_base_id).then(() => {
        this.message.success(this.i18n.transform('knowledge_open_success'));
        this.operationCallEmit.emit({
          type: KbCardOperationType.MENU,
          data: {
            status: 'success',
            key: KbOperationKey.CHANGE_STATUS,
            beforeStatus: KBStatus.CLOSE,
            currentStatus: KBStatus.OPEN,
          },
        });
      });
    }
  }

  setAdvancedSetting(kb: KbItem) {
    this.knowledgeRepoService.retrieveKb(this.kbAbilitiesService.getKbId(kb)).then(detail => {
      const drawerRef = this.nzDrawerService.create({
        nzTitle: this.i18n.transform('advanced_setting'),
        nzWidth: '700px',
        nzContent: KnowledgeFormDrawerComponent,
        nzContentParams: {
          formData: detail,
        },
      });
    });
  }

  showKbReference(kb: KbItem) {
    this.nzDrawerService.create({
      nzTitle: this.i18n.transform('kb_reference'),
      nzWidth: '900px',
      nzContent: ReferenceModalComponent,
      nzContentParams: {
        kbId: this.kbAbilitiesService.getKbId(kb),
      },
    });
  }

  delKb(kb: KbItem) {
    this.nzModalService.confirm({
      nzTitle: `${ knowledgeBaseDeleteTitleMap.get(kb.type) }`,
      nzContent: this.i18n.transform('confirm_action_tip', { action: knowledgeBaseDeleteTitleMap.get(kb.type), name: kb.name }),
      nzOkText: this.i18n.transform('ok'),
      nzOkType: 'primary',
      nzOkDanger: true,
      nzCancelText: kb.type === KnowledgeType.INTERNAL ? this.i18n.transform('cancel') : null,
      nzOnOk: () => {
        return this.knowledgeRepoService
          .deleteKb(kb.knowledge_base_id)
          .then(() => {
            this.message.success(this.i18n.transform('delete_success'));
            this.operationCallEmit.emit({
              type: KbCardOperationType.MENU,
              data: {
                status: 'success',
                key: KbOperationKey.DELETE,
              },
            });
          });
      },
    });
  }

  editSuccess() {
    this.operationCallEmit.emit({
      type: KbCardOperationType.MENU,
      data: {
        status: 'success',
        key: KbOperationKey.EDIT,
      },
    });
  }

  menuOperation(menuItem: any, kb: KbItem) {
    const { id } = menuItem;
    this.operationMap.get(id)?.(kb);
  }

  #isDiffRepoType(kb: KbItem) {
    if (!this.selectedKbs().length) {
      return false;
    }
    const representKb = this.selectedKbs()[0];
    let currentRepoType = representKb?.repo_type ?? '';
    if (currentRepoType !== KBRepoType.SHARE) {
      currentRepoType = '';
    }
    let targetRepoType = kb?.repo_type ?? '';
    if (targetRepoType !== KBRepoType.SHARE) {
      targetRepoType = '';
    }

    return currentRepoType !== targetRepoType;
  }

  #isClosed(kb: KbItem) {
    return kb.status === KBStatus.CLOSE;
  }

  #isSameSource(kb: KbItem) {
    if (!this.selectedKbs().length) {
      return true;
    }
    const representKb = this.selectedKbs()[0];
    let currentSource = representKb?.source?.knowledge_base_connection_id ?? '';
    if (representKb?.type === KBType.INTERNAL) {
      currentSource = '';
    }

    let targetSource = kb?.source?.knowledge_base_connection_id ?? '';
    if (kb?.type === KBType.INTERNAL) {
      targetSource = '';
    }

    return currentSource === targetSource;
  }

  #isKbInvalid(kb: KbItem) {
    return !kb.source?.knowledge_base_connection_id && kb.type === 'external';
  }
}
