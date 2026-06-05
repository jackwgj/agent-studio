import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { MODULES } from '@shared/modules';
import {
  knowledgeBaseSourceMap,
  knowledgeBaseStatusMap,
} from '@routes/knowledge-center/knowledge-base-list/knowledge-base-list.map';
import { PipesModule } from "../../../../pipes/pipes.module";
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzIconModule } from 'ng-zorro-antd/icon';

export interface ActionMenuItem {
  label: string;
  key: string;
  disabled: boolean;
}

@Component({
  selector: 'knowledge-base-access-card',
  templateUrl: './knowledge-base-access-card.component.html',
  styleUrls: ['./knowledge-base-access-card.component.less'],
  standalone: true,
  imports: [MODULES, PipesModule, NzTagModule, NzIconModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.KNOWLEDGE],
    },
    NzModalService,
  ],
})
export class KnowledgeBaseAccessCardComponent implements OnInit {
  @Input() accessKnowledgeListData: any;
  @Output() editAccessKnowledgeBase = new EventEmitter();
  @Output() deleteAccessKnowledgeBase = new EventEmitter();

  constructor(
    private nzModal: NzModalService,
    public kbAbilitiesService: KbAbilitiesService,
    private i18n: I18NextEagerPipe,
  ) {}

  ngOnInit(): void {}

  dataToItemsFn = (data: any): Array<ActionMenuItem> => {
    let items: Array<ActionMenuItem> = [
      {
        label: this.i18n.transform('edit'),
        key: 'edit',
        disabled: this.kbAbilitiesService.isResourceDisabled,
      },
      {
        label: this.i18n.transform('delete'),
        key: 'delete',
        disabled: knowledgeBaseStatusMap.get(data.status).disabled || this.kbAbilitiesService.isResourceDisabled,
      },
    ];
    return items;
  };

  public clickMenu(e: Event) {
    e.stopPropagation();
  }

  public listAction($event: ActionMenuItem, row: any) {
    if ($event.key === 'delete') {
      this.accessCardDeleteWarn(row);
    } else if ($event.key === 'edit') {
      this.editAccessKnowledgeBase.emit(row);
    }
  }

  accessCardDeleteWarn(data: any): void {
    this.nzModal.confirm({
      nzTitle: this.i18n.transform('delete'),
      nzContent: this.i18n.transform('confirm_delete_item', { name: data.name }),
      nzOkText: this.i18n.transform('ok'),
      nzOkType: 'primary',
      nzOkDanger: true,
      nzCancelText: this.i18n.transform('cancel'),
      nzOnOk: () => {
        this.deleteAccessKnowledgeBase.emit(data.id);
      }
    });
  }

  protected readonly knowledgeBaseStatusMap = knowledgeBaseStatusMap;
  protected readonly knowledgeBaseSourceMap = knowledgeBaseSourceMap;
}
