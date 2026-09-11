import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import type { PreviewState, PreviewType } from '../../preview.model';
import { PreviewTextComponent } from './preview-text.component';
import { PreviewUrlComponent } from './preview-url.component';

/** 预览面板容器：工具栏 + 渲染器路由 + 各状态位。 */
@Component({
  selector: 'app-preview-panel',
  templateUrl: './preview-panel.component.html',
  styleUrls: ['./preview-panel.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule,
    NzButtonModule,
    NzIconModule,
    NzToolTipModule,
    PreviewUrlComponent,
    PreviewTextComponent,
  ],
})
export class PreviewPanelComponent {
  @Input() state!: PreviewState;
  /** 窄屏全屏模式：顶栏显示「返回对话」 */
  @Input() narrow = false;

  @Output() readonly close = new EventEmitter<void>();
  @Output() readonly refresh = new EventEmitter<void>();
  @Output() readonly openExternal = new EventEmitter<string>();
  @Output() readonly download = new EventEmitter<PreviewState>();
  /** 文本渲染器上抛：外链 CORS 失败，改用 iframe 兜底 */
  @Output() readonly fallback = new EventEmitter<PreviewState>();
  /** 文本渲染器上抛：正文内链接点击，在面板内继续预览 */
  @Output() readonly navigate = new EventEmitter<string>();

  typeLabelMap: Record<PreviewType, string> = {
    url: '网页',
    md: 'Markdown',
    txt: '文本',
    unsupported: '文件',
  };

  public get iconType(): string {
    switch (this.state?.type) {
      case 'url':
        return 'global';
      case 'md':
      case 'txt':
        return 'file-text';
      default:
        return 'file-unknown';
    }
  }

  public get typeLabel(): string {
    return this.typeLabelMap[this.state?.type ?? 'unsupported'];
  }

  public onOpenExternal(): void {
    if (this.state?.url) {
      this.openExternal.emit(this.state.url);
    }
  }

}
