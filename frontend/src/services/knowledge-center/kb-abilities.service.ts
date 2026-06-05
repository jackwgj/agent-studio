import { ChangeDetectorRef, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Network } from '@model';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { KnowledgeType } from '@routes/knowledge-center/knowledge-base-list/knowledge-base-list.map';
import {
  KBAbility,
  KBAbilityCode,
  KbConstantConfig,
  KbItem,
  KBRetrieveMode,
  KBType,
  SearchModeType,
} from '@routes/knowledge-center/knowledge-base.interface';
import { KBScope } from '@routes/knowledge-center/knowledge.types';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { CommonService } from '@services/common.service';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { cloneDeep } from 'lodash';
import { catchError, from, map, Observable, of, shareReplay, Subject, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class KbAbilitiesService {
  showKbCustomConfig = false;

  subscribeBtnStatus = this.commonService.getSubscribeStatus();

  isDevelopSpace = false;

  kbConfig: KbConstantConfig[] = null;

  kbConfigCache: Map<string, Observable<any>> = new Map();

  #kbAbility: KBAbility[] = [];

  readonly #kbRetrieveModesSub = new Subject<KBRetrieveMode[]>();

  #retrieveModesMap: KBRetrieveMode[] = [
    {
      iconName: `${Network.ASSETS_PATH}agent-center/images/mode-doc.svg`,
      title: '',
      desc: '',
      summary: '',
      value: SearchModeType.DOC,
    },
    {
      iconName: `${Network.ASSETS_PATH}agent-center/images/mode-keyword.svg`,
      title: '',
      desc: '',
      summary: '',
      value: SearchModeType.KEYWORD,
    },
    {
      iconName: `${Network.ASSETS_PATH}agent-center/images/mode-mix.svg`,
      title: '',
      desc: '',
      summary: '',
      value: SearchModeType.MIX,
    },
    {
      iconName: `${Network.ASSETS_PATH}agent-center/images/mode-faq.svg`,
      title: '',
      desc: '',
      summary: '',
      value: SearchModeType.FAQ,
    },
  ];

  #retrieveModes: KBRetrieveMode[] = [];

  constructor(
    private kbApiService: KnowledgeRepoService,
    private configServ: AgentConfigService,
    private ctx: ContextService,
    private commonService: CommonService,
    private readonly http: HttpService,
    private router: Router,
  ) {
    this.isDevelopSpace = this.router.url.includes('develop-space');
  }


  public get isPermitShowImageInSearch() {
    return true;
  }

  public get isPermitCreateKB() {
    return true;
  }

  // 屏蔽三方相关信息
  public get isPermitThirdPartyKb() {
    return true;
  }

  public get isResourceDisabled() {
    return false;
  }

  isSupTag(kb: KbItem) {
    return kb?.type === KnowledgeType.INTERNAL && this.isPermitKBTagInGlobal();
  }

  isPermitKBTagInGlobal(): boolean {
    // 知识库类型屏蔽
    return ['KooSearch', 'LakeSearch'].includes(
      this.configServ.knowledgeSource(),
    );
  }

  getKbConfigWithCache(
    kbId?: string,
  ): Observable<{ configs: KbConstantConfig[] }> {
    let currentKey = kbId ?? 'allConfig';

    if (this.kbConfigCache.has(currentKey)) {
      return this.kbConfigCache.get(currentKey);
    }

    const api$ = from(this.kbApiService.getKbConfig(kbId)).pipe(
      shareReplay(1),
      tap({
        error: () => {
          this.kbConfigCache.delete('allConfig');
        },
      }),
    );

    const allConfigObs = this.kbConfigCache.get('allConfig');
    this.kbConfigCache.clear();
    if (allConfigObs) {
      this.kbConfigCache.set('allConfig', allConfigObs);
    }
    this.kbConfigCache.set(currentKey, api$);
    return api$;
  }

  public isPermitKBDefaultConfig(): boolean {
    return true;
  }
  public isSameSpace(spaceId: string) {
    return this.ctx.currentWorkspaceId === spaceId;
  }

  public getKbDetail(kbId: string): Observable<Record<string, any> | null> {
    return from(
      this.kbApiService.getKnowledgeBaseList({
        offset: 0,
        limit: 10,
        knowledge_base_ids: [kbId],
        share_scope: KBScope.ALL,
      }),
    ).pipe(
      map((res) => {
        return (
          res?.items?.find((kb) => {
            return this.getKbId(kb) === kbId;
          }) ?? null
        );
      }),
      catchError(() => {
        return null;
      }),
    );
  }

  /**
   * 兼容获取知识库ID
   * @param kbDetail
   */
  public getKbId(kbDetail?: Record<string, any>) {
    return (
      kbDetail?.knowledge_base_id ||
      kbDetail?.knowledge_repo_id ||
      kbDetail?.id ||
      ''
    );
  }

  public getKbRetrieveModes(
    connectionId: string = '',
  ): Observable<KBRetrieveMode[]> {
    this.#queryKbAbilities(connectionId);
    return this.#kbRetrieveModesSub;
  }

  /**
   * 通过kbId获取连接类型
   * @param kbId
   */
  public getKBConnectorType(kbId: string): Observable<string> {
    if (!kbId) {
      return of('');
    }
    return from(
      this.kbApiService.getKnowledgeBaseList({
        offset: 0,
        limit: 10,
        knowledge_base_ids: [kbId],
        share_scope: KBScope.ALL,
      }),
    ).pipe(
      map((res) => {
        const targetKb = res?.items?.find((kb) => {
          return this.getKbId(kb) === kbId;
        });
        if (targetKb) {
          return targetKb.type === 'internal'
            ? ''
            : targetKb.source?.knowledge_base_connector_id;
        }
        return '';
      }),
      catchError(() => {
        return '';
      }),
    );
  }

  /**
   * 通过kbId获取连接类型
   * @param kbId
   */
  public getKBConnectionId(kbId: string): Observable<string> {
    if (!kbId) {
      return of('');
    }
    return from(
      this.kbApiService.getKnowledgeBaseList({
        offset: 0,
        limit: 10,
        knowledge_base_ids: [kbId],
        share_scope: KBScope.ALL,
      }),
    ).pipe(
      map((res) => {
        const targetKb = res?.items?.find((kb) => {
          return this.getKbId(kb) === kbId;
        });
        return targetKb?.source?.knowledge_base_connection_id ?? '';
      }),
      catchError(() => {
        return '';
      }),
    );
  }

  /**
   * 获取连接名称，内部转为空
   * @param kbDetail
   */
  public getKbSourceName(kbDetail) {
    if (kbDetail?.type) {
      return kbDetail?.type === 'internal'
        ? ''
        : kbDetail?.source?.knowledge_base_connection_name || '';
    } else {
      return '';
    }
  }

  public getConnectionId(kbDetail: any, isExcludeInternal = true): string {
    const actualConnectionId =
      kbDetail?.source?.knowledge_base_connector_id ?? '';
    if (isExcludeInternal) {
      if (kbDetail.type === KBType.INTERNAL) {
        return '';
      }
      return actualConnectionId;
    }
    return actualConnectionId;
  }

  /**
   * 格式化知识库列表
   * @param kbs
   */
  public normalizeKBList(kbs: any[]): KbItem[] {
    return kbs.map((kb) => {
      return {
        ...kb,
        id: this.getKbId(kb),
      };
    });
  }

  #queryKbAbilities(connectionId: string) {
    this.kbApiService
      .getKBAbilities(connectionId)
      .then((res) => {
        this.#kbAbility = res?.abilities ?? [];
        this.#resolveRetrieveModes();
      })
      .catch(() => {
        this.#kbAbility = [];
        this.#retrieveModes = [];
        this.#kbRetrieveModesSub.next(this.#retrieveModes);
      });
  }

  #resolveRetrieveModes() {
    const subAbilities = this.#kbAbility.find(
      (ability) => ability.code === KBAbilityCode.RETRIEVE,
    )?.sub_ability;
    let retrieveModes = [];
    let showCustomConfig = false;
    if (subAbilities) {
      try {
        const subAbilitiesObj = JSON.parse(subAbilities);
        retrieveModes = subAbilitiesObj?.search_mode_list;
        showCustomConfig = subAbilitiesObj?.is_extension_supported;
      } catch (_) {
        retrieveModes = [];
        showCustomConfig = false;
      }
    }
    this.showKbCustomConfig = showCustomConfig;
    if (retrieveModes) {
      this.#retrieveModes = retrieveModes
        .map((actualMode) => {
          const { search_mode } = actualMode;
          if (
            search_mode === SearchModeType.FAQ &&
            !this.configServ.faqEnable()
          ) {
            return null;
          }
          const targetMode = this.#retrieveModesMap.find(
            (item) => item.value === search_mode,
          );
          if (targetMode) {
            const baseMode = cloneDeep(targetMode);
            baseMode.title = actualMode.search_mode_name;
            baseMode.desc = actualMode.description;
            baseMode.summary = actualMode.summary;
            return baseMode;
          }
          return null;
        })
        .filter(Boolean);
    } else {
      this.#retrieveModes = [];
    }
    this.#kbRetrieveModesSub.next(this.#retrieveModes);
  }
}
