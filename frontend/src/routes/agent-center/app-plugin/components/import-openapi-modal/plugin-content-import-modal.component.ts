import { ChangeDetectorRef, Component, Input, OnInit, inject } from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import {
  MonacoEditorLoaderService,
  MonacoEditorModule,
} from '@materia-ui/ngx-monaco-editor';
import { ArrTypeValidatorDirective } from '@shared/directives/array-type-validator.directive';
import { NonEmptyValidatorDirective } from '@shared/directives/variable-name-validator.directive';
import { AssetFormatIconComponent } from '@shared/components/assets/asset-format-icon/asset-format-icon.component';
import { StartImportJsonModalComponent } from '@routes/agent-center/app-flow/components/start-import-json-modal/start-import-json-modal.component';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'meta-plugin-content-import-modal',
  templateUrl: './plugin-content-import-modal.component.html',
  styleUrls: ['./plugin-content-import-modal.component.less'],
  standalone: true,
  imports: [
    MODULES,
    MonacoEditorModule,
    ArrTypeValidatorDirective,
    NonEmptyValidatorDirective,
    AssetFormatIconComponent,
    NzButtonModule,
    NzToolTipModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class pluginContentImportModal
  extends StartImportJsonModalComponent
  implements OnInit {
  readonly modalRef = inject(NzModalRef);
  @Input() importContentType: 'OpenAPI' | 'args';
  public editContent: string = '';

  contentTypes: any = [
    {
      title: 'Json',
      selected: true,
      id: 'json',
    },
    {
      title: 'JsonSchema',
      selected: false,
      id: 'jsonSchema',
    },
  ];
  public selectedContentType = this.contentTypes[0];

  constructor(
    cdr: ChangeDetectorRef,
    i18n: I18NextEagerPipe,
    monacoLoader: MonacoEditorLoaderService,
  ) {
    super(cdr, i18n, monacoLoader);
  }

  override  onConfirm() {
    if (this.importContentType === 'OpenAPI') {
      this.confirm.emit(this.editContent);
    } else {
      this.confirm.emit({
        type: this.selectedContentType.id,
        content: this.editContent,
      });
    }
    this.modalRef.destroy();
  }

  override dismiss() {
    this.modalRef.destroy();
  }

  selectContentType(typeId: string) {
    this.contentTypes.forEach((item) => {
      item.selected = item.id === typeId;
      if (item.selected) {
        this.selectedContentType = item;
      }
    });
  }

  onOk() {
    if (this.importContentType === 'args') {
      this.show = true;
      return;
    }
    this.onConfirm();
  }
}
