import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import HideLeftmenuGuard from '@shared/guard/hideLeftmenu.guard';
import { IntentPackageComponent } from './intent-package/intent-package.component';
import { IntentDetailComponent } from './intent-detail/intent-detail.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'manage',
        component: IntentPackageComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'intent-detail',
        component: IntentDetailComponent,
        canActivate: [HideLeftmenuGuard],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class IntentPackageRoutingModule {}
