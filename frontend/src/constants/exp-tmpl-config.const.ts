import { IModelParamInfo } from '@interfaces/prompt/model.interface';

/** 匹配形如 {{val}} 形式的子串 */
export const VARIANT_REG = /({{.*?}})/g;

/** 匹配一段字符串中末尾处形如 {{val 形式的子串 */
export const SNIPPET_REG = /{{[\w]*$/g;

/** 模板变量背景色 */
export const VARIANT_BG_COLORS = [
  '#c4e1ff',
  '#d7cbfd',
  '#c4f2ff',
  '#bbf2c8',
  '#daf3bc',
  '#eef5bf',
  '#fcf6c2',
  '#fcefc2',
  '#fcdeb8',
  '#e4c6fc',
];

/** 变量未定义时的背景色 */
export const VARIANT_BG_COLOR_UNDEFINED = '#cccccc';

/** 模板变量标示色 */
export const VARIANT_MARK_COLORS = [
  '#4190fc',
  '#6f50e0',
  '#58cbf2',
  '#24c251',
  '#7dcc27',
  '#c7de3d',
  '#f9de4d',
  '#f6c844',
  '#fe992e',
  '#9f51e8',
];

/** 变量未引用时的标示色 */
export const VARIANT_MARK_COLOR_UNREF = '#cccccc';

/** 变量未定义时的标示位 */
export const VARIANT_UNDEFINED_POS = -1;

/** 变量未引用时的标示位 */
export const VARIANT_UNREF_POS = -1;

/** 变量名称错误时的标示位 */
export const VARIANT_NAME_ERROR_POS = -2;

/** 变量名称错误时的标示色 */
export const VARIANT_NAME_ERROR_COLOR = '#f2303019';

/** 模型参数固定属性 */
export const MODEL_PARAMS_INFO_MAP: Map<string, IModelParamInfo> = new Map([
  [
    'temperature',
    {
      name: 'temperature',
      value: 0,
      min: 0,
      max: 1,
      mid: 0.5,
      step: 0.1,
      default: 0.5,
    },
  ],
  [
    'top_p',
    {
      name: 'top_p',
      value: 0,
      min: 0.1,
      max: 1,
      mid: 0.5,
      step: 0.1,
      default: 0.5,
    },
  ],
  [
    'max_tokens',
    {
      name: 'max_tokens',
      value: 16,
      min: 1,
      max: 2048,
      mid: 1024,
      step: 1,
      default: 2048,
    },
  ],
  [
    'presence_penalty',
    {
      name: 'presence_penalty',
      value: -2,
      min: -2,
      max: 2,
      mid: 0,
      step: 0.1,
      default: 0,
    },
  ],
]);

export const TOP_P_SCALES = [0.1, 0.4, 0.7, 1];

/** 变量定义上限 */
export const MAX_VAR_NUM = 20;

export const VARIANT_PADDING_X = '4px';

export const CANDIDATE_LIMIT = 9;

/** 形如 {{val}} 形式的子串，获取 val */
export const GETVARIANT_REG = /\{\{(.*?)\}\}/g;
/** 撰写Prompt question中，变量命名的正则检查 */
export const CHECKVARIABLENAME_REG = /^[a-zA-Z][a-zA-Z0-9_]{0,9}$/;
/** 模板名称的正则检查，对应第4条 */
export const TEMPLATENAME_REG =
  /^[A-Za-z\u4E00-\u9FA5](?:[\w\u4E00-\u9FA5-]{0,18}[\dA-Za-z\u4E00-\u9FA5])$/;
/** 事件看板中，订阅地址的正则检查 */
export const SUBSCRIBE_ADDRESS_REG =
  /^(https?):\/\/[-A-Za-z0-9+&@*#/%?=~_|!:,.;]+[-A-Za-z0-9+&@*#/%=~_|]$/;
/** 事件看板中，请求头的属性名的正则检查 */
export const HEADERKEY_REG = /^[a-zA-Z]+[a-zA-Z0-9\\-]*(?<!-)$/;
/** 事件看板中，订阅者名称的正则校验 */
export const SUBSCRIBE_NAME_REG = /^[\u4e00-\u9fa5-_a-zA-Z0-9]{2,64}$/;

export const EDITOR_BORDER_STYLE = '2px solid #c2c2c2';
export const EDITOR_BORDER_FOCUS_STYLE = '2px solid #1476ff59';
export const EDITOR_BORDER_ERROR_STYLE = '2px solid #F2303033';

export const EDITOR_BG_COLOR = '#fff';
export const EDITOR_BG_CONFIRM_COLOR = '#d0d0d0';
export const EDITOR_BG_ERROR_COLOR = '#F230300D';

// 变量值长度限制
export const VAR_VALUE_LEN = 1000;
export const SPACE_VAL = ' ';

export const PE_SESSION_KEY = 'PROMPT_ENGINEERING_ME';

// 用于存放cop类型的用户信息，且信息要适配cui默认用户信息
export const POC_JS_SESSION_KEY = 'POC_JS_SESSION_KEY';

export const DEFAULT_FONT_STYLE =
  '16px Helvetica, Arial, PingFangSC-Regular, "Hiragino Sans GB", "Microsoft YaHei", 微软雅黑, "Microsoft JhengHei"';

export const DEFAULT_MAX_TOKENS = 4096;

export const XLSX_FILE_TYPE =
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';

export const MCP_SERVER_ADDRESS =
  /^(?<protocol>(https|http):\/\/)(?<number>[0-9a-z.]+)/i;

export const MCP_SERVER_PARAM_VALUE = /^[^\s](.*[^\s])?$/;
