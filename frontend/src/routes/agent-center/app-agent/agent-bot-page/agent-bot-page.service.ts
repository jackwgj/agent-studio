import { Injectable, signal } from '@angular/core';
import { IMemoryLibItem } from '@routes/memory-lib/memory-lib-interfaces';

@Injectable({
  providedIn: 'root',
})
export class AgentBotPageService {
  agentDetail = signal(null);

  getMemoryLib(): IMemoryLibItem | null {
    return this.agentDetail()?.memory_config ?? null;
  }
}
