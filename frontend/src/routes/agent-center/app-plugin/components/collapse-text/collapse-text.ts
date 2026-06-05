import { Component, ContentChild, Input, TemplateRef } from '@angular/core';
import { MODULES } from '@shared/modules';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'collapse-text',
  templateUrl: './collapse-text.html',
  styleUrls: ['./collapse-text.less'],
  standalone: true,
  imports: [MODULES, NzIconModule, NzToolTipModule],
})
export class CollapseText {
  @Input() tip: string;

  @Input() infomation: string;

  @Input() title: string;

  @Input() open = true;

  @Input() classType: string = 'style1';

  @Input() collapseIconAlign: string = 'right';

  @Input() titleClick = false;

  @ContentChild('content') content: TemplateRef<any>;

  @ContentChild('icons') icons: TemplateRef<any>;

  public getClass(position: string): string {
    switch (true) {
      case this.classType === 'style1' && position === 'main-collapse':
        return 'style1-main-collapse';
      case this.classType === 'style1' && position === 'title-row':
        return 'style1-title-row';
      case this.classType === 'style1' && position === 'title-left':
        return 'style1-title-left';
      case this.classType === 'style1' && position === 'title-text':
        return 'style1-title-text';
      case this.classType === 'style1' && position === 'pdb':
        return '24px';
      case this.classType === 'style2' && position === 'main-collapse':
        return 'style2-main-collapse';
      case this.classType === 'style2' && position === 'title-row':
        return 'style2-title-row';
      case this.classType === 'style2' && position === 'title-left':
        return 'style2-title-left';
      case this.classType === 'style2' && position === 'title-text':
        return 'style2-title-text';
      case this.classType === 'style2' && position === 'pdb':
        return '0px';
      case this.classType === 'style3' && position === 'main-collapse':
        return 'style3-main-collapse';
      case this.classType === 'style3' && position === 'title-row':
        return 'style3-title-row';
      case this.classType === 'style3' && position === 'title-left':
        return 'style3-title-left';
      case this.classType === 'style3' && position === 'title-text':
        return 'style3-title-text';
      case this.classType === 'style3' && position === 'pdb':
        return '0px';
      case this.classType === 'style4' && position === 'main-collapse':
        return 'style4-main-collapse';
      case this.classType === 'style4' && position === 'title-row':
        return 'style4-title-row';
      case this.classType === 'style4' && position === 'title-left':
        return 'style4-title-left';
      case this.classType === 'style4' && position === 'title-text':
        return 'style4-title-text';
      case this.classType === 'style4' && position === 'pdb':
        return '0px';
      default:
        return '';
    }
  }

  collapseContent($event, isParent = false) {
    $event?.stopPropagation?.();
    if (!this.titleClick && isParent) {
      return;
    }
    this.open = !this.open;
  }
}
