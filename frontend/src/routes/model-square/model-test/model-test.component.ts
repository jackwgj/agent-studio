import { CommonModule } from '@angular/common';
import { Component, ElementRef, ViewChild } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';

import { ActivatedRoute } from '@angular/router';
import {OnlineTestComponent} from "@routes/model-management/online-test/online-test.component";
import {SetSidebarVisibilityService} from "@shared/services/set-sidebar-visibility.service";
import {MODULES} from "@shared/modules";


@Component({
  selector: 'meta-model-test',
  standalone: true,
  imports: [
    CommonModule,
    OnlineTestComponent,
    MODULES
  ],
  templateUrl: './model-test.component.html',
  styleUrls: ['./model-test.component.scss'],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MODEL_ACCESS,I18nNamespace.JIUWEN_MODEL],
    },
  ],
})
export class ModelTestComponent {
  public isLoading = false;

  constructor(
    private route: ActivatedRoute,
    private i18n: I18NextEagerPipe,
    private sidebarVisibilityServ: SetSidebarVisibilityService,

  ) {}


  ngOnInit() {
    this.sidebarVisibilityServ.setSidebarsVisibilityByState('init');
  }


  onPageBack() {
    window.history.back();
  }


  ngOnDestroy(): void {
    this.sidebarVisibilityServ.setSidebarsVisibilityByState('destroy');
  }

}
