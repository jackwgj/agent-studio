import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import HideLeftmenuGuard from '@shared/guard/hideLeftmenu.guard';
import { PluginMarketComponent } from './plugin-market/plugin-market.component';
import {PluginComponent} from "@routes/agent-center/app-plugin/plugin/plugin.component";
import { PluginMarketDetailComponent } from "@routes/plugin-market/plugin-detail/plugin-detail.component";
import { PluginMarketToolDetailComponent } from '@routes/plugin-market/tool-detail/tool-detail.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        canActivate: [ShowLeftmenuGuard],
        component: PluginMarketComponent,
      },
      {
        path: 'detail',
        component: PluginComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'market-detail',
        component: PluginMarketDetailComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'market-tool-detail',
        component: PluginMarketToolDetailComponent,
        canActivate: [HideLeftmenuGuard],
      },
    ]
  },

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PluginMarketRoutingModule {}
