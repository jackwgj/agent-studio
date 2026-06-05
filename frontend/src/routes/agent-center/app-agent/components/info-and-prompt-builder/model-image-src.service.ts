import {Injectable} from '@angular/core';
import {ContextService} from '@services/context.service';
import {HttpService} from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class ModelImageDataService {

  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  // 查询变量详情
  public getModelImageData(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/agents/icons`,
      params: params
    });
  }
}
