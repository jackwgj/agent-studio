import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})

export class SendQuestionService {
  private readonly sendQuestionAndFlag$ = new BehaviorSubject<any>({
    questionVal: '',
  });

  constructor() {}

  public setQuestionAndFlag(questionInputed: string): void {
    this.sendQuestionAndFlag$.next({
      questionVal: questionInputed,
    });
  }

  public sendQuestionAndFlagUpdate$(): Observable<any> {
    return this.sendQuestionAndFlag$.asObservable();
  }
}
