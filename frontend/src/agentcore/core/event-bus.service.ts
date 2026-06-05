import { Injectable } from '@angular/core';
import { Observable, take, ReplaySubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class EventBusService {
  private subjects = new Map<string, ReplaySubject<any>>();

  constructor() {}

  /**
   * 发送事件
   * @param eventType 事件类型
   * @param payload 事件数据
   */
  emit<T = any>(eventType: string, payload?: T): void {
    this.getSubject(eventType).next(payload);
  }

  /**
   * 监听事件
   * @param eventType 事件类型
   * @returns 事件 Observable
   */
  on<T = any>(eventType: string): Observable<T> {
    return this.getSubject(eventType).asObservable();
  }

  /**
   * 一次性监听
   * @param eventType 事件类型
   * @returns 
   */
  once<T = any>(eventType: string): Observable<T> {
    return this.on(eventType).pipe(take(1));
  }

  /**
   * 清理特定事件的所有监听
   * @param eventType 事件类型
   */
  clear(eventType: string): void {
    const subject = this.subjects.get(eventType);
    if (subject) {
      subject.complete();
      this.subjects.delete(eventType);
    }
  }

  /**
   * 清理所有事件
   */
  clearAll(): void {
    this.subjects.forEach(subject => subject.complete());
    this.subjects.clear();
  }

  /**
   * 根据事件类型获取Subject对象
   * @param eventType 事件类型
   * @returns 
   */
  private getSubject<T = any>(eventType: string): ReplaySubject<T> {
    if (!this.subjects.has(eventType)) {
      this.subjects.set(eventType, new ReplaySubject<T>(1));
    }
    return this.subjects.get(eventType);
  }
}
