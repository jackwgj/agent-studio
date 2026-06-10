import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { COMMON_MODULES, MODULES } from '@shared/modules';
import { cdnAssetUrl } from '../../../single-spa/assets-url';
import { HeaderComponent } from '@routes/prompt/prompt-optimize-task-execution-status/header/header.component';
import { SetSidebarVisibilityService } from '@shared/services/set-sidebar-visibility.service';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'meta-prompt-optimize-task-execution-status',
  standalone: true,
  imports: [COMMON_MODULES, MODULES, HeaderComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PROMPT_PLATFORM],
    },
  ],
  templateUrl: './prompt-optimize-task-execution-status.component.html',
  styleUrl: './prompt-optimize-task-execution-status.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PromptOptimizeTaskExecutionStatusComponent {
  @Input() taskExecutionStatus: string = '';
  currentStatus: string;
  currentStep: number;
  steps: Array<{ label: string; description?: { text: string } }> = [
    {
      label: '准备和分析已有数据',
      description: {
        text: '准备当前版本的prompt、变量、回答等大模型数据，大模型根据评分和评分原因分析回答效果',
      },
    },
    {
      label: '优化生成prompt(1/10)',
      description: {
        text: '大模型优化生成新的 Prompt、根据测试数据生成新的回答并自动评分 ',
      },
    },
    {
      label: '查看优化结果',
      description: {
        text: '查看和对比大模型优化前后的 Prompt、回答和评分 ',
      },
    },
  ];

  public changeUrl = cdnAssetUrl;

  constructor(private sidebarVisibilityServ: SetSidebarVisibilityService) {}

  ngOnInit() {
    this.sidebarVisibilityServ.setSidebarsVisibilityByState('init');
    this.currentStatus='success';
  }

  ngOnDestroy(): void {
    this.sidebarVisibilityServ.setSidebarsVisibilityByState('destroy');
  }
}
