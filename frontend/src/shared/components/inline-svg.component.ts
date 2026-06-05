import {
  Component,
  Input,
  ElementRef,
  OnChanges,
  SimpleChanges,
  Renderer2,
  OnDestroy,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { Observable, Subject, shareReplay, takeUntil } from 'rxjs';

/**
 * 使用该组件时，需要将svg动态填充颜色的路径的fill或stroke属性设置为currentColor，即可通过字体颜色控制svg
 * 例：src/assets/agent-center/icons/attachment.svg
 */
@Component({
  selector: 'meta-inline-svg',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="text-[1em]" [ngClass]="customWrapperClass">
      <ng-container *ngIf="error">Failed to load SVG</ng-container>
    </div>
  `,
})
export class InlineSvgComponent implements OnChanges, OnDestroy {
  @Input() src!: string;
  @Input() svgClass?: string; // 给 SVG 根节点加 class
  @Input() wrapperClass?: { [key: string]: boolean }; // 包裹 div 的类名
  @Input() isInline = true; // isInline为false时，组件dom宽高完全由svg决定，解决flex布局时交叉轴总是对不齐问题

  public get customWrapperClass() {
    return {
      'inline-block': this.isInline,
      'align-[-0.125em]': this.isInline,
      ...this.wrapperClass,
    };
  }

  public error = false;

  private static svgCache = new Map<string, string>();

  private static loadingCache = new Map<string, Observable<string>>();

  private destroy$ = new Subject<void>();

  constructor(
    private el: ElementRef,
    private http: HttpClient,
    private renderer: Renderer2,
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes.src && this.src) {
      this.insertSvg();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private insertSvg() {
    this.error = false;

    const container: HTMLElement = this.el.nativeElement.querySelector('div');
    if (!container) return;

    const cached = InlineSvgComponent.svgCache.get(this.src);
    if (cached) {
      this.renderSvg(cached, container);
      return;
    }

    const loading$ = InlineSvgComponent.loadingCache.get(this.src);
    if (loading$) {
      loading$.pipe(takeUntil(this.destroy$)).subscribe({
        next: (svg) => this.renderSvg(svg, container),
        error: () => {
          this.error = true;
        },
      });
      return;
    }

    // 👉 发起一次请求并缓存 Observable（仅首次）
    const request$ = this.http
      .get(cdnAssetUrl(this.src), { responseType: 'text' })
      .pipe(
        shareReplay(1), // 多个订阅共享一个请求
      );

    InlineSvgComponent.loadingCache.set(this.src, request$);

    request$.pipe(takeUntil(this.destroy$)).subscribe({
      next: (svg) => {
        InlineSvgComponent.svgCache.set(this.src, svg);
        InlineSvgComponent.loadingCache.delete(this.src); // 清理进行中的请求缓存
        this.renderSvg(svg, container);
      },
      error: () => {
        InlineSvgComponent.loadingCache.delete(this.src);
        this.error = true;
      },
    });
  }

  private renderSvg(raw: string, container: HTMLElement) {
    // 清空容器
    container.innerHTML = '';

    // 创建临时 DOM
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = raw;
    const svg = tempDiv.querySelector('svg');

    if (!svg) {
      this.error = true;
      return;
    }
    svg.style.width = '1em';
    svg.style.height = '1em';
    svg.style.fontSize = 'inherit';
    svg.style.fill = 'currentColor';

    // 设置 class
    if (this.svgClass) {
      svg.setAttribute('class', this.svgClass);
    }

    // 插入到 DOM
    this.renderer.appendChild(container, svg);

    this.error = false;
  }
}
