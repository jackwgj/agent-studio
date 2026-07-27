import { Injectable, NgZone } from '@angular/core';
import { NewTaskService } from './new-task.service';

export interface IChatTurn {
  role: 'user' | 'assistant';
  content: string;
  assets?: any[];
  created_at?: string;
}

export interface IAssetRef {
  kind: string;
  id: string;
  name?: string;
  meta?: any;
}

/**
 * 会话状态存储（new-task-chat）：管理多轮对话 turns、流式缓冲、右栏预览 data，
 * 并封装 create / append / load 与 SSE 订阅。SSE 复用 /platform/v1/tasks/{id}/stream，
 * EventSource 同源自动携带 Cookie，由部署层注入 Bearer。
 */
@Injectable({ providedIn: 'root' })
export class ConversationStore {
  taskId: string | null = null;
  agentId = '';
  taskName = '';
  turns: IChatTurn[] = [];
  preview = '';
  status: string | null = null;
  streaming = false;

  private es: EventSource | null = null;
  private _buffer = '';

  constructor(private svc: NewTaskService, private zone: NgZone) {}

  reset() {
    this.taskId = null;
    this.agentId = '';
    this.taskName = '';
    this.turns = [];
    this.preview = '';
    this.status = null;
    this.streaming = false;
    this._buffer = '';
    this.closeStream();
  }

  /** 首轮创建任务（创建即执行） */
  async create(
    agentId: string,
    name: string,
    input: string,
    assets: IAssetRef[],
    projectId?: string,
    workspaceId?: string,
  ): Promise<void> {
    this.agentId = agentId;
    this.taskName = name;
    const res: any = await this.svc.createTask({
      agent_id: agentId,
      name,
      params: { input, assets },
      project_id: projectId,
      workspace_id: workspaceId,
    });
    const payload = res?.data ?? res;
    this.taskId = payload?.id ?? payload?.task_id ?? null;
    this.turns = [{ role: 'user', content: input, assets }];
    this.status = payload?.status ?? 'pending';
    this.openStream();
  }

  /** 多轮追加重跑：追加一条 user turn 并基于历史重新执行 */
  async append(content: string, assets: IAssetRef[]): Promise<void> {
    if (!this.taskId) {
      return;
    }
    this.turns = [...this.turns, { role: 'user', content, assets }];
    const res: any = await this.svc.appendTurn(this.taskId, content, assets);
    const payload = res?.data ?? res;
    this.status = payload?.status ?? this.status;
    this.openStream();
  }

  /** 加载已有任务（点击左栏列表）：恢复 turns 与右栏预览 */
  async load(taskId: string): Promise<void> {
    const res: any = await this.svc.getTask(taskId);
    const payload = res?.data ?? res;
    if (!payload) {
      return;
    }
    this.taskId = taskId;
    this.agentId = payload.agent_id ?? '';
    this.taskName = payload.name ?? '';
    this.status = payload.status ?? null;
    this.turns = (payload.turns ?? []).map((t: any) => ({
      role: t.role,
      content: t.content,
      assets: t.assets ?? [],
      created_at: t.created_at,
    }));
    this.preview = payload.data ?? payload.result?.reply ?? '';
  }

  private openStream() {
    if (!this.taskId) {
      return;
    }
    this.closeStream();
    this.streaming = true;
    const url = this.svc.streamUrl(this.taskId);
    this.es = new EventSource(url, { withCredentials: true });
    this._buffer = '';
    this.es.onmessage = (ev) => {
      // EventSource 回调默认在 Angular zone 外执行，需包裹以触发变更检测。
      this.zone.run(() => {
        let frame: any;
        try {
          frame = JSON.parse(ev.data);
        } catch {
          return;
        }
        const text = typeof frame.data === 'string' ? frame.data : JSON.stringify(frame.data ?? '');
        switch (frame.type) {
          case 'token':
            this._buffer += text;
            this.appendAssistant(this._buffer);
            break;
          case 'done':
            this.status = frame.data?.status ?? 'succeeded';
            this.preview = frame.data?.result?.reply ?? this._buffer;
            this.finalizeAssistant(this.preview);
            this.streaming = false;
            this.closeStream();
            break;
          case 'error':
            this.status = 'failed';
            this.streaming = false;
            this.closeStream();
            break;
          default:
            break;
        }
      });
    };
    this.es.onerror = () => {
      // SSE 连接关闭时，若 done 帧因连接即时关闭而丢失，用已缓冲内容兜底预览。
      this.zone.run(() => {
        if (!this.preview && this._buffer) {
          this.preview = this._buffer;
          this.finalizeAssistant(this.preview);
        }
        this.streaming = false;
        this.closeStream();
      });
    };
  }

  private appendAssistant(text: string) {
    const last = this.turns[this.turns.length - 1];
    if (last && last.role === 'assistant') {
      last.content = text;
    } else {
      this.turns = [...this.turns, { role: 'assistant', content: text }];
    }
  }

  private finalizeAssistant(text: string) {
    const last = this.turns[this.turns.length - 1];
    if (last && last.role === 'assistant') {
      last.content = text;
    } else {
      this.turns = [...this.turns, { role: 'assistant', content: text }];
    }
  }

  private closeStream() {
    if (this.es) {
      this.es.close();
      this.es = null;
    }
  }
}
