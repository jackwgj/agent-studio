import { KbItem } from '@routes/knowledge-center/knowledge-base.interface';
import { KBScope, KbTagItem } from '@routes/knowledge-center/knowledge.types';

export interface IKnowledgeCreateReq {
  display_name: string;
  description: string;
  icon: string;
}

export interface IKnowledge extends KbItem{
  knowledge_repo_id: string;
  display_name: string;
  size: number;
  creator: string;
  create_time: string;
  update_time: string;
  remain_size?: number;
  max_size?: number;
  update_user_name?: string;
  rag_chunk_parser_conf?: any;
  workspace_id?: string,
}

export interface IKbListQuery {
  offset?: number;
  limit?: number;
  type?: 'external' | 'internal';
  id?: string;
  name?: string;
}

export interface IListKnowledgeResp {
  count: number;
  knowledge_repo_list: IKnowledge[];
  max_size: number;
  total_size: number;
}

export interface IKnowledgeSource {
  knowledge_base_connection_id: string;
  knowledge_base_connector_id: string;
  knowledge_base_connection_name: string;
  detail_page_url: string;
}

export interface IKnowledgeNew {
  knowledge_base_id: string;
  icon: string;
  type: string;
  name: string;
  description: string;
  status: string;
  created_user_id: string;
  created_user_name: string;
  last_update_user_id: string;
  last_update_user_name: string;
  create_time: string;
  update_time: string;
  source?: IKnowledgeSource;
}

export interface IListKnowledgeRespNew {
  total: number;
  items: IKnowledgeNew[];
}

export interface IFile {
  file_id: string;
  file_name: string;
  file_type: string;
  file_size: number;
  file_status: string;
  create_time: string;
  update_time: string;
  file_tags?: string[];
}
export interface IListFilesResp {
  count: number;
  file_info_list: IFile[];
}

export interface IKbQueryBase {
  offset: number;
  limit: number;
  knowledge_repo_id: string;
  name?: string;
  type?: string;
  status?: string;
}

export interface IFileChunkReq extends IKbQueryBase {
  file_id: string;
}

export interface IFileChunk {
  id: string;
  content: string;
  title: string;
  page_num: number;
  component_num: number;
}

export interface IFileChunkListRsp {
  count: number;
  file_chunk_list: any;
}

export interface ISearchKbHTReq extends IKbQueryBase {
  query: string;
  search_mode?: string;
}

export interface IKbSearch {
  file_id: string;
  chunk_id: string;
  repo_id: string;
  source: string;
  title: string;
  content: string;
  update_date_time: string;
  doc_type: string;
  file_path: string;
  category: string;
  score: number;
  subtitle: string;
  image_paths?: any;
  image?: any;
  kb_type?: any;
}

export interface IKnowledgeSearchRsp {
  total: number;
  search_result_list: IKbSearch[];
}

export interface IKbSearchRecord {
  id: string;
  query: string;
  create_time: string;
}

export interface IKbSearchRecordRsp {
  count: number;
  KnowledgeSearchRecord: IKbSearchRecord[];
}

export interface ICreateSegmentRuleReq {
  rule_regexs: string[];
}

export interface IKnowledgeDefaultConfigConnection {
  connector_id: string;
  params: Array<{ code: string; value: string }>;
  changed?: boolean;
}
