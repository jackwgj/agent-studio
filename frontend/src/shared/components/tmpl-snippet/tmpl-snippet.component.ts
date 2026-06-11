import {
  Component,
  ElementRef,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import type {
  IPromptChunk,
  IVariant,
} from '@interfaces/prompt/prompt-editor.interface';
import { PromptEditorService } from '@services/prompt-editor.service';
import { MODULES } from '@shared/modules';
import { Subject, takeUntil } from 'rxjs';
import { OverlayModule } from '@angular/cdk/overlay';
import { SNIPPET_REG } from '@constants/exp-tmpl-config.const';

@Component({
  selector: 'tmpl-snippet',
  templateUrl: './tmpl-snippet.component.html',
  imports: [MODULES, OverlayModule],
  styleUrls: ['./tmpl-snippet.component.less'],
  standalone: true,
})
export class TmplSnippetComponent implements OnInit, OnDestroy {
  @Input() snippetChunkProps: IPromptChunk;

  @ViewChild('candidateBox') candidateBox: ElementRef<HTMLDivElement>;

  @ViewChild('candidateList') candidateList: ElementRef<HTMLDivElement>;

  public varPool: IVariant[] = [];

  public candidates: IVariant[] = [];

  public selectedIndex = 0;

  private destroy$ = new Subject<void>();

  constructor(private promptEditorServ: PromptEditorService) {}

  ngOnInit() {
    this.subVarPoolUpdate();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  public subVarPoolUpdate(): void {
    this.promptEditorServ
      .varPoolUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe((varPool) => {
        this.varPool = varPool;
        this.candidates = this.getCandidates();
      });
  }

  public getCandidates(): IVariant[] {
    return this.varPool.filter((v) =>
      v.key.includes(this.snippetChunkProps.value),
    );
  }

  public onSelected(variant: IVariant): void {
    // 选中的变量的位置位于光标前一段 prompt 的子串中
    const selectionStart = this.promptEditorServ.getSelectionStart();
    const prompt = this.promptEditorServ.getPrompt();
    const pre = prompt.slice(0, selectionStart);
    const next = prompt.slice(selectionStart);
    const newPrompt = pre.replace(SNIPPET_REG, variant.key).concat(next);

    this.promptEditorServ.setPrompt(newPrompt);

    // 更新光标的位置
    this.promptEditorServ.setSelectionStart(
      selectionStart +
        (variant.key.length - this.snippetChunkProps.value.length),
    );
  }

  public getCandidateBGColor(index: number): string {
    return index === this.selectedIndex ? '#f2f2f2' : '';
  }

  @HostListener('window:keydown', ['$event'])
  public handleSelectVar(event: KeyboardEvent): void {
    const len = this.candidates.length;

    if (!['ArrowUp', 'ArrowDown', 'Enter'].includes(event.key) || !len) {
      return;
    }

    if (['ArrowUp', 'ArrowDown'].includes(event.key)) {
      this.selectedIndex =
        event.key === 'ArrowUp'
          ? (this.selectedIndex - 1 + len) % len
          : (this.selectedIndex + 1) % len;

      this.scrollToVisibleArea();
    } else if (event.key === 'Enter') {
      this.onSelected(this.candidates[this.selectedIndex]);
    }
  }

  private scrollToVisibleArea(): void {
    // 获取列表容器的包装元素
    const candidateBoxElement = this.candidateBox.nativeElement;

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
