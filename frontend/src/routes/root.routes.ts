import {Routes} from '@angular/router';

/**
 * 根路由名称
 */
export enum RootRoutePath {
  /** 主页 */
  HOME = 'home',
}

/**
 * 根路由
 */
export const ROOT_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: RootRoutePath.HOME,
  },
  {
    path: 'health',
    loadComponent: () => import('./health/health.component').then((module) => module.HealthComponent),
  },
  {
    path: RootRoutePath.HOME,
    loadComponent: () =>
      import('./home/home.component').then((module) => module.HomeComponent),
    loadChildren: () =>
      import('./home/home.routes').then((module) => module.HOME_ROUTES),
  },
  {
    path: 'subscription',
    loadComponent: () => import('./subscription/subscription.component').then((module) => module.SubscriptionComponent),
  },
  {
    path: 'apply-success',
    loadComponent: () => import('./subscription/apply-success/apply-success.component').then((module) => module.ApplySuccessComponent),
  },
  {
    path: 'mobile-index',
    loadComponent: () => import('./mobile/mobile-index.component').then((module) => module.MobileIndexComponent),
    canActivate: [],
    canActivateChild: [],
  },
  {
    path: '**',
    redirectTo: RootRoutePath.HOME,
  },
];
