import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";

const COMPONENTS = [];
@NgModule({
  declarations: [...COMPONENTS],
  imports: [CommonModule],
  exports: [...COMPONENTS],
})
export class CodeEditorModule {}
