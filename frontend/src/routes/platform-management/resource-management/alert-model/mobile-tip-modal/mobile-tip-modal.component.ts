import { Clipboard } from '@angular/cdk/clipboard';
import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  ViewEncapsulation
} from '@angular/core';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';

@Component({
  selector: 'app-mobile-tip-modal',
  templateUrl: './mobile-tip-modal.component.html',
  styleUrls: ['./mobile-tip-modal.component.scss'],
  encapsulation: ViewEncapsulation.None,
  standalone: true,
  imports: [
    MODULES,
    CommonModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON],
    }
  ],
})
export class MobileTipModalComponent implements OnInit {
  open = false;
   constructor(
    private i18n: I18NextEagerPipe,
    private clipboard: Clipboard,
  ) {
  }
  ngOnInit() {
  }
  copyUrl(){
   const currentUrl = window.location.href;
   this.clipboard.copy(currentUrl);
   this.open = true;
  }
  dismiss() {}
  close() {}
}
