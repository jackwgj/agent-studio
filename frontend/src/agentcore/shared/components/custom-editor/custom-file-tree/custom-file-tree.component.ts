import { Component, Input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { NzTreeModule } from 'ng-zorro-antd/tree';
import { NzIconModule } from 'ng-zorro-antd/icon';

import { CustomTreeNode } from '@agentcore/constants/skill.model';

import { CustomEditorIconComponent } from '../custom-editor-icon/custom-editor-icon.component';

@Component({
  selector: 'custom-file-tree',
  templateUrl: './custom-file-tree.html',
  styleUrls: ['./custom-file-tree.component.scss'],
  standalone: true,
  imports: [
    NzTreeModule,
    FormsModule,
    CommonModule,
    NzIconModule,
    CustomEditorIconComponent,
  ],
})
export class CustomFileTreeComponent {
  @Input('treeData') treeData: any[];
  onNodeSelect = output<CustomTreeNode>();

  onSelect(event: any): void {
    this.onNodeSelect.emit(event);
  }
}
