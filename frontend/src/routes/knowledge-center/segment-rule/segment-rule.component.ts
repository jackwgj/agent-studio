import { moveItemInArray, DragDropModule } from '@angular/cdk/drag-drop';
import { Component, OnInit, TemplateRef, forwardRef, Input, ViewChild } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  NG_VALUE_ACCESSOR,
  ControlValueAccessor,
  Validators
} from '@angular/forms';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { MODULES } from '@shared/modules';
import { SegmentRule } from '../knowledge.types';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { CommonModule } from '@angular/common';

import { NzModalModule, NzModalService, NzModalRef } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'knowledge-segment-rule',
  templateUrl: './segment-rule.component.html',
  styleUrls: ['./segment-rule.component.less'],
  imports: [
    MODULES,
    DragDropModule,
    CommonModule,
    NzModalModule,
    NzButtonModule,
    NzIconModule,
    NzInputModule,
    NzToolTipModule
  ],
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SegmentRuleComponent),
      multi: true,
    },
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.COMMON,
        I18nNamespace.AGENT_CENTER,
      ],
    },
  ],
})
export class SegmentRuleComponent implements OnInit, ControlValueAccessor {
  @Input() kbId: string;

  @ViewChild('ruleModalContent', { static: true }) ruleModalContent!: TemplateRef<any>;
  @ViewChild('ruleModalFooter', { static: true }) ruleModalFooter!: TemplateRef<any>;

  public value = '';

  public segmentRuleList: Array<SegmentRule> = [];

  public get showAdd() {
    return 10 > this.segmentRuleList.length;
  }

  public modalTitle = '';

  public form!: FormGroup;

  public get rule_regexs(): FormControl[] {
    return (this.form.get('rule_regexs') as FormArray).controls as FormControl[];
  }

  public saveLoading = false;

  private modalRef?: NzModalRef;
  private editingRule?: SegmentRule;

  constructor(
    private nzModal: NzModalService,
    private nzMessage: NzMessageService,
    private kbRepoServ: KnowledgeRepoService,
    private fb: FormBuilder,
    private i18n: I18NextEagerPipe,
  ) {}

  ngOnInit(): void {
    this.listSegmentRule();
  }

  private onChange: (value: string) => void = () => {};

  private onTouched: () => void = () => {};

  writeValue(val: string): void {
    if (val !== undefined && val !== null) {
      this.value = val;
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  public emit(value: string) {
    this.value = value;
    this.onChange(value);
    this.onTouched();
  }

  public async listSegmentRule() {
    try {
      const data = await this.kbRepoServ.listSegmentRule(this.kbId ? {
        repo_id: this.kbId,
      } : null);
      this.segmentRuleList = data.rule_list;
      this.value || this.emit(data.rule_list?.[0]?.id || '');
    } finally {
    }
  }

  public drop(e: any) {
    const formArray = this.form.get('rule_regexs') as FormArray;
    moveItemInArray(formArray.controls, e.previousIndex, e.currentIndex);
    formArray.updateValueAndValidity();
  }

  public handleRuleSelect(rule: SegmentRule) {
    this.emit(rule.id);
  }

  public openRuleModal(e: Event, rule?: SegmentRule) {
    e.stopPropagation();
    this.editingRule = rule;
    this.modalTitle = rule ? 'edit_segment_rule' : 'add_segment_rule';
    const list = rule?.rule_regexs || [];
    this.form = this.fb.group({
      rule_regexs: this.fb.array(
        list.map((item) =>
          this.fb.control(item, [
            Validators.required,
            Validators.maxLength(200),
          ]),
        ),
      ),
    });

    this.modalRef = this.nzModal.create({
      nzTitle: this.i18n.transform(this.modalTitle),
      nzContent: this.ruleModalContent,
      nzFooter: this.ruleModalFooter,
      nzMaskClosable: false,
      nzWidth: 520
    });
  }

  public handleInputChange() {
    const formArray = this.form.get('rule_regexs') as FormArray;
    formArray.push(
      this.fb.control('', [
        Validators.required,
        Validators.maxLength(200),
      ]),
    );
  }

  public handleRuleDelete(i: number) {
    const formArray = this.form.get('rule_regexs') as FormArray;
    formArray.removeAt(i);
  }

  public async handleRuleSave() {
    try {
      this.saveLoading = true;
      const rule = this.editingRule;
      const rule_regexs = this.form.get('rule_regexs').value as string[];
      const params = {
        rule_regexs: rule_regexs.filter((item) => item),
        ...(this.kbId ? { repo_id: this.kbId } : {}),
      };
      !rule
        ? await this.kbRepoServ.createSegmentRule(params)
        : await this.kbRepoServ.modifySegmentRule(rule.id, params);
      await this.listSegmentRule();
      this.closeModal();
    } finally {
      this.saveLoading = false;
    }
  }

  public closeModal() {
    this.modalRef?.close();
  }

  public async deleteSegmentRule(e: Event, rule: SegmentRule) {
    e.stopPropagation();
    const { id } = rule;
    this.nzModal.confirm({
      nzIconType: 'exclamation-circle',
      nzTitle: this.i18n.transform('delete'),
      nzContent: this.i18n.transform('segment_rule_delete_content'),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOkDanger: true,
      nzOnOk: async () => {
        try {
          await this.kbRepoServ.deleteSegmentRule(id);
          this.nzMessage.success(this.i18n.transform('delete_success'));
          await this.listSegmentRule();
          if (this.value !== id) {
            return true;
          }
          this.emit(this.segmentRuleList[0].id);
          return true;
        } catch (error) {
          return false;
        }
      },
    });
  }
}
