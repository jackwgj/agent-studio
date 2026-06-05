import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environment/environment';

@Injectable()
class DevelopGuard {
  constructor(private router: Router) {}

  canActivate(): Observable<boolean> | Promise<boolean> | boolean {
    const isIframe = window.self !== window.top;
    if (isIframe && environment.envType === 'hc') {
      // 重定向到develop
      this.router.navigate(['/home/develop-space']);
      return false;
    }
    return true;
  }
}

export default DevelopGuard;
