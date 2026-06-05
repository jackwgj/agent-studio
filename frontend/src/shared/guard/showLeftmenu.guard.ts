import { Injectable } from '@angular/core';
import { UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { ContextService } from '@services/context.service';

@Injectable()
class ShowLeftmenuGuard  {
  constructor(private ctxServ: ContextService) {}

  canActivate():
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    this.ctxServ.setIsHideLeftMenu(false);

    return true;
  }
}

export default ShowLeftmenuGuard;
