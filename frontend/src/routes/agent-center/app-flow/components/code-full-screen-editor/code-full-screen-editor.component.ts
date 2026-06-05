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
import { cdnAssetUrl } from '../../../../../single-spa/assets-url';
import {
  MonacoEditorComponent,
  MonacoEditorConstructionOptions,
  MonacoEditorLoaderService,
  MonacoEditorModule,
} from '@materia-ui/ngx-monaco-editor';
import { filter, take } from 'rxjs';

@Component({
  selector: 'code-full-screen-editor',
  templateUrl: './code-full-screen-editor.component.html',
  styleUrls: [
    './code-full-screen-editor.component.less',
    '.././common-styles.less',
  ],
  standalone: true,
  imports: [MODULES, MonacoEditorModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class CodeFullScreenEditorComponent implements OnInit, OnDestroy {
  @Input() isFullScreen: boolean = false;
  @Input() code: string;
  @Output() codeChange = new EventEmitter<string>();
  @Output() isFullScreenChange = new EventEmitter<boolean>();

  @ViewChild('editorCmp') editorCmp: MonacoEditorComponent;

  public changeUrl = cdnAssetUrl;

  editorOptions: MonacoEditorConstructionOptions = {
    theme: 'myCustomTheme',
    language: 'json',
    minimap: {
      enabled: false,
    },
    formatOnPaste: true,
    formatOnType: true,
  };

  formatContent() {
    this.editorCmp?.editor?.getAction('editor.action.formatDocument')?.run();
  }

  constructor(
    private fb: FormBuilder,
    private i18n: I18NextEagerPipe,
    private elementRef: ElementRef<HTMLDivElement>,
    private monacoLoaderService: MonacoEditorLoaderService,
  ) {
    this.monacoLoaderService.isMonacoLoaded$
      .pipe(
        filter((isLoaded) => !!isLoaded),
        take(1),
      )
      .subscribe(() => {
        monaco.editor.defineTheme('myCustomTheme', {
          base: 'vs',
          inherit: true,
          rules: [],
          colors: {
            'editor.background': '#F5F5F5',
            'editor.lineHighlightBackground': '#0000000A',
            'editorLineNumber.foreground': '#191919',
          },
        });
      });
  }

  async ngOnInit() {}

  closeScreen() {
    this.isFullScreen = false;
    this.codeChange.emit(this.code);
    this.isFullScreenChange.emit(false);
  }

  ngOnDestroy(): void {}
}
