import {ChangeDetectionStrategy, Component} from '@angular/core';
import {MODULES} from "@shared/modules";
import {I18NEXT_NAMESPACE} from "angular-i18next";
import {I18nNamespace} from "@i18n";
import {Router} from "@angular/router";
import {SendRequestService} from "@routes/subscription/send-request.service";
import * as angularI18next from 'angular-i18next';
import { CommonService } from '@services/common.service';

@Component({
  selector: 'meta-subscription',
  standalone: true,
  imports: [MODULES],
  templateUrl: './subscription.component.html',
  styleUrl: './subscription.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.PLATFORM_MANAGEMENT,
        I18nNamespace.COMMON,
        I18nNamespace.AGENT,
        I18nNamespace.NODE,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.STATIC_APP_ORCHESTRATION,
      ],
    },
  ],
})
export class SubscriptionComponent {

  cards: any[] = []

  constructor(
    private router: Router,
    private sendRequest:SendRequestService,
    private readonly i18n: angularI18next.I18NextEagerPipe,
    private commonService: CommonService
  ) {
  }


  ngOnInit() {}

  goToModelArts() {
    this.router.navigate(['/home']);
  }

  buyResource(item) {
    this.router.navigate(['/shopping'],{
      queryParams: {
        sku_code:item.skuCode,
        sku_type:item.skuType
      },
    });
  }
}
