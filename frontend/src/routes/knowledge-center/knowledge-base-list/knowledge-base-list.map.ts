import { KBScope } from '@routes/knowledge-center/knowledge.types';
import i18next from 'i18next';

type StatusIcon = "alarmFatal" | "alarmHigh" | "alarmMedium" | "alarmLow" | "alarmPrompt" | "active" | "cancel" | "creating" | "error" | "freeze";

interface StatusInfo {
  icon: StatusIcon;
  text: string;
  button: string;
  disabled: boolean;
  cardTagColor: string;
}

export const knowledgeBaseStatusMap = new Map<string, StatusInfo>([
  [
    'OPEN',
    {
      icon: 'active' as const,
      text: i18next.t('enabled'),
      button: i18next.t('disable'),
      disabled: true,
      cardTagColor: 'green',
    },
  ],
  [
    'CLOSE',
    {
      icon: 'cancel' as const,
      text: i18next.t('disabled'),
      button: i18next.t('enable'),
      disabled: false,
      cardTagColor: 'rgb(245, 245, 245)',
    },
  ],
]);

export const knowledgeBaseSourceMap = new Map([
  ['internal', { label: i18next.t('default'), color: 'blue' }],
  ['external', { label: i18next.t('third_party'), color: 'purple' }],
]);


export const KB_SCOPE_MAP = new Map([
  [KBScope.SELF,
    {
      label: 'kb_self_space',
      color: 'blue',
      buttonName: 'kb_share_operation',
      rangeName: i18next.t('kb_self_space'),
      operationParam: KBScope.GLOBAL,
    }],
  [KBScope.GLOBAL,
    {
      label: 'kb_share_space',
      color: 'purple',
      buttonName: 'kb_cancel_share_operation',
      rangeName: i18next.t('kb_share_space'),
      operationParam: KBScope.SELF,
    }],
  [KBScope.PARTIAL,
    {
      label: 'kb_share_space',
      color: 'purple',
      buttonName: 'kb_cancel_share_operation',
      rangeName: i18next.t('kb_share_space'),
      operationParam: KBScope.SELF,
    }],
]);
export const KB_SHARE_SCOPE_NAME_MAP = new Map([
  [KBScope.SELF, i18next.t('kb_self_space')],
  [KBScope.GLOBAL, i18next.t('kb_all_space')],
  [KBScope.PARTIAL, i18next.t('kb_part_space')],
]);

export const knowledgeBaseDeleteTitleMap = new Map([
  ['internal', i18next.t('delete')],
  ['external', i18next.t('cancel_access')],
]);

export enum KnowledgeType {
  INTERNAL = 'internal',
  EXTERNAL = 'external',
}
