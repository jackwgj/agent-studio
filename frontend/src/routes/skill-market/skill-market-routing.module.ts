import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';

import { SkillMarketComponent } from './skill-market-component/skill-market.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        canActivate: [ShowLeftmenuGuard],
        component: SkillMarketComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SkillMarketRoutingModule {}
