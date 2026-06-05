import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root', // 使服务成为单例全局可用
})
export class KnowledgeCardUsedService {
  tags:Array<{repoId:string,tagIds:Array<string>}> = [];

  constructor() {}
}
