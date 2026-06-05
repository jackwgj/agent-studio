import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {HasPermissionDirective} from "@shared/directives/has-permission.directive";
import {HasRoleDirective} from "@shared/directives/has-role.directive";


@NgModule({
  declarations: [],
  imports: [CommonModule, HasRoleDirective, HasPermissionDirective],
  exports: [HasPermissionDirective, HasRoleDirective],
})
export class SharedModule {}
