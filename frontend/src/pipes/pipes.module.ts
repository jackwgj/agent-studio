import { NgModule } from '@angular/core';
import { FormateTimePipe } from './formate-time.pipe';

const PIPES = [FormateTimePipe];
@NgModule({
  imports: [],
  declarations: [...PIPES],
  exports: [...PIPES],
})
export class PipesModule {}
