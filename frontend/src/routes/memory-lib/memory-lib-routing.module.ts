import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'detail/:id',
        loadComponent: () =>
          import('./memory-lib-detail/memory-lib-detail.component').then(
            (m) => m.MemoryLibDetailComponent,
          ),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class MemoryLibRoutingModule {}
