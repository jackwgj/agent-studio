import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class VariableService {
  // 用于确保打开的变量类型选择器只有一个
  private variableIdSubject = new BehaviorSubject<any>(null); // 初始值为 null， 类型可自定义

  public variableId$: Observable<any> = this.variableIdSubject.asObservable();

  // 变量订阅记录
  private variablesSubject = new BehaviorSubject<any[]>([]);

  public variablesSubscription$: Observable<any[]> = this.variablesSubject.asObservable();

  constructor() {}

  public setId(id: string): void {
    this.variableIdSubject.next(id);
  }

  public getCurrentData(): any {
    return this.variableIdSubject.value;
  }

  public setVariables(variables: any[]): void {
    this.variablesSubject.next(variables);
  }

  public getVariables(): any[] {
    return this.variablesSubject.value;
  }

}
