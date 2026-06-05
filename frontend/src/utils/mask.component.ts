import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class MaskComponent {
  static maskCount = 0;
  private static background: HTMLDivElement = document.createElement('div');
  private static loading: HTMLDivElement = document.createElement('div');

  static show(): void {
    if (++this.maskCount === 1) {
      this.background.className = 'cf-mask-cover-background';
      document.body.appendChild(this.background);
      this.loading.className = 'cf-mask-loading';
      document.body.appendChild(this.loading);
    }
  }

  static hide(): void {
    if (this.maskCount > 0) {
      this.maskCount--;
    }
    if (this.maskCount === 0) {
      const pBackground = this.background?.parentNode;
      if (pBackground) {
        pBackground.removeChild(this.background);
      }
      const pLoading = this.loading?.parentNode;
      if (pLoading) {
        pLoading.removeChild(this.loading);
      }
    }
  }

  static pageInitShow(): void {
    const background = document.createElement('div');
    background.className = 'cf-page-init-background';
    document.body.appendChild(background);
    const loading = document.createElement('div');
    loading.className = 'cf-page-init-loading';
    document.body.appendChild(loading);
  }

  static pageInitHide(): void {
    const background = document.querySelector('.cf-page-init-background');
    if (background) {
      const pBackground = background.parentNode;
      pBackground?.removeChild(background);
    }
    const loading = document.querySelector('.cf-page-init-loading');
    if (loading) {
      const pLoading = loading.parentNode;
      pLoading?.removeChild(loading);
    }
  }
}