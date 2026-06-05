import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { I18nService } from '@agentcore/core/i18n.service';
import { I18nPipe } from '@agentcore/shared/pipes/i18n.pipe';
import { SimpleModalConfig } from './model';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzSpinModule } from 'ng-zorro-antd/spin';

@Component({
  standalone: true,
  template: `
    <div class="simple-modal-content">
      @if (config.type === 'warn') {
        <nz-alert nzType="warning" [nzMessage]="config.title" [nzDescription]="config.warn"></nz-alert>
      }
      @if (config.type === 'error') {
        <nz-alert nzType="error" [nzMessage]="config.title" [nzDescription]="config.warn"></nz-alert>
      }
      @if (config.type === 'success') {
        <nz-alert nzType="success" [nzMessage]="config.title"></nz-alert>
      }
      @if (config.type === 'simple') {
        <div class="simple-text-list">
          @for (text of config.simpleText; track text) {
            <p class="simple-text">{{ text }}</p>
          }
        </div>
      }
    </div>
  `,
  styleUrl: './simple-modal.scss',
  imports: [CommonModule, NzAlertModule, NzButtonModule, NzSpinModule, I18nPipe],
  providers: [I18nService],
})
export class SimpleModalComponent {
  @Input() config: any = { type: 'warn' };
  confirmLoading = false;

  dismiss(): void {}
  close(): void {}
}
