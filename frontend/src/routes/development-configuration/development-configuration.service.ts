import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class InformationTemplateService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }
  constructor(private http: HttpService, private ctxServ: ContextService) {}
}
