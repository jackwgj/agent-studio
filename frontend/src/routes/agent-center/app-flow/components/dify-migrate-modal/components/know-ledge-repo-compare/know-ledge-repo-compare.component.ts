import { Component, Input } from '@angular/core';
import { CommonModule } from "@angular/common";
import { I18NEXT_NAMESPACE, I18NextEagerPipe, I18NextModule } from "angular-i18next";
import { NzModalModule } from "ng-zorro-antd/modal";
import { NzIconModule } from "ng-zorro-antd/icon";

import { I18nNamespace } from "@i18n";
import type { IKnowledgeRepo } from "@routes/agent-center/app-flow/node.type";
import { MigrateCompareRowComponent } from "@routes/agent-center/app-flow/components/dify-migrate-modal/components/migrate-compare-row/migrate-compare-row.component";
import { MODULES } from "@shared/modules";
import { KnowledgeBaseSelectorComponent } from "@routes/knowledge-center/components/knowledge-base-selector/knowledge-base-selector.component";
import { KbAbilitiesService } from "@services/knowledge-center/kb-abilities.service";
import { WORKFLOW_SVGS } from "@routes/agent-center/app-flow/flow.const";
import { NzDrawerService } from "ng-zorro-antd/drawer";

@Component({
  selector: 'know-ledge-repo-compare',
  templateUrl: './know-ledge-repo-compare.component.html',
  styleUrls: ['../common-compare.less', './know-ledge-repo-compare.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    NzModalModule,
    NzIconModule,
    I18NextModule,
    MigrateCompareRowComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class KnowLedgeRepoCompareComponent {
  @Input() node: IKnowledgeRepo;

  public compareKbList = [];

  public modelValidation = {
    errorMessage: {
      required: this.i18n.transform('select_model'),
    },
    type: 'changeAlert',
  };

  public get icon() {
    return WORKFLOW_SVGS.KnowledgeRepo;
  }

  constructor(
    private i18n: I18NextEagerPipe,
    private kbAbilitiesService: KbAbilitiesService,
    private drawerServ: NzDrawerService,
  ) {
  }

  ngOnInit(): void {
    this.compareKbList = this.node.configs.repos.map(repo => {
      return {
        old: repo,
        new: null
      };
    });

    if (!this.compareKbList.length) {
      this.compareKbList = [{
        old: null,
        new: null
      }];
    }
  }

  public onUpdateKnowLedge(index: number): void {
    if (this.kbAbilitiesService.isResourceDisabled) {
      return;
    }

    const drawerRef = this.drawerServ.create({
      nzTitle: undefined,
      nzContent: KnowledgeBaseSelectorComponent,
      nzWidth: '600px',
      nzClosable: true,
      nzMaskClosable: true,
      nzMask: true,
      nzFooter: null,
      nzData: {}
    });
    drawerRef.afterClose.subscribe(() => {
      const kbs = drawerRef.getContentComponent().selectedKbs ?? [];
      const newKb = kbs[0];
      if (!newKb) {
        return;
      }

      this.compareKbList[index].new = newKb;
      this.compareKbList = [...this.compareKbList];
      this.node.configs.repos.push({
        id: newKb.id,
        name: newKb.name,
        description: newKb.description
      });
    });
  }

  public onDeleteKnowLedge(index: number): void {
    const tempKb = this.compareKbList[index]?.new;

    if (!tempKb || !tempKb.id) {
      return;
    }

    this.compareKbList[index].new = null;
    this.compareKbList = [...this.compareKbList];
    this.node.configs.repos = this.node.configs.repos.filter(
      (repo: { id: string }) => repo.id !== tempKb.id
    );
  }

}
