import { Injectable } from '@angular/core';
import { CanDeactivate } from '@angular/router';
import { OneWorkFlowComponent } from 'src/routes/agent/static-app-orchestration/graph-manage/work-flow/work-flow.component';
import { of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LeaveGuard implements CanDeactivate<OneWorkFlowComponent> {
  canDeactivate(component: OneWorkFlowComponent) {
    if (
      component.onPageLeaving &&
      typeof component.onPageLeaving === 'function'
    ) {
      return component.onPageLeaving();
    }
    return of(true);
  }
}
