import { Routes } from '@angular/router';

export const skillRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'detail',
        loadComponent: () => import('./skill-detail/skill-detail.component').then((m) => m.SkillDetailComponent),
      },
    ],
  },
];
