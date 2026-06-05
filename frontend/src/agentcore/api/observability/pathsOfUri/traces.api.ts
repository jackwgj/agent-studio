import { Observable } from "rxjs";

export default {
  postTraces: '/v1/{projectId}/api/ops-observe/traces',
  getTrace: '/v1/{projectId}/api/ops-observe/traces/{traceId}',
};

type ApiHandler<T> = (httpConfig: Partial<any>) => Observable<T>;

// 应用信息项
interface SpanInfo {
  /** trace id */
  trace_id: string;
  /** 父span id */
  parent_span_id: string;
  /** spn id */
  span_id: string;
  /** span 类型 */
  span_type: string;
  /** span名称 */
  span_name: string;
  /** 状态码 */
  status_code: string;
  /** 输入 */
  input: string;
  /** 输出 */
  output: string;
  /** 时延 */
  duration: number;
  /** 会话ID */
  session_id: string;
  /** tokens消耗 */
  tokens: number;
  /** 输入tokens */
  input_tokens: number;
  /** 输出tokens */
  output_tokens: number;
  /** 开始时间 */
  start_time: string;
  /** 触发类型 */
  call_type: string;
  /** 元数据 */
  metadata: object;
  /** 标注 */
  annotation: object;
  /** 点赞点睬, 0为未点赞，1为点赞，2为点睬 */
  vote: number;
  /** 应用id */
  resource_id: string;
  /** 应用名称 */
  resource_name: string;
  /** 应用类型 */
  resource_type: string;
  /** 模型名称 */
  model_name: string;
}

// 出参类型
interface PostTracesResponse {
  span_list: SpanInfo[];
  total: number;
}

export interface ITracesApis {
  /**
   * 获取调用链列表数据
   * @param {PostTracesRequest} request.query - 请求参数
   * @returns {Observable<PostTracesResponse>} 应用列表响应数据
    * @example
    * ```typescript
    * const query = {
    *   page_no?: number; (可选)
    *   page_size?: number,
    *   start_time?: number,
    *   end_time?: number,
    *   resource_id?: string,
    *   trace_id?: string,
    *   status_code?: string,
    * }
    *
    * postTraces({query})
    * ```
    * */
  postTraces?: ApiHandler<PostTracesResponse>

  /**
   * 获取调用链详情数据
   * @returns {Observable<PostTracesResponse>} 应用列表响应数据
    * @example
    * ```typescript
    * const params = {
    *   traceId: '1234567'
    * }
    *
    * getTrace({params})
    * ```
    * */
  getTrace?: ApiHandler<PostTracesResponse>
}
