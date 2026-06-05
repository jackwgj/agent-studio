export interface IResponse {
  code: number;
  message: string;
  data: any;
}

export interface RefResponse {
  code: number;
  message: string;
  body: Map<string, number>;
}

// 插件入参
export interface RequestArguments {
  name: string;
  description: string;
  type: string;
  method: string;
  required: boolean;
  default_value: string;
}

// 插件出参
export interface ResponseArguments {
  name: string;
  description: string;
  type: string;
  required: boolean;
}

export interface PluginDetail {
  name: string;
  description: string;
  url: string;
  method: string;
  headers?: any;
  logo?: string;
  auth?: any;
  arguments?: any;
  response?: any;
}

export interface PluginListResponse {
  message: string;
  data: PluginInfo[];
  total: number;
  code: number;
}

export interface PluginInfo {
  pluginDesc: string;
  pluginId: number;
  pluginLogo: string;
  pluginName: string;
  publisher: string;
  status: string;
}

export interface ApiKeyAuthArguments {
  name: string;
  value: string;
}

export interface UserAuthArguments {
  name: string;
  sourceName: string;
}
