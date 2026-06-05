import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { ContextService } from './context.service';

@Injectable({
  providedIn: 'root',
})
export class EvaluationDetailsService {
  constructor(private http: HttpService, private ctxServ: ContextService) {}
}
