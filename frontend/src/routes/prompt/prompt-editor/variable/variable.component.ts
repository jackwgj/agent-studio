import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnChanges,
  SimpleChanges,
  ViewChild,
} from '@angular/core';

import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import { COMMON_MODULES } from '@shared/modules';
import { VariableInsertComponent } from '@routes/prompt/prompt-editor/variable-insert/variable-insert.component';
import { VariableService } from '@services/agent-center/prompt-optimize-task/prompt-editor.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'meta-variable',
  standalone: true,
  imports: [COMMON_MODULES, VariableInsertComponent],
  templateUrl: './variable.component.html',
  styleUrl: './variable.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VariableComponent implements OnChanges {
  promptType: 'text' | 'multi';
  // 仅展示控制
  onlyShow = false;
  // 变量编辑控制
  canEdit = false;
  // 变量类型
  type: 'text' | 'image';
  // 初始设置的变量值
  variable: string; //仅初始化使用
  // 类型选择开关
  isChangeType = false;
  // 内部存储的实际变量值
  variableNowValue: string; //实际存储值
  // 失焦回调
  onBlur: () => void = () => {};
  // 变量值修改时的回调
  onChange: (variable: any) => void = () => {};
  private variableInserterId = Math.random().toString(36).substring(2, 15);
  private variableSubscription: Subscription;
  @ViewChild('variableContainer', { read: ElementRef, static: true })
  variableContainer: ElementRef;
  @ViewChild('variableInsertRef', { read: ElementRef, static: true })
  variableInsertRef: ElementRef;

  constructor(
    private cdr: ChangeDetectorRef,
    private variableServ: VariableService,
  ) {
    this.variableSubscription = this.variableServ.variableId$.subscribe(
      (id) => {
        if (this.variableInserterId === id) {
          return;
        }
        //其他插入器被打开时，关闭当前插入器
        this.hideTypeChange();
      },
    );
  }

  ngOnInit() {
    if (this.onlyShow) {
      // 仅展示模式
      this.canEdit = false;
      return;
    }
    this.variableNowValue = this.variable;
    this.hideTypeChange();
  }

  ngOnChanges(changes: SimpleChanges): void {}

  ngOnDestroy() {
    this.variableSubscription?.unsubscribe();
  }

  focus() {
    //等待页面渲染完成后，设置光标位置
    setTimeout(() => {
      const range = document.createRange();
      range.selectNodeContents(this.variableContainer.nativeElement); // 选中元素内所有内容
      range.collapse(false); // 折叠范围到结尾（false 表示终点，true 表示起点）
      const selection = window.getSelection();
      selection.removeAllRanges(); // 清除现有选区
      selection.addRange(range); // 添加新范围（光标）
      this.variableContainer.nativeElement.focus();
    }, 0);
  }

  onInput(event: any) {
    event.stopPropagation(); // 阻止事件传递
    if (this.onlyShow) {
      // 仅展示模式
      this.canEdit = false;
      return;
    }
    this.onChange({
      origin: {
        type: this.type,
        value: this.variableNowValue,
      },
      target: {
        type: this.type,
        value: event.currentTarget.innerText,
      },
    });
    this.variableNowValue = event.currentTarget.innerText;
  }

  public setPromptType(promptType: 'text' | 'multi') {
    this.promptType = promptType;
    // 提示词切换为文本类型时，将变量类型设置为文本类型
    if(this.promptType=== 'text' && this.type !== 'text'){
      this.type = 'text';
      this.onChange({
        origin: {
          type: 'image',
          value: this.variableNowValue,
        },
        target: {
          type: this.type,
          value: this.variableNowValue,
        },
      });
    }
    this.cdr.markForCheck();
  }

  public onClick() {
    if (this.onlyShow) {
      // 仅展示模式
      this.canEdit = false;
      return;
    }
    if (this.canEdit) {
      return;
    }
    this.canEdit = true; // 点击时设置为可编辑
    this.focus();
  }

  public setOnBlur(fn: () => void) {
    this.onBlur = () => {
      this.canEdit = false;
      this.hideTypeChange();
      fn();
    };
  }

  public setOnChange(fn: (variable: any) => void) {
    this.onChange = fn;
  }

  public onEnterKeyPress(event: any) {
    event.preventDefault(); // 阻止默认的回车行为
    event.stopPropagation(); // 阻止事件传递
    this.onBlur();
  }

  protected readonly changeUrl = cdnAssetUrl;

  public handleTypeChange(type) {
    try {
      if (this.type !== type) {
        this.type = type;
        this.onChange({
          origin: {
            type: !this.type,
            value: this.variableNowValue,
          },
          target: {
            type: this.type,
            value: this.variableNowValue,
          },
        });
      }
    } finally {
      this.hideTypeChange();
    }
  }

  public showTypeChange() {
    this.variableServ.setId(this.variableInserterId);
    this.isChangeType = true;
    // 获取 variable-box 的位置
    const boxRect =
      this.variableContainer.nativeElement.getBoundingClientRect();
    // 获取 variableInsertRef 的 ElementRef
    const insertRef = this.variableInsertRef.nativeElement;
    // 设置 variableInsertRef 的位置
    insertRef.style.position = 'fixed';
    insertRef.style.top = `${boxRect.bottom + 5}px`; // 在 variable-box 下方 5px
    insertRef.style.left = `${boxRect.left-35}px`; // 与 variable-box 左边对齐
  }

  public hideTypeChange() {
    this.isChangeType = false;
    // 主动进行变更检测，不然选择器关不掉
    this.cdr.markForCheck();
  }
  public setType(type: 'text' | 'image') {
    this.type = type;
    this.cdr.markForCheck();
  }
}
