import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { StorageService } from '@shared/services/cfdata.service';
import { JiuwenSse } from "@services/jiuwen-model/jiuwen-sse";

@Injectable({
  providedIn: 'root',
})
export class AiAssistCreateHomeService {
  // 定义持久化用的 Key
  private readonly AGENT_CREATE_CONFIG = 'AGENT_CREATE_CONFIG';

  // 内部变量用于内存读取
  private _modelName = '';
  private _authId = '';
  private _modelExplicitName = '';
  private _modelType = '';
  private _modelInterfaceProtocol = '';

  // 使用 getter/setter 确保内存操作的同时自动同步到 sessionStorage
  set modelName(val: string) {
    this._modelName = val;
    this.persistConfig();
  }
  get modelName(): string { return this._modelName; }

  set authId(val: string) {
    this._authId = val;
    this.persistConfig();
  }
  get authId(): string { return this._authId; }

  set modelExplicitName(val: string) {
    this._modelExplicitName = val;
    this.persistConfig();
  }
  get modelExplicitName(): string { return this._modelExplicitName; }

  set modelType(val: string) {
    this._modelType = val;
    this.persistConfig();
  }
  get modelType(): string { return this._modelType; }

  set modelInterfaceProtocol(val: string) {
    this._modelInterfaceProtocol = val;
    this.persistConfig();
  }
  get modelInterfaceProtocol(): string { return this._modelInterfaceProtocol; }

  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(
    private ctxServ: ContextService,
    private configServ: AgentConfigService
  ) {
    // 实例化时立即从缓存恢复数据
    this.restoreConfig();
  }

  /**
   * 将当前配置持久化到 sessionStorage
   */
  private persistConfig() {
    const config = {
      modelName: this._modelName,
      authId: this._authId,
      modelExplicitName: this._modelExplicitName,
      modelType: this._modelType,
      modelInterfaceProtocol: this._modelInterfaceProtocol
    };
    StorageService.setSessionStorage(this.AGENT_CREATE_CONFIG, JSON.stringify(config));
  }

  /**
   * 从 sessionStorage 恢复配置到内存
   */
  private restoreConfig() {
    const data = StorageService.getSessionStorage(this.AGENT_CREATE_CONFIG);
    if (data) {
      try {
        const config = JSON.parse(data);
        this._modelName = config.modelName || '';
        this._authId = config.authId || '';
        this._modelExplicitName = config.modelExplicitName || '';
        this._modelType = config.modelType || '';
        this._modelInterfaceProtocol = config.modelInterfaceProtocol || '';
      } catch (e) {

      }
    }
  }

  /**
   * 离开页面时销毁缓存
   */
  public clearPersistedConfig() {
    StorageService.delSessionStorage(this.AGENT_CREATE_CONFIG);
    // 清空内存
    this._modelName = '';
    this._authId = '';
    this._modelExplicitName = '';
    this._modelType = '';
    this._modelInterfaceProtocol = '';
  }

  public configureJiuwenSse(
    seeUrl: string,
    params: any,
    lang: string,
    handlers: {
      onStatus?: () => void;
      onOpen?: () => void;
      onMessage?: (event: any) => void;
      onError?: (event: any) => void;
      onAbort?: () => void;
      onReadyStateChange?: (event: any) => void;
      onModeration?: (event: any) => void;
      onTimeout?: () => void;
      onDone?: () => void;
    } = {},
  ): JiuwenSse {
    const nilFunc = () => void 0;
    const { stream_first_chunk_timeout, stream_interval_timeout } =
      this.configServ.getConfigs();

    const source = new JiuwenSse(seeUrl, {
      headers: {
        'Content-Type': 'application/json',
        stream: 'true',
        'X-Language': lang ?? 'zh-cn',
      },
      payload: JSON.stringify({ ...params }),
      method: 'POST',
      withCsrf: true,
      timeout: 3600000,
      streamFirstChunkTimeout: stream_first_chunk_timeout ?? 180000,
      streamTimeout: stream_interval_timeout ?? 180000,
    });

    // 添加事件监听器
    source.addEventListener('open', handlers.onOpen ?? nilFunc);
    source.addEventListener('message', handlers.onMessage ?? nilFunc);
    source.addEventListener('status', handlers.onStatus ?? nilFunc);
    source.addEventListener('abort', handlers.onAbort ?? nilFunc);
    source.addEventListener(
      'readystatechange',
      handlers.onReadyStateChange ?? nilFunc,
    );
    source.addEventListener('moderation', handlers.onModeration ?? nilFunc);
    source.addEventListener('timeout', handlers.onTimeout ?? nilFunc);
    source.addEventListener('error', handlers.onError ?? nilFunc);
    source.addEventListener('done', handlers.onDone ?? nilFunc);

    source.stream();

    return source;
  }
}
