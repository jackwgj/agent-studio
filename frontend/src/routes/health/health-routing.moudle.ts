import { RouterModule, Routes } from '@angular/router';
import { HealthComponent } from '@routes/health/health.component';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import { NgModule } from '@angular/core';


const routes: Routes = [
  {
    path: '',
    component: HealthComponent,
    canActivate: [ShowLeftmenuGuard],
    children: [],
  }
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class HealthRoutingModule {

}
