import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ActivatedRoute,
  Router,
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
} from '@angular/router';
import { environment } from '../../environment/environment';
import { StorageService } from '@shared/services/cfdata.service';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';

@Injectable()
class SpaceTeamGuard {
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private appAgentRepoServ: AppAgentRepoService,
  ) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot,
  ): Observable<boolean> | Promise<boolean> | boolean {
    const cur_space_options = JSON.parse(
      StorageService.getSessionStorage('CUR_SPACE_OPTIONS'),
    );
    if (!cur_space_options ||  cur_space_options.type === 'PERSON') {
      this.router.navigate(['/home/overview']);
      return false;
    }
    return true;
  }
}

export default SpaceTeamGuard;
