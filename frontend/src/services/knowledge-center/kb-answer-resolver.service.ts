import { Injectable } from '@angular/core';
import { KbAnswerImageResolverClass } from '@services/knowledge-center/kb-answer-image-resolver.class';

@Injectable({
  providedIn: 'root',
})
export class KbAnswerResolverService {

  imageResolver = new KbAnswerImageResolverClass();

  constructor(
  ) {}

  // 在app-md公共组件调用了当前service，但是上层组件使用时请 注意清除缓存！
  clear() {
    this.imageResolver.clear();
  }

  resolveImage(content: string, fetchImgRequest: (imgId: string) => Promise<any>) {
    return this.imageResolver.resolveImage(content, fetchImgRequest)
  }
}
