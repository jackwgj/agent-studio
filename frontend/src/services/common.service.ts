import { Injectable } from '@angular/core';

import { environment } from 'src/environment/environment';

import { FURION_CONFIG } from '@shared/config/furionConfig';
import {HttpService} from "@services/http.service";
import { of,BehaviorSubject, Observable, Subject } from 'rxjs';
import { StorageService } from '@shared/services/cfdata.service';
import { CommonUtils } from 'src/utils/common.util';



@Injectable({
  providedIn: 'root',
})
export class CommonService {
  constructor(private http: HttpService,) { }
  public getSubscribeStatus(){
    return  true;
  }

  public getOrderValidityPeriod(){
   const _resourceInfo =  StorageService.getSessionStorage('resourceInfo');
    if (_resourceInfo && _resourceInfo[0]?.status === 'ACTIVE') {
      const cards = _resourceInfo[0];
      if (cards.expTime) {
        const curTime = new Date();
        const date1 = new Date(cards.expTime);
        const effTime = CommonUtils.daysBetweenDates(date1, curTime);
        StorageService.setSessionStorage('effTime', effTime);
        return effTime && effTime <= CommonUtils.user_set_effTime
      }
    }
    return false

  }

  public getDueStatus(){
    const _resourceInfo =  StorageService.getSessionStorage('resourceInfo');
    return  _resourceInfo && _resourceInfo[0]?.status === 'DELETED';
  }

  public getCountDownStatus(){
    const _resourceInfo =  StorageService.getSessionStorage('resourceInfo');
    return  _resourceInfo && _resourceInfo[0]?.countDownStatus;
  }

  public isPOC() {
    return false;
  }

  public isSite() {
    return true;
  }


  public isNorth4(){
    return false;
  }

  public FurionReportCustomTime() {
    let sign = true;
    const loop = () => {
      if (sign) {
        const { FURION } = window as any;
        if (FURION && typeof FURION.reportCustomTime === 'function') {
          FURION.reportCustomTime('rtti', FURION_CONFIG.appId, 1);
          sign = false;
        } else {
          setTimeout(loop, 0);
        }
      }
    };
  }
}
