import { Component, Input, OnInit } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';

@Component({
  selector: 'delete-skill-modal',
  templateUrl: './delete-skill-modal.component.html',
  styleUrls: ['./delete-skill-modal.component.scss'],
  standalone: true,
  imports: [
    MODULES,
    NzTableModule,
    NzAlertModule,
    NzButtonModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.COMMON],
    },
  ],
})
export class DeleteSkillModalComponent implements OnInit {
  @Input() modalTitle = this.i18n.transform('plugin_delete_label');
  @Input() quotaData: {
    guideName: string;
    sceneName: string;
  }[];

  constructor(private i18n: I18NextEagerPipe) {}

  ngOnInit(): void {}

  close(): void {}
  dismiss(): void {}
}
