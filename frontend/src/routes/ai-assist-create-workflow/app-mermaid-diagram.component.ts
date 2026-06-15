import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  ViewChild,
  SimpleChanges,
  NgZone,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import mermaid from 'mermaid';
import * as d3 from 'd3';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { Subject, debounceTime, takeUntil } from 'rxjs';

@Component({
  selector: 'app-mermaid-diagram',
  standalone: true,
  imports: [CommonModule, MODULES],
  providers: [
    I18NextEagerPipe,
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MODEL_ACCESS, I18nNamespace.AGENT_CENTER],
    },
  ],
  template: `
    <div class="mermaid-outer-wrapper" [class.fullscreen]="isFull">
      <div (mouseenter)="showControls = true"
           (mouseleave)="showControls = false" class="mermaid-container">

        <div *ngIf="title" #mermaidTitle class="mermaid-title">{{ title }}</div>

        <div #mermaidDiagram class="mermaid-content"></div>

        <div *ngIf="isLoading" class="status-overlay">
          <div class="loading-spinner"></div>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .mermaid-outer-wrapper {
        width: 100%;
        height: 100%;
        transition: all 0.2s ease;

        &.fullscreen {
          position: fixed;
          top: 0;
          left: 0;
          width: 100vw;
          height: 100vh;
          z-index: 9999;
          background: #fff;
          padding: 20px;
        }
      }

      .mermaid-container {
        position: relative;
        width: 100%;
        height: 100%;
        overflow: hidden;
      }

      .mermaid-title {
        position: absolute;
        top: 40px;
        left: 0;
        width: 100%;
        z-index: 20;
        pointer-events: none;
        color: rgba(25, 25, 25, 1);
        font-size: 24px;
        font-weight: 600;
        line-height: 36px;
        text-align: center;
        will-change: transform;
      }

      .mermaid-content {
        width: 100%;
        height: 100%;
        cursor: grab;

        ::ng-deep svg {
          width: 100% !important;
          height: 100% !important;
          max-width: none !important;
        }
      }

      .mermaid-controls {
        position: absolute;
        top: 12px;
        right: 12px;
        z-index: 30;
        background: #fff;
        padding: 4px;
        border-radius: 4px;
        display: flex;
        gap: 4px;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
      }

      .status-overlay {
        position: absolute;
        inset: 0;
        display: flex;
        justify-content: center;
        align-items: center;
        background: rgba(255, 255, 255, 0.8);
      }

      .loading-spinner {
        width: 30px;
        height: 30px;
        border: 3px solid #f3f3f3;
        border-top: 3px solid #1890ff;
        border-radius: 50%;
        animation: spin 1s linear infinite;
      }

      @keyframes spin {
        to {
          transform: rotate(360deg);
        }
      }
    `,
  ],
})
export class AppMermaidDiagramComponent
  implements OnChanges, AfterViewInit, OnDestroy
{
  @Input() code: string = '';
  @Input() title: string = '';

  @ViewChild('mermaidDiagram') mermaidDiagram!: ElementRef<HTMLDivElement>;
  @ViewChild('mermaidTitle') mermaidTitle?: ElementRef<HTMLDivElement>;

  public isLoading = false;
  public hasError = false;
  public showControls = false;
  public isFull = false;

  private destroy$ = new Subject<void>();
  private renderSubject = new Subject<string>();

  private svgSelection?: d3.Selection<SVGSVGElement, unknown, null, undefined>;
  private zoomBehavior?: d3.ZoomBehavior<SVGSVGElement, unknown>;

  private diagramBBox?: SVGRect;
  private containerWidth?: number;

  @HostListener('window:resize')
  onResize() {
    this.resetZoom(false);
  }

  constructor(private cdr: ChangeDetectorRef, private zone: NgZone) {
    mermaid.initialize({
      startOnLoad: false,
      theme: 'default',
      flowchart: {
        useMaxWidth: false,
        htmlLabels: true
      },
      securityLevel: 'loose',
      themeCSS: `
        .node foreignObject > div {
          display: inline-block !important;
          white-space: nowrap !important;
          line-height: 24px !important;
          padding: 0 !important;
          margin: 0 !important;
        }
        .node .label, .node text, .nodeLabel {
          font-family: "PingFang SC", "Microsoft YaHei", sans-serif !important;
          font-size: 14px !important;
          white-space: nowrap !important;
          line-height: 24px !important;
        }
        .node foreignObject p {
          margin: 0 !important;
          padding: 0 !important;
          line-height: 24px !important;
          white-space: nowrap !important;
        }
      `
    });

    this.renderSubject
      .pipe(debounceTime(200), takeUntil(this.destroy$))
      .subscribe((c) => this.executeRender(c));
  }

  ngAfterViewInit() {
    if (this.code) {
      this.renderSubject.next(this.code);
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes?.code && !changes.code?.firstChange) {
      this.renderSubject.next(this.code);
    }
  }

  ngOnDestroy() {
    if (this.svgSelection) {
      this.svgSelection.on('.zoom', null);
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  private async executeRender(code: string) {
    if (!this.mermaidDiagram || !code.trim()) {
      return;
    }
    this.isLoading = true;
    this.hasError = false;
    this.cdr.detectChanges();

    try {
      let processedCode = code;

      processedCode = processedCode.replace(/([\[\{\(>])\s*"([^"]*?)"\s*([\]\}\)])/g, (match, open, text, close) => {
        return `${open}"${text.trim()}"${close}`;
      });

      processedCode = processedCode.replace(/([a-zA-Z0-9_-]+)\s*\(\[([^\]]+)\]\)/g, '$1[$2]');
      processedCode = processedCode.replace(/([a-zA-Z0-9_-]+)\s*\(([^)(]+)\)/g, '$1[$2]');

      await mermaid.parse(processedCode);

      const id = `svg-${Date.now()}`;
      const { svg } = await mermaid.render(id, processedCode);

      this.mermaidDiagram.nativeElement.innerHTML = svg;

      this.zone.runOutsideAngular(() => {
        // 在 DOM 更新后初始化 D3
        setTimeout(() => this.initD3Zoom(), 50);
      });
    } catch (e) {
      this.hasError = true;
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  private initD3Zoom() {
    const svgElement = this.mermaidDiagram.nativeElement.querySelector('svg');
    if (!svgElement) {
      return;
    }
    this.svgSelection = d3.select(svgElement);
    this.svgSelection
      .attr('viewBox', null)
      .attr('width', '100%')
      .attr('height', '100%');

    const innerG = this.svgSelection.select('g');

    this.zoomBehavior = d3
      .zoom<SVGSVGElement, unknown>()
      .scaleExtent([0.05, 10])
      .on('zoom', (event) => {
        innerG.attr('transform', event.transform);

        if (this.mermaidTitle?.nativeElement && this.diagramBBox && this.containerWidth) {
          const bbox = this.diagramBBox;
          const k = event.transform.k;

          const svgCenterX = bbox.x + bbox.width / 2;
          const svgTopY = bbox.y;

          const screenCenterX = event.transform.x + svgCenterX * k;
          const screenTopY = event.transform.y + svgTopY * k;

          const dx = screenCenterX - (this.containerWidth / 2);
          const dy = screenTopY - 120;

          this.mermaidTitle.nativeElement.style.transform = `translate(${dx}px, ${dy}px)`;
        }
      });

    this.svgSelection.call(this.zoomBehavior);
    this.resetZoom(false);
  }

  public resetZoom(animated: boolean = true) {
    if (!this.svgSelection || !this.zoomBehavior) {
      return;
    }
    const container = this.mermaidDiagram.nativeElement;
    const innerG = this.svgSelection.select('g').node() as SVGGElement;

    if (!innerG || container.clientWidth === 0) {
      return;
    }
    const bbox = innerG.getBBox();
    const cw = container.clientWidth;
    const ch = container.clientHeight;

    this.diagramBBox = bbox;
    this.containerWidth = cw;

    const padding = 20;
    let scale = Math.min(
      (cw - padding) / bbox.width,
      (ch - padding) / bbox.height,
    );

    const maxScale = this.isFull ? 1.0 : 1.2;
    if (scale > maxScale) {
      scale = maxScale;
    }
    if (!this.isFull && scale < 0.5) {
      scale = 0.5;
    }

    const tx = (cw / 2) - ((bbox.x + (bbox.width / 2)) * scale);
    const ty = 120 - (bbox.y * scale);

    const transform = d3.zoomIdentity.translate(tx, ty).scale(scale);

    if (animated) {
      this.svgSelection
        .transition()
        .duration(500)
        .ease(d3.easeCubicOut)
        .call(this.zoomBehavior.transform, transform);
    } else {
      this.svgSelection.call(this.zoomBehavior.transform, transform);
    }
  }
}
