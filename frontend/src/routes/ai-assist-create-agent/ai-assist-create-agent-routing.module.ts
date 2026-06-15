import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AiAssistCreateAgentComponent } from './ai-assist-create-agent.component';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        component: AiAssistCreateAgentComponent,
        canActivate: [ShowLeftmenuGuard],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AiAssistCreateAgentRoutingModule {}
