import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { Observable, catchError, map, of } from 'rxjs';
@Injectable({
  providedIn: 'root',
})
export class PromptWritingRepoService {
  constructor(private http: HttpService, private ctxServ: ContextService) {}

  public isOpAccount(): Observable<boolean> {
    return this.http.get({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt-engineering/template/is-admin`,
    });
  }
}
