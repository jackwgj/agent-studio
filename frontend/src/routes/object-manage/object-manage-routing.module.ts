import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ObjectManageComponent } from './object-manage.component';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'manage',
        component: ObjectManageComponent,
        canActivate: [ShowLeftmenuGuard],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ObjectManageRoutingModule {}
