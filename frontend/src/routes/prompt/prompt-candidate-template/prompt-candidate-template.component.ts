import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from '@angular/core';
import { MODULES } from '@shared/modules';
import { SubpageHeaderComponent } from '@shared/components/subpage-header/subpage-header.component';
import { SingleTemplateComponent } from './single-template/single-template.component';
import { I18nNamespace } from '@i18n';
import * as angularI18next from 'angular-i18next';
import { CandidateTemplateListService } from '@services/candidate-template-list.service';
import { RouteParamsService } from '@services/routeParams.service';
import { ActivatedRoute, Router } from '@angular/router';
import { PromptWritingRepoService } from '@services/repositories/prompt-writing-repo.service';
import { TaskService } from '@services/task.service';

interface SearchboxItem {
  groupKey: string;
  label: string;
  field: string;
  options: any[];
}

@Component({
  selector: 'meta-prompt-candidate-template',
  templateUrl: './prompt-candidate-template.component.html',
  styleUrls: ['./prompt-candidate-template.component.scss'],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MODULES, SubpageHeaderComponent, SingleTemplateComponent],
  providers: [
    {
      provide: angularI18next.I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PROMPT_PLATFORM],
    },
  ],
})
export class PromptCandidateTemplateComponent implements OnInit {
  public selectAllData: Array<any> = [];

  public checked: any = [];

  public selectValue = '';

  public options: Array<any> = [
    {
      label: this.i18NextEagerPipe.transform('base_manual_score'),
      id: 'score',
    },
    {
      label: this.i18NextEagerPipe.transform('create_time'),
      id: 'time',
    },
  ];

  public diffSortRule: boolean = false;

  public searchValue = '';

  public searchboxItems: Array<SearchboxItem> = [
    {
      groupKey: this.i18NextEagerPipe.transform('base_filter_criteria'),
      label: this.i18NextEagerPipe.transform('candidate_template_templateName'),
      field: 'tmplName',
      options: [],
    },
    {
      groupKey: this.i18NextEagerPipe.transform('base_filter_criteria'),
      label: this.i18NextEagerPipe.transform('candidate_tmpl_text'),
      field: 'tmplContent',
      options: [],
    },
  ];

  public defaultField: string = 'tmplName';

  public emptyPlaceholder: string = this.i18NextEagerPipe.transform(
    'candidate_tmpl_input_placeholder',
  );

  public tags: string[] = [];

  public task_id: string = '';

  public pageTitle: string = '';
  public tmplCount: number;

  public noDataInfo: string = this.i18NextEagerPipe.transform('base_no_data');
  public loading: boolean = false;
  public isShowAlter: boolean = false;
  public createEvaluationState: boolean = true;

  public promptTmplIds: string[] = [];

  private searchName: string;
  private searchContent: string;
  private evaluationTaskId: string;

  constructor(
    private candidateTemplateListServe: CandidateTemplateListService,
    private router: Router,
    private routeParamsServe: RouteParamsService,
    private readonly i18NextEagerPipe: angularI18next.I18NextEagerPipe,
    private route: ActivatedRoute,
    private promptWritingRepoServ: PromptWritingRepoService,
    private taskServ: TaskService,
    private cdr: ChangeDetectorRef,
  ) {
    this.route.queryParams.subscribe((params) => {
      this.task_id = params.taskId;
    });
  }

  ngOnInit() {
    this.getCandidateTmplList();
    this.getLabelsByTaskId();
    this.queryEvaluationTaskState();
  }

  public onNgModelChange(values: string[]): void {
    this.searchName = '';
    this.searchContent = '';
    if (!values || !values.length) {
      this.getCandidateTmplList();
      return;
    }
    values.forEach((field: string) => {
      const item = this.searchboxItems.find(s => s.field === field);
      if (item) {
        if (field === 'tmplName') {
          this.searchName = this.tags.find((t: any) => typeof t === 'object' && t.field === 'tmplName')?.value || '';
        } else {
          this.searchContent = this.tags.find((t: any) => typeof t === 'object' && t.field === 'tmplContent')?.value || '';
        }
      }
    });
    this.getCandidateTmplList();
  }

  public handleSortChange(event: any) {
    if (event.id === 'time') {
      this.diffSortRule = false;
    } else {
      this.diffSortRule = true;
    }
    this.getCandidateTmplList();
  }

  public getCandidateTmplList() {
    const params = {
      offset: 1,
      limit: 10,
      task_id: this.task_id,
      name: this.searchName,
      content: this.searchContent,
      source: '',
      model: '',
      creator: '',
      query_self: true,
      is_good_rating: this.diffSortRule,
      type: 1,
    };

    this.loading = true;
    this.candidateTemplateListServe.getCandidateTmplList(params).subscribe({
      next: (res) => {
        this.loading = false;
        const { data, count } = res;
        this.selectAllData = data;
        this.tmplCount = count;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  public toHorizontalComparison(): void {
    const idHorizontalArr = this.checked.map((item) => item.id).join(';');
    this.routeParamsServe.setHorizontalTmpl(idHorizontalArr);
    this.router.navigate(['/home/prompt/prompt-writing'], {
      queryParams: {
        taskId: this.task_id || '',
        activeMenuItemId: 'comparison',
      },
    });
  }

  public refreshAllData() {
    this.getCandidateTmplList();
  }

  public toCreateEvaluation(): void {
    const idTestCases = this.checked.map((item) => item.id).join(';');
    this.router.navigate([`/home/prompt/create-evaluation`], {
      queryParams: {
        taskId: this.task_id || '',
        candidateId: idTestCases || '',
      },
    });
  }

  public toNewPromptWriting() {
    this.router.navigate(['/home/prompt/prompt-writing'], {
      queryParams: {
        taskId: this.task_id || '',
      },
    });
  }

  public toEvaluationRecord() {
    this.router.navigate(['/home/prompt/prompt-writing'], {
      queryParams: {
        taskId: this.task_id || '',
        activeMenuItemId: 'evaluation',
      },
    });
  }

  public getLabelsByTaskId() {
    this.promptWritingRepoServ
      .getTaskInfo(this.task_id)
      .subscribe((taskInfo) => {
        const { name, id, tag_list } = taskInfo;
        this.pageTitle = name;
        this.cdr.markForCheck();
        this.taskServ.setTask({
          name,
          id,
          tags: tag_list,
        });
      });
  }

  public onClickBack(): void {
    window.history.back();
  }

  public queryEvaluationTaskState() {
    this.candidateTemplateListServe
      .queryEvaluationState(this.task_id)
      .subscribe({
        next: (res) => {
          if (res) {
            const { id, status, prompt_list } = res;
            const statusList = ['running', 'pending', 'retrying', 'pausing'];
            if (statusList.includes(status)) {
              this.isShowAlter = true;
              this.createEvaluationState = true;
              if (prompt_list.length) {
                this.promptTmplIds = prompt_list.map((item) => item.id);
              }
              this.evaluationTaskId = id;
              this.cdr.markForCheck();
            } else {
              this.isShowAlter = false;
              this.createEvaluationState = false;
              this.cdr.markForCheck();
            }
          } else {
            this.isShowAlter = false;
            this.createEvaluationState = false;
            this.cdr.markForCheck();
          }
        },
      });
  }

  public toEvaluationProgress() {
    this.router.navigateByUrl(
      `/home/prompt/evaluation-result?evaluationId=${this.evaluationTaskId}`,
    );
  }

  public onOpenChange(openState: boolean): void {
    this.isShowAlter = openState;
  }
}
