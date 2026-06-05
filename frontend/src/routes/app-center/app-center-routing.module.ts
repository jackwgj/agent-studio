import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AppCenterManagementComponent } from './app-center-management/app-center-management.component';
import { ChatKnowledgePageComponent } from './chat-knowledge-page/chat-knowledge-page.component';
import { ChatFlowPageComponent } from '@routes/app-center/chat-flow-page/chat-flow-page.component';
import { ScenarioExamplePageComponent } from './scenario-example-page/scenario-example-page.component';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import HideLeftmenuGuard from '@shared/guard/hideLeftmenu.guard';
import { RootRoutePath } from '@routes/root.routes';
import { FlowComponent } from "@routes/agent-center/app-flow/flow/flow.component";

const routes: Routes = [
  {
    path: '',
    data: { hideLeftMenu: false },
    children: [
      {
        path: '',
        component: AppCenterManagementComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'knowledge-bot',
        component: ChatKnowledgePageComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'flow/knowledge-bot',
        component: ChatFlowPageComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'flow/preload',
        component: FlowComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'scenario-bot/:appType/:appId',
        component: ScenarioExamplePageComponent,
        canActivate: [HideLeftmenuGuard],
      },
    ],
  },
  {
    path: '**',
    redirectTo: `/${RootRoutePath.HOME}/app-library`,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AppCenterRoutingModule {}
