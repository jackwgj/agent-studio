import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { FormBuilder, NgForm } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { filter, Subject, take } from 'rxjs';
import { cdnAssetUrl } from '../../../../../single-spa/assets-url';
import { AccBlockComponent } from '../acc-block/acc-block.component';
import * as nodeType from '@routes/agent-center/app-flow/node.type';
import {
  MonacoEditorComponent,
  MonacoEditorModule,
} from '@materia-ui/ngx-monaco-editor';
import { EditorOptions } from '@routes/agent-center/app-flow/components/utils';
import { AssetFormatIconComponent } from '@shared/components/assets/asset-format-icon/asset-format-icon.component';
import { NonEmptyValidatorDirective } from '@shared/directives/variable-name-validator.directive';
import { ObjTypeValidatorDirective } from '@shared/directives/obj-type-validator.directive';
import { CodeFullScreenEditorComponent } from '@routes/agent-center/app-flow/components/code-full-screen-editor/code-full-screen-editor.component';

@Component({
  selector: 'struct-output',
  templateUrl: './struct-output.component.html',
  styleUrls: ['./struct-output.component.less', '.././common-styles.less'],
  standalone: true,
  imports: [
    MODULES,
    AccBlockComponent,
    MonacoEditorModule,
    AssetFormatIconComponent,
    NonEmptyValidatorDirective,
    ObjTypeValidatorDirective,
    CodeFullScreenEditorComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class StructOutputComponent implements OnInit, OnDestroy {
  @Input('nodeInfo') nodeInfo: nodeType.INodeBase;

  @ViewChild('editorCmp') editorCmp: MonacoEditorComponent;

  @ViewChild('contentForm') contentForm: NgForm;

  @Output() changeEvent = new EventEmitter<void>();

  public destroy$ = new Subject<void>();

  public changeUrl = cdnAssetUrl;

  demoCode = JSON.stringify(
    {
      answer: '{{_NODE_OUTPUT}}',
    },
    null,
    2,
  );

  public struct_output_template = this.demoCode;

  public structEnable = false;

  isFullScreen = false;

  formatContent() {
    this.editorCmp?.editor?.getAction('editor.action.formatDocument')?.run();
  }

  getContentError() {
    return (
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      this.contentForm?.controls?.template?.errors?.arrErr?.msg ||
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      this.contentForm?.controls?.template?.errors?.serviceName?.tiErrorMessage
    );
  }

  constructor(
    private fb: FormBuilder,
    private i18n: I18NextEagerPipe,
    private elementRef: ElementRef<HTMLDivElement>,
  ) { }

  async ngOnInit() {
    this.structEnable = !!(this.nodeInfo as any)?.configs?.isStructMessage;
    this.struct_output_template =
      (this.nodeInfo as any)?.configs?.struct_output_template || this.demoCode;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  switchStructEnable(enable: boolean) {
    if (enable) {
      (this.nodeInfo as any).configs.struct_output_template =
        this.struct_output_template;
    }
    this.changeEvent.emit();
  }

  openFullScreen() {
    if (!this.structEnable) {
      return;
    }
    this.isFullScreen = true;
  }

  protected readonly editorOps = EditorOptions;

  handelChange() {
    this.changeEvent.emit();
  }
}
