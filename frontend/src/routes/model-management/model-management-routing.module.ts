import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import HideLeftmenuGuard from '@shared/guard/hideLeftmenu.guard';
import { ModalManagementComponent } from './model-management.component';
import { ModelDetailComponent } from './model-detail/model-detail.component';
import { OnlineTestComponent } from './online-test/online-test.component';
import {ModelRouteComponent} from "@routes/model-management/model-route/model-route.component";
import {CreateModelRouteComponent} from "@routes/model-management/create-model-route/create-model-route.component";
import {RouterPolicyDetail} from "@routes/model-management/router-policy-detail/router-policy-detail";
import { ModalManagementDetailComponent } from "./model-management-detail/model-management-detail.component"
import { CertificationComponent } from "@routes/model-management/certification/certification.component";
import {ModelTestComponent} from "@routes/model-square/model-test/model-test.component";

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'management',
        component: ModalManagementComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'management-detail',
        component: ModalManagementDetailComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'online-test',
        component: OnlineTestComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'online-test-manage',
        component: OnlineTestComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'certification',
        component: CertificationComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'detail',
        component: ModelDetailComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'route-policy',
        component: ModelRouteComponent,
        canActivate: [ShowLeftmenuGuard],
      },
      {
        path: 'route-policy-detail',
        component: RouterPolicyDetail,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'create-route-policy',
        component: CreateModelRouteComponent,
        canActivate: [HideLeftmenuGuard],
      },
      {
        path: 'model-test',
        component: ModelTestComponent,
        canActivate: [HideLeftmenuGuard],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ModelManagementRoutingModule {}
