/**
 * 通过字符串类型指明转化路径
 * eg:
 * $path: 'person.age',
 */
export interface PathRule {
  $path: string;
  $rule?: Rule;
}

/**
 * 遍历数组对象，对每个对象执行规则替换
 * eg:
 * $each: { fullName: src => `${src.personal.firstName} ${src.personal.lastName}` },
 */
export interface EachRule {
  $each: Rule;
}

/**
 * 通过字符串类型指明转化路径
 * eg:
 * {
 *    id: {
 *      $path: 'pid',
 *    },
 * }
 */
export interface ObjectRule {
  [key: string]: Rule;
}

export interface RecursiveRule {
  $recurse: {
    rule: Rule; // 应用于每个节点的规则
    path?: string; // 递归访问的路径（可选）
    attr?: string; // 递归访问的属性名称（可选）
    depth?: number; // 可选的最大递归深度
  };
}

export type Rule<T = any> =
  | string
  | ((source: any, context: any) => T)
  | PathRule
  | EachRule
  | ObjectRule
  | RecursiveRule;
