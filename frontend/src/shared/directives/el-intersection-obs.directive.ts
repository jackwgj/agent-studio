import { Directive, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';

export interface IntersectionOptions {
  threshold?: number | number[];
  rootMargin?: string;
  root?: Element | null;
}

@Directive({
  selector: '[elIntersectionObs]',
  standalone: true,
})
export class ElIntersectionObsDirective implements OnInit, OnDestroy {
  @Input() elIntersectionObserver: IntersectionOptions = { threshold: 0.5 };
  @Input() once = false;

  @Output() visible = new EventEmitter<IntersectionObserverEntry>();
  @Output() hidden = new EventEmitter<IntersectionObserverEntry>();
  @Output() change = new EventEmitter<IntersectionObserverEntry>();

  private observer: IntersectionObserver | null = null;
  private isTrigger = false;

  constructor(private element: ElementRef) {
  }

  ngOnInit(): void {
    this.createObserver();
  }

  ngOnDestroy(): void {
    this.destroyObserver();
  }

  private createObserver(): void {
    const options: IntersectionObserverInit = {
      threshold: this.elIntersectionObserver.threshold ?? 0.5,
      rootMargin: this.elIntersectionObserver.rootMargin ?? '0px',
      root: this.elIntersectionObserver.root ?? null,
    };

    this.observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          if (this.once && this.isTrigger) {
            return;
          }

          this.visible.emit(entry);
          this.change.emit(entry);
          this.isTrigger = true;

          if (this.once) {
            this.destroyObserver();
          }
        } else {
          if (!this.once) {
            this.hidden.emit(entry);
            this.change.emit(entry);
          }
        }
      });
    }, options);

    this.observer.observe(this.element.nativeElement);
  }

  private destroyObserver(): void {
    if (this.observer) {
      this.observer.unobserve(this.element.nativeElement);
      this.observer.disconnect();
      this.observer = null;
    }
  }
}
