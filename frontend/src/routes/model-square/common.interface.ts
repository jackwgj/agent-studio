import {AlgorithmEnum, SortByField} from "@routes/model-square/enums";


/**
 * 请求参数
 */
export interface QueryOptions {
  limit: number;
  offset: number;
  order: any;
  sort_by: SortByField;
  series?: string;
  model_name?: string;
  name?: string;
  model_type?: string;
  status?: string;
  model_status?: string;
  lang?: string;
  display_name?: string;
  operate_types?: string;
  display?: string;
  preset_model?: string;
  model_source?: string;
  uuid?: string;
  is_show_in_homepage?: boolean;
}


export interface ModelOption {
  service_id: string;
  label: string;
}


export interface VersionListItem {
  id?: string;
  created_at?: string;
  resident_model_id?: string;
  served_model_name?: string;
  scope?: string;
  version_name?: string;
  description?: string;
  is_scope_exist_with_batch_infer?: boolean;
  request_parameters?: any;
  supported_api_schemas?: string[];
  template_id?: string;
}

interface Context {
  is_upgrade?: string | boolean;
  model_id?: string;
  service_id?: string;
}

export interface BillingModeItem {
  type: string;
  zh_cn: string;
  en_us: string;
}

export interface amountInfoItem {
  amount: number;
  unit: string;
}

export interface BillingFactorItem {
  type: string;
  zh_cn: string;
  en_us: string;
  usage_factor_list: string[];
  measure_id: number;
  amountList?: amountInfoItem[]; // 记录价格用于展示
  millionAmountList?: amountInfoItem[]; // 记录百万tokens价格用于展示
}

// 模型部署tokens预置服务列表接口返回的types相关
export interface ListResidentUserFreeQuotaItem {
  id: string; // ID
  model_name?: string; // 服务名称（模型名称）
  model_id?: string;
  template_id?: string;
  domain_id?: string; // 用户ID
  quota?: number; // 免费额度（是否开通预置服务）
  business_type?: string;
  sku_code?: string; // sku
  quota_expire_time?: string;
  prompt_usage_factor?: string; // 输入token计费因子
  completion_usage_factor?: string; // 输出token计费因子
  enable_premium?: boolean; // 付费状态
  qpm?: number; // 调用流控
  prompt_usage?: number; // 用户消耗的输入token数
  completion_usage?: number; // 用户消耗的输出token数
  total_usage?: number; // 用户消耗的总计token数
  quota_limit_exhausted?: boolean; // 免费额度是否用完 true 代表用完
  created_at?: string; // 创建时间
  updated_at?: string; // 更新时间
  is_reveive?: boolean; // 是否领取过
  resident_model_id?: string;
  rpm?: number;
  tpm?: number;
  base_url?: string;
  project_id?: string;
  context?: Context;
  parameters?: any[];
  access_address?: string;
  status?: string; // free（enable）-免费、disable-禁用 、byToken 按token计费、 byQps 按QPS流量包计费、frozen_{reason原因} 冻结
  inputAmount?: number;
  outputAmount?: {
    default?: number;
    think?: number;
    noThink?: number;
  };
  displayStatus?: string;
  promotion_desc?: string[];
  promotionDiscount?: string;
  served_model_name?: string;
  service_id?: string;
  default_version_name?: string;
  default_version_id?: string;
  version_list?: VersionListItem[];
  allow_thinking_mode?: boolean;
  children?: ListResidentUserFreeQuotaItem[];
  isDefaultVersion?: boolean;
  parent_model_name?: string;
  isParentNode?: boolean;
  version_id?: string;
  version_name?: string;
  endpoint_name?: string;
  model_type_key?: any[];
  servedModelVersionId?: string; // 预置服务多版本下的版本ID
  desc?: string;
  billing_mode_for_ui?: BillingModeItem;
  billing_factor_list?: BillingFactorItem[];
  billing_desc?: {
    en_us: string;
    zh_cn: string;
  };
  billing_desc_address?: string;
  request_parameters?: any;
  source?: string; // 自定义接入点来源 预置服务or modelarts（推理2.0）
  is_scope_exist_with_batch_infer?: boolean;
  pay_model?: string;
  currentIndex?: number; // 显示时用来处理高亮效果的参数，前端添加
  supported_api_schemas?: string[];
  resource_id?: string;
  openContent?: string;
  model_version_id?: string;
}

export interface ListResidentUserFreeQuota {
  total?: number;
  count?: number;
  items?: ListResidentUserFreeQuotaItem[];
  default_order?: AlgorithmEnum;
  moderation_visible?: boolean;
  openContent?: string;
}


export interface FilterOption {
  label: string;
  value: string;
}

export interface FilterGroupInfo {
  subtitle?: string;
  groupType: string;
  defaultActiveValue?: string;
  options: Array<FilterOption>;
}


export interface DisplayFilterOption extends FilterOption {
  isSelect: boolean;
  groupType: string;
  value: string;
}

export interface DisplayGroupInfo {
  groupTitle: string;
  groupType: string;
  options: Array<DisplayFilterOption>;
  isMultiple?: boolean;
}

export interface ClickedOptionParams {
  groupType: string;
  value: string;
}

export interface MultiSearchParam {
  value: string[];
  groupType: string;
}


export interface SpecificationFlavor {
  flavor: string;
  matchPackage: boolean;
}

export interface Specification {
  label: string;
  value: SpecificationFlavor;
  code: string;
}

