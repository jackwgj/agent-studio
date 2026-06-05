import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import DOMPurify from 'dompurify';

@Pipe({
  name: 'safeHtml',
  standalone: true,
})
export class SafeHtmlPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value): SafeHtml {
    if (!value || typeof value !== 'string') {
      return '';
    }
    const purifiedHtml = DOMPurify.sanitize(value);
    return this.sanitizer.bypassSecurityTrustHtml(purifiedHtml);
  }
}
