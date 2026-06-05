import { TemplateRef } from "@angular/core";
import { SafeUrl } from "@angular/platform-browser";
import {
  ApiKeyPositionEnum,
  AuthTypeEnum,
  ConnectorStatusEnum,
  FormatAttrsEnum,
  HttpTabsTypeEnum,
  TreeInputParamsType,
} from "./connectors-const";

export interface CustomConnectorsListMeta {
  id: string;
  name: string;
  description: string;
  createtime: string;
}

export interface HttpBasicInfoMeta {
  summary: string;
  description: string;
  operationId: string;
  visibility: string;
  triggerType: string;
  triggerConfig: any;
  produces: string[];
  consumes: string[];
}

export interface HttpRequestUrlAttachMeta {
  name: string;
  summary?: string;
  position?: string;
  description?: string;
  label?: string;
  defaultValue?: string;
  isNecessary: boolean;
  visibility: any;
  type: any;
  format?: string;
  dropdownType?: string;
  dropdownOptions?: any[];
}

export interface SwaggerReqUrlAttachMeta {
  name: string;
  in: string;
  description: string;
  required: boolean;
  type: string;
  format: string;
}

export type HttpRequestHeaderMeta = HttpRequestUrlAttachMeta;
export type SwaggerReqHeaderMeta = SwaggerReqUrlAttachMeta;

export interface HttpResponseBodyMeta {
  title: string;
  description: string;
  visibility: string;
  type: string;
  format: string;
}

export interface HttpAuthMeta {
  authType: string;
  config: any;
  backend_auth?: any;
}

export interface HttpBasicInfoEditMeta {
  version: string;
  title: string;
  description: string;
}

export interface ABMEditMeta {
  isAbm: boolean;
  modelName?: string;
  modelId?: string;
}

export type AlertTypeString = "success" | "error" | "warn" | "prompt" | "simple";

export interface AlertConfig {
  type: AlertTypeString;
  open: boolean;
  label: string;
  dismissOnTimeout?: number;
}

export interface FunctionAlertConfig {
  type: AlertTypeString;
  open: boolean;
  label: string;
  name: string;
  reason?: string;
}

export interface ConnectorStatusItem {
  icon: string;
  color: string;
  text: string;
  tpl: TemplateRef<any>;
}

export interface ApiKeyParamsPosition {
  label: string;
  type: ApiKeyPositionEnum;
}

export type TriggerEventStepType = "basicInfoStep" | "requestStep" | "responseStep" | "pollingStep" | "flowStep";

export interface TriggerEventStep {
  id: TriggerEventStepType;
  label: string;
  disabled?: boolean;
}

export interface HttpTabItem {
  title: string;
  active: boolean;
  type: HttpTabsTypeEnum;
}

export class SwaggerBasic {
  swagger: string;
  info: {
    version: string;
    title: string;
    description: string;
    // 扩展字段
    [key: string]: string;
  };
  schemes: Array<"http" | "https">;
  host: string;
  basePath: string;
  canModifyHostAddress?: boolean;
}

export class SwaggerModel extends SwaggerBasic {
  [key: string]: any;

  tags?: Array<{ name: string }>;
  paths?: any;
  securityDefinitions?: any;
  security?: any;
}

export class ConnectorAuthContent {
  auth_config: { [key: string]: any };
  auth_dynamic: { [key: string]: any };
  auth_info: { [key: string]: any };
  auth_prop: { [key: string]: any };
  auth_type: AuthTypeEnum;
  host_config?: {
    canModifyHostAddress?: boolean;
    host?: string;
  };
  cdm_params_config?: {
    cdmVisibility?: boolean;
    cdmParams?: string;
  };
}

/**
 * 列表页连接器响应模型
 */
export interface ConnectorItem {
  action_count: number;
  create_time: string;
  description: string;
  icon: string;
  iconSafeUrl?: SafeUrl;
  id: string;
  name: string;
  status: ConnectorStatusEnum;
  trigger_count: number;
  type: string;
  updated_time: string;
  basic?: any;
  version: string;
  share_status: any;
}

/**
 * 创建连接请求模型
 */
export class CreateConnectionReq {
  auth_config_id: string;
  auth_dynamic: {
    code: string;
  };
  // 基本信息
  description?: string;
  connection_name: string;
  connector_id: string;
  // 验证信息
  auth_prop: { [key: string]: string };
  auth_type: string;
}

/**
 * 创建连接器请求模型
 */
export class CreateConnectorReq {
  auth_content?: ConnectorAuthContent;
  icon: any;
  swagger: SwaggerModel;
}

/**
 * 单个连接器响应
 */
export class ConnectorDetailRes {
  auth_content: ConnectorAuthContent;
  created_time: string;
  description: string;
  icon: any;
  id: string;
  name: string;
  status: ConnectorStatusEnum;
  swagger: SwaggerModel;
  type: string;
  updated_time: string;
}

/**
 * @description swagger tree 平面结构 item
 */
export class TreeFormItem {
  name: string = "";
  pId: number = -1;
  isShow: boolean = true;
  isNecessary: boolean = true;
  level: number = 0;
  _treeId: number = 0;
  expand: boolean = true;
  hasChildren: boolean = false;
  type: { label: TreeInputParamsType } = { label: "string" };
  label: string = "";
  description?: string;
  isArrayChildNode?: boolean;
  cdmName?: string;
  // 高级中配置下的属性
  format?: { label: string; value: FormatAttrsEnum };
  isExtend?: boolean;
  default?: string;
  ["x-hw-label"]?: string;
  ["x-hw-visibility"]?: string;
  visibility: "none" | "advanced" | "internal" | "important" = "none";
  ["x-hw-select-options"]?: { label: string; value: string }[] = [];
  defaultCodeUrl: boolean;
  isMutiSelect: boolean;
  splitSign: string;
  xmlName?: string;
  defaultInput?: boolean;

  defaultValue?: any;

  constructor(data: Partial<TreeFormItem>) {
    Object.keys(data).forEach(key => {
      this[key] = data[key];
    });
  }
}

export interface ResponseTreeData {
  statusCode: string;
  active: boolean;
  tree_payload: {
    header: TreeFormItem[];
    body: TreeFormItem[];
  };
}

/**
 * EditJson 上下文
 */
export interface EditJsonContextData {
  value: string; // swagger json 片段
  modalTitle: string; // title 国际化 字符串
  backData: any;
}

/**
 * 行业 应用系统 子系统 实体
 */
export enum INDUSTRY_DAM_ENUM {
  SUBJECT_GROUP = "SUBJECT_GROUP",
  SUBJECT_AREA = "SUBJECT_AREA",
  BUSINESS_OBJECT = "BUSINESS_OBJECT",
  LOGICAL_ENTITY = "LOGICAL_ENTITY",
}

/**
 * 获取 abm 行业模型数据 请求体
 */
export interface QueryAbmIndustryModel {
  searchWords?: string;
  offset?: number;
  limit?: number;
}

/**
 *
 */
export const INDUSTRY_DAM_LIST = [
  INDUSTRY_DAM_ENUM.SUBJECT_GROUP,
  INDUSTRY_DAM_ENUM.SUBJECT_AREA,
  INDUSTRY_DAM_ENUM.BUSINESS_OBJECT,
  INDUSTRY_DAM_ENUM.LOGICAL_ENTITY,
];
