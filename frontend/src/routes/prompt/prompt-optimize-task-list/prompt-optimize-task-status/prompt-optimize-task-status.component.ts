import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { COMMON_MODULES } from '@shared/modules';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'meta-prompt-optimize-task-status',
  standalone: true,
  templateUrl: './prompt-optimize-task-status.component.html',
  styleUrl: './prompt-optimize-task-status.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NzProgressModule, NzTagModule, NzToolTipModule, COMMON_MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.PROMPT_PLATFORM],
    },
  ],
})
export class PromptOptimizeTaskStatusComponent {
  @Input() status: string;
  @Input() tip: string;
  @Input() progressRate: number;
  public statusConfigMap = {
    success: {
      label: this.i18n.transform('optimize_success'),
      type: 'active',
      isProgressBar: false,
    },
    failed: {
      label: this.i18n.transform('optimize_failed'),
      type: 'error',
      isProgressBar: false,
    },
    waiting: {
      label: this.i18n.transform('wait_optimize'),
      type: 'waiting',
      isProgressBar: false,
    },
    optimizing: {
      label: this.i18n.transform('optimizing'),
      type: 'creating',
      isProgressBar: true,
      showProgress: true,
    },
    pause: {
      label: this.i18n.transform('paused'),
      isProgressBar: true,
      showProgress: false,
    },
    pausing: {
      label: this.i18n.transform('promptoptimizetaskstatus1'),
      isProgressBar: true,
      showProgress: false,
    },
    draft: {
      label: this.i18n.transform('draft'),
      type: 'cancel',
      isProgressBar: false,
    },
  };

  constructor(private i18n: I18NextEagerPipe) {}

  protected readonly changeUrl = cdnAssetUrl;

  getStatusColor(type: string): string {
    const colorMap: Record<string, string> = {
      active: 'success',
      error: 'red',
      waiting: 'default',
      creating: 'processing',
      cancel: 'default',
    };
    return colorMap[type] || 'default';
  }
}
