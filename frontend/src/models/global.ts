/**
 * 下拉选中框
 */
export interface SelectOption {
  /** 索引签名 */
  [key: string]: unknown;
  /** 文本 */
  label: string;
  /** 索引 */
  value?: number | string;
  /** 禁用状态 */
  disabled?: boolean;
  /** 点击事件 */
  onClick?: (event: Event, optionItem: SelectOption) => void;
}
