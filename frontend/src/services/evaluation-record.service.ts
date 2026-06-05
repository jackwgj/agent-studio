import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { ContextService } from './context.service';

@Injectable({
  providedIn: 'root',
})
export class EvaluationRecordService {
  constructor(private http: HttpService, private ctxServ: ContextService) {}
}
