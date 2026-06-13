import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { SharedModule } from '@shared/shared.module';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EnvManagementListComponent } from "@routes/platform-management/environment-management/env-management-list.component";
import { EnvironmentVariablesManagementListComponent } from "@routes/platform-management/environment-variables-management/environment-variables-management-list.component";
import { NzTabsModule } from 'ng-zorro-antd/tabs';

@Component({
  selector: 'environment-management-home',
  templateUrl: './environment-management-home.component.html',
  styleUrls: ['./environment-management-home.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MODULES,
    SharedModule,
    EnvManagementListComponent,
    EnvironmentVariablesManagementListComponent,
    NzTabsModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
      ],
    },
  ],
})
export class EnvManagementHomeComponent implements OnInit {
  selectedTabIndex = 0;

  @ViewChild(EnvManagementListComponent) envList!: EnvManagementListComponent;
  @ViewChild(EnvironmentVariablesManagementListComponent) envVariablesList!: EnvironmentVariablesManagementListComponent;

  constructor() {}
  ngOnInit() {}

  onTabChange(index: number) {
    this.selectedTabIndex = index;
    if (index === 0) {
      this.envList?.getEnvironmentListData();
    } else if (index === 1) {
      this.envVariablesList?.getListData();
    }
  }
}
