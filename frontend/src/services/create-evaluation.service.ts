/**
 * 创建评估与服务端交互接口列
 */

import { HttpService } from '@services/http.service';
import { ContextService } from './context.service';
import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root',
})
export class CreateEvaluationService {
  constructor(private http: HttpService, private ctxServ: ContextService) {}

}
