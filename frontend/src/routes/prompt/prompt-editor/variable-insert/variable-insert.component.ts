import { ChangeDetectionStrategy, Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import { COMMON_MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'meta-variable-insert',
  standalone: true,
  imports: [COMMON_MODULES],
  templateUrl: './variable-insert.component.html',
  styleUrl: './variable-insert.component.scss',
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.PROMPT_PLATFORM
      ],
    },
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VariableInsertComponent {
  @Output() onselect =new EventEmitter<string>();
  constructor(private i18n: I18NextEagerPipe,) {
  }
  public readonly options = [
    {
      label: this.i18n.transform('text_variable'),
      value: 'text',
      icon:'assets/prompt/type-text-black.svg'
    },
    {
      label: this.i18n.transform('image_variable'),
      value: 'image',
      icon:'assets/prompt/type-image-black.svg'
    },
  ];
  handleTypeClick(type:string){
    this.onselect.emit(type)
  }
  protected readonly changeUrl = cdnAssetUrl;
}
