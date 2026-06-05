import {ChangeDetectionStrategy, Component} from '@angular/core';
import {MODULES} from "@shared/modules";
import {Router} from "@angular/router";
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';

@Component({
  selector: 'meta-apply-success',
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.AGENT,
        I18nNamespace.NODE,
        I18nNamespace.STATIC_APP_ORCHESTRATION,
        I18nNamespace.COMMON,
        I18nNamespace.PLATFORM_MANAGEMENT,
      ],
    },
  ],
  templateUrl: './apply-success.component.html',
  styleUrl: './apply-success.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplySuccessComponent {
  showFooter = true;

  scoreOptions: any = []

  constructor(
    private router: Router
  ) {
  }

  ngOnInit() {
    for (let i = 0; i < 11; i++) {
      this.scoreOptions.push({
        score: i,
        key: i
      })
    }
  }

  closeThis() {
    this.showFooter = false;
  }

  scoreThis(item) {
  }

  public trackByFn(index: number, item: any): number {
    return item.id;
  }

  goToModelArts() {
    this.router.navigate(['/home']);
  }

  gotoIndex() {
    this.router.navigate(['/subscription']);
  }
}
