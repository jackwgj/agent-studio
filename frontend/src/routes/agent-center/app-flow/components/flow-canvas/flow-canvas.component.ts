import {
  Component,
  OnInit,
  ViewChild,
  ElementRef,
  AfterViewInit,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
} from '@angular/core';

@Component({
  selector: 'flow-canvas',
  templateUrl: './flow-canvas.component.html',
  styleUrls: ['./flow-canvas.component.less'],
  standalone: true,
})
export class FlowCanvasComponent implements OnInit, AfterViewInit, OnChanges {
  @ViewChild('containerBg', { static: true })
  containerRef!: ElementRef<HTMLDivElement>;

  @ViewChild('canvasBg', { static: true })
  canvasRef!: ElementRef<HTMLCanvasElement>;

  @Input() initialTranslate: { x: number; y: number } = { x: 0, y: 0 };

  @Input() initialScale = 1;

  @Output() translateChange = new EventEmitter<{ x: number; y: number }>();

  @Output() scaleChange = new EventEmitter<number>();

  private scale = 1;

  private translate: { x: number; y: number } = { x: 0, y: 0 };

  private ctx: CanvasRenderingContext2D | null = null;

  ngOnInit(): void {
    this.scale = this.initialScale;
    this.translate = { ...this.initialTranslate };
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.initialTranslate?.currentValue) {
      const { x, y } = changes.initialTranslate.currentValue;

      const rate = this.scale * 2;
      this.translate = {
        x: -Math.floor(x / rate),
        y: -Math.floor(y / rate),
      };

      this.draw();
    }

    if (changes.initialScale?.currentValue) {
      this.scale = changes.initialScale.currentValue;
      this.resizeCanvas();
    }
  }

  ngAfterViewInit(): void {
    const canvas = this.canvasRef.nativeElement;
    this.ctx = canvas.getContext('2d');
    if (!this.ctx) return;

    this.resizeCanvas();
    this.draw();
  }

  private resizeCanvas = (): void => {
    const container = this.containerRef.nativeElement;
    const canvas = this.canvasRef.nativeElement;
    canvas.width = container.clientWidth;
    canvas.height = container.clientHeight;
    this.draw();
  };

  private draw = (): void => {
    if (!this.ctx || !this.canvasRef.nativeElement) return;

    const canvas = this.canvasRef.nativeElement;

    this.ctx.clearRect(0, 0, canvas.width, canvas.height);

    this.ctx.fillStyle = '#f7f7f8';
    this.ctx.fillRect(0, 0, canvas.width, canvas.height);

    const gridSize = 48;
    const plusSize = 10;

    this.ctx.strokeStyle = '#e7ebed';
    this.ctx.lineWidth = 2;

    const visibleWidth = canvas.width / this.scale;
    const visibleHeight = canvas.height / this.scale;

    const startX =
      Math.floor(this.translate.x / gridSize) * gridSize -
      (this.translate.x % gridSize);
    const startY =
      Math.floor(this.translate.y / gridSize) * gridSize -
      (this.translate.y % gridSize);
    const endX = startX + visibleWidth + gridSize;
    const endY = startY + visibleHeight + gridSize;

    this.ctx.save();
    this.ctx.scale(this.scale, this.scale);
    this.ctx.translate(-this.translate.x, -this.translate.y);

    for (let x = startX; x < endX; x += gridSize) {
      for (let y = startY; y < endY; y += gridSize) {
        this.ctx.beginPath();
        this.ctx.moveTo(x - plusSize / 2, y);
        this.ctx.lineTo(x + plusSize / 2, y);
        this.ctx.moveTo(x, y - plusSize / 2);
        this.ctx.lineTo(x, y + plusSize / 2);
        this.ctx.stroke();
      }
    }

    this.ctx.restore();
  };
}
