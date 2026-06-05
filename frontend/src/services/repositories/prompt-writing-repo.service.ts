import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class PromptWritingRepoService {
  constructor(private http: HttpService, private ctxServ: ContextService) {}
}
