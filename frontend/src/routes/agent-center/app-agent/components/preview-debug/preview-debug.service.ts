import {Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class PreviewDebugService {
  //用于监听输入框是否可以发送
  private readonly senderMeta$ = new BehaviorSubject<boolean>(false);

  public setSendMeta(status: boolean) {
    this.senderMeta$.next(status);
  }

  public updateSendMeta() {
    return this.senderMeta$.asObservable();
  }
}
