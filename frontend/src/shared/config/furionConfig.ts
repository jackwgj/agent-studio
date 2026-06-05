interface IFurionConfig {
  appId: string;
  setting: string;
  hashMode: boolean;
  smartJsErr: boolean;
}

export const FURION_SCRIPT_ID = 'furion-script';

export const FURION_SDK_URL =
  'https://res.hc-cdn.com/FurionSdkStatic/3.6.34/furion-cdn.min.js';

export const FURION_SDK_NAME = '__fr';

export const FURION_CONFIG: IFurionConfig = {
  appId: '2C1F16A4B335454FB79E5B99AC81A143',
  setting: 'api,jsTrack,uba,longtask,rtti',
  hashMode: true,
  smartJsErr: false,
};
