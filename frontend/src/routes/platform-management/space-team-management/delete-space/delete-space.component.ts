import { Component, Input, ViewEncapsulation } from "@angular/core";
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { SpaceTeamManagementService } from '@services/space-team-management.service';
import {ContextService} from "@services/context.service";
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { FormsModule } from '@angular/forms';
import {NzMessageService} from "ng-zorro-antd/message";

@Component({
  selector: 'meta-delete-modal',
  standalone: true,
  imports: [CommonModule, MODULES, NzAlertModule, FormsModule],
  templateUrl: './delete-space.component.html',
  styleUrls: ['./delete-space.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PLATFORM_MANAGEMENT, I18nNamespace.COMMON],
    },
  ],
})
export class DeleteSpaceComponent {
  @Input() title: '';
  @Input() tip: '';
  @Input() id: '';
  @Input() fnName: '';

  configWord = '';
  confirmText = 'DELETE';
  loading = false;

  constructor(
    private i18n: I18NextEagerPipe,
    private spaceTeamManagementService: SpaceTeamManagementService,
    private ctxServ: ContextService,
    private modalRef: NzModalRef,
    private message: NzMessageService
  ) {}

  delete() {
    this.loading = true;

    const params={
      id:this.ctxServ.currentWorkspaceId
    }
    this.spaceTeamManagementService
      .deleteSapceFn(params)
      .then(() => {
        this.spaceTeamManagementService.updateSpace('UPDATE');
        this.message.create('success', this.i18n.transform('delete_success'));
        this.modalRef.destroy();
      })
      .finally(() => {
        this.loading = false;
      });
  }

  close(): void {
    this.modalRef.destroy();
  }
  dismiss(): void {
    this.modalRef.destroy();
  }
}
