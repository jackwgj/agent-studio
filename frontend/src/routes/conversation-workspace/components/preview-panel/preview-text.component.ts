import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  Output,
} from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import DOMPurify from 'dompurify';
import { marked } from 'marked';
import type { PreviewState } from '../../preview.model';
import { ConversationWorkspaceService } from '../../conversation-workspace.service';

/** 超过该字符数只渲染前一段，避免大文件卡死渲染。 */
const MAX_CHARS = 500000;

/**
 * 纯文本预览渲染器（md / txt）。
 *
 * 取数采用渐进增强：
 * 1. 有 objectKey 时走后端同源下载（无跨域、无鉴权问题，主路径）
 * 2. 仅外链时先尝试 fetch，对方开放 CORS 则富渲染
 * 3. fetch 失败（CORS 被拒）则上抛 fallback，由父级改用 iframe 原生文本渲染兜底
 */
@Component({
  selector: 'app-preview-text',
  templateUrl: './preview-text.component.html',
  styleUrls: ['./preview-text.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [CommonModule],
})
export class PreviewTextComponent implements OnChanges {
  @Input() state!: PreviewState;

  /** 外链取数被 CORS 拒绝，请求改用 iframe 原生渲染 */
  @Output() readonly fallback = new EventEmitter<PreviewState>();
  /** 正文内的链接被点击，请求在面板内继续预览 */
  @Output() readonly navigate = new EventEmitter<string>();

  html: SafeHtml | null = null;
  plainText = '';
  loading = false;
  error = '';
  truncated = false;

  private seq = 0;

  constructor(
    private readonly sanitizer: DomSanitizer,
    private readonly cdr: ChangeDetectorRef,
    private readonly conversationService: ConversationWorkspaceService,
  ) {}

  public ngOnChanges(): void {
    void this.load();
  }

  /** 正文链接点击：拦截后在面板内继续预览，而不是跳走。 */
  @HostListener('click', ['$event'])
  public onClick(event: MouseEvent): void {
    const anchor = (event.target as HTMLElement | null)?.closest?.('a');
    if (!anchor) {
      return;
    }
    event.preventDefault();
    const href = anchor.getAttribute('href');
    if (href) {
      this.navigate.emit(href);
    }
  }

  public async load(): Promise<void> {
    const token = ++this.seq;
    this.loading = true;
    this.error = '';
    this.truncated = false;
    this.html = null;
    this.plainText = '';
    this.cdr.markForCheck();

    try {
      const text = await this.fetchText();
      if (token !== this.seq) {
        return;
      }
      this.render(text);
    } catch (e) {
      if (token !== this.seq) {
        return;
      }
      // 外链场景：CORS 被拒 → 交给 iframe 原生渲染兜底
      if (this.state.url && !this.state.objectKey) {
        this.fallback.emit({ ...this.state, type: 'url', key: `${this.state.key}-fallback` });
        return;
      }
      this.error = '内容加载失败，请重试';
    } finally {
      if (token === this.seq) {
        this.loading = false;
        this.cdr.markForCheck();
      }
    }
  }

  private async fetchText(): Promise<string> {
    if (this.state.objectKey && this.state.conversationId) {
      const blob = await this.conversationService.downloadArtifact(
        this.state.conversationId,
        this.state.objectKey,
      );
      return blob.text();
    }
    if (this.state.url) {
      const response = await fetch(this.state.url, { credentials: 'omit' });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      return response.text();
    }
    throw new Error('缺少可预览的内容来源');
  }

  private render(text: string): void {
    if (text.length > MAX_CHARS) {
      this.plainText = text.slice(0, MAX_CHARS);
      this.truncated = true;
    } else {
      this.plainText = text;
    }
    const source = this.truncated ? this.plainText : text;

    if (this.state.type === 'md') {
      const rawHtml = marked(source) as string;
      const purified = DOMPurify.sanitize(rawHtml);
      this.html = this.sanitizer.bypassSecurityTrustHtml(purified);
    }
    this.cdr.markForCheck();
  }
}
