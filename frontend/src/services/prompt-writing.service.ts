import { Injectable } from '@angular/core';
import { EWritingStatus } from '@enums/prompt-writing.enum';
import { ICandChangeInfo } from '@interfaces/prompt/prompt-writing.interface';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PromptWritingService {
  private readonly currCmpState$ = new BehaviorSubject<EWritingStatus>(
    EWritingStatus.WRITING,
  );

  // 当前taskId下候选的数目
  private readonly candNum$ = new BehaviorSubject<number>(0);

  // 刷新菜单的变更信息
  private readonly candChangeInfo$ =
    new BehaviorSubject<ICandChangeInfo | null>(null);

  public getCurrCmpState(): EWritingStatus {
    return this.currCmpState$.getValue();
  }

  public setCurrCmpState(prompt: EWritingStatus): void {
    this.currCmpState$.next(prompt);
  }

  public currCmpStateUpdate$(): Observable<EWritingStatus> {
    return this.currCmpState$.asObservable();
  }

  public setCandChangeInfo(info: ICandChangeInfo): void {
    this.candChangeInfo$.next(info);
  }

  public candChangeInfoUpdate$(): Observable<ICandChangeInfo> {
    return this.candChangeInfo$.asObservable();
  }

  public setCandNum(num: number): void {
    this.candNum$.next(num);
  }

  public candNumUpdate$(): Observable<number> {
    return this.candNum$.asObservable();
  }
}
