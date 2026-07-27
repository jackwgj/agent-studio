import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';

interface IAssetItem {
  id: string;
  name: string;
}

interface IAttachment {
  id: string;
  name: string;
  url: string;
}

/**
 * 资产选择器（new-task-chat）：两个真实数据页签
 * - 「智能体」：调 studio agent-manager 拉取当前团队空间的真实智能体列表（单选）；
 *   选中的智能体作为 {kind:'agent'} 资产随发送提交，后端据此路由到该智能体关联的 LLM。
 * - 「附件」：调 studio upload-file 真实上传接口，上传结果作为 {kind:'attachment', url}
 *   资产提交，后端将其 url 透传给对话链路。
 * 以 [(selected)] 双向绑定，选中项作为 assets 随发送提交。
 */
@Component({
  selector: 'app-asset-picker',
  templateUrl: './asset-picker.component.html',
  styleUrls: ['./asset-picker.component.less'],
  standalone: true,
  imports: [CommonModule, FormsModule, MODULES],
})
export class AssetPickerComponent implements OnInit {
  @Input() selected: any[] = [];
  @Output() selectedChange = new EventEmitter<any[]>();

  activeIndex = 0;

  /** 智能体真实列表 */
  agents: IAssetItem[] = [];
  agentsLoading = false;

  /** 附件上传态与已上传列表 */
  uploading = false;
  attachments: IAttachment[] = [];
  /** 上传失败时的可见提示（上游接口暂不可用等），不静默吞掉 */
  uploadError = '';

  constructor(private agentRepo: AppAgentRepoService) {}

  ngOnInit(): void {
    this.loadAgents();
    // 从既有 selected 恢复附件展示（多轮/回填场景）
    this.attachments = (this.selected || [])
      .filter((s) => s?.kind === 'attachment')
      .map((s) => ({ id: s.id, name: s.name, url: s.url }));
  }

  async loadAgents(): Promise<void> {
    this.agentsLoading = true;
    try {
      const res: any = await this.agentRepo.getAgentList({}, 0, 100);
      const list =
        res?.agent_list ||
        res?.data?.agent_list ||
        res?.data?.records ||
        res?.records ||
        res?.data ||
        [];
      this.agents = (Array.isArray(list) ? list : []).map((a: any) => ({
        id: a.agent_id || a.id,
        name: a.agent_name || a.name || a.id,
      }));
    } catch {
      this.agents = [];
    } finally {
      this.agentsLoading = false;
    }
  }

  isAgentChecked(id: string): boolean {
    return this.selected.some((s) => s.kind === 'agent' && s.id === id);
  }

  /** 智能体单选：选一个即互斥替换其它 agent 资产 */
  toggleAgent(item: IAssetItem): void {
    const already = this.isAgentChecked(item.id);
    const rest = this.selected.filter((s) => s.kind !== 'agent');
    this.selected = already
      ? rest
      : [...rest, { kind: 'agent', id: item.id, name: item.name }];
    this.selectedChange.emit(this.selected);
  }

  async onFileSelected(ev: Event): Promise<void> {
    const input = ev.target as HTMLInputElement;
    const file = input.files && input.files[0];
    if (!file) {
      return;
    }
    this.uploading = true;
    try {
      const fd = new FormData();
      fd.append('file', file);
      const res: any = await this.agentRepo.uploadFile(fd);
      const payload = res?.data ?? res;
      const url =
        payload?.url ||
        payload?.fileUrl ||
        payload?.file_url ||
        payload?.path ||
        payload?.downloadUrl ||
        '';
      const id =
        payload?.id ||
        payload?.fileId ||
        payload?.file_id ||
        url ||
        `${file.name}-${Date.now()}`;
      const att: IAttachment = { id, name: file.name, url };
      this.attachments = [...this.attachments, att];
      this.selected = [...this.selected, { kind: 'attachment', ...att }];
      this.selectedChange.emit(this.selected);
    } catch (e) {
      // 上游 upload-file 接口暂不可用（如 studio-service 端点缺失）时，给出可见提示，
      // 不静默吞掉，避免用户误以为上传成功。
      console.error('附件上传失败', e);
      this.uploadError = '附件上传失败（上游接口暂不可用），不影响对话，可稍后重试';
    } finally {
      this.uploading = false;
      input.value = '';
    }
  }

  removeAttachment(id: string): void {
    this.attachments = this.attachments.filter((a) => a.id !== id);
    this.selected = this.selected.filter((s) => !(s.kind === 'attachment' && s.id === id));
    this.selectedChange.emit(this.selected);
  }
}
