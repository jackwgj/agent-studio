import {Injectable} from '@angular/core';
import {HttpService} from "@services/http.service";

@Injectable({
  providedIn: 'root'
})
export class SendRequestService {

  constructor(
    private http: HttpService
  ) {
  }


  header = {
  }

  // 发送请求
  sendRequest(url, data, options, method = 'post',): Promise<any> {
    if (method === 'post') {
      return this.http.postAsync({
        url,
        params: data,
        ...options
      });
    }
    if (method === 'get') {
      return this.http.getAsync({
        url,
        params: data,
        ...options
      });
    }
    if (method === 'delete') {
      return this.http.deleteAsync({
        url,
        params: data,
        ...options
      });
    }
    return null;
  }
}
