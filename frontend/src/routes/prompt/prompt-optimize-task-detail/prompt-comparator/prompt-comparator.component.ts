import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  Output,
  SimpleChanges,
  ViewEncapsulation,
} from '@angular/core';
import { PromptEditorComponent } from '@routes/prompt/prompt-editor/prompt-editor.component';
import { MODULES } from '@shared/modules';
import { FormsModule } from '@angular/forms';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { Clipboard } from '@angular/cdk/clipboard';
import { MessageComponent } from '@shared/services/cfdata.service';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { VariableType } from '@interfaces/prompt/prompt-optimize-task.interface';

@Component({
  selector: 'meta-prompt-comparator',
  standalone: true,
  imports: [PromptEditorComponent, MODULES, FormsModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.PROMPT_PLATFORM],
    },
  ],
  templateUrl: './prompt-comparator.component.html',
  styleUrl: './prompt-comparator.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class PromptComparatorComponent {
  @Input() originPrompt: string = '';
  @Input() originAcc?: number;
  @Input() optimizedPrompt: Array<{
    iteration_round: number;
    optimized_prompt: string;
    success_rate: number;
    is_best: boolean;
  }> = [];
  @Input() highlight: boolean;
  @Input() variables: Array<{ type: VariableType; value: string }> = [];
  @Output() savePrompt = new EventEmitter<string>();
  @Output() showAssessDetail = new EventEmitter<string>();
  initVariables: Array<{ type: VariableType; value: string }> = [];
  readonly showDetailTip = this.i18n.transform(
    'evaluation_dataset_detail_desc',
  );

  constructor(
    private cdr: ChangeDetectorRef,
    private clipboard: Clipboard,
    private i18n: I18NextEagerPipe,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.optimizedPrompt) {
      this.optimizedPromptPageInfo.total =
        changes.optimizedPrompt.currentValue.length;

      // 从后往前遍历，当is_best为true时停下，初始显示最优提示词
      this.optimizedPromptPageInfo.current = this.optimizedPrompt.length;
      for (let i = this.optimizedPrompt.length - 1; i >= 0; i--) {
        if (this.optimizedPrompt[i].is_best) {
          this.optimizedPromptPageInfo.current = i + 1;
          break;
        }
      }

      this.comparePrompt =
        this.optimizedPrompt[this.optimizedPromptPageInfo.current - 1];
      this.cdr.markForCheck();
    }
    if (changes.variables) {
      this.initVariables = this.variables;
    }
  }

  public comparePrompt: {
    iteration_round: number;
    optimized_prompt: string;
    success_rate: number;
    is_best: boolean;
  };
  public optimizedPromptPageInfo: { total: number; current: number } = {
    total: 0,
    current: 0,
  };

  public handlePageChange(pageInfo: { currentPage: number }) {
    this.comparePrompt = this.optimizedPrompt[pageInfo.currentPage - 1];
  }

  public openSaveModal() {
    this.savePrompt.emit(this.comparePrompt.optimized_prompt);
  }

  public openAssessDetailModal(itemNum) {
    this.showAssessDetail.emit(itemNum);
  }

  public handleCopy() {
    // 复制到剪贴板
    if (this.clipboard.copy(this.comparePrompt.optimized_prompt)) {
      MessageComponent.showSuccess(this.i18n.transform('copied_to_clipboard'));
    } else {
      MessageComponent.showError(this.i18n.transform('base_copy_error'));
    }
  }

  protected readonly changeUrl = cdnAssetUrl;
}
