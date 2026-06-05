import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SpaceTeamManagementService {
  // 更新空间选项
  private dataSubject = new BehaviorSubject<any>(null);
  data$ = this.dataSubject.asObservable();
  // 更新空间
  private space = new BehaviorSubject<any>(null);
  space$ = this.space.asObservable();

  private spaceOptions = new BehaviorSubject<any>([]);
  spaceOptions$ = this.spaceOptions.asObservable();

  updateSpaceOptionsSelected(newData: Object) {
    this.dataSubject.next(newData);
  }
  updateSpace(newData: string) {
    this.space.next(newData);
  }
  updateSpaceOptions(newData) {
    this.spaceOptions = newData;
  }

  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }
  get cur_workspace_id() {
    return this.ctxServ.currentWorkspaceId;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  public getWorkspace(): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/workspace/init`,
    });
  }
  public postWorkspace(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/workspace`,
      params,
    });
  }
  public putWorkspace(params): Promise<any> {
    return this.http.putAsync({
      url: `${this.prefix}/workspace`,
      params,
    });
  }
  public deleteSapceFn(params): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/workspace`,
      params,
    });
  }
  //获取项目空间详情
  public getWorkspaceDetial(workspace_id: string): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/workspace/${workspace_id}`,
    });
  }
  // 查询项目空间成员列表
  public getSpaceMembers(params: any) {
    return this.http.getAsync({
      url: `${this.prefix}/workspace/member`,
      query: {
        ...params,
        workspace_id: this.cur_workspace_id,
      },
    });
  }
  //批量添加项目空间成员
  public addSpaceMembers(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/workspace/member`,
      params,
      query: {
        workspace_id: this.cur_workspace_id,
      },
    });
  }
  //批量移除项目空间成员
  public deleteSpaceMembers(params): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/workspace/member`,
      params,
      query: {
        workspace_id: this.cur_workspace_id,
      },
    });
  }
  //修改项目空间成员角色
  public editSpaceMembers(params): Promise<any> {
    return this.http.patchAsync({
      url: `${this.prefix}/workspace/member`,
      params,
      query: {
        workspace_id: this.cur_workspace_id,
      },
    });
  }
  //查询项目空间中指定成员的详情
  public getSpaceMemberDetail(user_id) {
    return this.http.getAsync({
      url: `${this.prefix}/workspace/members/${user_id}`,
      query: {
        workspace_id: this.cur_workspace_id,
      },
    });
  }
  //获取团队空间下的角色列表
  public getSpaceMemberRoles() {
    return this.http.getAsync({
      url: `${this.prefix}/workspace/member/role`,
    });
  }
  //获取当前租户下面的用户列表
  public getAllUsers() {
    return this.http.getAsync({
      url: `${this.prefix}/workspace/users`,
      query: {
        workspace_id: this.cur_workspace_id,
      }
    });
  }
  //转让空间所有权给指定用户
  public putOwnership(params) {
    return this.http.putAsync({
      url: `${this.prefix}/workspace/member/ownership`,
      params,
    });
  }
  //当前用户退出指定空间
  public deleteSpace(): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/workspace/member/me`,
      query: {
        workspace_id: this.cur_workspace_id,
      },
    });
  }
}
