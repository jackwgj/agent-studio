import { LineClampDirective } from '@shared/directives/line-clamp.directive';
import { InputParamsComponent } from './node-param-cmp/input-params.component';
import { OutputParamsComponent } from './node-param-cmp/output-params.component';
import { NodeCardDescriptionComponent } from './node-card-description/node-card-description.component';
import { ValueWarnDirective } from '@shared/directives/value-warn.directive';
import { NgModule } from "@angular/core";
import { MODULES } from "@shared/modules";
export const NodeDeps = [
  MODULES,
  LineClampDirective,
  InputParamsComponent,
  OutputParamsComponent,
  NodeCardDescriptionComponent,
  ValueWarnDirective,
];
@NgModule({
  imports: [...NodeDeps],
  exports: [...NodeDeps],
})

export class NodeDependencies {}
