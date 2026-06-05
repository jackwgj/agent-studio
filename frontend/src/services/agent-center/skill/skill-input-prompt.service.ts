import { Injectable } from '@angular/core';

import { BehaviorSubject, pipe } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class SkillInputPromptService {
  // 提示词监听
  public inputPromptSubject$ = new BehaviorSubject<string>('');

  // 可订阅的 Observable
  public input$ = this.inputPromptSubject$.asObservable().pipe(
    debounceTime(500),
  );

  // 设置输入值（由输入框调用）
  setInputValue(value: string): void {
    this.inputPromptSubject$.next(value);
  }

  // 获取当前值（可选）
  getCurrentValue(): string {
    return this.inputPromptSubject$.value;
  }
}
