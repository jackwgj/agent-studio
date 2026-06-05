import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  forwardRef,
  HostBinding,
  Input,
  NgZone,
  OnDestroy,
  Output,
  Renderer2,
  SimpleChanges,
  ViewChild,
} from "@angular/core";
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from "@angular/forms";
import { CodeEditorService } from "./code-editor.service";
import { fromEvent, Subject, takeUntil, timer } from "rxjs";
import { BaseCommonVariable } from "./base-common-variable";

declare const monaco: any;

@Component({
  selector: "app-editor",
  template: ` <div #editor class="my-editor"></div> `,
  styleUrls: ["./code-editor.component.less"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  // 这里我们会通过双向绑定的方式来给 editor 传值，注意引入 NG_VALUE_ACCESSOR, ControlValueAccessor
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CodeEditorComponent),
      multi: true,
    },
  ],
})
export class CodeEditorComponent implements AfterViewInit, OnDestroy, ControlValueAccessor {
  // 这里详细的 options 可以查看 https://microsoft.github.io/monaco-editor/api/interfaces/monaco.editor.IStandaloneEditorConstructionOptions.html
  @Input() options: any;
  @Input() language: string;
  // 设置高度
  @Input() @HostBinding("style.height") height: string;
  @Input() maxHeight: number = this.baseCommonVariable.getCodeEditorMaxHeight();
  @Input() minHeight: number = this.baseCommonVariable.getCodeEditorMinHeight();
  // 初始化完成后将 editor 实例抛出，用户可以使用 editor 实例做一些个性化操作
  @Output() readonly editorInitialized: EventEmitter<any> = new EventEmitter<any>();
  @ViewChild("editor", { static: true }) editorContentRef: ElementRef;

  private destroy$: Subject<void> = new Subject<void>();
  public _editor: any = undefined;
  private _value: string = "";
  // 由于 monaco 的很多方法都会返回 IDisposable 类型，大家在使用的时候需要注意，在组件销毁时将他们销毁，执行 dispose() 方法
  private _disposables: any[] = [];

  onChange = (_: any) => {};
  onTouched = () => {};

  constructor(
    private zone: NgZone,
    private codeEditorService: CodeEditorService,
    private renderer: Renderer2,
    private baseCommonVariable: BaseCommonVariable
  ) {}

 // 双向绑定设置 editor 内容
  writeValue(value: string): void {
    if (Object.prototype.toString.call(value) === "[object Object]" && Object.keys(value).length === 0) {
      if (Object.keys(value).length > 0) {
        return;
      }
      value = "";
    }
    this._value = value || "";
    this.setValue();
  }

  // 通过 onChange 方法将改变后的值传出，即 ngModelChange
  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (this._editor) {
      this._editor.dispose();
      this._editor = undefined;
      this.initMonaco();
    }
  }

  ngAfterViewInit(): void {
    this.initMonaco();

    // 监听浏览器窗口大小，做 editor 的自适应
    fromEvent(window, "resize")
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (this._editor) {
          this._editor.layout();
        }
      });

    // 监听鼠标松开事件，适应侧边栏拖拽，做 editor 的自适应
    fromEvent(document, "mouseup")
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (this._editor) {
          this._editor.layout();
        }
      });
  }

  updateEditorHeight() {
    const editorElement = this._editor.getDomNode();
    if (!editorElement) {
      return;
    }
    const padding = 30;
    const lineHeight = this._editor.getOption(monaco.editor.EditorOption.lineHeight);
    const lineCount = this._editor.getModel()?.getLineCount() || 1;
    let actualityH = this._editor.getTopForLineNumber(lineCount + 1) + lineHeight + padding;
    if (actualityH > this.maxHeight) {
      actualityH = this.maxHeight;
    }
    if (actualityH < this.minHeight) {
      actualityH = this.minHeight;
    }
    editorElement.style.height = `${actualityH}px`;
    this.renderer.setStyle(this.editorContentRef.nativeElement, "height", actualityH);
    this._editor.layout();
  }

  public refreshEditor() {
    timer(500).subscribe(() => {
      this._editor && this._editor.layout();
    });
  }

  public initMonaco(): void {
    const options = this.options;
    options.scrollBeyondLastLine = false; //Enable that scrolling can go one screen size after the last line. Defaults to true.
    options.scrollbar = {
      horizontalScrollbarSize:0,
      verticalScrollbarSize:0,
      verticalSliderSize:8,
      horizontalSliderSize:8,
      useShadows:false,
    }
    const language = this.options.language;
    if (language === "mySpecialLanguage") {
      this.registerCustomLanguage();
    }
    const editorDiv: HTMLDivElement = this.editorContentRef.nativeElement;
    if (!this._editor) {
      this._editor = monaco.editor.create(editorDiv, options);
      if (Object.prototype.toString.call(this._value) === "[object Object]") {
        this._value = "";
      }
      this._editor.setModel(monaco.editor.createModel(this._value, language));
      this.editorInitialized.emit(this._editor);
      this.setValueEmitter();
      if (this.height) {
        this.renderer.setStyle(this.editorContentRef.nativeElement, "height", this.height);
      } else {
        this.updateEditorHeight();
        this._editor.onDidChangeModelDecorations(() => {
          this.updateEditorHeight();
        });
      }
      this._editor.layout();
    }
  }

  registerCustomLanguage() {
    //  注册一个语言
    monaco.languages.register({ id: "mySpecialLanguage" });

    // 通过正则注册解析规则
    monaco.languages.setMonarchTokensProvider("mySpecialLanguage", {
      tokenizer: {
        root: [
          [/\[(info)((?!\[warning|warning|\[error|error).)*/, "info"],
          [/\[(notice)((?!\[warning|warning|\[error|error).)*/, "notice"],
          [/(\[warning|warning).*/, "warning"],
          [/(\[error|error).*/, "error"],
          [/\[[0-9 \-:]+\]/, "custom-date"],
        ],
      },
    });
    // 定义仅包含与此语言匹配的规则的新主题
    monaco.editor.defineTheme("logTheme", {
      base: "vs",
      inherit: false,
      rules: [
        { token: "info", foreground: "808080" },
        { token: "error", foreground: "ff0000", fontStyle: "bold" },
        { token: "notice", foreground: "FFA500" },
        { token: "custom-date", foreground: "808080" },
      ],
      colors: {
        "editor.foreground": "#000000",
      },
    });
    monaco.editor.setTheme("logTheme");
  }

  private setEditorTheme() {
    if (this.options.readOnly) {
      monaco.editor.defineTheme("readonlyTheme", {
        base: "vs",
        inherit: true,
        rules: [{ background: "FBFBFB" }],
        colors: {
          "editor.background": "#FBFBFB",
        },
      });
      monaco.editor.setTheme("readonlyTheme");
      return;
    }
    monaco.editor.defineTheme("normalTheme", {
      base: "vs",
      inherit: true,
      rules: [{ background: "FFFFFF" }],
      colors: {
        "editor.background": "#FFFFFF",
      },
    });
    monaco.editor.setTheme("normalTheme");
  }

  public setValue(): void {
    if (!this._editor || !this._editor.getModel()) {
      return;
    }
    if (Object.prototype.toString.call(this._value) === "[object Object]") {
      this._value = "";
    }
    this._editor.getModel().setValue(this._value);

    this._editor.layout();
  }

  private setValueEmitter() {
    if (this._editor) {
      const model = this._editor.getModel();
      // 在内容改变时会触发 onDidChangeContent 方法，在此时将 value 抛出，注意将其返回值加到 _disposables 中
      this._disposables.push(
        model.onDidChangeContent(() => {
          this.zone.run(() => {
            this.onChange(model.getValue());
            this._value = model.getValue();
          });
        })
      );
    }
  }

  ngOnDestroy(): void {
    // 组件销毁时需要将订阅，实例都清除
    this.destroy$.next();
    this.destroy$.complete();
    if (this._editor) {
      this._editor.dispose();
      this._editor = undefined;
    }
    // 在 monaco 中很多方法都会返回一个 IDisposable，需要在销毁时统一执行其 dispose() 方法进行销毁
    if (this._disposables.length) {
      this._disposables.forEach(disposable => disposable.dispose());
      this._disposables = [];
    }
  }
}
