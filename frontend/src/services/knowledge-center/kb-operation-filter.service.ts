import { inject, Injectable } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { KbItem, KbOperation, KbOperationKey, KBStatus, KBType } from '@routes/knowledge-center/knowledge-base.interface';
import { KBScope } from '@routes/knowledge-center/knowledge.types';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import i18next from 'i18next';

@Injectable({
  providedIn: 'root',
})
export class KbOperationFilterService {
  nameSpace = [
    I18nNamespace.COMMON,
    I18nNamespace.KNOWLEDGE,
  ];
  kbAbilitiesService = inject(KbAbilitiesService);
  kbErrorTip = this.#translate('knowledge_base_error_tip');

  public getDetailOperations(kb: KbItem, isMarket = false) {


  }

  public getKbOperations(kb: KbItem, isMarket = false): Array<KbOperation> {
    let items = [];
    if (isMarket) {
      items = [
        this.opHitTest(kb),
      ];
      if (this.kbAbilitiesService.isSameSpace(kb.workspace_id)) {
        items.push(
          this.opCancelOperation(kb),
        );
      }
      return items;
    }
    if (kb.type === 'internal') {
      items = [
        this.opChangeStatus(kb),
        this.opHitTest(kb),
        this.opEdit(kb),
        this.opAdvanced(kb),
        this.opKbReference(kb),
        this.opDelete(kb),
      ];
    } else if (kb.type === 'external') {
      items = [
        this.opChangeStatus(kb),
        this.opHitTest(kb),
        this.opKbReference(kb),
        this.opDelete(kb),
      ];
    }
    return items;
  }

  isKbError = (kb: KbItem) => kb.type === 'external' && !kb.source?.knowledge_base_connection_id;

  opHitTest = (kb: KbItem) => {
    let tip = '';

    if (kb.status === KBStatus.CLOSE) {
      tip = this.#translate('plz_enable_kb_first');
    }

    if (this.isKbError(kb)) {
      tip = this.kbErrorTip;
    }

    return {
      label: this.#translate('hit_test'),
      key: KbOperationKey.HIT_TEST,
      id: KbOperationKey.HIT_TEST,
      disabled: kb.status === KBStatus.CLOSE || this.kbAbilitiesService.isResourceDisabled || this.isKbError(kb),
      tip,
    };
  };

  opCancelOperation = (kb: KbItem) => {
    let tip = '';

    return {
      label: this.#translate('kb_cancel_share_operation'),
      key: KbOperationKey.CANCEL_SHARE,
      id: KbOperationKey.CANCEL_SHARE,
      disabled: this.kbAbilitiesService.isResourceDisabled,
      tip,
    };
  };

  opChangeStatus = (kb: KbItem) => {
    let tip = '';

    if (kb.share_scope !== KBScope.SELF) {
      tip = this.#translate('plz_cancel_share_kb');
    }

    if (kb.status === KBStatus.CLOSE && this.isKbError(kb)) {
      tip = this.kbErrorTip;
    }

    return {
      label: kb.status === KBStatus.OPEN ? this.#translate('disable') : this.#translate('enable'),
      key: KbOperationKey.CHANGE_STATUS,
      id: KbOperationKey.CHANGE_STATUS,
      disabled: kb.share_scope !== KBScope.SELF || this.kbAbilitiesService.isResourceDisabled || (kb.status === KBStatus.CLOSE && this.isKbError(kb)),
      tip,
    };
  }

  opEdit = (kb: KbItem) => {
    let tip = '';

    if (kb.status === KBStatus.OPEN) {
      tip = this.#translate('plz_disable_kb_first');
    }

    return {
      label: this.#translate('edit'),
      key: KbOperationKey.EDIT,
      id: KbOperationKey.EDIT,
      disabled: kb.status === KBStatus.OPEN || this.kbAbilitiesService.isResourceDisabled,
      tip,
    };
  }

  opAdvanced = (kb: KbItem) => {
    let tip = '';

    if (kb.status === KBStatus.OPEN) {
      tip = this.#translate('plz_disable_kb_first');
    }

    return {
      label: this.#translate('advanced'),
      key: KbOperationKey.ADVANCED_SET,
      id: KbOperationKey.ADVANCED_SET,
      disabled: kb.status === KBStatus.OPEN || this.kbAbilitiesService.isResourceDisabled,
      tip,
    };
  }

  opKbReference = (kb: KbItem) => {
    let tip = '';

    return {
      label: this.#translate('view_citation'),
      key: KbOperationKey.KB_REFERENCE,
      id: KbOperationKey.KB_REFERENCE,
      disabled: this.kbAbilitiesService.isResourceDisabled,
      tip,
    };
  };

  opDelete = (kb: KbItem) => {
    let tip = '';

    if (kb.status === KBStatus.OPEN) {
      tip = this.#translate('plz_disable_kb_first');
    }

    return {
      label: kb.type === KBType.EXTERNAL ? this.#translate('cancel_access') : this.#translate('delete'),
      key: KbOperationKey.DELETE,
      id: KbOperationKey.DELETE,
      disabled: kb.status === KBStatus.OPEN || this.kbAbilitiesService.isResourceDisabled,
      tip,
    };
  };

  opObsSetting = (kb: KbItem) => {
    let tip = '';

    return {
      label: this.#translate('obs_access_file'),
      key: KbOperationKey.OBS_SETTING,
      id: KbOperationKey.OBS_SETTING,
      disabled: this.kbAbilitiesService.isResourceDisabled,
      tip,
    };
  };

  #translate(key: string) {
    return i18next.t(key, {
      ns: this.nameSpace,
    });
  }
}
