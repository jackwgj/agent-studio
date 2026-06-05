/** 将一段 prompt 按下面三种块分类 */
export enum EChunkType {
  /** 模板变量 */
  VARIANT = 'VARIANT',

  /** 普通字符串 */
  COMMON = 'COMMON',

  /** prompt 末尾处以 {{ 开头的字符串 */
  SNIPPET = 'SNIPPET',
}

/** 发起变量同步的消息源 */
export enum EVarSyncSrc {
  /** 表示源来自变量定义面板 */
  VAR_DEF = 'VAR_DEF',

  /** 表示源来自变量输入面板 */
  VAR_INPUT = 'VAR_INPUT',
}

export enum EModelQuerySource {
  PLAYGROUND = 'PLAYGROUND',
  CANDIDATE = 'CANDIDATE',
}

/** 撰写prompt时，确认按钮的不同状态 */
export enum EConfirmBtnStatus {
  /** 使用popConfirm指令的确认按钮可见 */
  CONFIRMVISIBLE_WITHPOP = 'CONFIRMVISIBLE_WITHPOP',

  /** 不使用popConfirm指令的确认按钮可见 */
  CONFIRMVISIBLE_WITHOUTPOP = 'CONFIRMVISIBLE_WITHOUTPOP',

  /** 确认按钮隐藏 */
  CONFIRMHIDDEN = 'CONFIRMHIDDEN',
}
