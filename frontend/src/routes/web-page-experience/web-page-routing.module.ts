import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChatKnowledgePageComponent } from '@routes/app-center/chat-knowledge-page/chat-knowledge-page.component';
import { ChatFlowPageComponent } from '@routes/app-center/chat-flow-page/chat-flow-page.component';
import { RootRoutePath } from '@routes/root.routes';
import HideLeftmenuGuard from '@shared/guard/hideLeftmenu.guard';

const routes: Routes = [
  {
    path: '',
    data: { hideLeftMenu: false },
    children: [
      {
        path: ':id',
        component: ChatKnowledgePageComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'flow/:id',
        component: ChatFlowPageComponent,
        canActivate: [HideLeftmenuGuard],
      },
    ],
  },
  {
    path: '**',
    redirectTo: `/${RootRoutePath.HOME}/dialog-home`,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class WebPageRoutingModule {}
