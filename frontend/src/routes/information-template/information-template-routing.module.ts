import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { InformationTemplateComponent } from './information-template.component';
import ShowLeftmenuGuard from "@shared/guard/showLeftmenu.guard";

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'template',
        component: InformationTemplateComponent,
        canActivate: [ShowLeftmenuGuard],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class InformationTemplateRoutingModule {}
