import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';

@Component({
  selector: 'meta-footer',
  standalone: true,
  imports: [NzIconModule, MODULES],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.PROMPT_PLATFORM
      ],
    },
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FooterComponent {
  @Input() tip: string;
  @Input() activeStep: number;
  @Input() disabled:boolean= false;
  @Output() nextStepClick = new EventEmitter<void>();
  @Output() previousStepClick = new EventEmitter<void>();
  @Output() confirmClick = new EventEmitter<void>();
  constructor(    private i18n: I18NextEagerPipe,) {}

  public handleNextStepClick(): void {
    this.nextStepClick.emit();
  }
  public handlePreviousStepClick(): void {
    this.previousStepClick.emit();
  }
  public handleConfirmClick(): void {
    this.confirmClick.emit();
  }
}
