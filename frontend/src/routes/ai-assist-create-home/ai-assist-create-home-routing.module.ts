import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AiAssistCreateHomeComponent } from './ai-assist-create-home.component';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        component: AiAssistCreateHomeComponent,
        canActivate: [ShowLeftmenuGuard],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AiAssistCreateHomeRoutingModule {}
