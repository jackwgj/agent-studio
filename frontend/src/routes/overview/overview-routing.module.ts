import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {OverviewComponent} from "@routes/overview/overview.component";
import ShowLeftmenuGuard from "@shared/guard/showLeftmenu.guard";
import DevelopGuard from "@shared/guard/develop.guard";

const routes: Routes = [
  {
    path: '',
    component: OverviewComponent,
    canActivate: [ShowLeftmenuGuard,DevelopGuard],
    children: [],
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class OverviewRoutingModule { }
