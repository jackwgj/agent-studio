import { Injectable } from '@angular/core';
import { ITask } from '@interfaces/prompt/task.interface';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private readonly task$ = new BehaviorSubject<ITask>({
    name: '',
    id: '',
    tags: [],
    desc: '',
  });

  public getTask(): ITask {
    return this.task$.getValue();
  }

  public setTask(task: ITask): void {
    this.task$.next(task);
  }
}
