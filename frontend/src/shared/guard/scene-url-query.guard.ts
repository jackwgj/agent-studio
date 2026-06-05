import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class SceneUrlQueryGuard {
  private sceneQuery: Record<string, any> | null = null;

  constructor(private router: Router) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot,
  ): boolean | UrlTree {

    // agentBuilder 场景 start
    const match = window.location.href.match(/work_space_id=([^&]*)/);
    if (match?.[1]) {
      this.sceneQuery = {
        from: 'agentBuilder',
        work_space_id: match[1],
      };
      if (!next.queryParams?.work_space_id) {
        return this.router.createUrlTree(
          [state.url.split('?')[0]],
          {
            queryParams: {
              ...next.queryParams,
              ...this.sceneQuery,
            },
            queryParamsHandling: 'merge',
            preserveFragment: true,
          });
      }
      return true;
    }
    // agentBuilder 场景 end

    return true;
  }
}
