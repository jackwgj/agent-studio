import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'  // 使服务成为单例全局可用
})
export class KnowledgeBaseListService {
  isShowList = true;
  listCurrentPage = 1;
  listSize = 10;
  cardCurrentPage = 1;
  cardSize = 12;

  isAccessShowList = true;
  accessListCurrentPage = 1;
  accessListSize = 10;
  accessCardCurrentPage = 1;
  accessCardSize = 12;
  constructor() { }

  getKbSource(kbDetail) {
    if (kbDetail?.type) {
      return kbDetail?.type === 'internal' ? '' : (kbDetail?.source?.knowledge_base_connection_name || '')
    } else {
      return '';
    }
  }
}
