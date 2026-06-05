import { Injectable } from "@angular/core";
import { TtsPlayerService } from "./tts-player.service";
import { VoiceService } from "./voice.service";

@Injectable({ providedIn: 'root' })
export class AudioLockService {
  private currentOwner: TtsPlayerService | VoiceService = null;

  public requestLock(requester: TtsPlayerService | VoiceService): boolean {
    if (this.currentOwner && this.currentOwner !== requester) {
      // 已被别人占用
      return false;
    }
    this.currentOwner = requester;
    return true;
  }

  public releaseLock(requester: TtsPlayerService | VoiceService): void {
    if (this.currentOwner === requester) {
      this.currentOwner = null;
    }
  }

  public forceStopOthers(requester: TtsPlayerService | VoiceService) {
    this.currentOwner?.stop();
    this.currentOwner = requester;
  }
}
