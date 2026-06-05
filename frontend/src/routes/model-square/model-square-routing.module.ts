import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import {ModelSquareListComponent} from "@routes/model-square/model-square-list/model-square-list.component";
import {ModelTestComponent} from "@routes/model-square/model-test/model-test.component";
import HideLeftmenuGuard from "@shared/guard/hideLeftmenu.guard";

const routes: Routes = [
  {
    path: '',
    component: ModelSquareListComponent,
    canActivate: [ShowLeftmenuGuard],
  },
  {
    path: 'model-test',
    component: ModelTestComponent,
    canActivate: [HideLeftmenuGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ModelSquareRoutingModule {}
