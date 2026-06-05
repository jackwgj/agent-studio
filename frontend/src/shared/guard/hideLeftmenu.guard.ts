import { Injectable } from '@angular/core';
import { UrlTree } from '@angular/router';
import { ContextService } from '@services/context.service';
import { Observable } from 'rxjs';

@Injectable()
class HideLeftmenuGuard  {
  constructor(private ctxServ: ContextService) {}

  canActivate():
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    this.ctxServ.setIsHideLeftMenu(true);

    return true;
  }
}

export default HideLeftmenuGuard;
