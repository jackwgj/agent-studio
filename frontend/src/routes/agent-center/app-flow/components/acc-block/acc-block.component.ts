import { Component, ContentChild, Input, TemplateRef } from '@angular/core';
import { MODULES } from '@shared/modules';

@Component({
  selector: 'meta-acc-block',
  templateUrl: './acc-block.component.html',
  styleUrls: ['./acc-block.component.less'],
  standalone: true,
  imports: [MODULES],
})
export class AccBlockComponent {
  @Input() title: string;

  @Input() tip: string;

  @Input() exp: string;

  @Input() open = true;

  @Input() isNewStyle = false;

  @Input() isHideHeader = false;

  @ContentChild('content') content: TemplateRef<any>;

  @ContentChild('right') right: TemplateRef<any>;

  @ContentChild('tipComp') tipComp: TemplateRef<any>;

  @ContentChild('blockTitle') blockTitle: TemplateRef<any>;

  clickOpenOrCloseFn(event) {
    if (event) {
      event.stopPropagation();
    }
    this.open = !this.open
  }
}
