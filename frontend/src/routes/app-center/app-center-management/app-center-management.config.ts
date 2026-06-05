import i18next from 'i18next';

export enum MARKET_TYPE {
  OFFICIAL = 'OFFICIAL',
  SHARE = 'SHARE',
}

export enum OFFICIAL_TYPE {
  Agent = 'agent',
  workflow = 'workflow',
  controller = 'controller',
}

export enum SHARE_TYPE {
  CAN_COPY = 'CAN_COPY',
  CAN_USE = 'CAN_USE',
}

export enum SHARE_CARD_MENU_TYPE {
  CANCEL_SHARE = 'CANCEL_SHARE',
  COPY_SHARE = 'COPY_SHARE',
  COPY_TO_WORK = 'COPY_TO_WORK',
  INFO = 'INFO'
}

export enum SHARE_TYPE {
  MY_SHARE = 'MY_SHARE',
  OTHER_SHARE = 'OTHER_SHARE',
}

export const AppCenterTabs = [
  {
    title: i18next.t('platform_selection_1'),
    id: MARKET_TYPE.OFFICIAL,
    active: true,
  },
  {
    title: i18next.t('appcentermanagement_2'),
    id: MARKET_TYPE.SHARE,
    active: false,
  },
];

export const AppShareTabs = [
  {
    title: i18next.t('app-center-management_1'),
    id: SHARE_TYPE.OTHER_SHARE,
    active: true,
  },
  {
    title: i18next.t('app-center-management_2'),
    id: SHARE_TYPE.MY_SHARE,
    active: false,
  },
];

export const ShareTypeOptions = [
  {
    label: i18next.t('all'),
    value: '',
  },
  {
    label: i18next.t('app-center-management_3'),
    value: SHARE_TYPE.CAN_COPY,
    disabled: true,
  },
  {
    label: i18next.t('app-center-management_4'),
    value: SHARE_TYPE.CAN_USE,
  },
  {
    label: i18next.t('app-center-management_2'),
    value: '',
  },
];
export const ShareTypeTag = (type: any): string => {
  return ShareTypeOptions.find((item: any) => type && item.value === type)?.label ?? '';
}


export enum ADD_TYPE {
  OFFICIAL = 'OFFICIAL',
  SHARE = 'SHARE',
  CURRENT_SPACE = 'CURRENT_SPACE',
}

export const AddTypeOptions = [
  {
    title: i18next.t('appcentermanagement_1'),
    id: ADD_TYPE.CURRENT_SPACE,
    active: true,
  },
  {
    title: i18next.t('appcentermanagement_2'),
    id: ADD_TYPE.SHARE,
    active: false,
  },
];
