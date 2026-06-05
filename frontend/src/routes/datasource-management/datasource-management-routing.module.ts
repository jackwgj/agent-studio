import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import { RootRoutePath } from '@routes/root.routes';
import { DatasourceManagementComponent } from '@routes/datasource-management/datasource-management.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'management',
        component: DatasourceManagementComponent,
        canActivate: [ShowLeftmenuGuard],
      },
    ],
  },
  {
    path: '**',
    redirectTo: `/${RootRoutePath.HOME}/agent-center/management`,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DatasourceManagementRoutingModule {}
