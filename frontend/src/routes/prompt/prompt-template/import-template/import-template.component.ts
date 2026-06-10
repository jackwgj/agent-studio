import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import { CommonModule } from '@angular/common';
import {I18NextModule} from "angular-i18next";
import {NzButtonModule, NzModalModule} from "ng-zorro-antd";
import {SystemTemplateComponent} from "@routes/prompt/prompt-template/system-template/system-template.component";
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'meta-import-template',
  standalone: true,
  imports: [CommonModule, I18NextModule, NzModalModule, NzButtonModule, SystemTemplateComponent],
  templateUrl: './import-template.component.html',
  styleUrls: ['./import-template.component.scss'],
  changeDetection: ChangeDetectionStrategy.Default,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PROMPT_PLATFORM, I18nNamespace.COMMON],
    },
  ],

})
export class ImportTemplateComponent {
  @Input() importModal:string
  @Output() callBack = new EventEmitter<string>();

  private content:string;
  setPromptOK (){
    if(this.content){
      this.callBack.emit(this.content);
    }
    document.getElementById('createTemplate_close').click();
  }
  setPromptCancel(){
    document.getElementById('createTemplate_close').click();
  }
  setPromptContent(content){
    this.content = content;
  }
  protected readonly Boolean = Boolean;
  protected readonly event = event;
}
