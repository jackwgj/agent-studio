import { Injectable } from '@angular/core';
import { I18nNamespace } from '@i18n';
import * as angularI18next from 'angular-i18next';
import { cdnAssetUrl } from 'src/single-spa/assets-url';

@Injectable({
  providedIn: 'root',
})
export class EvaluationStatusConstantService {
  public EvaluationTaskStatusMap: {
    [key: string]: { title: string; icon: string };
  } = {
    PENDING: {
      title: this.i18n.transform('eval_status_pending', {
        ns: I18nNamespace.PROMPT_PLATFORM,
      }),
      icon: cdnAssetUrl('assets/prompt/PAUSED.svg'),
    },
    RUNNING: {
      title: this.i18n.transform('eval_status_evaluating', {
        ns: I18nNamespace.PROMPT_PLATFORM,
      }),
      icon: cdnAssetUrl('assets/prompt/EVALUATING.svg'),
    },
    FAILED: {
      title: this.i18n.transform('eval_status_failed', {
        ns: I18nNamespace.PROMPT_PLATFORM,
      }),
      icon: cdnAssetUrl('assets/prompt/FAILED.svg'),
    },
    RETRYING: {
      title: this.i18n.transform('eval_status_restart', {
        ns: I18nNamespace.PROMPT_PLATFORM,
      }),
      icon: cdnAssetUrl('assets/prompt/RETRYING.svg'),
    },
    PARTLY_SUCCESS: {
      title: this.i18n.transform('eval_status_partial_success', {
        ns: I18nNamespace.PROMPT_PLATFORM,
      }),
      icon: cdnAssetUrl('assets/prompt/PARTLY_SUCCESS.svg'),
    },
    PAUSING: {
      title: this.i18n.transform('eval_status_aborting', {
        ns: I18nNamespace.PROMPT_PLATFORM,
      }),
      icon: cdnAssetUrl('assets/prompt/PAUSING.svg'),
    },
    PAUSED: {
      title: this.i18n.transform('eval_status_aborted', {
        ns: I18nNamespace.PROMPT_PLATFORM,
      }),
      icon: cdnAssetUrl('assets/prompt/PAUSED.svg'),
    },
    SUCCESS: {
      title: this.i18n.transform('eval_status_success', {
        ns: I18nNamespace.PROMPT_PLATFORM,
      }),
      icon: cdnAssetUrl('assets/prompt/SUCCESS.svg'),
    },
  };

  constructor(private readonly i18n: angularI18next.I18NextEagerPipe) {}
}
