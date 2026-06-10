import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { ActivatedRoute, Router} from '@angular/router';
import { I18nNamespace } from '@i18n';
import { PipesModule } from '../../../../pipes/pipes.module';
import { StorageService } from '@shared/services/cfdata.service';

@Component({
  selector: 'meta-header',
  standalone: true,
  imports: [NzIconModule, MODULES, PipesModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.PROMPT_PLATFORM
      ],
    },
  ],
})
export class HeaderComponent {
  @Input() activeStep: number;
  @Input() saveTime: string;
  @Output() onStepChange = new EventEmitter<number>();
  steps: Array<{ label: string; description?: { text: string } }> = [
    {
      label: this.i18n.transform('fill_info_and_use_case_set'),
    },
    {
      label: this.i18n.transform('config_optimization_strategy'),
    },
  ];

  constructor(
    private i18n: I18NextEagerPipe,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  handleStepChange(activeStep: number): void {
    this.onStepChange.emit(activeStep);
  }

  onBack() {
    this.route.queryParams.subscribe((queryParams) => {
      StorageService.setSessionStorage('activeTab','1');
      this.router.navigate(['/home/agent-center/library-home'], {
        state: {
          activeTab: 1,
        },
        queryParams: {
          tabId: 'prompt',
        }
      });
    });
  }
}
