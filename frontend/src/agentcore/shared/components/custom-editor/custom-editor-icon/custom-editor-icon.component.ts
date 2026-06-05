import { Component, Input } from '@angular/core';

import { NzIconModule } from 'ng-zorro-antd/icon';

import { CommonModule } from '@angular/common';

@Component({
  selector: 'custom-editor-icon',
  templateUrl: './custom-editor-icon.html',
  styleUrls: ['./custom-editor-icon.component.scss'],
  standalone: true,
  imports: [CommonModule, NzIconModule],
})
export class CustomEditorIconComponent {
  @Input('name') name: string;
}
