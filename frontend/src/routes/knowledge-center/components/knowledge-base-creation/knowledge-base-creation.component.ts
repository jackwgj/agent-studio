import { Component, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { I18nNamespace } from '@i18n';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import {
  CreateThirdPartyComponent,
} from '@routes/knowledge-center/components/create-third-party/create-third-party.component';
import {
  DefaultConfigModalComponent,
} from '@routes/knowledge-center/components/default-config-modal/default-config-modal.component';
import {
  SelectCreateTypeComponent,
} from '@routes/knowledge-center/components/select-create-type/select-create-type.component';
import {
  KbSourceConnectionComponent,
} from '@routes/knowledge-center/kb-source-connection/kb-source-connection.component';
import {
  KnowledgeFormDrawerComponent,
} from '@routes/knowledge-center/knowledge-form-drawer/knowledge-form-drawer.component';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { NzDrawerService } from 'ng-zorro-antd/drawer';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'knowledge-base-creation',
  templateUrl: './knowledge-base-creation.component.html',
  styleUrls: ['./knowledge-base-creation.component.less'],
  standalone: true,
  imports: [
    MODULES,
    SelectCreateTypeComponent,
    DefaultConfigModalComponent,
    CreateThirdPartyComponent,
    KbSourceConnectionComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT],
    },
    NzDrawerService,
    NzMessageService
  ],
})
export class KnowledgeBaseCreationComponent {
  @ViewChild('selectCreateType') selectCreateType: SelectCreateTypeComponent;
  @ViewChild('createThirdPartyRef') createThirdPartyRef: CreateThirdPartyComponent;
  @ViewChild('kbSourceConnection') kbSourceConnection: KbSourceConnectionComponent;
  @ViewChild('defaultConfigModalRef') defaultConfigModalRef: DefaultConfigModalComponent;

  constructor(
    public knowledgeService: KnowledgeRepoService,
    private router: Router,
    private route: ActivatedRoute,
    private configServ: AgentConfigService,
    private i18n: I18NextEagerPipe,
    private nzDrawerService: NzDrawerService,
    private nzMessage: NzMessageService
  ) {
  }

  public createKb() {
    if (this.configServ.createKnowledgeBase()) {
      this.selectCreateType.show();
    } else {
      this.createThirdPartyRef.clearValue();
      this.createThirdPartyRef.showDrawer();
    }
  }

  public openDefaultModel() {
    const drawerRef = this.nzDrawerService.create<KnowledgeFormDrawerComponent, any>({
      nzTitle: undefined,
      nzContent: KnowledgeFormDrawerComponent,
      nzWidth: '700px',
      nzPlacement: 'right',
      nzMaskClosable: false,
    });

    drawerRef.afterOpen.subscribe(() => {
      const instance = drawerRef.getContentComponent();

      instance.submitForm.subscribe(
        async ({ form, setConfirmLoading }) => {
          try {
            setConfirmLoading(true);
            let res = await this.knowledgeService.createKb(form);
            this.nzMessage.success(this.i18n.transform('create_success'));
            drawerRef.close();
            await this.router.navigate([`/home/knowledge-center/knowledge/${ res.knowledge_repo_id }`],
              {
                queryParams: {
                  previousUrl: this.router.url,
                  externalAdd: true,
                },
              });
          } finally {
            setConfirmLoading(false);
          }
        },
      );

      instance.cancelForm.subscribe(() =>
        drawerRef.close(),
      );
    });
  }

  public thirdPartyKbCreate(kbId: string) {
    const queryParams = { ...this.route.snapshot.queryParams };
    this.router.navigate([], {
      queryParams: {
        ...queryParams,
        kbId,
      },
      replaceUrl: true,
    });
  }

  public openDefaultConfigModal(event) {
    this.defaultConfigModalRef.open(event);
  }
}
