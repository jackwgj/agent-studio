import { Injectable, Input } from '@angular/core';

@Injectable({
  providedIn: 'root'  // 使服务成为单例全局可用
})
export class ModelConfigTipService {
  isShowAdvancedConfig = false;
  detail:any
  useModelMode = 'one'; // plan 使用规划  question使用问答  one使用合一模型
  constructor() { }
}
