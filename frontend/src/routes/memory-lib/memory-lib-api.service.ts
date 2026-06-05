import { inject, Injectable } from '@angular/core';

import {
  IMemoryLibChangeParams,
  ICreateMemoryLibResponse,
  IDeleteMemoryLibResponse,
  IMemoryLibItem,
  IMemoryLibsQueryParams,
  IMemoryLibsQueryResponse,
  IPatchMemoryLibResponse,
} from '@routes/memory-lib/memory-lib-interfaces';
import { HttpService } from '@services/http.service';
import { ContextService } from '@services/context.service';

@Injectable({
  providedIn: 'root',
})
export class MemoryLibApiService {
  readonly ctxServ = inject(ContextService);
  readonly http = inject(HttpService);

  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`.replaceAll('/v1/', '/v2/');
  }

  queryMemoryLibs(params: IMemoryLibsQueryParams): Promise<IMemoryLibsQueryResponse> {
    return this.http.getAsync({
      url: `${this.prefix}/memory-repositories`,
      query: params,
    });
  }

  createMemoryLibs(params: IMemoryLibChangeParams): Promise<ICreateMemoryLibResponse> {
    return this.http.postAsync({
      url: `${this.prefix}/memory-repositories`,
      params,
    });
  }

  queryMemoryLibDetail(memoryLibId: string): Promise<IMemoryLibItem> {
    return this.http.getAsync({
      url: `${this.prefix}/memory-repositories/${memoryLibId}`,
    });
  }

  patchMemoryLib(memoryLibId: string, params: IMemoryLibChangeParams): Promise<IPatchMemoryLibResponse> {
    return this.http.putAsync({
      url: `${this.prefix}/memory-repositories/${memoryLibId}`,
      params,
    });
  }

  deleteMemory(memoryLibId: string): Promise<IDeleteMemoryLibResponse> {
    return this.http.deleteAsync({
      url: `${this.prefix}/memory-repositories/${memoryLibId}`,
    });
  }
}
