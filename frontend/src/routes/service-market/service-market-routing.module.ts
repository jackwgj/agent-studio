import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import { McpSquareListComponent } from './mcp-square-list.component';

const routes: Routes = [
  {
    path: '',
    component: McpSquareListComponent,
    canActivate: [ShowLeftmenuGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ServiceMarketRoutingModule {}
