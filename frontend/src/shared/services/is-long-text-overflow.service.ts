import { Injectable, Renderer2, RendererFactory2 } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class IsLongTextOverflowService {
  private renderer: Renderer2;
  constructor(rendererFactory: RendererFactory2) {
    this.renderer = rendererFactory.createRenderer(null, null);
  }

  setStyles(element: any, styles: { [key: string]: any }) {
    Object.keys(styles).forEach((key) => {
      this.renderer.setStyle(element, key, styles[key]);
    });
  }

  public isOverflow(element: Element): boolean {
    const elementStyles: any = getComputedStyle(element);
    const cloneElement: any = element.cloneNode(true);

    this.setStyles(cloneElement, {
      fontSize: elementStyles.fontSize,
      fontWeight: elementStyles.fontWeight,
      fontFamily: elementStyles.fontFamily,
      padding: elementStyles.padding,
      paddingLeft: elementStyles.paddingLeft,
      paddingRight: elementStyles.paddingRight,
      border: elementStyles.border,
      boxSizing: elementStyles.boxSizing,
      height: elementStyles.height,
      maxWidth: 'none',
      width: 'auto',
      overflow: 'visible',
      display: 'inline-block',
      visibility: 'hidden',
      whiteSpace: elementStyles.whiteSpace,
      position: 'absolute',
      top: '-9999px',
      left: '-9999px',
    });
    this.renderer.appendChild(document.body, cloneElement);

    const maxWidth: number = parseFloat(
      element.getBoundingClientRect().width.toFixed(2),
    );
    const textWidth: number = parseFloat(
      cloneElement.getBoundingClientRect().width.toFixed(2),
    );

    document.body.removeChild(cloneElement);

    return textWidth > maxWidth;
  }

  public isTextOverflowDiv(hostElm: HTMLElement): boolean {
    const hostHeight = hostElm.clientHeight;
    const innerFullHeight = hostElm.querySelector('div').scrollHeight;
    return innerFullHeight > hostHeight;
  }

  public isTextOverflow(
    hostElm: HTMLElement,
    containerHeight: number,
  ): boolean {
    const innerFullHeight = hostElm?.scrollHeight || 0;
    return innerFullHeight > containerHeight;
  }
}
