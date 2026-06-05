import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DevelopmentConfigurationComponent } from './development-configuration.component';
import showLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
const routes: Routes = [
  {
    path: '',
    component: DevelopmentConfigurationComponent,
    canActivate: [showLeftmenuGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class  DevelopmentConfigurationRoutingModule {}
