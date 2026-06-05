import { Injectable } from '@angular/core';
import { IDataset } from '@interfaces/prompt/var-set.interface';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DatasetService {
  private readonly dataset$ = new BehaviorSubject<IDataset[]>([]);

  public getDatasets(): IDataset[] {
    return this.dataset$.getValue();
  }

  public setDataset(dataset: IDataset[]): void {
    this.dataset$.next(dataset);
  }

  public datasetUpdate$(): Observable<IDataset[]> {
    return this.dataset$.asObservable();
  }
}
