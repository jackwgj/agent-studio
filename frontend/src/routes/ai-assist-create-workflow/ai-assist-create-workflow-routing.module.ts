import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AiAssistCreateWorkflowComponent } from './ai-assist-create-workflow.component';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        component: AiAssistCreateWorkflowComponent,
        canActivate: [ShowLeftmenuGuard],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AiAssistCreateWorkflowRoutingModule {}
