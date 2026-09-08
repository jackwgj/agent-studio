import {
  Directive,
  ElementRef,
  EventEmitter,
  Input,
  NgZone,
  OnDestroy,
  Output,
  Renderer2,
} from '@angular/core';

/**
 * 预览面板宽度拖拽指令。
 *
 * - 鼠标：按下后跟随指针，按容器宽度换算百分比并钳制在 [min, max]
 * - 键盘：聚焦后 ← / → 每次调整 step 个百分点（无障碍要求）
 * - 拖拽期间事件挂在 document 上，松开即解绑，避免监听泄漏
 */
@Directive({
  selector: '[appPreviewResizer]',
  standalone: true,
  host: {
    '(mousedown)': 'onMouseDown($event)',
    '(keydown)': 'onKeyDown($event)',
  },
})
export class PreviewResizerDirective implements OnDestroy {
  /** 最小宽度百分比 */
  @Input() min = 30;
  /** 最大宽度百分比 */
  @Input() max = 60;
  /** 键盘单次调整步长（百分点） */
  @Input() step = 2;
  /** 绝对像素下限，避免窄屏时百分比换算后过窄 */
  @Input() minPx = 360;
  /** 绝对像素上限，避免超宽屏时百分比换算后过宽 */
  @Input() maxPx = 1080;
  /** 当前宽度百分比，键盘调整时基于它累加 */
  @Input() width = 60;

  @Output() widthChange = new EventEmitter<number>();

  private dragging = false;
  private disposers: Array<() => void> = [];

  constructor(
    private readonly host: ElementRef<HTMLElement>,
    private readonly renderer: Renderer2,
    private readonly zone: NgZone,
  ) {}

  public ngOnDestroy(): void {
    this.release();
  }

  public onMouseDown(event: MouseEvent): void {
    event.preventDefault();
    this.dragging = true;
    this.renderer.addClass(this.host.nativeElement, 'dragging');
    this.renderer.addClass(document.body, 'preview-resizing');

    // 拖拽期间的所有事件都在 zone 外处理，避免每帧触发变更检测
    this.zone.runOutsideAngular(() => {
      const onMove = (moveEvent: MouseEvent) => this.applyFromPointer(moveEvent.clientX);
      const onUp = () => this.release();
      document.addEventListener('mousemove', onMove);
      document.addEventListener('mouseup', onUp);
      this.disposers = [
        () => document.removeEventListener('mousemove', onMove),
        () => document.removeEventListener('mouseup', onUp),
      ];
    });
  }

  public onKeyDown(event: KeyboardEvent): void {
    if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') {
      return;
    }
    event.preventDefault();
    // 面板在右侧：← 变宽，→ 变窄
    const delta = event.key === 'ArrowLeft' ? this.step : -this.step;
    this.emitWidth(this.clampPercent(this.width + delta));
  }

  private applyFromPointer(clientX: number): void {
    const container = this.host.nativeElement.parentElement;
    if (!container) {
      return;
    }
    const rect = container.getBoundingClientRect();
    if (!rect.width) {
      return;
    }
    const px = rect.right - clientX;
    const percent = (px / rect.width) * 100;
    this.emitWidth(this.clampPercent(percent));
  }

  /** 先按百分比钳制，再按绝对像素钳制，最后回到百分比。 */
  private clampPercent(percent: number): number {
    const container = this.host.nativeElement.parentElement;
    let next = Math.min(this.max, Math.max(this.min, percent));
    if (container) {
      const width = container.getBoundingClientRect().width;
      if (width) {
        const px = (next / 100) * width;
        const clampedPx = Math.min(this.maxPx, Math.max(this.minPx, px));
        next = (clampedPx / width) * 100;
        next = Math.min(this.max, Math.max(this.min, next));
      }
    }
    return Math.round(next * 10) / 10;
  }

  private emitWidth(next: number): void {
    if (Math.abs(next - this.width) < 0.05) {
      return;
    }
    this.width = next;
    // 事件流来自 zone 外，需要回到 zone 内再发射，保证视图更新
    this.zone.run(() => this.widthChange.emit(next));
  }

  private release(): void {
    if (!this.dragging) {
      return;
    }
    this.dragging = false;
    for (const dispose of this.disposers) {
      dispose();
    }
    this.disposers = [];
    this.renderer.removeClass(this.host.nativeElement, 'dragging');
    this.renderer.removeClass(document.body, 'preview-resizing');
  }
}
