import { inject, Injectable } from '@angular/core';
import { KbItem } from '@routes/knowledge-center/knowledge-base.interface';
import { KbTagItem, KbTagListRes, KbTagQueryParams } from '@routes/knowledge-center/knowledge.types';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { from, Observable, shareReplay, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class KbTagService {
  private cache: Map<string, Observable<any>> = new Map();

  private readonly kbApiService = inject(KnowledgeRepoService);

  private readonly kbAbilitiesService = inject(KbAbilitiesService);
  constructor(
  ) {}


  getKbTags(kbId: string, params: KbTagQueryParams, isCache = false): Observable<KbTagListRes> {
    if (this.cache.has(kbId) && isCache) {
      return this.cache.get(kbId);
    }

    const api$ = from(this.kbApiService.getKbTags(kbId, params)).pipe(
      shareReplay(1),
      tap({
        error: () => {
          this.cache.delete(kbId);
        },
      }),
    );

    if (isCache) {
      this.cache.set(kbId, api$);
    }

    return api$;
  }

  batchGetTags(kbs: KbItem[], params: KbTagQueryParams, isCache = false) {
    const cacheKey = kbs.map(kb => this.kbAbilitiesService.getKbId(kb)).sort().join('-');

    if (isCache && this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey) as Observable<{ name: string; children: KbTagItem[] }[]>;
    }

    const api$= from(
      Promise.all(
        kbs.map(kb => {
          return this.kbApiService.getKbTags(this.kbAbilitiesService.getKbId(kb), params).then(res => {
            return {
              name: kb.name,
              children: res.tag_list ?? [],
            };
          });
        })
      )
    ).pipe(
      shareReplay(1),
      tap({
        error: () => {
          this.cache.delete(cacheKey);
        },
      })
    );

    if (isCache) {
      this.cache.set(cacheKey, api$);
    }

    return api$;
  }

  clearCache() {
    this.cache.clear();
  }
}
