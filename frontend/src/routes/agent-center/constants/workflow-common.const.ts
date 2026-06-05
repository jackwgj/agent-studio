import i18next from 'i18next';
import { I18nNamespace } from '@i18n';

export const NODE_ACTIONS = [
  {
    id: 'update',
    label: i18next.t('rename'),
  },
  {
    id: 'copy',
    label: i18next.t('copy'),
  },
  {
    id: 'delete',
    label: i18next.t('delete'),
  },
  {
    id: 'connect_end',
    label: i18next.t('connect_to_end_node',{
      ns: [I18nNamespace.COMMON, I18nNamespace.AGENT_CENTER],
    }),
  },
];

export const WORKFLOW_STATUS = {
  SUCCESS: 'Success',
  FAIL: 'Fail',
  DEVELOPMENT: 'Development',
  PUBLISHED: 'Published',
};
