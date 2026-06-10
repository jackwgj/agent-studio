import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { Router } from '@angular/router';
import { I18nNamespace } from '@i18n';
import { StorageService } from '@shared/services/cfdata.service';
@Component({
  selector: 'meta-header',
  standalone: true,
  imports: [NzIconModule, MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.PROMPT_PLATFORM],
    },
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
})
export class HeaderComponent {
  @Input() taskStatus: string;
  @Input() progress?: number;
  @Output() retryTask = new EventEmitter<void>();
  @Output() stopTask = new EventEmitter<void>();
  @Output() continueTask = new EventEmitter<void>();
  @Output() completeTask = new EventEmitter<void>();

  constructor(private i18n: I18NextEagerPipe, private router: Router) {}

  onBack() {
    StorageService.setSessionStorage('activeTab','1');
    this.router.navigate(['/home/agent-center/library-home'], {
      queryParams: {
        tabId: 'prompt',
      }
    });
  }

  public retry() {
    this.retryTask.emit();
  }

  public stop() {
    this.stopTask.emit();
  }

  public continue() {
    this.continueTask.emit();
  }
  public complete() {
    this.completeTask.emit();
  }
}
