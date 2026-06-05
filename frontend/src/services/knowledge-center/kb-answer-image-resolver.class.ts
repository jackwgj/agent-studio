import { KbUtils } from '@routes/knowledge-center/kb.utils';
import { from, Observable, shareReplay, tap } from 'rxjs';

export class KbAnswerImageResolverClass {
  imageClass = 'common-image-in-kb';
  imageActiveClass = 'active';

  private cache: Map<string, Observable<any>> = new Map();
  private imageCache: Map<string, string> = new Map;

  constructor() {
    this.#addStyleSheet();
  }

  clear() {
    this.cache.clear();
  }

  // 解析并替换文本 markdown格式的
  resolveImage(content: string, fetchImgRequest: (imgId: string) => Promise<any>) {
    let ansContent = content;
    const regex = /!\[img]\(https:\/\/agent_arts_knowledge_img_url\/([a-zA-Z0-9]+)\)/gm;
    ansContent = ansContent.replace(regex, (_, imgId) => {
      const imgCache = this.imageCache.get(`${imgId}`);
      if (!imgCache) {
        this.#resolveImgDom(imgId, fetchImgRequest);
      }
      return `<img src="${imgCache ?? ''}" class="common-image-in-kb${imgCache ? ' active' : ''}" data-img="${ imgId }" alt=""/>`;
    });

    return ansContent;
  }

  // 渲染页面dom
  #resolveImgDom(imgId: string, fetchImgRequest: (imgId: string) => Promise<any>) {
    this.#getImage(imgId, fetchImgRequest).subscribe(base64 => {
      if (base64) {
        const targetImages: NodeListOf<HTMLImageElement> = document.querySelectorAll(`.${ this.imageClass }[data-img="${ imgId }"]`);
        if (targetImages?.length) {
          targetImages.forEach(imageDom => {
            if (!imageDom.classList.contains(this.imageActiveClass)) {
              imageDom.src = base64;
              imageDom.classList.add(this.imageActiveClass);
            }
          });
        }
      }
    });
  }

  #getImage(imgId: string, requestFn: (imgId: string) => Promise<any>): Observable<any> {
    const id = `${ imgId }`;
    if (this.cache.has(id)) {
      return this.cache.get(id);
    }

    const api$ = from(this.#getImgBase(imgId, requestFn)).pipe(
      shareReplay(1),
      tap({
        error: () => {
          this.cache.delete(id);
        },
      }),
    );

    this.cache.set(id, api$);
    return api$;
  }

  #getImgBase(imgId: string, fetchImgRequest: (imgId: string) => Promise<any>): Promise<string> {
    return new Promise(resolve => {
      fetchImgRequest(imgId).then(imageFile => {
        KbUtils.readFileAsBase64(imageFile).then(res => {
          if (!this.imageCache.get(`${imgId}`)) {
            this.imageCache.set(`${imgId}`, res);
          }
          resolve(res);
        }).catch(() => {
          resolve('');
        });
      });
    });
  }

  #addStyleSheet() {
    const styleId = 'image-resolver-style';
    if (!document.getElementById(styleId)) {
      const style = document.createElement('style');
      style.id = styleId;
      style.innerHTML = `
			   .${ this.imageClass }{display: none;max-width:200px;height: auto;}
			   .${ this.imageClass }.${ this.imageActiveClass }{display: unset;}
			   `;
      document.head.appendChild(style);
    }
  }
}
