import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, catchError, from, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';

export interface UserPermissions {
  roles: string[];
  permissions: string[]; // 后端接口标识列表
  tenantId?: string;
}

@Injectable({
  providedIn: 'root',
})
export class PermissionService {
  private permissionsSubject = new BehaviorSubject<UserPermissions>({
    roles: [],
    permissions: [],
  });
  public permissions$ = this.permissionsSubject.asObservable();

  constructor(private appService: AppAgentRepoService) {}

  // 获取用户权限信息
  fetchPermissions(): Observable<UserPermissions> {
    return from(
      this.appService.acquirePermission().then(permissions => {
        return permissions as UserPermissions; // 或进行必要的数据转换
      })
    ).pipe(
      tap(permissions => {
        this.permissionsSubject.next(permissions);
        localStorage.setItem('userPermissions', JSON.stringify(permissions));
      })
    );
  }
  // 获取当前权限（同步方法）
  getCurrentPermissions(): any {
    return this.permissionsSubject.value;
  }

  hasPermission(permission: string): boolean {
    const currentPermissions = this.permissionsSubject.value;

    // 检查currentPermissions是否存在
    if (!currentPermissions) return false;

    // 遍历所有角色权限数组，检查是否包含目标权限
    return Object.values(currentPermissions).some(
      (permissionsArray: string[]) => permissionsArray.includes(permission)
    );
  }


  // 检查是否具有特定角色
  hasRole(role: string): boolean {
    const currentPermissions = this.permissionsSubject.value;
    return currentPermissions.roles.includes(role);
  }

  // 检查是否具有任意指定权限
  hasAnyPermission(permissions: string[]): boolean {
    return permissions.some((permission) => this.hasPermission(permission));
  }

  // 检查是否具有任意指定角色
  hasAnyRole(roles: string[]): boolean {
    return roles.some((role) => this.hasRole(role));
  }

  // 清除权限信息（用于退出登录）
  clearPermissions(): void {
    this.permissionsSubject.next({ roles: [], permissions: [] });
    localStorage.removeItem('userPermissions');
  }

  // 初始化权限（应用启动时调用）
  initializePermissions(): void {
    const stored = localStorage.getItem('userPermissions');
    if (stored) {
      this.permissionsSubject.next(JSON.parse(stored));
    }
  }
}
