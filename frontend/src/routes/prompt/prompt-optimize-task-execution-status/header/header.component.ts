import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { MODULES } from '@shared/modules';
import { I18NextEagerPipe } from 'angular-i18next';
import { Router } from '@angular/router';

@Component({
  selector: 'meta-header',
  standalone: true,
  imports: [NzIconModule, MODULES],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
})
export class HeaderComponent {


  constructor(
    private i18n: I18NextEagerPipe,
    private router: Router) {
  }



  onBack() {
    this.router.navigate(['/home/agent-center/library-home'], { queryParams: { tabId: 'prompt', }});
  }
}
