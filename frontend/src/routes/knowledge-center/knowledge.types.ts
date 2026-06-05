export interface KnowledgeChunk {
  title: string;
  content: string;
  id?: string;
  page_num?: number;
  component_num?: number;
  timestamp?: string;
}

export interface PaginationQuery {
  offset: number;
  limit: number;
}

export interface KnowledgeChunkRes {
  file_chunk_list: KnowledgeChunk[];
  total_count: number;
}

export interface KnowledgeFaq {
  question: string;
  answer: string;
  question1?: string;
  question2?: string;
  question3?: string;
  question4?: string;
  create_time?: string;
  update_time?: string;
  faq_id?: string;
}

export interface FaqListQuery extends PaginationQuery {
  question?: string;
  answer?: string;
}

export interface FaqListRes {
  faq_list: KnowledgeFaq[];
  count: number;
}

export interface SegmentRule {
  id?: string;
  rule_regexs: Array<string>;
}

export interface SegmentRuleRes {
  rule_list: SegmentRule[];
  count: number;
}

export enum TaskType {
  RETRY_FILES = 'RETRY_FILES',
  RETRY_ALL_FILES = 'RETRY_ALL_FILES',
  QA = 'QA',
}

export interface TaskReq {
  task_type: TaskType;
  file_ids?: string[];
}

export enum ModalType {
  CREATE = 'create',
  EDIT = 'edit',
  ADVANCED = 'advanced',
}

export enum SplitMode {
  AUTO = 'AUTO',
  LENGTH = 'LENGTH',
  LEVEL = 'LEVEL',
  CATALOG = 'CATALOG',
  RULE = 'RULE',
}

export interface TaskQuery extends PaginationQuery {
  task_type?: string;
  task_status?: string;
  file_name?: string;
}

export interface DeleteTaskParams {
  task_ids: string[];
}

export interface TaskInfo {
  id: string;
  name: string;
  type: string;
  status: string;
  process: string;
  create_time: string;
  task_desc: string;
}

export interface TaskListRes {
  tasks: TaskInfo[];
  total: number;
  fail_count: number;
  success_count: number;
  running_count: number;
  pending_count: number;
}

export interface BatchDeleteRes {
  total_count: number,
  deleted_count: number,
}

export enum KBScope {
  GLOBAL = 'GLOBAL',
  SELF = 'SELF',
  ALL = 'ALL', // 查询自己和的他人共享的
  PARTIAL = 'PARTIAL',
}

export interface ConnectionUiData {
  name: string;
  code: string;
  type?: ConnectionFormType;
  placeholder?: string;
}

export enum ConnectionUiType {
  STRING = 'STRING',
  SECRET = 'SECRET',
}

export enum ConnectionFormType {
  INPUT = 'INPUT',
  SECRET = 'SECRET',
}

export enum KBRepoType {
  SHARE = 'share',
}

export interface TagRule {
  basic: AdvancedRuleNode;
  advancedRules: AdvancedRuleNode;
}

export interface AdvancedRuleNode {
  type: 'GROUP' | 'RULE';
  operator?: 'OR' | 'AND' | 'NOT';
  value?: string;
  children?: AdvancedRuleNode[];
}

export interface KbTagListRes {
  count?: number;
  tag_list: KbTagItem[];
}

export interface KbTagQueryParams {
  offset: number;
  limit: number;
}

export interface KbTagItem {
  name: string;
  id?: string;
  color?: KbTagColor;
  create_time?: string;
}

export interface KbTagCreation {
  name: string;
  color: string;
}

export interface KbTagCreationRes {
  tag_id: string;
}

export interface KbFileUpdateParams {
  tags: string[];
}

export enum KbTagColor {
  red = 'red',
  yellow = 'yellow',
  green = 'green',
  blue = 'blue',
  purple = 'purple',
  grey = 'grey'
}
