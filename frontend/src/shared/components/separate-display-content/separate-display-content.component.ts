import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, Input } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

export type MarkedContentReplacer = (content: string) => string;

export interface MarkedTextConfig {
  [marker: string]: MarkedContentReplacer;
}

@Component({
  selector: 'separate-display-content',
  standalone: true,
  imports: [CommonModule],
  template: `<span [innerHTML]="safeHtml" [class]="containerClass"></span>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [],
})
export class SeparateDisplayContentComponent {
  @Input({ required: true }) text: string;
  @Input({ required: true }) config: MarkedTextConfig;
  @Input() containerClass = '';

  private sanitizer = inject(DomSanitizer);

  get safeHtml(): SafeHtml {
    const html = this.parse(this.text, this.config);
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }

  private parse(text: string, config: MarkedTextConfig): string {
    const pattern = /<(\w+)>([\s\S]*?)<\/\1>|<(\w+)>/g;

    return text.replace(pattern, (match, closeKey, content, openKey) => {
      const key = closeKey || openKey;
      const replacer = config[key];

      if (!replacer) return match;

      const innerContent = content || '';
      return replacer(innerContent);
    });
  }
}
