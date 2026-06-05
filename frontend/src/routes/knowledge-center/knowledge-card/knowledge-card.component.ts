import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import {
  KnowledgeRepoAction,
} from '@enums/knowledge-repo.enum';
import { I18nNamespace } from '@i18n';
import { KBStatus } from '@routes/knowledge-center/knowledge-base.interface';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import type { IKnowledge } from '@routes/agent-center/app-knowledge/knowledge.types';
import { InlineSvgComponent } from '@shared/components/inline-svg.component';

import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzModalService, NzModalModule } from 'ng-zorro-antd/modal';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'knowledge-card',
  templateUrl: './knowledge-card.component.html',
  styleUrls: ['./knowledge-card.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    InlineSvgComponent,
    NzDropDownModule,
    NzIconModule,
    NzToolTipModule,
    NzModalModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.KNOWLEDGE],
    },
  ],
})
export class KnowledgeCardComponent implements OnInit {
  @Input() knowledgeData: IKnowledge = {
    knowledge_repo_id: '',
    display_name: '',
    id: '',
    name: '',
    description: '',
    size: 0,
    icon: '',
    creator: '',
    create_time: '',
    update_time: '',
    status: null,
  };

  @Output() handleAction = new EventEmitter();

  public get statusTag() {
    return KBStatus.CLOSE !== this.knowledgeData.status
      ? 'status_open'
      : 'status_close';
  }

  public knowledgeActions = [];

  constructor(
    private nzModal: NzModalService,
    private router: Router,
    private kbRepo: KnowledgeRepoService,
    private i18n: I18NextEagerPipe,
  ) {}

  ngOnInit(): void {
    this.knowledgeActions = [
      {
        id: KnowledgeRepoAction.COPY,
        label: this.i18n.transform('copy_id', {
          ns: I18nNamespace.KNOWLEDGE,
        }),
      },
      {
        id: KnowledgeRepoAction.DELETE,
        label: this.i18n.transform('delete'),
      },
    ];

    if (this.knowledgeData.status) {
      this.knowledgeActions.splice(
        1,
        0,
        KBStatus.CLOSE === this.knowledgeData.status
          ? {
            id: KnowledgeRepoAction.OPEN,
            label: this.i18n.transform('button_open', {
              ns: I18nNamespace.KNOWLEDGE,
            }),
          }
          : {
            id: KnowledgeRepoAction.CLOSE,
            label: this.i18n.transform('button_close', {
              ns: I18nNamespace.KNOWLEDGE,
            }),
          },
      );
    }
  }

  public clickMenu(e: Event) {
    e.stopPropagation();
  }

  public onClickAction(action) {
    const { id } = action;
    const { knowledge_repo_id } = this.knowledgeData;
    if (KnowledgeRepoAction.COPY === id) {
      this.kbRepo.handleIdCopy(knowledge_repo_id);
      return;
    }
    if (KnowledgeRepoAction.DELETE === id) {
      this.kbRepo
        .handleKbDelete(this.knowledgeData, this.nzModal as any)
        .then(() => this.handleAction.emit(id));
      return;
    }
    if (KnowledgeRepoAction.CLOSE === id) {
      this.kbRepo
        .handleKbStop(knowledge_repo_id)
        .then(() => this.handleAction.emit(id));
      return;
    }
    this.kbRepo
      .handleKbStart(knowledge_repo_id)
      .then(() => this.handleAction.emit(id));
  }

  public handleKnowledgeDetailView() {
    const { knowledge_repo_id } = this.knowledgeData;
    this.router.navigateByUrl(
      `/home/knowledge-center/knowledge/${knowledge_repo_id}`,
    );
  }
}
