/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable @typescript-eslint/no-unsafe-call */
/* eslint-disable @typescript-eslint/no-unsafe-member-access */
import {Injectable} from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  from,
  Observable,
  of,
  switchMap,
  throwError,
} from 'rxjs';
import {ICFUser} from '@interfaces/prompt/common.interface';
import {
  POC_JS_SESSION_KEY,
  PE_SESSION_KEY,
} from '@constants/exp-tmpl-config.const';
import {StorageService} from '@shared/services/cfdata.service';
import {environment} from 'src/environment/environment'
import {CommonService} from "@services/common.service";
import { HttpService } from '@services/http.service';

enum mapKeys {
  _USER_ID_ = '_userId_',
  USER_ID = 'userId',
}

@Injectable({
  providedIn: 'root',
})
export class ContextService {

  constructor(private readonly http: HttpService,) {
  }

  public user: ICFUser | undefined;

  public projectId = '';

  public workspaceId = '';

  public pocIsOpAccount = false;

  private PROMPT_ENGINEERING_ME: string = 'PROMPT_ENGINEERING_ME';

  public get prefix(): Readonly<string> {
    const {prefixPath} = environment;
    if (prefixPath) {
      return prefixPath;
    }

    return '/modelartsstudio-agent/rest';
  }

  /** 判断是否为op账号/承载租户 */
  public get isOpAccount() {
    if (environment.envType === 'poc') {
      return this.pocIsOpAccount;
    }

    const opPrefixes = ['op-svc', 'op_svc'];
    return Boolean(
      opPrefixes.some((prefix) => this.user?.domainName?.startsWith(prefix)),
    );
  }

  /** 判断是否为超级管理员 */
  public get isSuperAdmin() {
    return Boolean(
      this.user?.userRoles?.includes('te_admin'),
    );
  }

  public get baseUrl(): Readonly<string> {
    // projectId 为空则从 session 里恢复
    if (!this.projectId) {
      let userInfo = StorageService.getLocalStorage('PROMPT_ENGINEERING_ME');
      if (userInfo?.projectId) {
        this.user = userInfo;
        this.projectId = userInfo.projectId;
      }
      if (userInfo?.workspaceId) {
        this.workspaceId = userInfo.workspaceId;
      }
    }
    if (this.projectId === null || this.projectId === undefined || this.projectId === '') {
      this.projectId = "project_mock";
    }
    return `/v1/${this.projectId}`;
  }

  public get baseUrlWithWorkspace(): Readonly<string> {
    return `${this.baseUrl}/workspaces/${this.currentWorkspaceId}`;
  }

  public get currentWorkspaceId(): Readonly<string> {

    return this.http.getWorkspaceId()
  }

  public addUserIdToLocal(data: any) {
    const meData = StorageService.getLocalStorage(this.PROMPT_ENGINEERING_ME);
    if (meData) {
      data[mapKeys._USER_ID_] = meData[mapKeys.USER_ID] || '';
    }
  }

  public isHideLeftMenu$ = new BehaviorSubject<boolean>(false);

  public setIsHideLeftMenu(hideLeftMenu: boolean) {
    this.isHideLeftMenu$.next(hideLeftMenu);
  }

  public getIsHideLeftMenu() {
    return this.isHideLeftMenu$.value;
  }

  public isHideLeftMenuUpdate(): Observable<boolean> {
    return this.isHideLeftMenu$.asObservable();
  }

  public refreshUserData() {
    return from(this.getUserData()).pipe(
      switchMap((meData: ICFUser) => {
        this.user = meData;
        this.projectId = meData.projectId;

        // Fetch workspace_id if not already set — needed by Java Manager/Service APIs.
        if (!this.workspaceId && this.projectId) {
          return from(this.http.fetchAsyncDefaultWorkspaceId<any>({
            method: 'GET',
            url: `/v1/${this.projectId}/agent-manager/workspace`,
          })).pipe(
            catchError(() => of(null)),
            switchMap((wsRes: any) => {
              if (wsRes?.workspaceList?.length > 0) {
                this.workspaceId = wsRes.workspaceList[0].id;
                meData.workspaceId = this.workspaceId;
              }
              StorageService.setSessionStorage(PE_SESSION_KEY, meData);
              StorageService.setLocalStorage(PE_SESSION_KEY, meData);
              return of(this.user);
            }),
          );
        }

        StorageService.setSessionStorage(PE_SESSION_KEY, meData);
        StorageService.setLocalStorage(PE_SESSION_KEY, meData);
        return of(this.user);
      }),
      catchError((err) => {
        return throwError(() => err);
      }),
    );
  }

  private getUserData(): Promise<any> {
    let pocUserInfo: any = {};
    try {
      const sCopUserInfo = StorageService.getLocalStorage(POC_JS_SESSION_KEY);
      pocUserInfo = JSON.parse(sCopUserInfo || '{}');
    } catch (error) {
      pocUserInfo = {};
    }
    const {projectId, userId} = pocUserInfo;
    return new Promise((resolve) => {
      resolve({projectId, userId});
    });
  }

  /** 判断是否为F1项目需求条目 */
  public isF1ReqList(): boolean {
    return !!(window as any).service_cf3_config?.isF1Project;
  }
}
