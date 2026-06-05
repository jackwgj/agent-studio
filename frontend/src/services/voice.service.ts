import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { MessageComponent } from '@shared/services/cfdata.service';
import { I18NextEagerPipe } from 'angular-i18next';
import { HttpService } from '@services/http.service';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { AudioLockService } from './audio-lock.service';
import { ContextService } from './context.service';
import { I18nNamespace } from '@i18n';
import { SSE } from '@shared/services/sse';

export const SILENCE_THRESHOLD = 0.03;
export const SILENCE_DURATION = 1500; // ms

const MAX_BASE64_SIZE = 4_194_304; // 4M
const MAX_DURATION = 60_000; // 60s
const SIZE_PERCENT = 1.4; // 转为base64后数据大约膨胀33%（即：原始二进制大小 * 1.33）

export interface RecordConfig {
  isDetect?: boolean;
  silence_threshold?: number;
  silence_duration?: number;
  timeslice?: number;
  format?: 'webm' | 'pcm';
  sample_rate?: number;
}

export interface AsrConfig {
  audio_format?:
    | 'pcm16k16bit'
    | 'pcm8k16bit'
    | 'ulaw16k8bit'
    | 'ulaw8k8bit'
    | 'alaw16k8bit'
    | 'alaw8k8bit'
    | 'mp3'
    | 'aac'
    | 'wav'
    | 'amr'
    | 'amrwb'
    | 'auto';
  property?:
    | 'chinese_16k_general'
    | 'chinese_16k_travel'
    | 'sichuan_16k_common'
    | 'cantonese_16k_common'
    | 'shanghai_16k_common'
    | 'arabic_16k_general'
    | 'english_16k_common';
  add_punc?: 'yes' | 'no';
  digit_norm?: 'yes' | 'no';
  vocabulary_id?: string;
  need_word_info?: 'yes' | 'no';
}

@Injectable({ providedIn: 'root' })
export class VoiceService {
  private mediaRecorder?: MediaRecorder;

  private stream?: MediaStream;

  private audioCtx?: AudioContext;

  private analyser?: AnalyserNode;

  private rafId?: number;

  private silenceStart = Date.now();

  private workletNode?: AudioWorkletNode;

  public isRecording$ = new BehaviorSubject(false);

  public stopRecord$ = new Subject<any>();

  private audioBlob$ = new BehaviorSubject<{
    role: 'assistant' | 'user';
    blob?: Blob;
    base64?: string;
    index?: number;
    isLast?: boolean;
    format: 'webm' | 'wav' | 'mp3';
  }>(null);

  constructor(
    private i18n: I18NextEagerPipe,
    private http: HttpService,
    private ngZone: NgZone,
    private audioLock: AudioLockService,
    private ctxServ: ContextService,
  ) {}

  public audioBlobObservable() {
    return this.audioBlob$.asObservable();
  }

  /**
   * 开始录音，可选支持静音检测、格式切换（webm 或 pcm），以及分段时间控制。
   *
   * @param config 可选配置项：
   *  - `isDetect`：是否启用静音检测（开启后在检测到用户停止说话后自动停止录音）。
   *  - `silence_threshold`：静音判定阈值（范围 0~1，默认 0.01），表示音频频率平均值低于此值视为静音。
   *  - `silence_duration`：静音持续时间（毫秒，默认 1500），达到该时间则触发自动停止。
   *  - `timeslice`：录音分段时间间隔（毫秒），用于触发分段。
   *  - `format`：录音格式，可选值为 `'webm'` 或 `'pcm'`（默认 `'webm'`）。pcm 格式将使用 AudioWorklet 采集并编码为 WAV。
   *  - `sample_rate`：采样率（Hz，仅PCM格式有效），默认值16KHz。
   *
   * @example
   * this.voiceService.startRecording({
   *   isDetect: true,
   *   silence_threshold: 0.02,
   *   silence_duration: 2000,
   *   timeslice: 1000,
   *   format: 'pcm',
   *   sample_rate: 16000,
   * });
   */
  public async startRecording(config?: RecordConfig) {
    if (!this.audioLock.requestLock(this)) {
      this.audioLock.forceStopOthers(this);
    }
    const {
      isDetect = false,
      silence_threshold = SILENCE_THRESHOLD,
      silence_duration = SILENCE_DURATION,
      timeslice = 1000,
      format = 'webm',
      sample_rate = 16000,
    } = config || {};

    if (this.isRecording$.getValue()) return;

    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    } catch {
      MessageComponent.showError(
        this.i18n.transform('microphone_permission_refuse', {
          ns: I18nNamespace.AGENT_CENTER,
        }),
      );
      this.stopRecord$.next(true);
      return;
    }

    this.audioCtx = new AudioContext();
    if (format === 'pcm') {
      await this.startPCMRecording({ timeslice, sample_rate });
    } else {
      this.startWebmRecording({ timeslice });
    }

    if (!isDetect) return;

    this.analyser = this.audioCtx?.createAnalyser();
    this.analyser.fftSize = 512;
    const source = this.audioCtx.createMediaStreamSource(this.stream);
    source.connect(this.analyser);

    const data = new Uint8Array(this.analyser.frequencyBinCount);
    this.silenceStart = Date.now();

    const detect = () => {
      this.analyser?.getByteFrequencyData(data);
      const avg = data.reduce((a, b) => a + b, 0) / data.length / 255;
      const now = Date.now();

      if (avg < silence_threshold) {
        if (now - this.silenceStart > silence_duration) {
          this.stop();
          return;
        }
      } else {
        this.silenceStart = now;
      }

      this.rafId = requestAnimationFrame(detect);
    };

    detect();
  }

  private async close() {
    this.workletNode?.disconnect();
    await this.audioCtx?.close();
    this.stopStream();
    this.ngZone.run(() => {
      this.isRecording$.next(false);
    });
    this.audioCtx = undefined;
    this.analyser = undefined;
    this.mediaRecorder = undefined;
    this.workletNode = undefined;
  }

  private startWebmRecording({ timeslice }) {
    this.mediaRecorder = new MediaRecorder(this.stream, {
      mimeType: 'audio/webm;codecs=opus',
    });

    let group: Blob[] = [];
    let groupSize = 0;
    let groupDuration = 0;
    let index = 0;

    const flush = async (isLast = false) => {
      const merged = new Blob(group, { type: 'audio/webm' });
      const base64 = await this.blobToBase64(merged);
      this.ngZone.run(() => {
        this.audioBlob$.next({
          role: 'user',
          blob: merged,
          base64,
          index: index++,
          isLast,
          format: 'webm',
        });
      });
    };

    this.mediaRecorder.ondataavailable = async (event) => {
      const chunk = event.data;
      if (!chunk || chunk.size === 0) return;

      const estimatedBase64 = (groupSize + chunk.size) * SIZE_PERCENT;
      const duration = groupDuration + timeslice;

      if (estimatedBase64 > MAX_BASE64_SIZE || duration > MAX_DURATION) {
        await flush(false);
        group = [];
        groupSize = 0;
        groupDuration = 0;
        this.mediaRecorder.ondataavailable = null;
        this.mediaRecorder.onstop = null;
        await this.close();
      }

      group.push(chunk);
      groupSize += chunk.size;
      groupDuration += timeslice;
    };
    this.mediaRecorder.onstop = async () => {
      if (group.length > 0) {
        await flush(true);
      }
      this.mediaRecorder.ondataavailable = null;
      this.mediaRecorder.onstop = null;
      await this.close();
    };

    this.isRecording$.next(true);
    this.mediaRecorder.start(timeslice);
  }

  private async startPCMRecording({ timeslice, sample_rate }) {
    await this.audioCtx.audioWorklet.addModule(
      cdnAssetUrl('assets/worklet/pcm-processor.js'),
    );

    const source = this.audioCtx.createMediaStreamSource(this.stream);
    this.workletNode = new AudioWorkletNode(this.audioCtx, 'pcm-processor');

    this.workletNode.port.postMessage({
      sampleRate: sample_rate,
      timeslice,
    });

    let pcmGroup = [];
    let pcmGroupSize = 0;
    let pcmStartTime = Date.now();
    let pcmIndex = 0;

    this.workletNode.port.onmessage = async (event) => {
      const { buffer, isLast } = event.data || {};
      const chunk = new Int16Array(buffer); // AudioWorklet 发送的是 Int16Array 的 buffer, 默认16KHz16bit, 即每秒采样16000次，每个采样点2字节
      if (!chunk?.length) return;

      const now = Date.now();

      pcmGroup.push(chunk);
      pcmGroupSize += chunk.length * 2; // Int16Array，每个元素2字节

      const duration = now - pcmStartTime;
      const estimatedBase64Size = pcmGroupSize * SIZE_PERCENT;

      const flush = async (isLast: boolean) => {
        const wavBlob = this.encodeWAV(pcmGroup, sample_rate);
        const base64 = await this.blobToBase64(wavBlob);

        this.ngZone.run(() => {
          this.audioBlob$.next({
            role: 'user',
            blob: wavBlob,
            base64,
            index: pcmIndex++,
            isLast,
            format: 'wav',
          });
        });
      };
      if (
        estimatedBase64Size > MAX_BASE64_SIZE ||
        duration > MAX_DURATION ||
        isLast
      ) {
        await flush(isLast);
        pcmGroup = [];
        pcmGroupSize = 0;
        pcmStartTime = now;
        if (isLast) {
          this.workletNode.port.onmessage = null;
          await this.close();
        }
      }
    };

    this.isRecording$.next(true);
    source.connect(this.workletNode);
    this.workletNode.connect(this.audioCtx.destination);
  }

  public async stop() {
    this.audioLock.releaseLock(this);
    if (this.rafId) cancelAnimationFrame(this.rafId);

    if (this.workletNode) {
      this.workletNode.port.postMessage({ stop: true });
    } else if (this.mediaRecorder) {
      this.mediaRecorder.stop();
    }
  }

  private stopStream() {
    this.stream?.getTracks().forEach((track) => track.stop());
    this.stream = undefined;
  }

  private mergeBuffers(buffers: Int16Array[]): Int16Array {
    const length = buffers.reduce((acc, b) => acc + b.length, 0);
    const result = new Int16Array(length);
    let offset = 0;
    for (const b of buffers) {
      result.set(b, offset);
      offset += b.length;
    }
    return result;
  }

  private encodeWAV(chunks: Int16Array[], sampleRate: number): Blob {
    const merged = this.mergeBuffers(chunks);
    const buffer = new ArrayBuffer(44 + merged.length * 2);
    const view = new DataView(buffer);

    const writeString = (offset: number, str: string) => {
      for (let i = 0; i < str.length; i++) {
        view.setUint8(offset + i, str.charCodeAt(i));
      }
    };

    writeString(0, 'RIFF'); // 文件标识符，必须是 "RIFF"
    view.setUint32(4, 36 + merged.length * 2, true); // 整个文件大小 - 8 字节（= 36 + 数据大小）
    writeString(8, 'WAVE'); // 格式标识符，必须是 "WAVE"
    writeString(12, 'fmt '); // 子块1标识符，固定为 "fmt "
    view.setUint32(16, 16, true); // 子块1大小，PCM 固定为 16 字节
    view.setUint16(20, 1, true); // 音频格式，PCM 格式为 1
    view.setUint16(22, 1, true); // 声道数，1 = 单声道，2 = 双声道
    view.setUint32(24, sampleRate, true); // 采样率，如 16000 或 44100
    view.setUint32(28, sampleRate * 2, true); // 每秒传输的字节数 = SampleRate * NumChannels * BitsPerSample / 8
    view.setUint16(32, 2, true); // 每个采样块的字节数 = NumChannels * BitsPerSample / 8
    view.setUint16(34, 16, true); // 每个采样点的位数，常用为 16 位
    writeString(36, 'data'); // 子块2标识符，固定为 "data"
    view.setUint32(40, merged.length * 2, true); // 原始 PCM 数据的字节数 = 采样点数 * 每个样本字节数

    let offset = 44;
    for (let i = 0; i < merged.length; i++, offset += 2) {
      view.setInt16(offset, merged[i], true);
    }

    return new Blob([view], { type: 'audio/wav' });
  }

  private blobToBase64(blob: Blob): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onloadend = () => resolve((reader.result as string).split(',')[1]);
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  }

  public voiceToText(options: {
    data: any;
    config?: AsrConfig;
    onmessage: (event) => void;
    ondone: () => void;
    onerror: (error) => void;
    ontimeout: () => void;
  }): SSE {
    const sse = new SSE(
      `${this.http.prefixPath}${
        this.ctxServ.baseUrl
      }/agent-manager/agents/audio/transcriptions?workspace_id=${this.http.getWorkspaceId()}`,
      {
        payload: JSON.stringify({
          data: options.data,
          config: {
            audio_format: 'auto',
            property: 'chinese_16k_general',
            add_punc: 'yes',
            ...options.config,
          },
        }),
        method: 'POST',
        withCsrf: true,
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: 60_000,
      },
    );

    sse.addEventListener('message', options.onmessage);
    sse.addEventListener('done', options.ondone);
    sse.addEventListener('error', options.onerror);
    sse.addEventListener('timeout', options.ontimeout);

    sse.stream();

    return sse;
  }

  public voiceToTextPost(options: any): Promise<any> {
    const params = {
      data: options.data,
      config: {
        audio_format: 'auto',
        property: 'chinese_16k_general',
        add_punc: 'yes',
      },
    };
    return this.http.postAsync({
      url: `${this.ctxServ.baseUrl}/agent-manager/agents/audio/transcriptions`,
      params,
    });
  }
}
