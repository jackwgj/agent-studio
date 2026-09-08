import {
  ChangeDetectionStrategy,
  Component,
  Input,
  NgZone,
  OnChanges,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NzIconModule } from 'ng-zorro-antd/icon';
import type { PreviewState } from '../../preview.model';

/** iframe 超过该时长仍未 load，给出「可能不允许被嵌入」的软提示。 */
const STALL_TIMEOUT = 15000;

/**
 * 网址预览渲染器。
 *
 * 注意：多数站点通过 X-Frame-Options / CSP 拒绝被嵌套，浏览器不会抛出可读错误，
 * 因此这里只做「超时软提示」，真正的兜底由工具栏的「在新标签打开」承担。
 */
@Component({
  selector: 'app-preview-url',
  templateUrl: './preview-url.component.html',
  styleUrls: ['./preview-url.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [CommonModule, NzIconModule],
})
export class PreviewUrlComponent implements OnChanges, OnDestroy {
  @Input() state!: PreviewState;

  safeUrl: SafeResourceUrl | null = null;
  loading = true;
  stalled = false;

  private timer: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private readonly sanitizer: DomSanitizer,
    private readonly zone: NgZone,
  ) {}

  public ngOnChanges(): void {
    this.clearTimer();
    this.loading = true;
    this.stalled = false;
    this.safeUrl = this.state.url
      ? this.sanitizer.bypassSecurityTrustResourceUrl(this.state.url)
      : null;
    this.zone.runOutsideAngular(() => {
      this.timer = setTimeout(() => {
        if (this.loading) {
          this.zone.run(() => {
            this.stalled = true;
          });
        }
      }, STALL_TIMEOUT);
    });
  }

  public ngOnDestroy(): void {
    this.clearTimer();
  }

  public onLoad(): void {
    this.clearTimer();
    // iframe 的 load 可能来自 zone 之外的浏览器事件
    this.zone.run(() => {
      this.loading = false;
      this.stalled = false;
    });
  }

  private clearTimer(): void {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  }
}
