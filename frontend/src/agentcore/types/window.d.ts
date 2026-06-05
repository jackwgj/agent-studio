interface Window {
  app_cookie_prefix?: string;
  CDNAddress?: string;
  getConsoleContext: Function;
  agentCore?: {
    supportNoMapping: boolean;
    supportIam5Service: {
      [key: string]: string[]; // region数组，支持ALL字段（不区分大小写）
    };
  };
  arsI18n?: any;
  __resourceStatus?: boolean;
  monaco?: any;
}
