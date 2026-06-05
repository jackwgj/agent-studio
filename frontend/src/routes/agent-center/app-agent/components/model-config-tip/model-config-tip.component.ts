import {
  Component,
  OnInit,
  Input,
  SimpleChanges,
  ChangeDetectorRef,
  OnDestroy,
} from '@angular/core';
import { MODULES } from '@shared/modules';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzSliderModule } from 'ng-zorro-antd/slider';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { ActivatedRoute } from '@angular/router';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { agentCommonLogic, modeSelectTipText } from '../../common-logic-agent';
import { ContextService } from '@services/context.service';
import { IUpdateSingleAgentConfig } from '@routes/agent-center/types/my-workspace.types';
import { LLMSelectComponent } from '@routes/agent-center/app-flow/components/llm-select/llm-select.component';
import { ModelConfigTipService } from '@routes/agent-center/app-agent/components/model-config-tip/model-config-tip.service';
import { Subject, Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'model-config-tip',
  templateUrl: './model-config-tip.component.html',
  styleUrls: ['./model-config-tip.component.scss'],
  imports: [MODULES, NzFormModule, NzRadioModule, NzSliderModule, NzInputNumberModule, NzSwitchModule, NzAlertModule, LLMSelectComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT,I18nNamespace.MODEL_ACCESS],
    },
  ],
})
export class ModelConfigTipComponent implements OnInit, OnDestroy {
  @Input() editData;
  @Input() availableModels = [];
  @Input() arrowStyle = {};
  @Input() modelConfig: any;
  @Input() isShowLLMSelect = true;
  private _isVisible = false;

  @Input()
  set isVisible(value: boolean) {
    this._isVisible = value;
    if (value) {
      // 组件被显示出来
      this.onShow();
    }
  }

  get isVisible(): boolean {
    return this._isVisible;
  }

  private onShow(): void {
    if (!this.modelConfigTipService.isShowAdvancedConfig) {

    }
  }

  public availableModelList = [];

  public agentId = '';

  public modelSelected: any;
  public modelSelectedInfo: any;

  planModelSelected: any;
  planModelSelectedObj: any;

  // 模式选择
  public modes: any = [
    {
      value: 'exact',
      text: this.i18n.transform('accurate'),
    },
    {
      value: 'balance',
      text: this.i18n.transform('balanced'),
    },
    {
      value: 'creative',
      text: this.i18n.transform('creative'),
    },
    {
      value: 'custom',
      text: this.i18n.transform('custom'),
    },
  ];

  public mode: any = this.modes[0];

  public plan_modes: any = [
    {
      value: 'exact',
      text: this.i18n.transform('accurate'),
    },
    {
      value: 'balance',
      text: this.i18n.transform('balanced'),
    },
    {
      value: 'creative',
      text: this.i18n.transform('creative'),
    },
    {
      value: 'custom',
      text: this.i18n.transform('custom'),
    },
  ];

  public plan_mode: any = this.plan_modes[0];

  public outputModes: any = [
    {
      value: 'text',
      label: this.i18n.transform('text'),
    },
    {
      value: 'markdown',
      label: 'Markdown',
    },
  ];

  public modesTemperatureAndTop: any = [
    {
      temperature: 0.1,
      top_p: 0.7,
    },
    {
      temperature: 1,
      top_p: 0.7,
    },
    {
      temperature: 1,
      top_p: 0.8,
    },
    {
      temperature: 0,
      top_p: 1,
    },
  ];

  public outputMode: any = this.outputModes[0].value;

  public plan_outputMode: any = this.outputModes[0].value;

  public temperature = this.modesTemperatureAndTop[0].temperature;

  public plan_temperature = this.modesTemperatureAndTop[0].temperature;

  public top = this.modesTemperatureAndTop[0].top_p;

  public plan_top = this.modesTemperatureAndTop[0].top_p;

  public rounds = 3;

  public plan_rounds = 3;

  public maxReplyLength = null;

  public plan_maxReplyLength = null;

  public frequencyPenalty = 0;

  public enable_thinking = true;

  public scales = [0, 1];

  public roundScales = [0, 100];
  public maxReplScales = [1, 131072];

  public modeSelectTipText = modeSelectTipText;

  public temperatureDescTip = this.i18n.transform('model_temperature_desc');

  public topPDescTipText = this.i18n.transform('model_top_p_desc');

  public maxReplyLengthDescTip = this.i18n.transform('model_max_tokens_desc');

  public roundsDescTip = this.i18n.transform('roundsDescTip');

  public modelFrequencyPenaltyDescTip = this.i18n.transform(
    'model_frequency_penalty_desc',
  );

  public isF1 = this.ctxServ.isF1ReqList();


  public maxReplySetting = {
    max: 131072,
    min: 1,
  };

  // 防抖相关属性
  private saveConfigSubject = new Subject<void>();
  private saveConfigSubscription: Subscription;
  private readonly saveDebounceTime = 500; // 500毫秒防抖

  constructor(
    private agentDataServe: AgentDataService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private ctxServ: ContextService,
    private i18n: I18NextEagerPipe,
    private commonLogic: agentCommonLogic,
    public modelConfigTipService: ModelConfigTipService,
  ) {
    this.route.queryParams.subscribe((params) => {
      this.agentId = params.agentId;
    });

    // 设置防抖订阅
    this.saveConfigSubscription = this.saveConfigSubject
      .pipe(debounceTime(this.saveDebounceTime))
      .subscribe(() => {
        this.saveConfig();
      });
  }

  ngOnInit() {}

  ngOnDestroy() {
    // 清理订阅
    if (this.saveConfigSubscription) {
      this.saveConfigSubscription.unsubscribe();
    }
    this.saveConfigSubject.complete();
  }

  getmodelStatus(is_connecting) {
    if (is_connecting) {
      return this.i18n.transform('running');
    }

    return this.i18n.transform('call_failed');
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.availableModels) {
      this.availableModelList = changes.availableModels.currentValue.map(
        (item: any) => ({
          ...item,
          merge_name: item.model_name,
        }),
      );
    }
    if (changes.modelConfig) {
      const {
        model_deployment_id,
        temperature,
        top_p,
        isUpdated,
        history_size,
        max_tokens,
        output_format,
        frequency_penalty,
        enable_thinking,
        plan_model_deployment_id,
        plan_temperature,
        plan_top_p,
        plan_history_size,
        plan_max_tokens,
        plan_output_format,
      } = changes.modelConfig.currentValue;

      // 设置模型选择
      this.modelSelected = model_deployment_id;
      this.planModelSelected = plan_model_deployment_id;

      // 根据 useModelMode 设置对应的配置值
      if (this.modelConfigTipService.useModelMode === 'one' || this.modelConfigTipService.useModelMode === 'question') {
        // 合一或问答模型使用主配置
        const modelConfigSelected = this.availableModelList.find(
          (item) => item.model_deployment_id === this.modelSelected,
        );
        this.modelSelectedInfo = modelConfigSelected;
        this.maxReplyLength = max_tokens;
        this.setMaxReplySetting(modelConfigSelected);
        this.temperature = temperature;
        this.top = top_p;
        this.isCustomMode();
        this.rounds = history_size;
        this.frequencyPenalty = frequency_penalty || 0;
        this.enable_thinking = enable_thinking ?? true;
        this.outputMode = output_format;
      }

      if (this.modelConfigTipService.useModelMode === 'plan') {
        // 规划模型使用规划配置
        const planModelConfigSelected = this.availableModelList.find(
          (item) => item.model_deployment_id === this.planModelSelected,
        );
        this.planModelSelectedObj = planModelConfigSelected;
        this.plan_maxReplyLength = plan_max_tokens || max_tokens;
        this.planSetMaxReplySetting(planModelConfigSelected);
        this.plan_temperature = plan_temperature || temperature;
        this.plan_top = plan_top_p || top_p;
        this.planIsCustomMode();
        this.plan_rounds = plan_history_size || history_size;
        this.plan_outputMode = plan_output_format || output_format;
      }

      this.initAndSubscribe(isUpdated);
      this.cdr.markForCheck();
    }
  }

  private initializeConfigForMode() {
    // 根据当前模式初始化配置
    if (this.modelConfig) {
      const {
        model_deployment_id,
        temperature,
        top_p,
        history_size,
        max_tokens,
        output_format,
        frequency_penalty,
        enable_thinking,
        plan_model_deployment_id,
        plan_temperature,
        plan_top_p,
        plan_history_size,
        plan_max_tokens,
        plan_output_format,
      } = this.modelConfig;

      switch (this.modelConfigTipService.useModelMode) {
        case 'one':
        case 'question': {
          // 使用大括号创建块级作用域
          const modelConfigSelected = this.availableModelList.find(
            (item) => item.model_deployment_id === model_deployment_id,
          );
          this.modelSelectedInfo = modelConfigSelected;
          this.maxReplyLength = max_tokens;
          this.setMaxReplySetting(modelConfigSelected);
          this.temperature = temperature;
          this.top = top_p;
          this.isCustomMode();
          this.rounds = history_size;
          this.frequencyPenalty = frequency_penalty || 0;
          this.enable_thinking = enable_thinking ?? true;
          this.outputMode = output_format;
          break;
        }

        case 'plan': {
          // 同样为这个 case 添加大括号
          const planModelConfigSelected = this.availableModelList.find(
            (item) => item.model_deployment_id === plan_model_deployment_id,
          );
          this.planModelSelectedObj = planModelConfigSelected;
          this.plan_maxReplyLength = plan_max_tokens || max_tokens;
          this.planSetMaxReplySetting(planModelConfigSelected);
          this.plan_temperature = plan_temperature || temperature;
          this.plan_top = plan_top_p || top_p;
          this.planIsCustomMode();
          this.plan_rounds = plan_history_size || history_size;
          this.plan_outputMode = plan_output_format || output_format;
          break;
        }

        default:
          break;
      }

    }
  }

  public changeModelSelected(item) {
    this.modelSelected = item.id;
    const modelConfigSelected = this.availableModelList.find(
      (item) => item.model_deployment_id === this.modelSelected,
    );
    this.modelSelectedInfo = modelConfigSelected;
    this.setMaxReplySetting(modelConfigSelected);
    this.triggerSave();
  }

  public planChangeModelSelected(item) {
    this.planModelSelectedObj = item.modelInfo;
    this.planModelSelected = item.id;
    const modelConfigSelected = this.availableModelList.find(
      (item) => item.model_deployment_id === this.planModelSelected,
    );
    this.planSetMaxReplySetting(modelConfigSelected);
    this.triggerSave();
  }

  public changeMode(event: any) {
    this.mode = event;
    const modeMapping: any = {
      exact: this.modesTemperatureAndTop[0],
      balance: this.modesTemperatureAndTop[1],
      creative: this.modesTemperatureAndTop[2],
      custom: this.modesTemperatureAndTop[3],
    };

    if (modeMapping[event.value]) {
      this.temperature = modeMapping[event.value].temperature;
      this.top = modeMapping[event.value].top_p;
    }
    this.triggerSave();
  }

  public planChangeMode(event: any) {
    this.plan_mode = event;
    const modeMapping: any = {
      exact: this.modesTemperatureAndTop[0],
      balance: this.modesTemperatureAndTop[1],
      creative: this.modesTemperatureAndTop[2],
      custom: this.modesTemperatureAndTop[3],
    };

    if (modeMapping[event.value]) {
      this.plan_temperature = modeMapping[event.value].temperature;
      this.plan_top = modeMapping[event.value].top_p;
    }
    this.triggerSave();
  }

  public changeOutputMode(event: any) {
    this.outputMode = event;
    this.triggerSave();
  }

  public changeCollapsedState(configInfo: any) {
    try {
      const collapsed = configInfo.collapsed;
      configInfo.collapsed = !collapsed;
      this.cdr.markForCheck();
    } catch {}
  }

  public changeTemperature() {
    this.isCustomMode();
    if (this.mode.value !== 'custom') {
      this.switchToCustomMode();
    }
    this.triggerSave();
  }

  public planChangeTemperature() {
    this.planIsCustomMode();
    if (this.plan_mode.value !== 'custom') {
      this.switchToCustomModePlan();
    }
    this.triggerSave();
  }

  public changeTemperatureSpinner(newValue: number) {
    this.temperature = newValue;
    this.isCustomMode();
    if (this.mode.value !== 'custom') {
      this.switchToCustomMode();
    }
    this.triggerSave();
  }

  public planChangeTemperatureSpinner(newValue: number) {
    this.plan_temperature = newValue;
    this.planIsCustomMode();
    if (this.plan_mode.value !== 'custom') {
      this.switchToCustomModePlan();
    }
    this.triggerSave();
  }

  public changeTop() {
    this.isCustomMode();
    if (this.mode.value !== 'custom') {
      this.switchToCustomMode();
    }
    this.triggerSave();
  }

  public planChangeTop() {
    this.planIsCustomMode();
    if (this.plan_mode.value !== 'custom') {
      this.switchToCustomModePlan();
    }
    this.triggerSave();
  }

  public changeTopSpinner(newValue: number) {
    this.top = newValue;
    this.isCustomMode();
    if (this.mode.value !== 'custom') {
      this.switchToCustomMode();
    }
    this.triggerSave();
  }

  public planChangeTopSpinner(newValue: number) {
    this.plan_top = newValue;
    this.planIsCustomMode();
    if (this.plan_mode.value !== 'custom') {
      this.switchToCustomModePlan();
    }
    this.triggerSave();
  }

  public changeMaxReplyLength() {
    this.isCustomMode();
    if (this.mode.value !== 'custom') {
      this.switchToCustomMode();
    }
    this.triggerSave();
  }

  public planChangeMaxReplyLength() {
    this.planIsCustomMode();
    if (this.plan_mode.value !== 'custom') {
      this.switchToCustomModePlan();
    }
    this.triggerSave();
  }

  public changeMaxReplyLengthSpinner(newValue: number) {
    this.maxReplyLength = newValue;
    this.isCustomMode();
    if (this.mode.value !== 'custom') {
      this.switchToCustomMode();
    }
    this.triggerSave();
  }

  public planChangeMaxReplyLengthSpinner(newValue: number) {
    this.plan_maxReplyLength = newValue;
    this.planIsCustomMode();
    if (this.plan_mode.value !== 'custom') {
      this.switchToCustomModePlan();
    }
    this.triggerSave();
  }

  changeThinking(enable: boolean) {
    this.enable_thinking = enable ?? true;
    this.triggerSave();
  }

  public changRound() {
    this.isCustomMode();
    if (this.mode.value !== 'custom') {
      this.switchToCustomMode();
    }
    this.triggerSave();
  }

  public planChangRound() {
    this.planIsCustomMode();
    if (this.plan_mode.value !== 'custom') {
      this.switchToCustomModePlan();
    }
    this.triggerSave();
  }

  public changeRoundSpinner(newValue: number) {
    this.rounds = newValue;
    this.isCustomMode();
    if (this.mode.value !== 'custom') {
      this.switchToCustomMode();
    }
    this.triggerSave();
  }

  public planChangeRoundSpinner(newValue: number) {
    this.plan_rounds = newValue;
    this.planIsCustomMode();
    if (this.plan_mode.value !== 'custom') {
      this.switchToCustomModePlan();
    }
    this.triggerSave();
  }

  public changeFrequencyPenalty() {
    this.isCustomMode();
    if (this.mode.value !== 'custom') {
      this.switchToCustomMode();
    }
    this.triggerSave();
  }

  public changeFrequencyPenaltySpinner(newValue: number) {
    this.frequencyPenalty = newValue;
    this.isCustomMode();
    if (this.mode.value !== 'custom') {
      this.switchToCustomMode();
    }
    this.triggerSave();
  }

  // 检查并切换到自定义模式（问答模型）
  public checkAndSwitchToCustom(changedParam: string, newValue: any): void {
    // 更新对应的属性值
    switch (changedParam) {
      case 'temperature':
        this.temperature = newValue;
        break;
      case 'top':
        this.top = newValue;
        break;
      case 'maxReplyLength':
        this.maxReplyLength = newValue;
        break;
      case 'rounds':
        this.rounds = newValue;
        break;
      case 'frequencyPenalty':
        this.frequencyPenalty = newValue;
        break;
      default:
        break;
    }

    // 如果不是自定义模式，则切换到自定义模式
    if (this.mode.value !== 'custom') {
      this.switchToCustomMode();
    }

    // 触发防抖保存
    this.triggerSave();
  }

  // 检查并切换到自定义模式（规划模型）
  public checkAndSwitchToCustomPlan(changedParam: string, newValue: any): void {
    // 更新对应的属性值
    switch (changedParam) {
      case 'temperature':
        this.plan_temperature = newValue;
        break;
      case 'top':
        this.plan_top = newValue;
        break;
      case 'maxReplyLength':
        this.plan_maxReplyLength = newValue;
        break;
      case 'rounds':
        this.plan_rounds = newValue;
        break;
      default:
        break;
    }

    // 如果不是自定义模式，则切换到自定义模式
    if (this.plan_mode.value !== 'custom') {
      this.switchToCustomModePlan();
    }

    // 触发防抖保存
    this.triggerSave();
  }

  // 切换到自定义模式（问答模型）
  private switchToCustomMode(): void {
    this.mode = this.modes[3];
  }

  // 切换到自定义模式（规划模型）
  private switchToCustomModePlan(): void {
    this.plan_mode = this.plan_modes[3];
  }

  // 触发保存（防抖）
  public triggerSave(): void {
    this.saveConfigSubject.next();
  }

  private doTriggerSave(
    config: IUpdateSingleAgentConfig = {
      data: {},
      successCb: () => {},
      failCb: () => {},
      finallyCb: () => {},
    },
  ) {
    this.commonLogic.doTriggerSave(this.agentId, config);
  }

  private initAndSubscribe(isUpdateAgent: boolean = true) {
    let modelConfigSelected;

    if (this.modelConfigTipService.useModelMode === 'one' || this.modelConfigTipService.useModelMode === 'question') {
      modelConfigSelected = this.availableModelList.find(
        (item) => item.model_deployment_id === this.modelSelected,
      );
    } else if (this.modelConfigTipService.useModelMode === 'plan') {
      modelConfigSelected = this.availableModelList.find(
        (item) => item.model_deployment_id === this.planModelSelected,
      );
    } else {}


    if (modelConfigSelected) {
      this.agentDataServe.setPromptAndModelConfig({
        modelConfig: modelConfigSelected,
      });
    }

    if ((this.modelSelected || this.planModelSelected) && isUpdateAgent) {
      this.triggerSave();
    }
  }

  // 实际保存配置的方法
  private saveConfig(): void {
    // 根据当前模式构建数据
    let data: any = {};

    if (this.modelConfigTipService.useModelMode === 'one') {
      // 合一模式：保存问答模型配置，并设置 plan_qa_independent = false
      const modelConfigSelected = this.availableModelList.find(
        (item) => item.model_deployment_id === this.modelSelected,
      );

      if (!modelConfigSelected || !this.modelSelected) {
        return;
      }

      const { model_name, model_version, model_type, model } = modelConfigSelected;

      data = {
        model_name,
        model_config: {
          temperature: this.temperature,
          top_p: this.top,
          history_size: this.rounds,
          max_tokens: this.maxReplyLength,
          output_format: this.outputMode,
          frequency_penalty: this.frequencyPenalty,
          enable_thinking: modelConfigSelected?.is_support_close_reasoning ? this.enable_thinking : null
        },
        model_deployment_id: this.modelSelected,
        model_version,
        model_type,
        model,
        plan_qa_independent: false, // 合一模式
      };
    }
    else if (this.modelConfigTipService.useModelMode === 'question') {
      // 问答模式：保存问答模型配置
      const modelConfigSelected = this.availableModelList.find(
        (item) => item.model_deployment_id === this.modelSelected,
      );

      if (!modelConfigSelected || !this.modelSelected) {
        return;
      }

      const { model_name, model_version, model_type, model } = modelConfigSelected;

      data = {
        model_name,
        model_config: {
          temperature: this.temperature,
          top_p: this.top,
          history_size: this.rounds,
          max_tokens: this.maxReplyLength,
          output_format: this.outputMode,
          frequency_penalty: this.frequencyPenalty,
          enable_thinking: modelConfigSelected?.is_support_close_reasoning ? this.enable_thinking : null
        },
        model_deployment_id: this.modelSelected,
        model_version,
        model_type,
        model,
        // 不设置 plan_qa_independent，保持原样
      };
    }
    else if (this.modelConfigTipService.useModelMode === 'plan') {
      // 规划模式：保存规划模型配置
      const planModelConfigSelected = this.availableModelList.find(
        (item) => item.model_deployment_id === this.planModelSelected,
      );

      if (!planModelConfigSelected || !this.planModelSelected) {
        return;
      }

      const { model_name, model_version, model_type, model } = planModelConfigSelected;

      data = {
        plan_model_deployment_id: this.planModelSelected,
        plan_model_type: model_type,
        plan_model: model,
        plan_model_name: model_name,
        plan_model_config: {
          temperature: this.plan_temperature,
          top_p: this.plan_top,
          history_size: this.plan_rounds,
          max_tokens: this.plan_maxReplyLength,
          output_format: this.plan_outputMode,
        },
        // 不设置 plan_qa_independent，保持原样
      };
    }

    this.doTriggerSave({
      data,
      successCb: () => {
        // 保存成功后触发页面更新
        this.triggerPageUpdate();
      },
    });
  }

  // 触发页面更新的方法
  private triggerPageUpdate(): void {
    // 强制变更检测，更新当前组件视图
    this.cdr.markForCheck();
    this.cdr.detectChanges();

    // 更新模型配置显示
    this.updateModelConfigDisplay();
  }

  // 更新模型配置显示
  private updateModelConfigDisplay(): void {
    let modelConfigSelected;

    if (this.modelConfigTipService.useModelMode === 'one' || this.modelConfigTipService.useModelMode === 'question') {
      modelConfigSelected = this.availableModelList.find(
        (item) => item.model_deployment_id === this.modelSelected,
      );
    } else if (this.modelConfigTipService.useModelMode === 'plan') {
      modelConfigSelected = this.availableModelList.find(
        (item) => item.model_deployment_id === this.planModelSelected,
      );
    }

    if (modelConfigSelected) {
      // 更新数据服务中的模型配置
      this.agentDataServe.setPromptAndModelConfig({
        modelConfig: modelConfigSelected,
      });
    }
  }

  private isCustomMode() {
    const modeMap = [
      {
        temperature: this.modesTemperatureAndTop[0].temperature,
        top: this.modesTemperatureAndTop[0].top_p,
        mode: this.modes[0].value,
      },
      {
        temperature: this.modesTemperatureAndTop[1].temperature,
        top: this.modesTemperatureAndTop[1].top_p,
        mode: this.modes[1].value,
      },
      {
        temperature: this.modesTemperatureAndTop[2].temperature,
        top: this.modesTemperatureAndTop[2].top_p,
        mode: this.modes[2].value,
      },
    ];

    const matchedMode = modeMap.find(
      (mode) => this.temperature === mode.temperature && this.top === mode.top,
    );
    if (matchedMode) {
      this.mode = this.findModeByValue(matchedMode.mode);
    } else {
      this.mode = this.modes[3];
    }
  }

  private planIsCustomMode() {
    const modeMap = [
      {
        temperature: this.modesTemperatureAndTop[0].temperature,
        top: this.modesTemperatureAndTop[0].top_p,
        mode: this.modes[0].value,
      },
      {
        temperature: this.modesTemperatureAndTop[1].temperature,
        top: this.modesTemperatureAndTop[1].top_p,
        mode: this.modes[1].value,
      },
      {
        temperature: this.modesTemperatureAndTop[2].temperature,
        top: this.modesTemperatureAndTop[2].top_p,
        mode: this.modes[2].value,
      },
    ];
    const matchedMode = modeMap.find(
      (mode) =>
        this.plan_temperature === mode.temperature &&
        this.plan_top === mode.top,
    );
    if (matchedMode) {
      this.plan_mode = this.findModeByValue(matchedMode.mode);
    } else {
      this.plan_mode = this.plan_modes[3];
    }
  }

  findModeByValue(value) {
    return this.modes.find((mode) => mode.value === value);
  }

  // 设置max_tokens默认范围和值（问答模型）
  private setMaxReplySetting(selectedModel): void {
    const { env = [] } = selectedModel || {};
    const envList = env[0]?.env_list;
    const maxReplySetting = envList?.find(
      (item) => item?.env_name === 'max_tokens',
    );
    const defaultVal = 4096;
    this.maxReplySetting = {
      min: maxReplySetting?.min ?? this.maxReplySetting.min,
      max: maxReplySetting?.max ?? this.maxReplySetting.max,
    };

    this.maxReplyLength =
      this.maxReplyLength || maxReplySetting?.default_value || defaultVal;
  }

  // 设置max_tokens默认范围和值（规划模型）
  private planSetMaxReplySetting(selectedModel): void {
    const { env = [] } = selectedModel || {};
    const envList = env[0]?.env_list;
    const maxReplySetting = envList?.find(
      (item) => item?.env_name === 'max_tokens',
    );
    const defaultVal = 4096;
    this.maxReplySetting = {
      min: maxReplySetting?.min ?? this.maxReplySetting.min,
      max: maxReplySetting?.max ?? this.maxReplySetting.max,
    };

    this.plan_maxReplyLength =
      this.plan_maxReplyLength || maxReplySetting?.default_value || defaultVal;
  }

  items: Array<any> = [
    {
      text: this.i18n.transform('in_one'),
      value: 'unify'
    },
    {
      text: this.i18n.transform('independence'),
      value: 'independence'
    },
  ];

  unfoldStatus = false;

  unfoldAdvancedConfig() {
    this.unfoldStatus = !this.unfoldStatus;
    this.triggerSave();
  }

  planChangeCig($event) {
    this.triggerSave();
  }

  qaStatus = true;
  planQaStatus = true;

  toggleQaStatus = (): void => {
    this.qaStatus = !this.qaStatus;
  };

  togglePlanQaStatus = (): void => {
    this.planQaStatus = !this.planQaStatus;
  };
}
