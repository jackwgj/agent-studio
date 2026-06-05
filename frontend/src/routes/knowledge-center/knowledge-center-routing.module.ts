import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { KbPlazaComponent } from '@routes/knowledge-center/kb-plaza/kb-plaza.component';
import HideLeftmenuGuard from '@shared/guard/hideLeftmenu.guard';
import { SceneUrlQueryGuard } from '@shared/guard/scene-url-query.guard';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import { HomeComponent } from './home/home.component';
import { KnowledgeComponent } from './knowledge/knowledge.component';
import { KnowledgeHitTestComponent } from './hit-test/hit-test.component';
import { FaqFileComponent } from './faq-file/faq-file.component';
import { KnowledgeFileComponent } from './knowledge-file/knowledge-file.component';
import { KnowledgeBaseListComponent } from './knowledge-base-list/knowledge-base-list.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [SceneUrlQueryGuard],
    children: [
      {
        path: 'kb-plaza',
        component: KbPlazaComponent,
        canActivate: [ShowLeftmenuGuard, SceneUrlQueryGuard],
      },
      {
        path: 'KnowledgeBaseList',
        component: KnowledgeBaseListComponent,
        canActivate: [ShowLeftmenuGuard, SceneUrlQueryGuard],
      },
      {
        path: 'management',
        component: HomeComponent,
        canActivate: [ShowLeftmenuGuard, SceneUrlQueryGuard],
      },
      {
        path: 'knowledge/:id',
        component: KnowledgeComponent,
        canActivate: [HideLeftmenuGuard, SceneUrlQueryGuard],
      },
      {
        path: 'knowledge/:id/hit-testing',
        component: KnowledgeHitTestComponent,
        canActivate: [HideLeftmenuGuard, SceneUrlQueryGuard],
      },
      {
        path: 'knowledge/:id/files/:fileId',
        component: KnowledgeFileComponent,
        canActivate: [HideLeftmenuGuard, SceneUrlQueryGuard],
      },
      {
        path: 'knowledge/:id/faq-files/:fileId',
        component: FaqFileComponent,
        canActivate: [HideLeftmenuGuard, SceneUrlQueryGuard],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class KnowledgeCenterRoutingModule {}
