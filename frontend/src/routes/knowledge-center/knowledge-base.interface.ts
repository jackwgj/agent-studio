import { KBRepoType, KBScope } from '@routes/knowledge-center/knowledge.types';

export enum KBType {
  INTERNAL = 'internal',
  EXTERNAL = 'external',
}

export enum KBStatus {
  OPEN = 'OPEN',
  CLOSE = 'CLOSE',
}

export interface KBListItem {
  knowledge_base_id: string;
  icon: string;
  type: string;
  source?: KBListItemSource;
  name: string;
  description: string;
  status: KBStatus;
  created_user_id: string;
  created_user_name: string;
  create_time: number;
  last_update_user_id: string;
  last_update_user_name: string;
  update_time: number;
}

export interface KBListItemSource {
  knowledge_base_connector_id: string;
  knowledge_base_connection_id: string;
  knowledge_base_connection_name: string;
  detail_page_url: string;
}

export interface KbItem {
  name: string;
  id: string;
  icon?: string;
  description?: string;
  type?: string;
  source?: KBListItemSource;
  status?: KBStatus;
  repo_type?: KBRepoType;
  share_scope?: KBScope,

  // front
  operations?: KbOperation[];
  selected?: boolean;

  [k: string]: any;
}

export interface KbOperation {
  label: string,
  key: string,
  id: string,
  disabled: boolean;
}

export enum KbOperationKey {
  HIT_TEST = 'test',
  CANCEL_SHARE = 'cancelShare',
  CHANGE_STATUS = 'runOrStop',
  ADVANCED_SET = 'advanced',
  KB_REFERENCE = 'kbReference',
  DELETE = 'delete',
  EDIT = 'edit',
  OBS_SETTING = 'obsSetting',
}

export interface KBAbilityRes {
  abilities: KBAbility[];
}

export interface KBAbility {
  id: string;
  code: KBAbilityCode;
  name: string;
  connector_id: string;
  description: string;
  sub_ability: string | null;
}

export enum KBAbilityCode {
  KNOWLEDGE_LIST = 'KNOWLEDGE_LIST',
  RETRIEVE = 'RETRIEVE',
}

export interface SubAbility {
  search_mode_list: SearchMode[];
}

export interface SearchMode {
  search_mode: SearchModeType;
  search_mode_name: string;
  search_mode_name_en: string;
  summary: string;
  summary_en: string;
  description: string;
  description_en: string;
}

export interface KBRetrieveMode {
  iconName: string;
  title: string;
  desc: string;
  summary: string;
  value: SearchModeType;
}

export enum SearchModeType {
  DOC = 'doc',
  KEYWORD = 'keyword',
  MIX = 'mix',
  FAQ = 'faq',
}

export interface ReferenceItemData {
  document_name: string;
  file_id: string;
  knowledge_base_id: string;
  knowledge_base_type: string;
  retrieval_id: string;
  type: string;
  slicingFiles: {
    document_name: string;
    file_id: string;
    knowledge_base_id: string;
    retrieval_id: string;
    type: string;
    content: string;
    score: number,
  }[],
}

export enum KbTagType {
  INVALID = 'invalid',
  STATUS = 'status',
  TYPE = 'type',
  SOURCE = 'source',
  SHARE = 'share',
}

export enum KbCardOperationType {
  CARD_CLICK = 'click',
  MENU = 'menu',
  SELECT_CHANGE = 'selectChange'
}

export interface KBCardOperation {
  kb: KbItem;
  type: KbCardOperationType;
  data?: Record<string, any>;
}

export interface CardOperationEmit {
  type: KbCardOperationType;
  data: {
    status: 'success' | 'error';

    [k: string]: any;
  };
}

export interface KbCardDisplayConfig {
  layoutConfig?: {
    layout?: 'grid' | 'flex';
    flexQuantities?: number;
  };
  cardSize?: 'small' | 'middle' | 'large';
  cardBgColor?: string;
  cardNameBold?: boolean;
  descriptionConfig?: {
    enable?: boolean;
    maxLine?: number;
  };
  operationConfig?: {
    enable?: boolean;
    singleOperationEnable?: (kb: KbItem, operationType: KbCardOperationType) => boolean;
  };
  tagsConfig?: {
    enable?: boolean | KbTagType[] | ((kb: KbItem, tagType: KbTagType) => boolean);
    tagSize?: 'xs' | 'small' | 'middle' | 'large';
  };
  noDataConfig?: {
    showImg?: boolean;
    showTitle?: boolean;
    showDescription?: boolean;
    showCreate?: boolean;
  };
  footerConfig?: {
    userShow?: boolean;
    timeShow?: boolean;
    kbOperation?: boolean;
  };
  detailLink?: {
    enable?: boolean;
  };
}

export enum KbModelType {
  RERANK = 'rerank',
  EMBEDDING = 'embedding'
}

export enum TagRuleMode {
  BASIC = 'basic',
  RULE = 'rule'
}

export interface KbConstantConfig {
  key: KbConstantConfigKey;
  value: any;
}

export enum KbConstantConfigKey {
  FILE_SIZE = 'FILE_SIZE',  // 知识库内上传文件单个文件最大大小
  KB_MODEL_MANAGE_URL = 'KB_MODEL_MANAGE_URL', // 创建kooSearch知识库时，kooSearch的模型管理页面地址
  OBS_CAPACITY = 'OBS_CAPACITY', // 知识库上传的容量限制，当 -1 时则不限制
}

export enum KbShareTargetType {
  OTHER_SHARED = 'other_shared',
  MY_SHARED = 'my_shared',
}

