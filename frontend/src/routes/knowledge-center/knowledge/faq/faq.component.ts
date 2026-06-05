import { Component, Input, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { Network } from '@model';
import { KnowledgeFaq } from '@routes/knowledge-center/knowledge.types';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { BehaviorSubject, Subject, catchError, from, map, of, takeUntil } from 'rxjs';
import { FormateTimePipe } from 'src/pipes/formate-time.pipe';
import { BatchDeleteComponent } from '../batch-delete.component';
import { FormsModule } from '@angular/forms';
import { NzTableModule, NzTableQueryParams } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzModalService, NzModalModule } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzTypographyModule } from 'ng-zorro-antd/typography';

@Component({
  selector: 'knowledge-faq',
  templateUrl: './faq.component.html',
  standalone: true,
  imports: [
    MODULES,
    BatchDeleteComponent,
    FormsModule,
    NzTableModule,
    NzButtonModule,
    NzInputModule,
    NzIconModule,
    NzDrawerModule,
    NzFormModule,
    NzDescriptionsModule,
    NzDividerModule,
    NzModalModule,
    NzEmptyModule,
    NzTypographyModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.KNOWLEDGE, I18nNamespace.AGENT_CENTER],
    },
    FormateTimePipe,
    NzModalService,
    NzMessageService
  ],
})
export class FAQComponent implements OnInit, OnDestroy {
  @Input() kbId!: string;
  @Input() isMarket = false;

  @ViewChild('batchDeleteModal') batchDeleteModal: BatchDeleteComponent;

  tableData: any[] = [];
  loading = false;
  total = 0;
  pageIndex = 1;
  pageSize = 10;
  searchQuestion = '';
  emptyImage = `${Network.ASSETS_PATH}images/noData.svg`;
  setOfCheckedId = new Set<string>();
  allChecked = false;
  indeterminate = false;
  faqModalVisible = false;
  detailModalVisible = false;

  public form: FormGroup;
  public faqModalTitle = '';
  public temp = '';
  public createLoading = false;
  public selectedFaq: KnowledgeFaq | null = null;
  private destroy$ = new Subject<void>();
  private loadTrigger$ = new BehaviorSubject<void>(undefined);

  public get similarQuestions() {
    return this.form.get('similarQuestions') as FormArray;
  }

  public get controls(): FormControl[] {
    return this.similarQuestions.controls as FormControl[];
  }

  public get similarCount() {
    return this.similarQuestions.value.length;
  }

  constructor(
    private knowledgeRepoServ: KnowledgeRepoService,
    private nzModal: NzModalService,
    private nzMessage: NzMessageService,
    private fb: FormBuilder,
    private i18n: I18NextEagerPipe,
    public kbAbilitiesService: KbAbilitiesService,
    private formateTimePipe: FormateTimePipe
  ) {
    this.form = this.fb.group({
      question: ['', [Validators.required]],
      answer: ['', [Validators.required]],
      similarQuestions: this.fb.array([]),
    });
  }

  ngOnInit() {
    this.setupDataFetching();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupDataFetching() {
    this.loadTrigger$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.fetchData();
    });
  }

  private fetchData() {
    this.loading = true;
    const query = {
      offset: (this.pageIndex - 1) * this.pageSize,
      limit: this.pageSize,
      question: this.searchQuestion || '',
    };

    from(this.knowledgeRepoServ.listFaq(this.kbId, query)).pipe(
      map(res => {
        const { faq_list, count } = res || {};
        faq_list?.forEach((item: any) => {
          item.create_time = this.formateTimePipe.transform(item.create_time);
        });
        return {
          total: count || 0,
          data: faq_list || [],
        };
      }),
      catchError(() => of({ total: 0, data: [] }))
    ).subscribe(res => {
      this.tableData = res.data;
      this.total = res.total;
      this.loading = false;
      this.refreshCheckedStatus();
    });
  }

  public onQueryParamsChange(params: NzTableQueryParams) {
    this.pageIndex = params.pageIndex;
    this.pageSize = params.pageSize;
    this.loadTrigger$.next();
  }

  public onSearch() {
    this.pageIndex = 1;
    this.loadTrigger$.next();
  }

  public operationDisableFn(): boolean {
    return this.setOfCheckedId.size === 0 || this.kbAbilitiesService.isResourceDisabled;
  }

  public openCreateModal() {
    this.faqModalTitle = 'create_faq';
    this.similarQuestions.clear();
    this.form.reset({
      question: '',
      answer: '',
      similarQuestions: [],
    });
    this.faqModalVisible = true;
  }

  public openEditModal(data: KnowledgeFaq) {
    this.faqModalTitle = 'edit_faq';
    this.selectedFaq = data;
    const { question, answer } = data || {};
    this.form.patchValue({ question, answer });

    this.similarQuestions.clear();
    ['1', '2', '3', '4'].forEach(item => {
      const key = `question${item}`;
      const value = (data as any)?.[key];
      if (value) {
        this.similarQuestions.push(this.fb.control(value, [Validators.required]));
      }
    });
    this.faqModalVisible = true;
  }

  public openDetailModal(data: KnowledgeFaq) {
    this.selectedFaq = data;
    this.detailModalVisible = true;
  }

  public closeHalfmodal(modalName: 'faqModal' | 'detailModal'): void {
    if (modalName === 'faqModal') {
      this.faqModalVisible = false;
    } else {
      this.detailModalVisible = false;
    }
  }

  public addQuestion() {
    if (this.temp && this.temp.trim()) {
      this.similarQuestions.push(this.fb.control(this.temp, [Validators.required]));
      this.temp = '';
    }
  }

  public deleteQuestion(i: number) {
    this.similarQuestions.removeAt(i);
  }

  public async handleModalConfirm() {
    if (this.form.invalid) {
      Object.values(this.form.controls).forEach(control => {
        if (control instanceof FormArray) {
          control.controls.forEach(c => {
            c.markAsDirty();
            c.updateValueAndValidity({ onlySelf: true });
          });
        } else {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
      return;
    }

    try {
      this.createLoading = true;
      const { similarQuestions, ...formVal } = this.form.value;
      const { faq_id } = this.selectedFaq || {};

      similarQuestions.forEach((item: string, i: number) => {
        const key = `question${i + 1}`;
        (formVal as any)[key] = item;
      });

      if (this.faqModalTitle === 'edit_faq' && faq_id) {
        await this.knowledgeRepoServ.modifyFaq(this.kbId, faq_id, formVal);
      } else {
        await this.knowledgeRepoServ.createFaq(this.kbId, formVal);
      }

      this.closeHalfmodal('faqModal');
      this.loadTrigger$.next();
    } finally {
      this.createLoading = false;
    }
  }

  public onBatchDelete() {
    const selectedRows = this.tableData.filter(d => this.setOfCheckedId.has(d.faq_id));
    this.batchDeleteModal.openModal(
      [
        { headerName: 'ID', field: 'faq_id' },
        { headerName: this.i18n.transform('question'), field: 'question' }
      ],
      selectedRows,
      'faq_batch_delete_alert'
    );
  }

  public async handleBatchDelete(ctx: any) {
    try {
      const selectedRows = ctx.tableData || [];
      const faq_ids = selectedRows.map((r: any) => r.faq_id);
      const { total_count = 0, deleted_count = 0 } = (await this.knowledgeRepoServ.batchDeleteFaq(this.kbId, { faq_ids })) ?? {};

      this.nzMessage.success(
        this.i18n.transform('batch_delete_success', {
          totalCount: total_count,
          deletedCount: deleted_count,
        })
      );
      this.setOfCheckedId.clear();
      this.loadTrigger$.next();
      this.batchDeleteModal.closeModal();
    } finally {
      ctx.confirmLoading = false;
    }
  }

  public onDelete(data: KnowledgeFaq) {
    if (this.kbAbilitiesService.isResourceDisabled) return;

    this.nzModal.confirm({
      nzTitle: this.i18n.transform('faq_delete_title'),
      nzContent: this.i18n.transform('faq_delete_content', { id: data.faq_id }),
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOkText: this.i18n.transform('delete'),
      nzOnOk: async () => {
        try {
          await this.knowledgeRepoServ.deleteFaq(this.kbId, data.faq_id);
          this.nzMessage.success(this.i18n.transform('delete_success'));
          this.loadTrigger$.next();
          return true;
        } catch (error) {
          return false;
        }
      }
    });
  }

  onAllChecked(checked: boolean): void {
    this.tableData.forEach(({ faq_id }) => this.updateCheckedSet(faq_id, checked));
    this.refreshCheckedStatus();
  }

  onItemChecked(id: string, checked: boolean): void {
    this.updateCheckedSet(id, checked);
    this.refreshCheckedStatus();
  }

  private updateCheckedSet(id: string, checked: boolean): void {
    if (checked) {
      this.setOfCheckedId.add(id);
    } else {
      this.setOfCheckedId.delete(id);
    }
  }

  private refreshCheckedStatus(): void {
    this.allChecked = this.tableData.length > 0 && this.tableData.every(({ faq_id }) => this.setOfCheckedId.has(faq_id));
    this.indeterminate = this.tableData.some(({ faq_id }) => this.setOfCheckedId.has(faq_id)) && !this.allChecked;
  }
}
