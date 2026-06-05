/**
 * 服务ID，根据服务LandingZone上线情况，如果大部分局点都上线了，此处使用iam5的服务ID即可，否则使用iam3的并针对部分接口使用LandingZoneCheck装饰器进行适配
 */
export const enum ServiceId {
  SWR = 'swr',
  IAM = 'iam',
  LTS = 'lts-iam5',
  JIUWEN = 'jiuwen-console',
}
