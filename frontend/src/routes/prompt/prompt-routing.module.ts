import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CreateTemplateComponent } from '@routes/prompt/prompt-template/create-template/create-template.component';
import { PromptTemplateComponent } from './prompt-template/prompt-template.component';
import { ViewTemplateComponent } from './prompt-template/view-template/view-template.component';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import HideLeftmenuGuard from '@shared/guard/hideLeftmenu.guard';
import { PromptOptimizeTaskConfigComponent } from '@routes/prompt/prompt-optimize-task-config/prompt-optimize-task-config.component';
import { PromptOptimizeTaskDetailComponent } from '@routes/prompt/prompt-optimize-task-detail/prompt-optimize-task-detail.component';
import { PromptOptimizeTaskExecutionStatusComponent } from '@routes/prompt/prompt-optimize-task-execution-status/prompt-optimize-task-execution-status.component';

export const promptRootPath = 'prompt';
const routes: Routes = [
  {
    path: '',
    children: [   
      {
        path: 'prompt-template',
        canActivate: [ShowLeftmenuGuard],
        component: PromptTemplateComponent,
      },
      {
        path: 'templates',
        data: { hideLeftMenu: false },
        canActivate: [ShowLeftmenuGuard],
        component: PromptTemplateComponent,
      },
      {
        path: 'create-template',
        canActivate: [HideLeftmenuGuard],
        component: CreateTemplateComponent,
      },
      {
        path: 'view-template',
        canActivate: [HideLeftmenuGuard],
        component: ViewTemplateComponent,
      },
         //提示词优化
         {
          path: 'optimize',
          canActivate: [HideLeftmenuGuard],
          children: [
            {
              path: 'edit',
              canActivate: [HideLeftmenuGuard],
              component: PromptOptimizeTaskConfigComponent,
            },
            {
              path: 'create',
              canActivate: [HideLeftmenuGuard],
              component: PromptOptimizeTaskConfigComponent,
            },
            {
              path: 'detail',
              canActivate: [HideLeftmenuGuard],
              component: PromptOptimizeTaskDetailComponent,
            },
            {
              path: 'submission',
              canActivate: [HideLeftmenuGuard],
              component: PromptOptimizeTaskExecutionStatusComponent,
            },
          ],
        },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PromptRoutingModule {}
