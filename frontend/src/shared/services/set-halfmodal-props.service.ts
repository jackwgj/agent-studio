import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class SetHalfmodalPropsService {
  public calcHalfmodalPosition() {
    const consoleSidebar = document.querySelector('#J_header');
    return consoleSidebar ? { top: 48 } : { top: 0 };
  }
}
