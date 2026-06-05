import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";
import { TreeFormItem } from "./custom-connectors-models/connectors-model";
import i18next from 'i18next';

export interface ResponseTreeData {
  statusCode: string;
  active: boolean;
  tree_payload: {
    header: TreeFormItem[];
    body: TreeFormItem[];
  };
}

@Injectable({
  providedIn: "root",
})
export class TreeDataCacheService {
  private _reqHeaderSubject = new BehaviorSubject([new TreeFormItem({ name: "header", label: "header" })]);
  private _reqQuerySubject = new BehaviorSubject([new TreeFormItem({ name: "query", label: "query" })]);
  private _reqPathSubject = new BehaviorSubject([new TreeFormItem({ name: "path", label: "path" })]);
  private _reqBodySubject = new BehaviorSubject([
    new TreeFormItem({ name: "body", label: "body", type: { label: "object" } }),
  ]);

  private static get initResData(): ResponseTreeData[] {
    return [
      {
        statusCode: i18next.t('connectorswaggerconvertservice_974'),
        active: true,
        tree_payload: {
          header: [new TreeFormItem({ name: "header", label: "header" })],
          body: [new TreeFormItem({ name: i18next.t('connectorswaggerconvertservice_974'), label: "body", type: { label: "object" } })],
        },
      },
    ];
  }

  private _resDataSubject = new BehaviorSubject<ResponseTreeData[]>(TreeDataCacheService.initResData);

  constructor() {}

  public getReqHeaderList(): Observable<TreeFormItem[]> {
    return this._reqHeaderSubject.asObservable();
  }

  public setReqHeaderList(list: TreeFormItem[]): void {
    this._reqHeaderSubject.next(list);
  }

  public getReqBodyList(): Observable<TreeFormItem[]> {
    return this._reqBodySubject.asObservable();
  }

  public setReqBodyList(list: TreeFormItem[]): void {
    this._reqBodySubject.next(list);
  }

  public getReqQueryList(): Observable<TreeFormItem[]> {
    return this._reqQuerySubject.asObservable();
  }

  public setReqQueryList(list: TreeFormItem[]): void {
    this._reqQuerySubject.next(list);
  }

  public getReqPathList(): Observable<TreeFormItem[]> {
    return this._reqPathSubject.asObservable();
  }

  public setReqPathList(list: TreeFormItem[]): void {
    this._reqPathSubject.next(list);
  }

  public getResDataList(): Observable<ResponseTreeData[]> {
    return this._resDataSubject.asObservable();
  }

  public setResDataList(data: ResponseTreeData[]): void {
    this._resDataSubject.next(data);
  }
}
