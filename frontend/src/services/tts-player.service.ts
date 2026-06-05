import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, from, Subject, throwError } from 'rxjs';
import {
  catchError,
  concatMap,
  finalize,
  switchMap,
  takeUntil,
  tap,
} from 'rxjs/operators';
import { HttpService } from './http.service';
import { AudioLockService } from './audio-lock.service';
import { ContextService } from './context.service';
import { MessageComponent } from '@shared/services/cfdata.service';
import { I18NextEagerPipe } from 'angular-i18next';

export interface TtsConfig {
  audio_format?: string;
  sample_rate?: string;
  property: string;
  speed?: number;
  pitch?: number;
  volume?: number;
}

export enum TtsState {
  Idle = 'idle',
  Loading = 'loading',
  Playing = 'playing',
  Error = 'error',
}

@Injectable({ providedIn: 'root' })
export class TtsPlayerService {
  private stop$ = new Subject<void>();
  private context: AudioContext | null = null;
  private currentSource: AudioBufferSourceNode | null = null;
  private segments: string[] = [];
  private base64Cache: string[] = [];
  private preloadState = TtsState.Loading;

  public state$ = new BehaviorSubject<TtsState>(TtsState.Idle);

  constructor(
    private http: HttpService,
    private ngZone: NgZone,
    private audioLock: AudioLockService,
    private ctxServ: ContextService,
    private i18n: I18NextEagerPipe,
  ) {}

  public play(text: string, config: TtsConfig) {
    if (!this.audioLock.requestLock(this)) {
      this.audioLock.forceStopOthers(this);
    }
    this.cleanup();
    this.state$.next(TtsState.Loading);
    this.context = new AudioContext();
    this.segments = this.splitTextByPunctuation(text);
    this.base64Cache = new Array(this.segments.length).fill(null);

    // 预请求第一段
    from(this.tts(this.segments[0], config))
      .pipe(
        takeUntil(this.stop$),
        switchMap((base64) => {
          if (!base64 || base64.trim() === '') {
            this.state$.next(TtsState.Error);
            MessageComponent.showError(this.i18n.transform('tts_res_empty'));
            return throwError(() => new Error(this.i18n.transform('ttsplayerservice_36')));
          }
          this.base64Cache[0] = base64;
          return this.playNext(config);
        }),
        catchError((err) => {
          this.state$.next(TtsState.Error);
          return throwError(() => err);
        }),
        finalize(() => this.stop()),
      )
      .subscribe();
  }

  private playNext(config: TtsConfig) {
    return from(this.segments).pipe(
      takeUntil(this.stop$),
      concatMap((_, i) => {
        const nextIndex = i + 1;

        // 播放当前段
        const base64 = this.base64Cache[i];
        const currentPlay$ = base64
          ? from(this.decodeAndPlayWav(base64))
          : from(this.tts(this.segments[i], config)).pipe(
            tap((b64) => (this.base64Cache[i] = b64)),
            switchMap((b64) => this.decodeAndPlayWav(b64)),
          );

        // 同时请求下一段（异步）
        if (nextIndex < this.segments.length && !this.base64Cache[nextIndex]) {
          this.preloadState = TtsState.Loading;
          this.tts(this.segments[nextIndex], config)
            .then((b64) => {
              this.base64Cache[nextIndex] = b64;
              this.preloadState = TtsState.Playing;
              return b64;
            })
            .catch(() => {
              this.preloadState = TtsState.Error;
            });
        }

        return currentPlay$;
      }),
    );
  }

  public stop() {
    this.audioLock.releaseLock(this);
    this.cleanup();
  }

  private cleanup() {
    this.stop$.next();
    if (this.currentSource) {
      this.currentSource.stop();
      this.currentSource.disconnect();
      this.currentSource = null;
    }
    if (this.context) {
      this.context.close().catch(() => {});
      this.context = null;
    }
    this.segments = [];
    this.base64Cache = [];
    this.state$.next(TtsState.Idle);
  }

  private decodeAndPlayWav(base64: string) {
    const buffer = this.base64ToArrayBuffer(base64);
    const context = this.context!;

    return from(context.decodeAudioData(buffer)).pipe(
      switchMap(
        (decoded) =>
          new Promise<void>((resolve) => {
            const source = context.createBufferSource();
            source.buffer = decoded;
            source.connect(context.destination);
            source.start();

            this.currentSource = source;
            this.state$.next(TtsState.Playing);

            source.onended = () => {
              this.currentSource = null;
              this.ngZone.run(() => {
                if (this.preloadState === TtsState.Error) {
                  this.stop();
                } else if (this.preloadState === TtsState.Loading) {
                  this.state$.next(TtsState.Loading);
                }
              });
              resolve();
              source.onended = null;
            };
          }),
      ),
    );
  }

  private base64ToArrayBuffer(base64: string): ArrayBuffer {
    const binary = atob(base64);
    return Uint8Array.from(binary, (c) => c.charCodeAt(0)).buffer;
  }

  private async tts(text: string, config: TtsConfig): Promise<string> {
    return this.http
      .postAsync({
        url: `${this.ctxServ.baseUrl}/agent-manager/agents/audio/tts?`,
        params: {
          text,
          config: {
            volume: 50,
            ...config,
          },
        },
      })
      .then((res: any) => res?.data ?? '');
  }

  private splitTextByPunctuation(text: string, maxLen = 500): string[] {
    const result: string[] = [];
    const punctuations = [
      '。',
      '！',
      '？',
      '；',
      '：',
      '.',
      '!',
      '?',
      ';',
      ':',
      '\n',
    ];
    let start = 0;

    while (start < text.length) {
      const end = Math.min(start + maxLen, text.length);
      let slice = text.slice(start, end);

      if (end === text.length) {
        result.push(slice);
        break;
      }

      let lastPuncIndex = -1;
      for (let i = slice.length - 1; i >= 0; i--) {
        if (punctuations.includes(slice[i])) {
          lastPuncIndex = i;
          break;
        }
      }

      if (lastPuncIndex === -1) {
        result.push(slice);
        start += slice.length;
      } else {
        const part = slice.slice(0, lastPuncIndex + 1);
        result.push(part);
        start += lastPuncIndex + 1;
      }
    }

    return result;
  }
}
