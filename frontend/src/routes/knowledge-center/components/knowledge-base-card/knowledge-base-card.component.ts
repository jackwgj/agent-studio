import {
  ChangeDetectorRef,
  Component,
  EventEmitter, inject,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';
import { Router } from '@angular/router';
import { I18nNamespace } from '@i18n';
import { ReferenceModalComponent } from '@routes/knowledge-center/knowledge/reference-modal/reference-modal.component';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { KbOperationFilterService } from '@services/knowledge-center/kb-operation-filter.service';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { MODULES } from '@shared/modules';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import {
  knowledgeBaseStatusMap,
  knowledgeBaseSourceMap, KB_SCOPE_MAP,
} from '@routes/knowledge-center/knowledge-base-list/knowledge-base-list.map';
import { PipesModule } from "../../../../pipes/pipes.module";
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzDrawerService } from 'ng-zorro-antd/drawer';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'knowledge-base-card',
  templateUrl: './knowledge-base-card.component.html',
  styleUrls: ['./knowledge-base-card.component.less'],
  standalone: true,
  imports: [
    MODULES,
    PipesModule,
    NzDropDownModule,
    NzIconModule,
    NzTagModule,
    NzToolTipModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.KNOWLEDGE,
      ],
    },
    NzModalService,
    NzDrawerService,
    NzMessageService
  ],
})
export class KnowledgeBaseCardComponent implements OnChanges {
  @Input() knowledgeListData: any;
  @Input() isMarket = false;
  @Output() handleAction = new EventEmitter();
  @Output() editKnowledgeBase = new EventEmitter();
  @Output() advancedKnowledgeBase = new EventEmitter();
  @Output() changeKbScopeSuccess = new EventEmitter();

  kbs = [];

  private readonly kbOperationFilterService = inject(KbOperationFilterService);
  constructor(
    private router: Router,
    private kbRepo: KnowledgeRepoService,
    public i18n: I18NextEagerPipe,
    public kbAbilitiesService: KbAbilitiesService,
    private cdr: ChangeDetectorRef,
    private nzModal: NzModalService,
    private nzDrawer: NzDrawerService,
    private nzMessage: NzMessageService
  ) { }

  ngOnChanges(changes: SimpleChanges) {
    this.kbs = changes.knowledgeListData?.currentValue?.map(item => {
      return {
        ...item,
        actions: this.kbOperationFilterService.getKbOperations(item, this.isMarket),
      }
    }) ?? [];
    this.cdr.markForCheck();
  }

  public clickMenu(e: Event) {
    e.stopPropagation();
  }

  public listAction($event: any, row: any) {
    if ($event.key === 'delete') {
      this.kbRepo.handleKbDelete(row, this.nzModal as any).then(() => {
        this.handleAction.emit();
      });
    } else if ($event.key === 'runOrStop') {
      if (row.status === 'OPEN') {
        this.createListStopWarn(row);
      } else if (row.status === 'CLOSE') {
        this.kbRepo.startKb(row.knowledge_base_id).then(() => {
          this.nzMessage.success(this.i18n.transform('knowledge_open_success'));
          this.handleAction.emit();
        });
      }
    } else if ($event.key === 'edit') {
      this.editKnowledgeBase.emit(row);
    } else if ($event.key === 'test') {
      this.router.navigate([`/home/knowledge-center/knowledge/${row.knowledge_base_id}/hit-testing`], {
        queryParams: {
          display_name: row.name,
          id: row.knowledge_base_id,
          isMarket: this.isMarket,
        },
      });
    } else if ($event.key === 'advanced') {
      this.advancedKnowledgeBase.emit(row);
    } else if ($event.key === 'kbReference') {
      this.nzDrawer.create({
        nzContent: ReferenceModalComponent,
        nzWidth: '900px',
        nzMaskClosable: true,
        nzData: {
          kbId: row.knowledge_base_id,
        }
      });
    } else if ($event.key === 'cancelShare') {
      this.changeKbScope(row);
    }
  }

  changeKbScope(row: any) {
    const scopeItem = KB_SCOPE_MAP.get(row.share_scope);
    if (scopeItem) {
      this.nzModal.confirm({
        nzTitle: this.i18n.transform('confirm_cancel_shared_app'),
        nzContent: this.i18n.transform('confirm_cancel_knowledge_base'),
        nzOkText: this.i18n.transform('ok'),
        nzCancelText: this.i18n.transform('cancel'),
        nzOnOk: () => new Promise<void>((resolve, reject) => {
          this.kbRepo.changeKbScope(row.knowledge_base_id, { share_scope: scopeItem.operationParam }).then(() => {
            this.nzMessage.success(`${this.i18n.transform(scopeItem.buttonName)}${this.i18n.transform('success')}`);
            this.changeKbScopeSuccess.emit();
            resolve();
          }).catch(err => {
            reject(err);
          });
        })
      });
    }
  }

  createListStopWarn(data: any): void {
    this.nzModal.confirm({
      nzTitle: this.i18n.transform('confirm_disable_knowledge_base'),
      nzContent: this.i18n.transform('disable_knowledge_base_usage_hint'),
      nzOkText: this.i18n.transform('button_close'),
      nzOkType: 'primary',
      nzCancelText: this.i18n.transform('cancel'),
      nzOnOk: () => new Promise<void>((resolve, reject) => {
        this.kbRepo.stopKb(data.knowledge_base_id).then(() => {
          this.handleAction.emit();
          this.nzMessage.success(this.i18n.transform('knowledge_close_success'));
          resolve();
        }).catch(err => {
          reject(err);
        });
      })
    });
  }

  public handleKnowledgeDetailView(data: any) {
    if (data.type !== 'external') {
      this.router.navigate([`/home/knowledge-center/knowledge/${data.knowledge_base_id}`], {
        queryParams: {
          isMarket: this.isMarket,
          previousUrl: this.router.url,
        }
      });
    }
  }

  public isStatusError(row: any) {
    return !row.source?.knowledge_base_connection_id && row.type === 'external';
  }

  public copyUrl(event: MouseEvent, url: string) {
    event.stopPropagation();
    if (url) {
      navigator.clipboard.writeText(url).then(() => {
        this.nzMessage.success(this.i18n.transform('copy_success'));
      }).catch(() => {
        this.nzMessage.error('Failed to copy');
      });
    }
  }

  protected readonly knowledgeBaseStatusMap = knowledgeBaseStatusMap;
  protected readonly knowledgeBaseSourceMap = knowledgeBaseSourceMap;
}
