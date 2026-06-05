export interface IEnvironment {
  prefixPath: string;
  jumpObsPath: string;
  envType: 'hc' | 'hcs' | 'hcso' | 'poc' | 'site' | 'icsl';
  serviceType: 'dev' | 'test' | 'prod';
  toolAPIServiceId: string;
  toolAPIName: string;
  serviceName: any;
  userEnv?: '';
  env203?: 'dev' | 'test';
  websocketConnectRelay?: boolean;
  serviceId?: string;
}
