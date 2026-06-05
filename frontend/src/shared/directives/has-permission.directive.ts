import { Directive, Input, TemplateRef, ViewContainerRef, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import {PermissionService} from "@services/permission.service";

@Directive({
  standalone: true,
  selector: '[appHasPermission]',
})
export class HasPermissionDirective implements OnDestroy {
  private requiredPermissions: string | string[];
  private permissionSubscription: Subscription;
  private hasView = false;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private permissionService: PermissionService,
  ) {}

  @Input() set appHasPermission(permission: string | string[]) {
    this.requiredPermissions = permission;
    this.updateView();
    // 如果之前有订阅，先取消
    if (this.permissionSubscription) {
      this.permissionSubscription.unsubscribe();
    }
    // 订阅权限变化
    this.permissionSubscription = this.permissionService.permissions$.subscribe(
      () => {
        this.updateView();
      },
    );
  }

  private updateView(): void {
    const hasPermission = Array.isArray(this.requiredPermissions)
      ? this.permissionService.hasAnyPermission(this.requiredPermissions)
      : this.permissionService.hasPermission(this.requiredPermissions);

    if (hasPermission && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!hasPermission && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }

  ngOnDestroy(): void {
    if (this.permissionSubscription) {
      this.permissionSubscription.unsubscribe();
    }
  }
}
