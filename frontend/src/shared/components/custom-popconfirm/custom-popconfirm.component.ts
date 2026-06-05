import { OverlayModule } from '@angular/cdk/overlay';
import {
  Component,
  ChangeDetectionStrategy,
  Input,
  Output,
  EventEmitter,
} from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import * as I18next from 'angular-i18next';
import { FormControl, FormGroup } from '@angular/forms';
import { agentCommonLogic } from '@routes/agent-center/app-agent/common-logic-agent';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { ActivatedRoute } from '@angular/router';
import { ConsoleFrameworkService } from '@core/services';

@Component({
  selector: 'custom-popconfirm',
  templateUrl: './custom-popconfirm.component.html',
  styleUrls: ['./custom-popconfirm.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [MODULES, OverlayModule],
  providers: [
    {
      provide: I18next.I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
    agentCommonLogic,
  ],
})
export class CustomPopconfirmComponent {
  @Input() type: string = '';

  @Output() cancel = new EventEmitter<void>();

  @Output() submit = new EventEmitter<any>();

  @Output() close = new EventEmitter<void>();

  public groupFormControl: FormGroup;

  public dislikeItems: any = this.commonData.feedbackOptions.map(item => ({ label: item.label, value: item.id }));

  public suggestions = '';

  public hasChecked = false;
  public submitClicked = false;

  private agentId = '';

  public language =
    this.consoleFrameworkService?.dataService?.getLocale() ?? 'zh-cn';

  constructor(
    private commonData: agentCommonLogic,
    private agentRepoServe: AppAgentRepoService,
    private route: ActivatedRoute,
    private consoleFrameworkService: ConsoleFrameworkService,
  ) {
    this.route.queryParams.subscribe((params) => {
      this.agentId = params.agentId;
    });

    this.groupFormControl = new FormGroup({
      checkboxGroup: new FormControl(undefined, []),
    });
    this.groupFormControl.valueChanges.subscribe((val) => {
      this.hasChecked = val?.checkboxGroup?.length > 0;
    });
  }

  public onCancel(): void {
    this.cancel.emit();
  }

  public onSubmit(): void {
    this.submitClicked = true;
    const { checkboxGroup } = this.groupFormControl.value;
    const fbParams = {
      selected: checkboxGroup,
      suggestions: this.suggestions,
    };
    this.submit.emit(fbParams);
  }
}
