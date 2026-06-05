import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  I18NEXT_NAMESPACE,
  I18NextModule,
  I18NextEagerPipe,
} from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import {
  IRequestArgsView,
} from '@routes/agent-center/app-plugin/app-plugin.interface';
import {
  isSimpleType,
  pluginSchemaStr2Args,
} from '@routes/agent-center/app-plugin/utils';
import { AppPluginRepoService } from '@services/agent-center/app-plugin-repo.service';

export interface IPluginRefTipUpdatedValue {
  isSelect: boolean;
  pluginArgs?: IRequestArgsView[];
}

interface PluginTypeOption {
  text: string;
  id: string;
}

@Component({
  selector: 'plugin-ref-tip',
  template: `
    <div class="flex w-[380px] flex-col gap-[16px] p-[16px]">
      <div class="text-[14px] font-bold text-label">
        {{ 'reference_plugin' | i18nextEager }}
      </div>

      <div class="mt-[8px] flex flex-col gap-[4px]">
        <div>{{ 'plugin_type' | i18nextEager }}</div>
        <nz-radio-group [(ngModel)]="pluginTypeId" (ngModelChange)="onPluginTypeChange()">
          <label nz-radio-button [nzValue]="pluginTypes[0].id">
            {{ pluginTypes[0].text }}
          </label>
          <label nz-radio-button [nzValue]="pluginTypes[1].id">
            {{ pluginTypes[1].text }}
          </label>
        </nz-radio-group>
      </div>

      <div class="flex flex-col gap-[4px]">
        <div>{{ 'choose_plugins' | i18nextEager }}</div>
        <nz-select
          style="width: 100%"
          [(ngModel)]="selectedPluginId"
          [nzShowSearch]="true"
          [nzPlaceHolder]="'select_placeholder' | i18nextEager"
        >
          <nz-option
            *ngFor="let option of pluginOps"
            [nzLabel]="option.label"
            [nzValue]="option.value"
            [nzDisabled]="option.disabled"
          >
            <div class="flex items-center gap-[4px]">
              <span class="text-[14px] text-label">{{ option.label }}</span>
              <span nz-tooltip [nzTooltipTitle]="option.desc" class="text-[14px] text-desc">
                ({{ option.desc | slice:0:20 }})
              </span>
            </div>
          </nz-option>
        </nz-select>
      </div>

      <div class="flex justify-end gap-[8px]">
        <button nz-button (click)="handleCancel($event)">
          {{ 'cancel' | i18nextEager }}
        </button>
        <button nz-button nzType="primary" [disabled]="!selectedPluginId" (click)="handleConfirm($event)">
          {{ 'ok' | i18nextEager }}
        </button>
      </div>
    </div>
  `,
  styles: [``],
  standalone: true,
  imports: [
    NzButtonModule,
    NzRadioModule,
    NzSelectModule,
    NzToolTipModule,
    CommonModule,
    FormsModule,
    I18NextModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class PluginRefTipComponent implements OnInit, OnDestroy {
  @Output() updated = new EventEmitter<IPluginRefTipUpdatedValue>();

  pluginTypes: PluginTypeOption[] = [
    {
      text: '',
      id: 'inner',
    },
    {
      text: '',
      id: 'custom',
    },
  ];

  pluginTypeId: string = 'inner';

  pluginOps: any[] = [];

  selectedPluginId = '';

  plugins = [];

  private isLoading = false;

  constructor(
    public i18n: I18NextEagerPipe,
    private pluginRepoServe: AppPluginRepoService,
  ) {
    this.pluginTypes[0].text = this.i18n.transform('pre_installed_plugins');
    this.pluginTypes[1].text = this.i18n.transform('personal_plugin');
  }

  ngOnInit(): void {
    this.loadPlugins();
  }

  ngOnDestroy(): void {
    this.updated.emit({ isSelect: false });
  }

  onPluginTypeChange(): void {
    this.selectedPluginId = '';
    this.loadPlugins();
  }

  async loadPlugins(): Promise<void> {
    this.isLoading = true;
    const params = {
      offset: 0,
      limit: 1000,
      type: this.pluginTypeId,
    };

    try {
      const data = await this.pluginRepoServe.getPluginList(params);
      this.plugins = data?.plugin_list ?? [];
      const pluginOps: any[] = [];
      for (let plugin of this.plugins) {
        for (let tool of plugin?.request_info?.tool_info) {
          pluginOps.push({
            label: plugin.plugin_display_name + ':' + tool.tool_display_name,
            desc: tool.tool_desc,
            value: plugin.plugin_id + '#' + tool.tool_id,
          });
        }
      }
      this.pluginOps = pluginOps;
    } catch (error) {
    } finally {
      this.isLoading = false;
    }
  }

  handleCancel(e: Event): void {
    e.stopPropagation();
    this.onCancel();
  }

  handleConfirm(e: Event): void {
    e.stopPropagation();
    this.onConfirm();
  }

  onCancel() {
    this.updated.emit({
      isSelect: false,
    });
  }

  onConfirm() {
    const [plugin_id, tool_id] = this.selectedPluginId.split('#');
    const plugin = this.plugins.find(
      (p) => p.plugin_id === plugin_id,
    );
    const tool_input_schema = plugin?.input_schema.find(
      (p) => p.tool_id === tool_id,
    );
    this.updated.emit({
      isSelect: true,
      pluginArgs: pluginSchemaStr2Args(tool_input_schema?.input_schema).filter(
        (arg) =>
          isSimpleType(arg.type) &&
          arg.name !== 'USER_RESPONSE' &&
          arg.name !== 'STATUS',
      ),
    });
  }
}
