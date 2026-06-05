import { Component, ContentChild, Input, TemplateRef } from '@angular/core';
import { MODULES } from '@shared/modules';

@Component({
  selector: 'meta-acc-block-new',
  templateUrl: './acc-block-new.component.html',
  styleUrls: ['./acc-block-new.component.less'],
  standalone: true,
  imports: [MODULES],
})
export class AccBlockNewComponent {
  @Input() title: string;

  @Input() exp: string;

  @Input() open = true;

  @Input() lineTop = true;

  @ContentChild('content') content: TemplateRef<any>;

  @ContentChild('right') right: TemplateRef<any>;

  @ContentChild('blockTitle') blockTitle: TemplateRef<any>;
}
