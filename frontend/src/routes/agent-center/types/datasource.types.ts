export interface IDatasourceList {
  total_count?: number;
  count?: number;
  datasource_list: IDatasourceItem[];
}

export interface IDatasourceItem {
  id: string;
  name: string;
  desc: string;
  type: string;
  internet_access: string;
  status: string;
  error_message: string;
  created_by: string;
  creator_id: string;
  created_on: string;
  updated_by: string;
  updater_id: string;
  updated_on: string;
}

export interface IDatasourceDetail {
  id: string;
  name: string;
  description: string;
  type: string;
  internet_access: string;
  status: string;
  error_message: string;
  creator: string;
  creator_id: string;
  create_time: string;
  updater: string;
  updater_id: string;
  update_time: string;
  connection_info: IConnectionInfo;
}

export interface IConnectionInfo {
  host: string;
  port: string;
  ssl_enabled: boolean;
  database_name: string;
  user: string;
  password: string;
}

export interface IModifyDatasourceBody {
  name: string;
  desc: string;
  type: string;
  internet_access: string;
  connection_info: IConnectionInfo;
}
