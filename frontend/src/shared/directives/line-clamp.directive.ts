import {
  AfterViewInit,
  Directive,
  ElementRef,
  Input,
  OnDestroy,
  Renderer2,
} from '@angular/core';

@Directive({
  selector: '[lineClamp]',
  standalone: true,
})
export class LineClampDirective implements AfterViewInit, OnDestroy {
  private mutationObserver: MutationObserver | null = null;

  @Input('lineClamp') lineClamp: number | string = 1;

  constructor(
    private el: ElementRef<HTMLDivElement>,
    private renderer: Renderer2,
  ) {}

  ngAfterViewInit() {
    this.initRule();
    this.updateTitle();
    this.setupMutationObserver();
  }

  ngOnDestroy() {
    if (this.mutationObserver) {
      this.mutationObserver.disconnect();
      this.mutationObserver = null;
    }
  }

  private setupMutationObserver() {
    this.mutationObserver = new MutationObserver((mutations) => {
      const hasTextChange = mutations.some(
        (mutation) =>
          mutation.type === 'characterData' || mutation.type === 'childList',
      );

      if (hasTextChange) {
        this.updateTitle();
      }
    });

    const config = {
      characterData: true,
      childList: true,
      subtree: true,
    };

    this.mutationObserver.observe(this.el.nativeElement, config);
  }

  private initRule() {
    const element = this.el.nativeElement;
    let rule = '';

    if (typeof this.lineClamp === 'string') {
      rule = 'truncate';
    } else {
      rule = `line-clamp-${this.lineClamp}`;
    }

    this.renderer.addClass(element, rule);
  }

  private updateTitle() {
    const element = this.el.nativeElement;
    const textContent = element.textContent?.trim();

    if (textContent) {
      this.renderer.setAttribute(element, 'title', textContent);
    } else {
      this.renderer.removeAttribute(element, 'title');
    }
  }
}
