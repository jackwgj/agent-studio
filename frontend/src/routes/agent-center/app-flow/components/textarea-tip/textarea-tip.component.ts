import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { MODULES } from '@shared/modules';
import { OverlayModule } from '@angular/cdk/overlay';
import { fromEvent, throttleTime } from 'rxjs';
import { I18NextEagerPipe } from 'angular-i18next';

interface IPromptChunk {
  type: 'val' | 'txt';
  content: string;
}

const CmdArr = ['/', '{'];

@Component({
  selector: 'textarea-tip',
  templateUrl: './textarea-tip.component.html',
  styleUrls: ['./textarea-tip.component.less'],
  standalone: true,
  imports: [MODULES, OverlayModule],
})
export class TextareaTipComponent implements OnInit, OnChanges {
  @Input() prompt = '';

  @Input() overrideContent = '';

  @Input() variants: string[] = [];

  @Input() placeholder = this.i18n.transform('input_placeholder');

  @Output() promptChange = new EventEmitter<string>();

  @ViewChild('textarea') textarea: ElementRef<HTMLTextAreaElement>;

  @ViewChild('styledContent') styledContent: ElementRef<HTMLDivElement>;

  @ViewChild('candidateBox') candidateBox: ElementRef<HTMLDivElement>;

  @ViewChild('candidateList') candidateList: ElementRef<HTMLDivElement>;

  public promptChunks: IPromptChunk[] = [];

  // 光标位置
  public selectionStart = 0;

  // 变量选中的位置
  public selectedIndex = 0;

  /** 用于判断当前是否在输入法编辑器中进行输入 */
  private isComposition = false;

  private isShowVarPanel = false;

  constructor(private cdr: ChangeDetectorRef, private i18n: I18NextEagerPipe) {}

  ngOnInit() {
    this.promptChunks = this.buildNormalChunks(this.prompt);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.overrideContent?.currentValue) {
      this.prompt = changes.overrideContent.currentValue;

      this.promptChunks = this.buildNormalChunks(
        changes.overrideContent.currentValue as string,
      );
    }
  }

  ngAfterViewInit(): void {
    /**
     * 节流时间若超过 100ms 会导致：
     */
    fromEvent(this.textarea.nativeElement, 'keydown')
      .pipe(throttleTime(100))
      .subscribe((event: Event) => {
        const keyboardEvent = event as KeyboardEvent;

        if (['ArrowUp', 'ArrowDown', 'Enter'].includes(keyboardEvent.key)) {
          this.onSelectVar(keyboardEvent);
        }
      });

    this.cdr.detectChanges();
  }

  public onCompositionstart(): void {
    this.isComposition = true;
  }

  /**
   * 当使用输入法编辑器(例如拼音输入) 输入时, 双向绑定
   * 的 value 并不会更新, 只有在选中输入的值时(即触发
   * compositionend 事件) 才会进行更新, 所以此处需要
   * 手动更新该值到 prompt 渲染器中
   */
  public onCompositionupdate(event: CompositionEvent): void {
    this.selectionStart = (event.target as HTMLTextAreaElement).selectionStart;

    const prompt = this.prompt
      .slice(0, this.selectionStart)
      .concat(event.data, this.prompt.slice(this.selectionStart));

    this.promptChunks = this.buildNormalChunks(prompt);
  }

  public onCompositionend(): void {
    this.isComposition = false;
    this.promptChange.emit(this.prompt);
  }

  public onInput(event: Event): void {
    if (!this.isComposition) {
      /** 当前光标的位置 */
      this.selectionStart = (
        event.target as HTMLTextAreaElement
      ).selectionStart;
      const data = (event as InputEvent).data;

      if (CmdArr.includes(data)) {
        const pre = this.prompt.slice(0, this.selectionStart - 1);
        const after = this.prompt.slice(this.selectionStart - 1);
        this.promptChunks = [
          ...this.buildNormalChunks(pre),
          ...this.buildTipChunks(after),
        ];
        this.isShowVarPanel = true;
      } else {
        this.promptChunks = this.buildNormalChunks(this.prompt);
        this.isShowVarPanel = false;
      }

      this.promptChange.emit(this.prompt);
    }
  }

  /** styledContent 需要随着 textarea 一起滚动 */
  public onScroll(event: Event): void {
    if (event.target === this.textarea.nativeElement) {
      this.styledContent.nativeElement.scrollTop = (
        event.target as HTMLTextAreaElement
      ).scrollTop;
    }
  }

  public getCandidateBGColor(index: number): string {
    return index === this.selectedIndex ? '#f2f2f2' : '';
  }

  @HostListener('window:keydown', ['$event'])
  public handleSelectVar(event: KeyboardEvent): void {
    const len = this.variants.length;

    if (!['ArrowUp', 'ArrowDown', 'Enter'].includes(event.key) || !len) {
      return;
    }

    if (['ArrowUp', 'ArrowDown'].includes(event.key) && this.isShowVarPanel) {
      this.selectedIndex =
        event.key === 'ArrowUp'
          ? (this.selectedIndex - 1 + len) % len
          : (this.selectedIndex + 1) % len;

      this.scrollToVisibleArea();
    }

    if (event.key === 'Enter' && this.isShowVarPanel) {
      this.onSelect(this.variants[this.selectedIndex]);
    }
  }

  public onSelect(val: string) {
    const pre = this.prompt.slice(0, this.selectionStart - 1);
    const after = this.prompt.slice(this.selectionStart);

    this.prompt = `${pre}{{${val}}}${after}`;
    this.promptChunks = this.buildNormalChunks(this.prompt);

    const newSelectionIndex = this.selectionStart + val.length + 3;
    // 更新光标的位置
    this.textarea?.nativeElement.focus();
    window.setTimeout(() => {
      // 更新在变量提示面板中变量选中后光标的位置
      this.textarea?.nativeElement.setSelectionRange(
        newSelectionIndex,
        newSelectionIndex,
      );
    }, 0);

    this.promptChange.emit(this.prompt);
    this.isShowVarPanel = false;
  }

  /** 当有变量输入提示时，阻止默认的方向上下键和 enter 键的行为 */
  private onSelectVar(event: KeyboardEvent): void {
    if (!this.promptChunks.length) {
      return;
    }

    // 没有出现变量选择列表overlay时不阻止键盘默认事件
    if (this.isShowVarPanel) {
      event.preventDefault();
    }
  }

  private buildTipChunks(prompt: string): IPromptChunk[] {
    // 只对第一个字符进行tip chunk的构造，避免后续有同类字符出现两个选择框的情况
    if (prompt.length > 0) {
      if (CmdArr.includes(prompt[0])) {
        const promptStrArr = [prompt[0]];

        if (prompt.length > 1) {
          promptStrArr.push(prompt.slice(1));
        }

        return promptStrArr.map((content) => ({
          type: CmdArr.includes(content) ? 'val' : 'txt',
          content,
        }));
      }

      // fallback
      return this.buildNormalChunks(prompt);
    }

    return [];
  }

  private buildNormalChunks(prompt: string): IPromptChunk[] {
    return [
      {
        type: 'txt',
        content: prompt.endsWith('\n') ? `${prompt} ` : prompt,
      },
    ];
  }

  private scrollToVisibleArea(): void {
    // 获取列表容器的包装元素
    const candidateBoxElement = this.candidateBox?.nativeElement;

    // 获取选中元素的DOM元素
    const selectedElement = this.candidateList.nativeElement.children[
      this.selectedIndex
    ] as HTMLDivElement;

    const candidateBoxRect = candidateBoxElement.getBoundingClientRect();
    const selectedRect = selectedElement.getBoundingClientRect();

    if (
      selectedRect.top < candidateBoxRect.top ||
      selectedRect.bottom > candidateBoxRect.bottom
    ) {
      candidateBoxElement.scrollTop = selectedElement.offsetTop;
    }
  }
}
