import { Component, Input } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NextEagerPipe } from 'angular-i18next';

@Component({
  selector: 'meta-add-props-icon',
  templateUrl: './add-props-icon.component.html',
  styleUrls: ['./add-props-icon.component.less'],
  standalone: true,
  imports: [MODULES],
})
export class AddPropsIconComponent {
  @Input() tipContent = this.i18n.transform('add_sub_item');

  constructor(private i18n: I18NextEagerPipe) {
  }

}
