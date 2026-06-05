import i18next from 'i18next';

export const HttpPartMeta = {
  request: "request",
  response: "response",
};

export const HttpVisibilityType = {
  none: "none",
  advanced: "advanced",
  internal: "internal",
  important: "important",
};

export const HttpPositionType = {
  path: "path",
  query: "query",
  header: "header",
  body: "body",
  model: "model",
};

export const HttpReqParamTypeType = {
  string: "string",
  integer: "integer",
  number: "number",
  boolean: "boolean",
  array: "array",
  object: "object",
};

export const HttpReqParamFormatType = {
  any: "any",
  byte: "byte",
  binary: "binary",
  date: "date",
  datetime: "datetime",
  password: "password",
  html: "html",
};

export const HttpDropDownType = {
  disable: "disable",
  static: "static",
  dynamic: "dynamic",
};

export const localStorageItem = {
  connectorId: "connectorId",
  connectorContent: "swagger-content",
  isConnectorImport: "isConnectorImport",
  connectorPage: "connectorPage",
};

export const HttpReqTriggerType = {
  webhook: "Webhook",
  polling: "polling",
};

export const isImportedConnItem = {
  y: "true",
  n: "false",
};

export const swaggerExtProp = {
  "x-is-trigger": "x-hw-trigger",
  "x-securityDefinitions": "x-hw-securityDefinitions",
  "x-security": "x-hw-security",
  "x-param-visibility": "x-hw-visibility",
  "x-param-label": "x-hw-label",
  "x-abm-id": "x-abm-id",
  "x-abm-name": "x-abm-name",
  "x-param-dropdown-options": "x-hw-select-options",
  "x-bigData": "x-hw-bigData",
  "x-is-body-required": "x-hw-body-required",
};

export const customConnColumns: Array<any> = [
  {
    title: "common.icon", // 图标
    show: undefined,
    width: "6%",
  },
  {
    title: "customConnectors.nameID",
    show: undefined,
    width: "20%",
  },
  {
    title: "customConnectors.status",
    show: true,
    width: "11%",
  },
  // 共享状态
  {
    title: "customConnectors.share_status",
    show: true,
    width: "11%",
  },
  {
    title: "customConnectors.triggerEvent",
    show: true, // undefined表示该列不参与动态显示/隐藏
    width: "9%",
  },
  {
    title: "customConnectors.exeAction",
    show: true,
    width: "8%",
  },
  {
    title: "common.flowModifyDate",
    show: true,
    width: "15%",
  },
  {
    title: "common.operation",
    show: undefined,
    width: "20%",
  },
];

export const customConnShareColumns: Array<any> = [
  {
    title: "common.icon", // 图标
    show: undefined,
    width: "6%",
  },
  {
    title: "customConnectors.nameID",
    show: undefined,
    width: "15%",
  },
  {
    title: "customConnectors.triggerEvent",
    show: true, // undefined表示该列不参与动态显示/隐藏
    width: "10%",
  },
  {
    title: "customConnectors.exeAction",
    show: true,
    width: "10%",
  },
  {
    title: "common.flowModifyDate",
    show: true,
    width: "12%",
  },
  {
    title: "common.operation",
    show: undefined,
    width: "13%",
  },
];

/**
 * 连接器相关操作
 */
export enum OperatorTypeEnum {
  // 创建我的连接器
  createConnector = "createConnector",
  // 复制到我的连接器
  copyToConnector = "copyToConnector",
  // 从 open api 创建
  byOpenAPiCreate = "byOpenAPiCreate",
  // 编辑
  editConnector = "editConnector",
  // 发布
  publish = "publish",
  // 共享
  share = "share",
  // 创建触发事件
  createTrigger = "createTrigger",
  // 触发事件编辑
  editTrigger = "editTrigger",
  // 创建执行动作
  createAction = "createAction",
  // 执行动作编辑
  editAction = "editAction",
  // 从 open api 更新
  byOpenApiUpdate = "byOpenApiUpdate",
  // 复制连接器
  copy = "copy",
  // 下载连接器 swagger 文档
  download = "download",
  // 创建连接
  createConnect = "createConnect",
  // 编辑连接
  editConnect = "editConnect",
  copyConnect = "copyConnect",
  // 删除连接
  deleteConnect = "deleteConnect",
  // 创建公共连接器
  createCommonConnector = "createCommonConnector",
  delete = "delete",
  deleteShare = "deleteShare",
  copyConnectorSingle = "copyConnectorSingle",
  testConnect = "testConnect",
}

// 我的连接器操作
export const ActionMenuItems: Array<any> = [
  {
    label: "common.edit",
    association: "switch",
    type: OperatorTypeEnum.editConnector,
  },
  {
    label: "customConnectors.publish",
    association: "switch",
    type: OperatorTypeEnum.publish,
  },
  {
    label: i18next.t('connectorsconst_993'),
    association: "switch",
    type: OperatorTypeEnum.share,
  },
  {
    label: "customConnectors.createTriggerEvent",
    type: OperatorTypeEnum.createTrigger,
  },
  {
    label: "customConnectors.createExeAction",
    type: OperatorTypeEnum.createAction,
  },
  {
    label: "customConnectors.importOpenApiUpdate",
    type: OperatorTypeEnum.byOpenApiUpdate,
  },
  {
    label: "customConnectors.download",
    type: OperatorTypeEnum.download,
  },
  {
    label: "customConnectors.createConnect",
    type: OperatorTypeEnum.createConnect,
  },
  {
    label: "customConnectors.createCommonConnector",
    type: OperatorTypeEnum.createCommonConnector,
  },
  {
    label: "customConnectors.copyConnectorSingle",
    type: OperatorTypeEnum.copyConnectorSingle,
  },
  {
    label: "common.delete",
    type: OperatorTypeEnum.delete,
  },
];

// 我的公共连接器的操作提示，电子流状态
export const elecStatus: Array<any> = [
  {
    key: "onboard_to_be_verified",
    value: i18next.t('connectorsconst_994'),
  },
  {
    key: "onboard_verified",
    value: i18next.t('connectorsconst_995'),
  },
  {
    key: "onboard_audited",
    value: i18next.t('connectorsconst_996'),
  },
  {
    key: "onboard_withdraw",
    value: i18next.t('connectorsconst_997'),
  },
  {
    key: "onboard_verified_reject",
    value: i18next.t('connectorsconst_998'),
  },
  {
    key: "onboard_audited_reject",
    value: i18next.t('connectorsconst_999'),
  },
  {
    key: "off_to_be_verified",
    value: i18next.t('connectorsconst_1000'),
  },
  {
    key: "off_verified",
    value: i18next.t('connectorsconst_1001'),
  },
  {
    key: "off_audited",
    value: i18next.t('connectorsconst_1002'),
  },
  {
    key: "off_withdraw",
    value: i18next.t('connectorsconst_1003'),
  },
  {
    key: "off_verified_reject",
    value: i18next.t('connectorsconst_1004'),
  },
  {
    key: "off_audited_reject",
    value: i18next.t('connectorsconst_1005'),
  },
];

export enum ConnectorStatusEnum {
  unavailable = "unavailable", // 不可用 (触发事件执行动作均为0)
  dev = "dev", // 待发布(草稿)
  released = "released", // 已发布
  onboard = "onboard", // 已上架
}

export enum AuthTypeEnum {
  none = "none",
  basic = "basic",
  apikey = "apiKey",
  oauth = "oauth",
  iam = "iam",
  aksk = "aksk",
  custom = "custom_auth",
  iamAgency = "iam-agency",
  mixedCustom = "custom",
  indestyModel = "indestyModel",
}

export enum ApiKeyPositionEnum {
  HEADER = "header",
  QUERY = "query",
}

export enum OAuthTypeEnum {
  // 授权码
  authorizationCode = "accessCode",
  // 客户端
  clientCredentials = "application",
}

export const HttpIntroductoryI18nConfig = {
  header: i18next.t('connectorsconst_1006'),
  body: i18next.t('createtool_783'),
  query: i18next.t('",'),
  path: i18next.t('connectorsconst_1009'),
};

export type RESTFulMethodType = "get" | "delete" | "post" | "put" | "patch";

export const UrlMethodList: { label: string; value: RESTFulMethodType }[] = [
  { label: "GET", value: "get" },
  { label: "DELETE", value: "delete" },
  { label: "POST", value: "post" },
  { label: "PUT", value: "put" },
  { label: "PATCH", value: "patch" },
];

export enum HttpTabsTypeEnum {
  header = "header",
  body = "body",
  query = "query",
  path = "path",
}

export interface ConnCreateValidatorType {
  [key: string]: {
    reg: RegExp;
    errTips: string;
  };
}

export const ConnCreateRegValidator: ConnCreateValidatorType = {
  title: {
    reg: /^(?!_)(?![\s()])(?!-)[a-zA-Z\d_\-()\u4e00-\u9fa5]{1,64}$/,
    errTips: "customConnectors.nameNotSpecialCharSpace",
  },
  description: {
    reg: /^(?!_)(?![\s()])(?!-)[\u4e00-\u9fa5a-zA-Z\d_\s\-,，.。:：;；()（）、\\!！&*#]{0,512}$/,
    errTips: "customConnectors.desNotSpecialCharSpace",
  },
  host: {
    reg: /^[a-zA-z\d\u4e00-\u9fa5_.\-:]+$/,
    errTips: "customConnectors.hostNotSpecialCharSpace",
  },
};

export type TabInputParamType = "header" | "body" | "query" | "path" | "form-data";

export type TreeInputParamsType = "string" | "number" | "boolean" | "integer" | "array" | "object";

export enum FormatAttrsEnum {
  INPUT = "input", // 文本框
  DATE_TIME = "date_time", // 日期时间
  RICH_EDITOR = "richEditor", // 富文本
  DROPDOWN = "dropdown", // 下拉列表
  MUTIDROPDOWN = "mutidropdown", // 下拉列表
}

export enum ConnectorDetailTabEnum {
  BASIC = "BASIC", // 基本信息
  TRIGGER = "TRIGGER", // 触发事件
  ACTION = "ACTION", // 执行动作
  WATCH = "WATCH", // 监控
}

export enum ConnectorTabEnum {
  myConnector = "myConnector", // 我的连接器
  shareConnector = "shareConnector", // 共享连接器
  myCommonConnector = "myCommonConnector", // 我的公共连接器
}

export enum FlowTabEnum {
  myFlow = "myFlow", // 我的流模板
  shareFlow = "shareFlow", // 共享流模板
  myCommonFlow = "myCommonFlow", // 我的公共流模板
}

export const SWAGGER_TREE_NODE_MAX_LEVEL = 12;
