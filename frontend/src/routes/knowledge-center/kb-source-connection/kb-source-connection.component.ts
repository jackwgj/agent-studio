import { Component, EventEmitter, Output, ViewChild, TemplateRef } from '@angular/core';
import { NzDrawerRef, NzDrawerService } from 'ng-zorro-antd/drawer';
import { KbSourceConnectionModalComponent } from '@routes/knowledge-center/kb-source-connection/kb-source-connection-modal/kb-source-connection-modal.component';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'kb-source-connection',
  templateUrl: './kb-source-connection.component.html',
  styleUrls: ['./kb-source-connection.component.less'],
  standalone: true,
  imports: [MODULES, KbSourceConnectionModalComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.KNOWLEDGE, I18nNamespace.COMMON],
    },
  ],
})
export class KbSourceConnectionComponent {
  @Output() editSuccess = new EventEmitter();
  @ViewChild('drawerTemplate', { static: true }) drawerTemplate!: TemplateRef<any>;
  drawerRef: NzDrawerRef;

  constructor(private drawerService: NzDrawerService) {}

  showModal(connectionId = '') {
    this.drawerRef = this.drawerService.create({
      nzContent: KbSourceConnectionModalComponent,
      nzWidth: 700,
      nzPlacement: 'right',
      nzClosable: true,
      nzMaskClosable: true,
      nzTitle: null,
      nzFooter: null,
      nzContentParams: {
        connectionId: connectionId,
        drawerRef: this.drawerRef,
      },
    });
    this.drawerRef?.afterOpen?.subscribe(() => {
      const drawContentComponent = this.drawerRef?.getContentComponent();
      drawContentComponent.connectionId = connectionId;
      drawContentComponent.drawerRef =  this.drawerRef;
      drawContentComponent.editSuccess.subscribe(() => {
        this.editSuccess.emit();
        this.drawerRef.close();
      });
    });
  }
}
