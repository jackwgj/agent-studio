import { Injectable, NgZone, OnDestroy } from '@angular/core';
import { Subject, Subscription, interval, bufferTime, filter } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class StreamBufferService implements OnDestroy {
  // 用于接收原始事件流
  private dataStream = new Subject<any>();
  // 用于输出缓冲后的事件数组
  private bufferedData = new Subject<any[]>();

  private subscription: Subscription;

  timeThreshold = 50 //时间阈值
  countThreshold = 100  //数量阈值

  constructor(private ngZone: NgZone) {
    this.ngZone.runOutsideAngular(() => {
      this.subscription = this.dataStream.pipe(
        // 关键操作符：每timeThreshold ms为一个时间窗口进行缓冲
        bufferTime(this.timeThreshold),
        // 关键条件：仅当缓冲池内有数据（长度>0）且达到阈值时才发射
        filter(buffer => buffer.length > 0),
        // 可选：进一步控制，只有当事件数量超过countThreshold个，或虽未超过但时间窗到了也发射
        filter(buffer => buffer.length >= this.countThreshold || true)
      ).subscribe(batch => {
        this.ngZone.run(() => {
          this.bufferedData.next(batch);
        });
      });
    });
  }

  addEvent(event: any) {
    this.dataStream.next(event);
  }

  getBufferedStream() {
    return this.bufferedData.asObservable();
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
