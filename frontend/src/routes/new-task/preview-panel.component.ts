import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MarkdownComponent } from 'ngx-markdown';
import { MODULES } from '@shared/modules';

/**
 * 右栏实时预览面板（new-task-chat）：用已装 ngx-markdown 渲染后端返回的结构化 Markdown。
 * data 来自后端 task.data / SSE done 事件的 result.reply。
 */
@Component({
  selector: 'app-preview-panel',
  templateUrl: './preview-panel.component.html',
  styleUrls: ['./preview-panel.component.less'],
  standalone: true,
  imports: [CommonModule, MarkdownComponent, MODULES],
})
export class PreviewPanelComponent {
  @Input() data: string | null = null;
  @Input() loading = false;
}
