import { ISSEvent, SSE } from '@shared/services/sse';
import { buildErrorMsgStr, ICommonError } from 'src/utils/utils';
import { MessageComponent } from '@shared/services/cfdata.service';

export class JiuwenSse extends SSE {
  protected override parseEventChunk(chunk: string): ISSEvent | null {
    if (!chunk || chunk.length === 0) {
      return null;
    }
    const e = { id: null, retry: null, data: '', event: 'message' };
    chunk.split(/\n|\r\n|\r/).forEach((line: string) => {
      line = line.trimEnd();
      const index = line.indexOf(this.FIELD_SEPARATOR);
      if (index <= 0) {
        return;
      }

      const field: string = line.slice(0, Math.max(0, index));
      if (!(field in e)) {
        return;
      }
      const value = line.slice(Math.max(0, index + 1));
      if (field === 'data') {
        e.data += value;
      }
    });
    // 处理DONE事件

    let event: ISSEvent;
    if(e.data.trim() === '[DONE]' || e.data.trim() === '[Done]'){
      event=new CustomEvent('done');
    }else{
      event = new CustomEvent('message');
      // message 类型还需要检查是否是流式报错，统一拦截
      try{
        const eventData=JSON.parse(e.data);
        if (Object.prototype.hasOwnProperty.call(eventData,'event') && eventData?.event==='error'){
          // 流式响应报错
          const errorInstance:ICommonError=eventData?.data as ICommonError;
          const errMsg = buildErrorMsgStr(errorInstance);
          if  (errMsg){
            MessageComponent.showError(errMsg);
          }
        }
      }
      catch (e){
        // json解析失败，非json数据，联系后端排查
      }
    }
    event.data = e.data;
    event.id = e.id;
    return event;
  }
}
