import { Component } from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextModule } from 'angular-i18next';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'health',
  templateUrl: './health.component.html',
  styleUrls: ['./health.component.less'],
  standalone: true,
  imports: [
    I18NextModule,
    FormsModule,
    CommonModule,
    MODULES
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.PROMPT_PLATFORM,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.PLATFORM_MANAGEMENT,
        I18nNamespace.TOOL,
      ],
    },
  ],
})
export class HealthComponent {
  public context:string = '@the@health@is@good@';
}
