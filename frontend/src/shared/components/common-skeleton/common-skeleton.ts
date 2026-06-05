import { Component, Input, ChangeDetectionStrategy, Renderer2, Inject, OnDestroy, OnInit } from '@angular/core';
import { CommonModule, DOCUMENT } from '@angular/common';

type SkeletonType = 'text' | 'circular' | 'rectangular' | 'rounded' | 'avatar' | 'card' | 'list' | 'article' | 'table';

@Component({
  selector: 'common-skeleton',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @switch (type) {
      @case ('text') {
        <div [class]="getBaseClasses('rounded')" [style]="getShimmerStyles()" [style.width]="width" [style.height]="height"></div>
      }
      @case ('circular') {
        <div [class]="getBaseClasses('rounded-full')" [style]="getShimmerStyles()" [style.width]="size" [style.height]="size"></div>
      }
      @case ('rectangular') {
        <div [class]="getBaseClasses('rounded-none')" [style]="getShimmerStyles()" [style.width]="width" [style.height]="height"></div>
      }
      @case ('rounded') {
        <div [class]="getBaseClasses('rounded-xl')" [style]="getShimmerStyles()" [style.width]="width" [style.height]="height"></div>
      }
      @case ('avatar') {
        <div [class]="getBaseClasses('rounded-full flex items-center justify-center')" [style]="getShimmerStyles()" [style.width]="size" [style.height]="size">
          <svg viewBox="0 0 100 100" class="w-3/5 h-3/5 opacity-30">
            <circle cx="50" cy="35" r="20" fill="currentColor"/>
            <ellipse cx="50" cy="90" rx="40" ry="25" fill="currentColor"/>
          </svg>
        </div>
      }
      @case ('card') {
        <div [class]="cardClass">
          <div [class]="getBaseClasses('rounded-t-xl')" [style]="getShimmerStyles()" style="width: 100%;" [style.height]="cardImageHeight"></div>
          <div class="p-4 space-y-3">
            @for (line of cardLines; track $index) {
              <div [class]="getBaseClasses('rounded')" [style.width]="line.width" [style.height]="line.height" [style]="getShimmerStyles()"></div>
            }
          </div>
        </div>
      }
      @case ('list') {
        <div class="w-full flex flex-col" [style.gap]="listGap * 4 + 'px'">
          @for (item of listItemsArray; track $index) {
            <div class="w-full flex flex-col" [style.gap]="listLineGap * 4 + 'px'">
              @for (line of listLines; track $index) {
                <div [class]="getBaseClasses('rounded')" [style.width]="line.width" [style.height]="line.height" [style]="getShimmerStyles()"></div>
              }
            </div>
          }
        </div>
      }
      @case ('article') {
        <div [class]="articleClass">
          <div [class]="getBaseClasses('rounded')" [style.width]="articleTitleWidth" [style.height]="articleTitleHeight" [style]="getShimmerStyles()"></div>
          <div [class]="getBaseClasses('rounded-xl')" style="width: 100%;" [style.height]="articleImageHeight" [style]="getShimmerStyles()"></div>
          @for (line of articleLines; track $index) {
            <div [class]="getBaseClasses('rounded')" [style.width]="line.width" [style.height]="line.height" [style]="getShimmerStyles()"></div>
          }
        </div>
      }
      @case ('table') {
        <div class="w-full border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden">
          <div class="flex p-4 bg-gray-50 dark:bg-slate-800 border-b border-gray-200 dark:border-gray-700" [class]="tableHeaderGap">
            @for (col of tableColumns; track $index) {
              <div [class]="getBaseClasses('rounded flex-1')" [style.height]="tableHeaderHeight" [style]="getShimmerStyles()"></div>
            }
          </div>
          @for (row of tableRowsArray; track $index) {
            <div class="flex p-3 border-b border-gray-200 dark:border-gray-700 last:border-b-0" [class]="tableRowGap">
              @for (col of tableColumns; track $index) {
                <div [class]="getBaseClasses('rounded flex-1')" [style.width]="getTableCellWidth($index)" [style.height]="getTableCellHeight()" [style]="getShimmerStyles()"></div>
              }
            </div>
          }
        </div>
      }
    }
  `,
})
export class CommonSkeletonComponent implements OnInit, OnDestroy {
  @Input() type: SkeletonType = 'text';
  @Input() width = '100%';
  @Input() height = '1rem';
  @Input() size = '40px';

  @Input() animation: 'pulse' | 'shimmer' | 'none' = 'shimmer';
  @Input() baseColor = 'bg-gray-200';
  @Input() darkBaseColor = 'dark:bg-slate-700';

  // Card 配置
  @Input() cardClass = 'bg-white dark:bg-slate-800 rounded-xl shadow-md overflow-hidden';
  @Input() cardImageHeight = '160px';
  @Input() cardLines: { width: string; height: string }[] = [
    { width: '80%', height: '20px' },
    { width: '60%', height: '16px' },
    { width: '40%', height: '16px' }
  ];

  // List 配置 - 间距以4的倍数为单位
  @Input() set listItems(value: number) {
    this._listItems = value;
    this.listItemsArray = new Array(value).fill(0);
  }
  get listItems(): number {
    return this._listItems;
  }
  private _listItems = 4;
  listItemsArray: number[] = new Array(4).fill(0);

  @Input() listGap = 4; // 项间距，4的倍数，默认16px
  @Input() listLineGap = 2; // 行间距，4的倍数，默认8px
  @Input() listLines: { width: string; height: string }[] = [
    { width: '70%', height: '16px' },
    { width: '100%', height: '12px' }
  ];

  // Article 配置
  @Input() articleClass = 'max-w-3xl space-y-4';
  @Input() articleTitleWidth = '90%';
  @Input() articleTitleHeight = '32px';
  @Input() articleImageHeight = '200px';
  @Input() articleLines: { width: string; height: string }[] = [
    { width: '100%', height: '16px' },
    { width: '100%', height: '16px' },
    { width: '100%', height: '16px' },
    { width: '100%', height: '16px' },
    { width: '60%', height: '16px' }
  ];

  // Table 配置
  @Input() tableColumns: { field: string; width?: string }[] = [
    { field: 'col1', width: '60%' },
    { field: 'col2' },
    { field: 'col3' },
    { field: 'col4' }
  ];

  @Input() set tableRows(value: number) {
    this._tableRows = value;
    this.tableRowsArray = new Array(value).fill(0);
  }
  get tableRows(): number {
    return this._tableRows;
  }
  private _tableRows = 5;
  tableRowsArray: number[] = new Array(5).fill(0);

  @Input() tableHeaderHeight = '20px';
  @Input() tableHeaderGap = 'space-x-4';
  @Input() tableRowGap = 'space-x-4';
  @Input() tableCellHeight = '16px';
  @Input() tableCellDefaultWidth = '80%';

  @Input() shimmerSpeed = 1.5;

  private styleElement: HTMLStyleElement | null = null;
  private static injectedSpeeds = new Set<number>();

  constructor(
    private renderer: Renderer2,
    @Inject(DOCUMENT) private document: Document
  ) {}

  ngOnInit(): void {
    this.injectKeyframes();
  }

  ngOnDestroy(): void {
    if (this.styleElement) {
      this.renderer.removeChild(this.document.head, this.styleElement);
      CommonSkeletonComponent.injectedSpeeds.delete(this.shimmerSpeed);
    }
  }

  private injectKeyframes(): void {
    if (this.animation !== 'shimmer' || CommonSkeletonComponent.injectedSpeeds.has(this.shimmerSpeed)) {
      return;
    }

    const speedStr = this.shimmerSpeed.toString().replace('.', '-');
    const keyframes = `
      @keyframes skeleton-shimmer-${speedStr} {
        0% { background-position: 200% 0; }
        100% { background-position: -200% 0; }
      }
    `;

    this.styleElement = this.renderer.createElement('style');
    this.styleElement.id = 'common-skeleton-base-style';
    this.renderer.setProperty(this.styleElement, 'textContent', keyframes);
    this.renderer.appendChild(this.document.head, this.styleElement);

    CommonSkeletonComponent.injectedSpeeds.add(this.shimmerSpeed);
  }

  getBaseClasses(shapeClass: string): string {
    const animationClass = this.animation === 'pulse' ? 'animate-pulse' : '';
    return `${this.baseColor} ${this.darkBaseColor} ${shapeClass} ${animationClass}`;
  }

  getShimmerStyles(): { [key: string]: string } | null {
    if (this.animation !== 'shimmer') return null;

    const speedStr = this.shimmerSpeed.toString().replace('.', '-');

    return {
      'background-image': 'linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.4) 50%, transparent 100%)',
      'background-size': '200% 100%',
      'animation': `skeleton-shimmer-${speedStr} ${this.shimmerSpeed}s infinite linear`
    };
  }

  getTableCellWidth(colIndex: number): string {
    const col = this.tableColumns[colIndex];
    return col?.width || this.tableCellDefaultWidth;
  }

  getTableCellHeight(): string {
    return this.tableCellHeight;
  }
}
