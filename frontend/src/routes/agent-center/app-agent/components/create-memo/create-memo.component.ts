import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { FormControl, FormGroup, NgForm, Validators } from "@angular/forms";
import { NoDupParamNameValidatorDirective } from '@shared/directives/common-validator.directive';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { cloneDeep, isEqual } from 'lodash';
import type { IMemoModalInput, IMemoModalUpdate, IMemoParam } from './types';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import * as angularI18next from 'angular-i18next';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { I18NextModule } from 'angular-i18next';
import { MODULES } from "@shared/modules";

@Component({
  selector: 'meta-create-memo',
  templateUrl: './create-memo.component.html',
  styleUrls: ['./create-memo.component.less'],
  standalone: true,
  imports: [
    MODULES,
    CommonModule,
    FormsModule,
    I18NextModule,
    NoDupParamNameValidatorDirective,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.NODE,
        I18nNamespace.AGENT_CENTER
      ],
    },
  ]
})
export class CreateMemoComponent implements OnInit, AfterViewInit {
  @Input('memoInput') memoInput: IMemoModalInput;

  @Input() isFlowReadonly;

  @ViewChild('inputForm') inputForm: NgForm;

  @Output('confirm') confirm = new EventEmitter<IMemoModalUpdate>();

  public loading = false;

  public memos: IMemoParam[] = [];

  memoTypes = [
    {
      label: this.i18n.transform('long_term'),
      value: 'user',
    },
    {
      label: this.i18n.transform('single_session'),
      value: 'conversation',
    },
  ];

  constructor(
    private agentRepoServ: AppAgentRepoService,
    private agentDataServe: AgentDataService,
    private readonly i18n: angularI18next.I18NextEagerPipe,
  ) {}

  ngOnInit() {
    this.memos = cloneDeep(this.memoInput.memos);

    if (!this.memos.length) {
      this.memos.push({
        name: '',
        default_value: '',
        life_cycle: 'user',
        description: '',
      });
    }
  }

  ngAfterViewInit(): void {
    if (this.memoInput?.selectedName) {
      const index = this.memoInput.memos.findIndex(
        (memo) => memo.name === this.memoInput.selectedName,
      );

      if (index !== -1) {
        (
          document.querySelector(`#memoName${index}id`) as HTMLInputElement
        )?.focus();
      }
    }
  }

  dismiss(): void {}

  close(): void {}

  async onComfirm() {
    if (isEqual(this.memos, this.memoInput.memos)) {
      this.confirm.emit({ isUpdated: false });
      return;
    }

    this.loading = true;
    const param = {
      memory_variables: this.memos,
    };

    try {
      const res = await this.agentRepoServ.triggerSave(
        this.memoInput.agentId,
        param,
      );
      this.agentDataServe.setAgentStatus(res?.status);
      this.confirm.emit({ memos: this.memos, isUpdated: true });
      this.close();
    } finally {
      this.loading = false;
    }
  }

  deleteMemo(index: number) {
    this.memos.splice(index, 1);
  }

  addMemo() {
    this.memos.push({
      name: '',
      default_value: '',
      life_cycle: 'user',
      description: '',
    });
  }

  getExistingValues(index: number) {
    const existingValues = this.memos.map((p) => p.name);
    existingValues.splice(index, 1);

    return existingValues;
  }
}
