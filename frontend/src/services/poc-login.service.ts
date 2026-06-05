import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class PocLoginService {
  constructor(
    private http: HttpService,

  ) { }

  public postPocLogin(params: {
    "name": string,
    "password": string
  }): Observable<any> {
    return this.http.post({
      url: `/authui/login`,
      params,
      timeout: 60_000,
    });
  }



}
