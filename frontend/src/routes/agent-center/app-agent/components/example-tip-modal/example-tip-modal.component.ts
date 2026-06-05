import { Component, ChangeDetectionStrategy } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { exampleTipText } from '../../common-logic-agent';

@Component({
  selector: 'example-tip-modal',
  templateUrl: './example-tip-modal.component.html',
  styleUrls: ['./example-tip-modal.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ExampleTipModalComponent {
  public exampleContent = exampleTipText;
}
