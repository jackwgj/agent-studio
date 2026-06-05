export enum AUTH_RESULT {
  SUCCESS = 'success',
  EXCEED_LIMIT = 'exceed_limit',
  NO_NEED_TO_CREATE = 'no_need_to_create',
  FAILED = 'failed',
}

export enum AUTH_TIP_BTN {
  success = 'square_btn.know',
  exceed_limit = 'square_btn.to_create',
  no_need_to_create = 'square_btn.know',
}

export enum AUTH_TIP_TEXT {
  success = 'auth_tip.tip_1',
  exceed_limit = 'auth_tip.tip_2',
  no_need_to_create = 'auth_tip.tip_3',
}

export interface AuthRes {
  total?: number;
  data?: AuthItem[];
}

interface AuthItem {
  metadata_id: string;
  auth_type: string;
  auth_id: string;
  model_names: string;
  provider_id: string;
  auth_info: string;
  auth_url: string;
  is_record: boolean;
}

interface RequestParameter {
  type: string;
  default: number;
}

interface Version {
  id: string;
  created_at: string;
  updated_at: string;
  deleted_at: string | null;
  resident_model_id: string;
  served_model_name: string;
  scope: string;
  is_scope_exist_with_batch_infer: boolean;
  version_name: string;
  description: string;
  request_parameters: {
    temperature: RequestParameter;
    top_k: RequestParameter;
    top_p: RequestParameter;
  };
  template_id: string;
  supported_api_schemas: string[];
  batch_enable: boolean;
}

interface BillingModeForUI {
  type: string;
  zh_cn: string;
  en_us: string;
}

export interface BillingFactor {
  type: string;
  zh_cn: string;
  en_us: string;
  measure_id: number;
  usage_factor_list: string[];
}

export interface ModelItem {
  id: string;
  model_id: string;
  template_id: string;
  resident_model_id: string;
  domain_id: string;
  quota: number;
  sku_code: string;
  prompt_usage_factor: string;
  completion_usage_factor: string;
  enable_premium: boolean;
  rpm: number;
  tpm: number;
  prompt_usage: number;
  completion_usage: number;
  total_usage: number;
  quota_limit_exhausted: boolean;
  created_at: string;
  updated_at: string;
  is_reveive: boolean;
  base_url: string;
  project_id: string;
  context: Record<string, any>;
  parameters: any[];
  access_address: string;
  model_name: string;
  status: string;
  promotion_desc: string | null;
  served_model_name: string;
  service_id: string;
  allow_thinking_mode: boolean;
  default_version_id: string;
  version_list: Version[];
  default_version_name: string;
  model_type_key: string[];
  billing_mode_for_ui: BillingModeForUI;
  billing_factor_list: BillingFactor[];
  billing_desc: {
    zh_cn: string;
    en_us: string;
  };
  billing_desc_address: string;
  quota_expire_time: string | null;
  business_type: string;
  is_scope_exist_with_batch_infer: boolean;
  resource_id: string;
  moderation: boolean;
}

export interface ModelListRes {
  total: number;
  items: ModelItem[];
}

export enum ModelStatus {
  FROZEN = 'frozen',
  OPEN = 'open',
  CLOSE = 'close',
  INVALID = 'invalid',
}

export const MEASURE_ID = {
  SECOND: 6,
  THOUSAND: 109,
  PIECE: 43,
  MILLION: 111,
};

export interface ModelFactor extends BillingFactor {
  amount?: number;
  amountList?: Array<{
    amount: number;
    unit: string;
  }>;
}

export interface ModelRowData {
  id: string;
  routeId: string;
  name: string;
  templateId: string;
  data: ModelItem;
  status: ModelStatus;
  statusText: string;
  type: string;
  typeText: string;
  chargingMode: string;
  currency?: string;
  billingFactorList: ModelFactor[];
  rpm: number;
  tpm: number;
  enablePremium: boolean;
  residentModelId: string;
}
