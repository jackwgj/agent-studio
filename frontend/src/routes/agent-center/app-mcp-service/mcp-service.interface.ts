export interface ServiceDetail {
  id?: string;
  icon?: string;
  name: string;
  name_en?: string;
  desc?: string;
  visibility: string;
  is_template?: boolean;
  creator?: string;
  creator_id?: string;
  config_status?: string;
  auth?: any;
  url?: string;
  type?: string;
  update_time?: string;
  create_time?: string;
  tools?: ToolsInfo;
}

export interface ToolsInfo {
  count?: number;
  tools?: any[];
}

export interface AuthArguments {
  name: string;
  desc?: string;
  value: string;
}

export interface Query {
  offset: number;
  limit: number;
}
