import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  ChangeDetectorRef,
  AfterViewInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { FormsModule } from '@angular/forms';
import { MODULES } from '@shared/modules';
import { SetSidebarVisibilityService } from '@shared/services/set-sidebar-visibility.service';
import { cdnAssetUrl } from '../../single-spa/assets-url';
import { LLMSelectComponent } from '@routes/agent-center/app-flow/components/llm-select/llm-select.component';
import { ModelManagementService } from '@services/repositories/model-management-new';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { AiAssistCreateHomeService } from '@routes/ai-assist-create-home/ai-assist-create-home.service';

@Component({
  selector: 'ai-assist-create-home',
  templateUrl: './ai-assist-create-home.component.html',
  styleUrls: ['./ai-assist-create-home.component.less'],
  standalone: true,
  imports: [CommonModule, FormsModule, MODULES, LLMSelectComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.COMMON,
        I18nNamespace.GUIDE,
        I18nNamespace.AGENT,
        I18nNamespace.TOOL,
      ],
    },
  ],
})
export class AiAssistCreateHomeComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('textElement') textElement!: ElementRef;

  private _modelSelected: string = '';

  get modelSelected(): string {
    return this._modelSelected;
  }

  set modelSelected(value: string) {
    if (this._modelSelected !== value) {
      this._modelSelected = value;
      this.cdr.markForCheck();
    }
  }

  agentCards = [
    { label: this.i18n.transform('my_script_assistant_app'), icon: 'assets/ng2workflow/shooting-assist.png', description: this.i18n.transform('my_script_assistant_app') },
    { label: this.i18n.transform('my_recipe_generator_app'), icon: 'assets/ng2workflow/food-combinations.png', description: this.i18n.transform('my_recipe_generator_app') },
    { label: this.i18n.transform('my_meeting_transcriber_app'), icon: 'assets/ng2workflow/meeting-assistant-app.svg', description: this.i18n.transform('my_meeting_transcriber_app') },
    { label: this.i18n.transform('my_reading_analyzer_app'), icon: 'assets/ng2workflow/read-assistant-app.svg', description: this.i18n.transform('my_reading_analyzer_app') },
  ];

  workflowCards = [
    { label: this.i18n.transform('my_resume_initial_screening'), icon: 'assets/ng2workflow/resume-rating.png', description: this.i18n.transform('nl2workflow-1') },
    { label: this.i18n.transform('my_weather_decision_workflow'), icon: 'assets/ng2workflow/weather-decision.png', description: this.i18n.transform('nl2workflow-2') },
    { label: this.i18n.transform('my_credit_risk_approval_workflow'), icon: 'assets/ng2workflow/automated-approval.svg', description: this.i18n.transform('nl2workflow-3') },
    { label: this.i18n.transform('my_ecommerce_sentiment_workflow'), icon: 'assets/ng2workflow/commerce-graded.svg', description: this.i18n.transform('nl2workflow-4') },
  ];

  displayCards = this.agentCards;
  typeOptions = [
    { label: this.i18n.transform('single_agent_app'), value: 'agent', icon: 'assets/ng2workflow/agent-app.svg' },
    { label: this.i18n.transform('workflow_app'), value: 'workflow', icon: 'assets/ng2workflow/workflow-app.svg' },
  ];
  typeValue = 'agent';

  promptValue: string = '';
  textAreaHeight: number = 30;
  readonly maxHeight: number = 200;
  appType = 'agent';

  constructor(
    public router: Router,
    private setSidebarVisibilityService: SetSidebarVisibilityService,
    private i18n: I18NextEagerPipe,
    private route: ActivatedRoute,
    private modelManagementService: ModelManagementService,
    private configServ: AgentConfigService,
    private aiAssistCreateHomeService: AiAssistCreateHomeService,
    private cdr: ChangeDetectorRef,
  ) {
    this.route.queryParams.subscribe((params) => {
      if (params.appType) {
        this.appType = params.appType;
        this.updateDisplayByAppType(this.appType);
      }
    });
  }

  ngOnInit(): void {
    this.setSidebarVisibilityService.setSidebarsVisibilityByState('init');
    this.getModel();
  }

  ngOnDestroy(): void {
    this.setSidebarVisibilityService.setSidebarsVisibilityByState('destroy');
  }

  ngAfterViewInit() {
    if (this.textElement) {
      setTimeout(() => {
        this.autoGrow();
      }, 0);
    }
  }

  private updateDisplayByAppType(type: string) {
    if (type === 'agent') {
      this.displayCards = this.agentCards;
      this.typeValue = 'agent';
    } else if (type === 'workflow') {
      this.displayCards = this.workflowCards;
      this.typeValue = 'workflow';
    }
  }

  showPlaceholder() {
    if (this.typeValue === 'workflow') {
      return this.i18n.transform('confirm_workflow_description');
    } else if (this.typeValue === 'agent') {
      return this.i18n.transform('confirm_agent_description');
    }
    return '';
  }

  autoGrow() {
    const textarea = this.textElement?.nativeElement;
    if (!textarea) {
      return;
    }

    textarea.style.height = 'auto';
    const newHeight = textarea.scrollHeight;

    if (newHeight > this.maxHeight) {
      textarea.style.height = this.maxHeight + 'px';
      textarea.style.overflowY = 'auto';
    } else {
      const finalHeight = Math.max(24, newHeight);
      textarea.style.height = `${finalHeight}px`;
      textarea.style.overflowY = 'hidden';
    }

    this.cdr.markForCheck();
  }

  ngModelChange(value: string) {
    this.appType = value;
    this.promptValue = '';
    this.updateDisplayByAppType(this.appType);

    // 更新 URL 中的 appType 参数
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { appType: this.appType },
      queryParamsHandling: 'merge'
    });
  }

  close() {
    this.router.navigate(['/home/overview']);
  }

  sendCardContent(card: any) {
    this.promptValue = card.description;
    setTimeout(() => {
      this.autoGrow();
    });
  }

  toCreateApp() {
    if (this.promptValue) {
      const targetPath = this.appType === 'agent'
        ? '/home/ai-assist-create-agent'
        : '/home/ai-assist-create-workflow';

      this.router.navigate([targetPath], {
        queryParams: {
          promptValue: this.promptValue,
          appType: this.appType,
        },
      });
    }
  }

  changeModelSelected(event: any) {
    this.aiAssistCreateHomeService.modelName = event.modelInfo.id;
    this.aiAssistCreateHomeService.authId = event.modelInfo.auth_id;
    this.aiAssistCreateHomeService.modelExplicitName = event.modelInfo.model_name;
    this.aiAssistCreateHomeService.modelType = event.modelInfo.model_type;
    this.aiAssistCreateHomeService.modelInterfaceProtocol = event.modelInfo.interface_protocol;
    this.modelSelected = event.id;
  }

  // 初始化模型
  getModel() {
    this.modelManagementService
      .getAvailableModelList({
        groupby: 'provider',
        publish_status: 'online',
        model_type: 'LLM',
      })
      .then((res: any) => {
        //  后端setting接口返回配置   nl2agent_recognition_model （模型ID）
        //  1. 如果用户模型列表中包含推荐的模型，则默认选择推荐模型
        //  2. 如果没有用户推荐模型，根据模型名称模糊匹配，优先推荐deepseek-v3
        //  3. 如果没有deepseek-v3，优先选择不支持思考模式的模型
        //  4. 最后是默认选择第一个
        const recommendedId = this.configServ.getConfigs()?.nl2agent_recognition_model;
        let modelObj = null;

        if (recommendedId) {
          modelObj = this.findModelById(recommendedId, res.data);
        }

        if (!modelObj) {
          modelObj = this.findModelByName('deepseek-v3', res.data) ||
            this.findFirstNonReasoningModel(res.data) ||
            res.data[0]?.models[0];
        }

        if (modelObj?.id) {
          this.aiAssistCreateHomeService.modelName = modelObj.id;
          this.aiAssistCreateHomeService.authId = modelObj.auth_id;
          this.aiAssistCreateHomeService.modelExplicitName = modelObj.model_name;
          this.aiAssistCreateHomeService.modelType = modelObj.model_type;
          this.aiAssistCreateHomeService.modelInterfaceProtocol = modelObj.interface_protocol;
          this.modelSelected = modelObj.id;
        }
      });
  }

  findModelById(inputId: string, data: any[]) {
    for (const provider of data) {
      for (const model of provider.models) {
        if (model.id === inputId) {
          return model;
        }
      }
    }
    return null;
  }

  findModelByName(modelName: string, providersArray: any[]) {
    // 先进行精确匹配
    for (const provider of providersArray) {
      if (provider.models && Array.isArray(provider.models)) {
        const model = provider.models.find(m => m.model_name?.toLowerCase() === modelName.toLowerCase());
        if (model) {
          return model;
        }
      }
    }

    // 精确匹配失败，进行模糊匹配
    for (const provider of providersArray) {
      if (provider.models && Array.isArray(provider.models)) {
        const model = provider.models.find(m => m.model_name?.toLowerCase().includes(modelName.toLowerCase()));
        if (model) {
          return model;
        }
      }
    }
    return null;
  }

  findFirstNonReasoningModel(data: any[]) {
    for (const provider of data) {
      for (const model of provider.models) {
        // 检查 is_reasoning 是否为 false
        if (model.is_reasoning === false) {
          return model;
        }
      }
    }
    return null;
  }

  protected readonly changeUrl = cdnAssetUrl;
}
