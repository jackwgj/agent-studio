import { Component } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';

@Component({
  selector: 'stream-response-tip',
  templateUrl: './stream-response-tip.component.html',
  styleUrls: ['./stream-response-tip.component.scss'],
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class StreamResponseTipComponent {
  public configTips = [
    { key: 'stream_id' },
    { key: 'is_finish' },
    { key: 'content' },
    { key: 'ext' },
    { key: 'output_mode' },
    { key: 'content_type' },
    { key: 'return_type' },
    { key: 'context_mode' },
  ];
}
