import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { I18nNamespace } from '@i18n';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { Subscription } from 'rxjs';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'kb-add-modal-after-create',
  template: '',
  styleUrls: [],
  standalone: true,
  imports: [
    MODULES,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.AGENT,
      ],
    },
  ],
})
export class KbAddModalAfterCreateComponent implements OnInit, OnDestroy {
  @Input({
    required: true,
  }) serviceName: string;
  @Output() confirmAddKb = new EventEmitter();

  routerSub: Subscription;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private kbAbilitiesService: KbAbilitiesService,
    private i18n: I18NextEagerPipe,
    private nzModal: NzModalService,
    private nzMessage: NzMessageService,
  ) {
  }

  ngOnInit() {
    this.routerSub = this.route.queryParams.subscribe(params => {
      const { kbId } = params;
      if (kbId) {
        setTimeout(() => {
          this.showAddModal(kbId);
        }, 0);
      }
    });
  }

  private showAddModal(kbId) {
    this.nzModal.confirm({
      nzTitle: this.i18n.transform('creat_kb_success'),
      nzContent: this.i18n.transform('add_after_kb_create', {
        serviceName: this.serviceName,
      }),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzAutofocus: 'ok',
      nzOnOk: () => new Promise<void>((resolve) => {
        this.kbAbilitiesService.getKbDetail(kbId).subscribe(res => {
          if (res) {
            this.confirmAddKb.emit(res);
          } else {
            this.nzMessage.error(this.i18n.transform('add_kb_failed'));
          }
          this.clearRouteKbId();
          resolve();
        });
      }),
      nzOnCancel: () => {
        this.clearRouteKbId();
      },
    });
  }

  ngOnDestroy() {
    this.routerSub?.unsubscribe();
  }

  clearRouteKbId() {
    const queryParams = { ...this.route.snapshot.queryParams };
    delete queryParams.kbId;
    this.router.navigate([], {
      queryParams,
      replaceUrl: true,
    });
  }
}
