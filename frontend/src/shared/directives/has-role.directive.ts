import { Directive, Input, TemplateRef, ViewContainerRef, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import {PermissionService} from "@services/permission.service";


@Directive({
  standalone: true,
  selector: '[appHasRole]',
})
export class HasRoleDirective implements OnDestroy {
  private requiredRoles: string | string[];
  private roleSubscription: Subscription;
  private hasView = false;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private permissionService: PermissionService,
  ) {}

  @Input() set appHasRole(role: string | string[]) {
    this.requiredRoles = role;
    this.updateView();
    if (this.roleSubscription) {
      this.roleSubscription.unsubscribe();
    }
    this.roleSubscription = this.permissionService.permissions$.subscribe(
      () => {
        this.updateView();
      },
    );
  }

  private updateView(): void {
    const hasRole = Array.isArray(this.requiredRoles)
      ? this.permissionService.hasAnyRole(this.requiredRoles)
      : this.permissionService.hasRole(this.requiredRoles);

    if (hasRole && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!hasRole && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }

  ngOnDestroy(): void {
    if (this.roleSubscription) {
      this.roleSubscription.unsubscribe();
    }
  }
}
