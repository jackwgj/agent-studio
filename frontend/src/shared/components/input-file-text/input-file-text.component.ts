import {
  ChangeDetectionStrategy,
  Component,
  Input,
  Output,
  EventEmitter,
  ViewChild,
  ElementRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { TextFieldModule } from '@angular/cdk/text-field';
import { cdnAssetUrl } from 'src/single-spa/assets-url';

export interface OutputObject {
  pureText: string;
  tags: Array<{
    id: string;
    status: 'loading' | 'complete';
    name: string;
    index: number;
  }>;
}

@Component({
  selector: 'meta-input-file-text',
  templateUrl: './input-file-text.component.html',
  styleUrls: ['./input-file-text.component.scss'],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MODULES,
    TextFieldModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.FEATURE_OVERVIEW,
        I18nNamespace.SPEC_DRIVEN,
        I18nNamespace.AGENT_CENTER
      ],
    },
  ],
})
export class InputFileTextComponent {
  @ViewChild('editor', { static: true }) editor!: ElementRef<HTMLDivElement>;

  // 从 input-data-params 合并的输入输出
  @Input() isCreate: boolean = false;
  @Input() isSend: boolean = false;
  @Input() isSendDisabled: boolean = false;
  @Input() isErr: boolean = false;
  @Input() initialValue: OutputObject = { pureText: '', tags: [] };
  @Input() isSending: boolean = false;
  @Input() isNeedShadow: boolean = false;

  @Output() contentChange = new EventEmitter<OutputObject>();
  @Output() sendMsg = new EventEmitter<any>();
  @Output() stopSend = new EventEmitter<void>();

  get showBackground() {
    return this.isCreate || this.isSend;
  }

  isText = true;
  inputValue: string = '';
  currentOutput: OutputObject = { pureText: '', tags: [] };
  position: number[] = [];

  // 从 input-data-params 合并的部分
  public changeUrl = cdnAssetUrl;
  switchState = true;
  supportDemoOptions = [
    {
      iconUrl: this.changeUrl('assets/images/demo/demo1.svg'),
      title: this.i18n.transform('overview_demo_title1'),
      value: this.i18n.transform('overview_demo_value1'),
    },
    {
      iconUrl: this.changeUrl('assets/images/demo/demo2.svg'),
      title: this.i18n.transform('overview_demo_title2'),
      value: this.i18n.transform('overview_demo_value2'),
    },
    {
      iconUrl: this.changeUrl('assets/images/demo/demo6.svg'),
      title: this.i18n.transform('overview_demo_title6'),
      value: this.i18n.transform('overview_demo_value6'),
    },
  ];

  // 光标保存
  savedRange: Range | null = null;

  constructor(private i18n: I18NextEagerPipe,) {}

  ngAfterViewInit(): void {
    this.renderInitialValue();
  }

  valueChange(value) {
    this.currentOutput.pureText = value;
    this.contentChange.emit(this.currentOutput);
  }

  beforeInput() {
    const editor = this.editor.nativeElement;
    if (editor.children.length === 0) {
      const selection = window.getSelection();
      const div1 = document.createElement('div');
      div1.appendChild(document.createElement('br'));
      editor.appendChild(div1);
      const newRange = document.createRange();
      newRange.selectNodeContents(div1);
      newRange.collapse(false);

      selection.removeAllRanges();
      selection.addRange(newRange);
    }
  }

  onInput(event: Event): void {
    this.dealTag(this.editor.nativeElement);
    this.updateOutput();
  }

  onPaste(event: ClipboardEvent): void {
    this.beforeInput();
    event.preventDefault();
    // 只允许粘贴纯文本
    let text = event.clipboardData?.getData('text/plain') || '';
    text = text.replaceAll('\r', '');
    if (!this.hasContent()) {
      const editor = this.editor.nativeElement;
      editor.innerText = text;

      const range = document.createRange();
      const selection = window.getSelection();
      range.selectNodeContents(editor);
      range.collapse(false); // 设置为false表示光标在末尾
      selection.removeAllRanges();
      selection.addRange(range);
    } else {
      this.insertText(text);
    }
  }

  onKeydown(event: KeyboardEvent): void {
    if (this.isText) {
      this.onTextKeydown(event);
      return;
    }
    if (event.key === 'Enter' && !event.ctrlKey && (!this.isCreate || this.isSend)) {
      event.preventDefault();
      event.stopPropagation();
      this.sendMessage();
    } else if (event.key === 'Enter' && event.ctrlKey) {
      this.insertLineBreak();
    }
  }

  onTextKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.ctrlKey && (!this.isCreate || this.isSend)) {
      event.preventDefault();
      event.stopPropagation();
      this.sendMessage();
    } else if (event.key === 'Enter' && event.ctrlKey) {
      // 阻止默认的提交行为
      event.preventDefault();
      const textarea: HTMLTextAreaElement = document.getElementById('textarea-resize-none') as HTMLTextAreaElement;
      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      textarea.value = textarea.value.substring(0, start) + "\n" + textarea.value.substring(end);
      // 更新光标位置到下一行
      textarea.selectionStart = textarea.selectionEnd = start + 1;
      this.currentOutput.pureText = (textarea as any).value;
      this.contentChange.emit(this.currentOutput);
      // 更新一下滚动条位置信息
      const line = textarea.value.substring(0, start).split('\n').length + 1;
      const maxLine = textarea.value.split('\n').length + 1;
      const lineHeight = (textarea.scrollHeight + textarea.clientHeight) / maxLine * line;
      const showHeight = lineHeight - textarea.clientHeight;
      textarea.scrollTop = showHeight < 0 ? 0 : showHeight;
    }
  }

  insertText(text: string): void {
    if (!text) {
      return;
    }

    const editor = this.editor.nativeElement;
    const selection = window.getSelection();

    const range = selection.getRangeAt(0);
    range.deleteContents();
    this.insertTextAtRange(range, text);

    // 恢复选区并移动光标
    selection.removeAllRanges();
    selection.addRange(range);

    this.updateOutput();
  }

  // 将文本插入到指定位置
  private insertTextAtRange(range: Range, text: string): void {
    const lines = text.split('\n');
    lines.forEach((line, index) => {
      if (index > 0) {
        const br = document.createElement('br');
        range.insertNode(br);
        range.setStartAfter(br);
        range.setEndAfter(br);
      }

      if (line) {
        const textNode = document.createTextNode(line);
        range.insertNode(textNode);
        range.setStartAfter(textNode);
        range.setEndAfter(textNode);
      }
    });
  }

  private isBackwardSelection(range) {
    return range.startContainer.compareDocumentPosition(range.endContainer) & Node.DOCUMENT_POSITION_PRECEDING;
  }

  private insertLineBreak(): void {
    const selection = window.getSelection();
    if (!selection || selection.rangeCount === 0) {
      return;
    }

    // 范围选择改成位置
    const range = selection.getRangeAt(0);
    let node = null;
    let offset = 0;
    if (this.isBackwardSelection(range)) {
      node = range.startContainer;
      offset = range.startOffset;
    } else {
      node = range.endContainer;
      offset = range.endOffset;
    }
    const newRange = document.createRange();
    newRange.setEnd(node, offset);
    newRange.setStart(node, offset);
    selection.removeAllRanges();
    selection.addRange(newRange);

    const editor = this.editor.nativeElement;
    if (node === editor && editor.children.length === 0) {
      const div1 = document.createElement('div');
      div1.appendChild(document.createElement('br'));
      const div2 = document.createElement('div');
      div2.appendChild(document.createElement('br'));
      editor.appendChild(div1);
      editor.appendChild(div2);

      const newRange = document.createRange();
      newRange.selectNodeContents(div2);
      newRange.collapse(false);

      selection.removeAllRanges();
      selection.addRange(newRange);
    } else {
      if (node === editor) {
        node = editor.childNodes[0];
      }
      const insertNode = this.getNextLineNode(node);
      const isBr = insertNode?.nodeName === 'BR';
      let newLineDiv = null;
      const newRange = document.createRange();
      if (isBr) {
        newLineDiv = document.createElement('br');
        node.parentElement.insertBefore(newLineDiv, insertNode);
        newRange.setStartAfter(newLineDiv);
      } else {
        newLineDiv = document.createElement('div');
        newLineDiv.appendChild(document.createElement('br'));
        if (node.parentElement !== editor && !insertNode) {
          node?.parentElement?.parentElement.insertBefore(newLineDiv, node?.parentElement?.nextSibling);
        } else {
          node.parentElement.insertBefore(newLineDiv, insertNode);
        }
        newRange.selectNodeContents(newLineDiv);
      }
      newRange.collapse(false);
      selection.removeAllRanges();
      selection.addRange(newRange);
    }

    this.updateOutput();
  }

  getNextLineNode(node) {
    const nextNode = node?.nextSibling
    if (!nextNode) {
      return null;
    }
    if (nextNode?.nodeName === 'BR' && nextNode?.nextSibling && (nextNode.nextSibling.nodeName !== 'DIV') || nextNode.nextSibling?.dataset?.noCopy === 'true') {
      return nextNode;
    } else if (nextNode?.nodeName === 'DIV' && nextNode?.dataset?.noCopy !== 'true') {
      return nextNode;
    } else {
      return this.getNextLineNode(nextNode);
    }
  }

  onCopy(event: ClipboardEvent): void {
    const selection = window.getSelection();
    if (!selection || selection.rangeCount === 0) {
      return;
    }

    // 获取纯文本内容（跳过标签元素，保留换行）
    const range = selection.getRangeAt(0);
    const clonedFragment = range.cloneContents();
    const text = this.extractTextWithLineBreaks(clonedFragment);

    // 写入剪贴板
    event.clipboardData?.setData('text/plain', text);
    event.preventDefault();
  }

  // 递归提取文本并保留换行信息
  // 递归遍历提取文本
  private extractTextRecursive(
    node: Node,
    preResult: string[] = []
  ): string {
    const result: string[] = [];
    const blockLevelTags = new Set(['DIV', 'P', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6', 'UL', 'LI', 'BLOCKQUOTE', 'PRE']);
    let hasAddedBlock = false;
    const children = Array.from(node.childNodes);

    for (let i = 0; i < children.length; i++) {
      const child = children[i];

      if (child instanceof HTMLElement && child.dataset.noCopy === 'true') {
        this.position.push(preResult.join('').length + result.join('').length);
        continue;
      }
      // 正常处理
      if (hasAddedBlock) {
        result.push('\n');
        hasAddedBlock = false;
      }
      if (child instanceof HTMLElement && child.tagName === 'BR') {
        if (child.nextSibling || !blockLevelTags.has(child.parentElement.tagName)) {
          result.push('\n');
        }
      } else if (child instanceof HTMLElement) {
        if (blockLevelTags.has(child.tagName)) {
          const innerText = this.extractTextRecursive(child, [...preResult, ...result]);
          result.push(innerText);
          hasAddedBlock = true;
        } else {
          const text = this.extractTextRecursive(child, [...preResult, ...result]);
          result.push(text);
        }
      } else if (child instanceof Text) {
        result.push(child.textContent || '');
        if (this.needAddBlock(child)) {
          result.push('\n');
        }
      }
    }

    return result.join('');
  }

  // 追加第一行是node的时候，需要追加\n的判断
  private needAddBlock(child) {
    if (child.nextSibling && child.nextSibling.nodeName === 'DIV' && child.nextSibling.dataset.noCopy !== 'true') {
      return true;
    }
    return false;
  }

  private extractTextWithLineBreaks(node: Node): string {
    return this.extractTextRecursive(node);
  }

  // === 标签相关方法 ===
  addTag(id: string = '', text: string = '', status: 'loading' | 'complete' = 'loading'): void {
    if (id === '') {
      return;
    }
    this.insertTagElement(id, text, status);
  }

  // 插入标签元素的核心逻辑
  private insertTagElement(id: string, text: string, status: 'loading' | 'complete'): void {
    let insertRange: Range | null = null;

    if (this.savedRange) {
      // 克隆保存的光标位置，避免直接修改原始对象
      insertRange = this.savedRange.cloneRange();
      this.savedRange = null;
    }

    // 仍然没有可用的位置，在编辑器末尾插入
    if (!insertRange) {
      const editor = this.editor.nativeElement;
      insertRange = document.createRange();
      try {
        if (editor.lastChild) {
          insertRange.setStartAfter(editor.lastChild);
        } else {
          insertRange.setStart(editor, 0);
        }
        insertRange.collapse(true);
      } catch (e) {
        try {
          insertRange.selectNodeContents(editor);
          insertRange.collapse(false);
        } catch {
          // 最后的兜底：不设置范围，直接追加到 innerHTML 后面
          insertRange = null;
        }
      }
    }

    // 使用传入的标签ID
    const tagId = id;

    // 创建片段用于一次性插入所有元素
    const fragment = document.createDocumentFragment();

    // 创建标签元素
    const tagElement = document.createElement('div');
    tagElement.className = 'tag-element';
    tagElement.contentEditable = 'false';
    tagElement.dataset.tagId = tagId;
    tagElement.style.userSelect = 'none';
    tagElement.style.webkitUserSelect = 'none';
    tagElement.setAttribute('data-no-copy', 'true');

    // 使用纯 SVG 替代 ti-icon，避免 Angular 编译问题
    const closeIconSvg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    closeIconSvg.setAttribute('viewBox', '0 0 24 24');
    closeIconSvg.setAttribute('fill', 'none');
    closeIconSvg.setAttribute('stroke', 'currentColor');
    closeIconSvg.setAttribute('stroke-width', '2');
    closeIconSvg.setAttribute('stroke-linecap', 'round');
    closeIconSvg.setAttribute('stroke-linejoin', 'round');
    closeIconSvg.setAttribute('class', 'tag-icon-close');
    closeIconSvg.innerHTML = '<line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line>';

    // 删除按钮
    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'tag-delete';
    deleteBtn.type = 'button';
    deleteBtn.contentEditable = 'false';
    deleteBtn.appendChild(closeIconSvg);
    deleteBtn.addEventListener('click', (e: MouseEvent) => {
      e.preventDefault();
      e.stopPropagation();
      tagElement.remove();
      // 删除标签后更新输出值
      this.updateOutput();
      this.editor.nativeElement.focus();
    });

    // 状态图标 - 根据status参数显示不同图标
    const statusIcon = document.createElement('div');
    statusIcon.className = 'tag-icon';

    if (status === 'loading') {
      statusIcon.classList.add('loading-icon');
    } else {
      statusIcon.classList.add('complete-icon');
    }

    // 使用传入的文本内容
    const textSpan = document.createElement('span');
    textSpan.className = 'tag-text';
    textSpan.textContent = text;

    tagElement.appendChild(statusIcon);
    tagElement.appendChild(textSpan);
    tagElement.appendChild(deleteBtn);
    fragment.appendChild(tagElement);

    // 设置标签状态
    tagElement.dataset.status = status;

    // 使用 try-catch 处理可能的 Range 错误
    try {
      if (insertRange) {
        insertRange.collapse(true);
        insertRange.deleteContents();
        insertRange.insertNode(fragment);

        // 将光标移动到标签后
        const newRange = document.createRange();
        newRange.setStartAfter(tagElement);
        newRange.setEndAfter(tagElement);

        const currentSelection = window.getSelection();
        if (currentSelection) {
          currentSelection.removeAllRanges();
          currentSelection.addRange(newRange);
        }
      } else {
        // 如果 Range 创建失败，直接追加到编辑器内容后
        this.editor.nativeElement.appendChild(fragment);
      }
      this.dealTag(this.editor.nativeElement);

      // 标签插入成功后更新输出值
      this.updateOutput();
    } catch (e) {
      // 兜底：直接追加到编辑器内容后
      this.editor.nativeElement.appendChild(fragment);
    }
  }

  updateTagStatus(tagId: string, status: 'loading' | 'complete'): void {
    const editor = this.editor.nativeElement;
    const tagElements = editor.querySelectorAll('.tag-element');

    let targetTag: HTMLElement | null = null;
    for (const element of tagElements) {
      const tag = element as HTMLElement;
      if (tag.dataset.tagId === tagId) {
        targetTag = tag;
        break;
      }
    }

    if (!targetTag) {
      return;
    }

    targetTag.dataset.status = status;

    const statusIcon = targetTag.querySelector('.tag-icon') as HTMLElement;
    if (statusIcon) {
      if (status === 'loading') {
        statusIcon.classList.remove('complete-icon');
        statusIcon.classList.add('loading-icon');
      } else {
        statusIcon.classList.remove('loading-icon');
        statusIcon.classList.add('complete-icon');
      }
    }

    this.updateOutput();
  }

  markTagAsComplete(tagId: string = ''): void {
    if (tagId) {
      this.updateTagStatus(tagId, 'complete');
    }
  }

  // === 输出相关方法 ===
  private updateOutput(): void {
    const output = this.getOutputObject();
    this.currentOutput = output;
    this.contentChange.emit(output);
    if (!this.hasContent()) {
      this.editor.nativeElement.innerHTML = '';
    }
  }

  private getOutputObject(): OutputObject {
    const editor = this.editor.nativeElement;
    const tags: OutputObject['tags'] = [];

    // 使用递归方法提取纯文本（保留换行，跳过标签）
    this.position = [];
    const pureText = this.extractTextWithLineBreaks(editor);

    // 收集所有标签信息
    const tagElements = editor.querySelectorAll('.tag-element');

    // 重新遍历获取每个标签的位置
    tagElements.forEach((tag, index) => {
      const tagEl = tag as HTMLElement;

      tags.push({
        id: tagEl.dataset.tagId || '',
        status: (tagEl.dataset.status as 'loading' | 'complete') || 'loading',
        name: tagEl.querySelector('.tag-text')?.textContent || '',
        index: this.position[index] || 0,
      });
    });

    return { pureText, tags };
  }

  // === 初始值渲染 ===
  private renderInitialValue(): void {
    if (!this.initialValue.pureText) {
      return;
    }

    const editor = this.editor.nativeElement;
    editor.textContent = '';

    // 按 index 从大到小排序，避免插入位置偏移
    const sortedTags = [...this.initialValue.tags].sort((a, b) => b.index - a.index);

    // 先插入纯文本
    editor.textContent = this.initialValue.pureText;

    // 再插入标签
    sortedTags.forEach(tag => {
      const index = Math.min(tag.index, this.initialValue.pureText.length);
      const textBefore = this.initialValue.pureText.slice(0, index);
      const textAfter = this.initialValue.pureText.slice(index);

      // 找到插入位置
      let textNode: ChildNode | null = null;
      let offset = 0;

      for (const child of editor.childNodes) {
        if (child.nodeType === Node.TEXT_NODE) {
          const text = child.textContent || '';
          if (offset + text.length >= index) {
            textNode = child;
            break;
          }
          offset += text.length;
        }
      }

      if (textNode) {
        const splitOffset = index - offset;
        const beforePart = textNode.textContent?.slice(0, splitOffset) || '';
        const afterPart = textNode.textContent?.slice(splitOffset) || '';

        const beforeTextNode = document.createTextNode(beforePart);
        const afterTextNode = document.createTextNode(afterPart);

        textNode.parentNode?.insertBefore(beforeTextNode, textNode);
        textNode.parentNode?.removeChild(textNode);

        const tagElement = this.createTagElement(tag.id, tag.name, tag.status);
        editor.appendChild(tagElement);
        editor.appendChild(afterTextNode);
      } else {
        const tagElement = this.createTagElement(tag.id, tag.name, tag.status);
        editor.appendChild(tagElement);
      }
    });

    this.updateOutput();
  }

  private createTagElement(id: string, text: string, status: 'loading' | 'complete'): HTMLElement {
    const tagElement = document.createElement('span');
    tagElement.className = 'tag-element';
    tagElement.contentEditable = 'false';
    tagElement.dataset.tagId = id;
    tagElement.dataset.status = status;
    tagElement.dataset.noCopy = 'true';

    const content = document.createElement('span');
    content.className = 'tag-content';
    content.style.cssText = 'display: inline-flex; align-items: center; gap: 4px;';

    const statusIcon = document.createElement('div');
    statusIcon.className = 'tag-icon';

    if (status === 'loading') {
      statusIcon.classList.add('loading-icon');
    } else {
      statusIcon.classList.add('complete-icon');
    }

    const textSpan = document.createElement('span');
    textSpan.className = 'tag-text';
    textSpan.textContent = text;

    const deleteBtn = document.createElement('span');
    deleteBtn.className = 'tag-delete';
    deleteBtn.textContent = '×';
    deleteBtn.style.cssText = 'cursor: pointer; margin-left: 4px; font-size: 14px;';
    deleteBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      tagElement.remove();
      this.updateOutput();
      this.editor.nativeElement.focus();
    });

    content.appendChild(statusIcon);
    content.appendChild(textSpan);
    content.appendChild(deleteBtn);
    tagElement.appendChild(content);

    return tagElement;
  }

  // === input-data-params 相关方法 ===
  hasContent(): boolean {
    return (this.currentOutput.pureText.length > 0 && this.currentOutput.pureText !== '\n') || this.currentOutput.tags.length > 0;
  }

  showInitializeLabel(show: boolean): void {
    // 保存当前光标位置（因为点击按钮后焦点会离开编辑器）
    const selection = window.getSelection();
    if (selection && selection.rangeCount > 0) {
      this.savedRange = selection.getRangeAt(0).cloneRange();
    }
  }

  sendMessage(): void {
    if (!this.hasContent()) {
      return;
    }
    if (this.isSendDisabled) {
      return;
    }
    if (this.isSending) {
      return;
    }
    this.sendMsg.emit(this.currentOutput);
  }

  clearData() {
    if (this.isText) {
      this.inputValue = '';
      this.currentOutput.pureText = '';
      this.contentChange.emit(this.currentOutput);
    } else {
      this.editor.nativeElement.innerHTML = '';
      this.updateOutput();
    }
  }

  stopSendMsg(): void {
    this.stopSend.emit();
  }

  selectDemo(option: any): void {
    if (this.isText) {
      const textarea = document.getElementById('textarea-resize-none');
      (textarea as any).value = option.value;
      this.currentOutput.pureText = (textarea as any).value;
      this.contentChange.emit(this.currentOutput);
      return;
    }
    const editor = this.editor.nativeElement;
    editor.innerText = option.value;
    this.updateOutput();
  }

  dealTag(ele: any) {
    const length = ele.childNodes.length;
    for (let i = length - 1; i >= 0; i--) {
      if (ele.childNodes[i].nodeName === 'BR' && ele.childNodes[i+1]?.dataset?.noCopy === 'true' && ele.childNodes[i+1]?.nodeName === 'DIV') {
        const data = ele.childNodes[i+1];
        const newLineDiv = document.createElement('div');
        newLineDiv.appendChild(data);
        ele.insertBefore(newLineDiv, ele.childNodes[i]);
        ele.childNodes[i+1].remove();
      } else if (ele.childNodes[i].nodeName === 'DIV' && ele.childNodes[i].dataset?.noCopy !== 'true') {
        this.dealTag(ele.childNodes[i]);
      }
    }
  }

  upLoadFile(): void {
    console.log('upLoadFile');
    this.addTag('123','byc');
  }

  optimizePrompt(): void {
    console.log('Optimize prompt clicked');
  }
}
