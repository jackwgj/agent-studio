export interface ITinyTab {
  title: string;
  active: boolean;
}

export interface IErrorMessage {
  status: number;
  [key: string]: unknown;
}

export interface IInternalError extends IErrorMessage {
  name: string;
  data: {
    timestamp: string;
    status: number;
    error: string;
  };
}

export interface ICustomError extends IErrorMessage {
  message: string;
}

export interface IOBSCallError extends IErrorMessage {
  data: {
    error_code: string;
    error_msg: string;
  };
}

export interface IKV {
  key: string;
  value: string;
}

export interface ICFUser {
  userId: string;
  username: string;
  projectId: string;
  projectName: string;
  domainId: string;
  domainName: string;
  regionName: string;
  userRoles: string[];
  selectRegionId?: string;
  region?: string;
  workspaceId?: string;
}

export interface IFurionConfig {
  appId: string;
  setting: string;
  hashMode: boolean;
  smartJsErr: boolean;
}

/** response type 为 blob 请求返回的数据类型 */
export interface IBlobResponse extends Response {
  data: Blob;
}

export interface IBaseSelectItem {
  id: string;
  label: string;
}

export interface IBaseSelectResp {
  id: string;
  name: string;
  [key: string]: any;
}

export interface IError {
  data: {
    error_code: string;
    error_msg: string;
  };
}
