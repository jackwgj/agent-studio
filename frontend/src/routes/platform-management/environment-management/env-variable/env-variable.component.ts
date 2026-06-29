import { Component, Input, OnInit, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { SharedModule } from '@shared/shared.module';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import { EnvironmentVariablesManagementService } from '@routes/platform-management/environment-variables-management/environment-variables-management.service';

@Component({
  selector: 'env-variable',
  templateUrl: './env-variable.component.html',
  styleUrls: ['./env-variable.component.less'],
  standalone: true,
  imports: [CommonModule, MODULES, SharedModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.PLATFORM_MANAGEMENT,
        I18nNamespace.COMMON,
        I18nNamespace.PROMPT_PLATFORM,
        I18nNamespace.AGENT_CENTER,
      ],
    },
  ],
})
export class EnvVariableComponent implements OnInit {
  @Output() upDateEnv = new EventEmitter<any>();
  @Input() envId: string;
  loading = false;
  displayedData: any[] = [];
  srcData: { data: any[]; state: undefined } = {
    data: [],
    state: undefined,
  };
  noDataTitle: string = this.i18n.transform('no_data');
  listColumns: any[] = [{ title: this.i18n.transform('variable_name') }, { title: this.i18n.transform('base_var_value') }];

  constructor(
    private envVariablesManagement: EnvironmentVariablesManagementService,
    private i18n: I18NextEagerPipe
  ) {}

  ngOnInit() {
    if (this.envId) {
      this.getEnvVariables();
    }
  }

  public trackByFn(index: number, item: any): number {
    return item.id;
  }

  public async getEnvVariables(): Promise<void> {
    try {
      this.loading = true;
      const result = await this.envVariablesManagement.getEnvVariablesDetail(this.envId);
      this.upDateEnv.emit(result);
      this.srcData.data = result.variables || [];
    } finally {
      this.loading = false;
    }
  }

  public changeUrl = cdnAssetUrl;
}
