import { Injectable } from '@angular/core';

/**
 * CF UI 数据获取
 */
@Injectable({
  providedIn: 'root',
})
export class CfService {

  private static __userData: any = {};
  private static __endpointsData: any = {};
  private static __regionsData: any = {};
  private static __links: any = {};

  private static __init = false;

  public static isInit() {
    return this.__init;
  }

  public static get endpointsData() {
    return this.__endpointsData;
  }

  public static get endpointCollects() {
    return CfService.endpointsData.endpointCollects;
  }

  public static get regionsData() {
    return this.__regionsData;
  }

  public static get displayRegionName() {
    return CfService.regionsData.region.displayName;
  }

  public static get userData() {
    return this.__userData;
  }

  public static get projectId() {
    return CfService.userData.projectId;
  }

  public static get projectName() {
    return CfService.userData.projectName;
  }

  public static get domainId() {
    return CfService.userData.domainId;
  }

  public static get regionId() {
    return CfService.userData.selectRegionId;
  }

  public static get roles() {
    return CfService.userData.userRoles || [];
  }

  public static get links() {
    return this.__links;
  }

  public static async init() {
    this.__init = true;
  }

  /**
  * 获取目标局点消息
  *
  * @static
  * @returns {string | null}
  * @memberof CfService
  */
  static getTargetEndpointById(id) {
    return (CfService.endpointsData?.endpoints?.allServiceEndpointList ?? []).find(endpoint => endpoint.id === id) ?? null;
  }
}
