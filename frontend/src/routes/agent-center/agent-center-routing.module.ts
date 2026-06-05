import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { I18NEXT_NAMESPACE_RESOLVER } from 'angular-i18next';

import { AGENT_CORE, I18N_NAMESPACE } from '@agentcore/constants/common';
import { FlowAsyncTestComponent } from '@routes/agent-center/app-flow/flow-async-test/flow-async-test.component';
import { PluginToolDetailComponent } from '@routes/agent-center/app-plugin/tool-detail/tool-detail.component';
import { PublishChannelPageComponent } from '@shared/components/publish-channel-page/publish-channel-page.component';
import HideLeftmenuGuard from '@shared/guard/hideLeftmenu.guard';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import { ApplicationManagementComponent } from 'src/routes/agent-center/home/application-management.component';
import { AgentBotPageComponent } from './app-agent/agent-bot-page/agent-bot-page.component';
import { FlowComponent } from './app-flow/flow/flow.component';
import { MCPServiceDetailComponent } from './app-mcp-service/mcp-service-detail/mcp-service-detail.component';
import { PluginComponent } from './app-plugin/plugin/plugin.component';
import { ComponentLibraryComponent } from './component-library/component-library.component';
import { CreateAppComponent } from './create-app/create-app.component';
import { LibraryHomeComponent } from './library-home/library-home.component';
const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'management',
        component: ApplicationManagementComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'single',
        component: ApplicationManagementComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'workflow',
        component: ApplicationManagementComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'multi',
        component: ApplicationManagementComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'app-agent/detail',
        component: AgentBotPageComponent,
        canActivate: [HideLeftmenuGuard],
        data: {
          i18nextNamespaces: [I18N_NAMESPACE.Common, I18N_NAMESPACE.Skill].map(v => `${AGENT_CORE}.${v}`),
        },
        resolve: {
          i18next: I18NEXT_NAMESPACE_RESOLVER,
        },
      },
      {
        path: 'app-agent/publish',
        component: PublishChannelPageComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'app-flow/flow',
        component: FlowComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'app-flow/flow-async-test',
        component: FlowAsyncTestComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'mcp-service/detail',
        component: MCPServiceDetailComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'custom-plugin/detail',
        component: PluginComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'custom-plugin/tool/detail',
        component: PluginToolDetailComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'create',
        component: CreateAppComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'app-flow/publish',
        component: PublishChannelPageComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'plugin',
        component: ComponentLibraryComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'component-library',
        component: ComponentLibraryComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'mcp-service',
        component: ComponentLibraryComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'library-home',
        component: LibraryHomeComponent,
        canActivate: [ShowLeftmenuGuard],
        data: {
          i18nextNamespaces: [I18N_NAMESPACE.Common, I18N_NAMESPACE.Skill].map(v => `${AGENT_CORE}.${v}`),
        },
        resolve: {
          i18next: I18NEXT_NAMESPACE_RESOLVER,
        },
      },
    ],
  },
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  declarations: [],
  exports: [RouterModule],
})
export class AgentCenterRoutingModule {}
