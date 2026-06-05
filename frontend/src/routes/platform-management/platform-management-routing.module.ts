import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { EnvManagementListComponent } from '@routes/platform-management/environment-management/env-management-list.component';
import { EnvironmentVariablesManagementListComponent } from '@routes/platform-management/environment-variables-management/environment-variables-management-list.component';
import ShowLeftmenuGuard from "@shared/guard/showLeftmenu.guard";
import {EnvManagementHomeComponent} from "@routes/platform-management/environment-management/environment-management-home/environment-management-home.component";
import {SpaceTeamManagementComponent} from "@routes/platform-management/space-team-management/space-team-management.component";
import SpaceTeamGuard from "@shared/guard/spaceTeam.guard";

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'space-team-management',
        component: SpaceTeamManagementComponent,
        canActivate: [ShowLeftmenuGuard, SpaceTeamGuard],
      },
      {
        path: 'environment-variables-management',
        component: EnvironmentVariablesManagementListComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'environment-management',
        component: EnvManagementListComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'environment-management-home',
        component: EnvManagementHomeComponent,
        canActivate: [ShowLeftmenuGuard],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PlatformManagementRoutingModule {}
