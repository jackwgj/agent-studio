import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, NgForm, Validators } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { RefPromptComponent } from '@routes/agent-center/ref-prompt/ref-prompt.component';
import { ITmpl } from '@services/prompt.service';
import { DateSelectorComponent } from '@shared/components/date-selector/date-selector.component';
import { FlexibleTextFieldComponent } from '@shared/components/flexible-text-field/flexible-text-field.component';
import { NumberRangeComponent } from '@shared/components/number-range/number-range.component';
import { PositiveIntValidatorDirective, RefSelectedRequireDirective, UrlValidatorDirective } from '@shared/directives/common-validator.directive';
import {
  EnumStrValidatorDirective,
  FlowParamIsUnique,
  IntegerRangeValidatorDirective,
  NonEmptyValidatorDirective,
  NumberRangeValidatorDirective,
  ValueValidityValidatorDirective,
  VariableNameValidatorDirective,
} from '@shared/directives/variable-name-validator.directive';
import { MODULES } from '@shared/modules';
import { CommonValidation } from '@shared/validation/commonValidation';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { cloneDeep } from 'lodash';
import { BehaviorSubject, debounceTime, Subject, Subscription, takeUntil } from 'rxjs';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { CommonUtils } from 'src/utils/common.util';
import { AppFlowService } from '../../app-flow.service';
import type { IModel } from '../../app-flow.types';
import { getInitInputParamConfig, getInitOutputParamConfig, getLLMLikeOutputParamTypes, NODE_SAVE_DEBOUNCE_TIME } from '../../flow.const';
import { NodeService } from '../../node.service';
import type {
  IParamRef,
  IQuesRewriteChain,
  IQuestionerNode,
  IQuesValidator,
  IQuesWorkflowField,
  IWorkflowField,
  IWorkflowFieldType,
  QuesRailsAction,
} from '../../node.type';
import { AccBlockNewComponent } from '../acc-block-new/acc-block-new..component';
import { ModalBaseComponent } from '../base/modal-base.component';
import { ParamSelectorComponent } from '../param-selector/param-selector.component';
import { NodeUtils } from '../utils';
import { IPluginRefTipUpdatedValue, PluginRefTipComponent } from './plugin-ref-tip';
import { modelSettingsComponent, type IModelParam } from '@shared/components/model-settings-tip';
import { AssetIntelligentAddComponent } from '@shared/components/assets/asset-intelligent-add/asset-intelligent-add.component';
import { AssetIntelligentAddDisabledComponent } from '@shared/components/assets/asset-intelligent-add-disabled/asset-intelligent-add-disabled.component';
import { LLMSelectComponent } from '@routes/agent-center/app-flow/components/llm-select/llm-select.component';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { EditNameComponent } from '@routes/agent-center/app-flow/components/edit-name/edit-name.component';
import { NodeDescriptionComponent } from '../node-description/node-description.component';
import { InputTreeSelect } from 'src/routes/agent-center/app-flow/components/input-tree-select/input-tree-select';
import { NodeTypeTopic } from '@routes/agent-center/types/common.types';
import { HelpCenterService } from '@services/help-center.service';
import { CommonService } from '@services/common.service';
import { CmdTextareaComponent } from '@routes/agent-center/app-flow/components/cmd-textarea/cmd-textarea.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzTooltipDirective, NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzPopoverDirective, NzPopoverModule } from 'ng-zorro-antd/popover'; // 引入 Popover 指令和模块
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzBadgeModule } from 'ng-zorro-antd/badge';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzSliderModule } from 'ng-zorro-antd/slider';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { OptimizePromptModalComponent } from '@routes/agent-center/app-agent/components/optimize-prompt-modal/optimize-prompt-modal.component';

interface IQuesValidatorExt {
  type: QuesRailsAction;
  params: string;
}
interface IQuesWorkflowFieldExt extends Omit<IQuesWorkflowField, 'validator'> {
  validated: boolean;
  expended: boolean;
  validator: IQuesValidatorExt[];
}

@Component({
  selector: 'meta-questioner-modal',
  templateUrl: './questioner-modal.component.html',
  styleUrls: ['./questioner-modal.component.less', '.././common-styles.less'],
  standalone: true,
  imports: [
    MODULES,
    AccBlockNewComponent,
    VariableNameValidatorDirective,
    NonEmptyValidatorDirective,
    ValueValidityValidatorDirective,
    EnumStrValidatorDirective,
    NumberRangeValidatorDirective,
    IntegerRangeValidatorDirective,
    NumberRangeComponent,
    DateSelectorComponent,
    FlexibleTextFieldComponent,
    ParamSelectorComponent,
    UrlValidatorDirective,
    RefSelectedRequireDirective,
    PositiveIntValidatorDirective,
    FlowParamIsUnique,
    AssetIntelligentAddComponent,
    AssetIntelligentAddDisabledComponent,
    LLMSelectComponent,
    EditNameComponent,
    NodeDescriptionComponent,
    InputTreeSelect,
    CmdTextareaComponent,
    NzSelectModule,
    NzInputModule,
    NzButtonModule,
    NzIconModule,
    NzToolTipModule,
    NzPopoverModule, // 注册 Popover 模块
    NzDropDownModule,
    NzRadioModule,
    NzTagModule,
    NzBadgeModule,
    NzSwitchModule,
    NzSliderModule,
    NzInputNumberModule,
    NzCheckboxModule,
    PluginRefTipComponent, // 引入并注册子组件，以便直接使用标签
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class QuestionerModalComponent extends ModalBaseComponent implements OnInit, OnDestroy {
  @Input('names') names: string[];

  @Input('nodeInfo') nodeInfo: IQuestionerNode;

  @ViewChild('pluginRefTipRef')
  pluginRefTipRef: NzPopoverDirective; // 将类型变更为 Popover 指令

  @Output('confirm') confirm = new EventEmitter<IQuestionerNode>();

  @ViewChild('inputForm') inputForm: NgForm;

  @ViewChild('outputForm') outputForm: NgForm;

  @ViewChild('nameForm') nameForm: NgForm;

  @ViewChild('rewriteChainForm') rewriteChainForm: NgForm;

  @ViewChild('modelSettingsRef')
  modelSettingsRef: NzPopoverDirective; // 将模型设置的类型也同步变更为 Popover 指令

  protected override destroy$ = new Subject<void>();
  readonly showQuestionVarListSubject$ = new BehaviorSubject<boolean>(false);
  readonly showExtractionVarListSubject$ = new BehaviorSubject<boolean>(false);
  private plugin2QuesActionMap: Record<string, QuesRailsAction> = {
    CHAR: 'length_limit_validate',
    ENUM: 'enum_legality_validate',
    DATE: 'date_time_format',
    NUM: 'number_range_validate',
    COMMON: 'common_data_format_check',
  };

  public getQuesModesTip() {
    return `${this.i18n.transform('flow_ques_node_tip1')}<br>${this.i18n.transform('flow_ques_node_tip2')}`;
  }

  public getParameterExtractionTip() {
    return this.i18n.transform('parameter_extraction_tip');
  }

  public booleanOps = [
    {
      label: 'true',
      value: true,
    },
    {
      label: 'false',
      value: false,
    },
  ];

  public cdnUrl = cdnAssetUrl;

  public showPluginRefTip = false;

  public get getMaxResponse() {
    return this.configsFormGroup.controls.max_response.value;
  }

  public set getMaxResponse(value) {
    this.configsFormGroup.controls.max_response.setValue(value);
  }

  public get isParamExtract() {
    return this.configsFormGroup.controls.extract_fields_from_response.value;
  }

  public get outputParamValidates() {
    return this.outputParams.filter(param => param.validated);
  }

  public get customTemplateEnable() {
    return this.configsFormGroup.controls.auto_ask_mode.value === 'template';
  }

  public nameRefOptions: IParamRef[] = [];

  public modelListSubscription: Subscription;

  public outputDataTypes = getLLMLikeOutputParamTypes();

  public inputSourceOptions = [
    { label: this.i18n.transform('ref'), value: 'ref' },
    { label: this.i18n.transform('literal'), value: 'literal' },
  ];

  public modelOptions: IModel[] = [];

  public inputTableActions: any[] = [
    {
      label: this.i18n.transform('delete'),
    },
  ];

  public inputParams: IWorkflowField[] = [];

  public outputParams: IQuesWorkflowFieldExt[] = [];

  // 模型配置的Tip弹窗
  public modelSetRefCmp = modelSettingsComponent;

  public outputTableActions: any[] = [
    {
      label: this.i18n.transform('delete'),
    },
  ];

  public rewrite_chain: IQuesRewriteChain[] = [];

  public configsFormGroup = this.fb.group({
    model: ['', [Validators.required]],
    extra_prompt_for_fields_extraction: [''],
    question_content: ['', [Validators.required]],
    example_content: [''],
    with_chat_history: [true],
    extract_fields_from_response: [true],
    max_response: [10],
    allow_node_break: [false],
    allow_node_confirm: [false],
    auto_ask_template: [this.i18n.transform('ques_custom_inquiry_template_placeholder')],
    auto_ask_mode: ['default'],
    enum_visible: [false],
  });

  public autoAskModes = [
    { label: this.i18n.transform('default'), value: 'default' },
    { label: this.i18n.transform('intelligent_questioning'), value: 'ai' },
    { label: this.i18n.transform('custom_questioning'), value: 'template' },
  ];

  public validationRules = [Validators.required];

  public rewriteChainInfoMap = {
    rewrite_api: {
      name: this.i18n.transform('external_api_rewriting'),
      desc: `${this.i18n.transform('external_api_rewriting_desc1')}
              ${this.i18n.transform('external_api_rewriting_desc2')}
              ${this.i18n.transform('external_api_rewriting_desc3')}
              ${this.i18n.transform('external_api_rewriting_desc4')}`,
    },
  };

  public strValidateOps = [
    {
      label: this.i18n.transform('char_label'),
      value: 'length_limit_validate',
    },
    {
      label: this.i18n.transform('enum_label'),
      value: 'enum_legality_validate',
    },
    {
      label: this.i18n.transform('date_label'),
      value: 'date_time_format',
    },
    {
      label: this.i18n.transform('common_format'),
      value: 'common_data_format_check',
    },
  ];

  public numberValidateOps = [
    {
      label: this.i18n.transform('num_label'),
      value: 'number_range_validate',
    },
  ];

  public commonValidateOpts = [
    {
      label: this.i18n.transform('bank_card_number'),
      value: this.i18n.transform('bank_card_number'),
    },
    {
      label: this.i18n.transform('phone_number'),
      value: this.i18n.transform('phone_number'),
    },
    {
      label: this.i18n.transform('passport_no'),
      value: this.i18n.transform('passport_no'),
    },
    {
      label: this.i18n.transform('image_based64'),
      value: this.i18n.transform('image_based64'),
    },
    {
      label: 'url',
      value: 'url',
    },
    {
      label: this.i18n.transform('email'),
      value: this.i18n.transform('email'),
    },
    {
      label: this.i18n.transform('phone_number_country_code'),
      value: this.i18n.transform('phone_number_country_code'),
    },
    {
      label: this.i18n.transform('ip_address'),
      value: this.i18n.transform('ip_address'),
    },
    {
      label: this.i18n.transform('mac_address'),
      value: this.i18n.transform('mac_address'),
    },
    {
      label: this.i18n.transform('postal_code'),
      value: this.i18n.transform('postal_code'),
    },
    {
      label: 'UUID',
      value: 'UUID',
    },
    {
      label: this.i18n.transform('file_path'),
      value: this.i18n.transform('file_path'),
    },
    {
      label: this.i18n.transform('license_plate_number'),
      value: this.i18n.transform('license_plate_number'),
    },
  ];

  public pluginRefTip = PluginRefTipComponent;

  public exampleConfigTips = `${this.i18n.transform('example_config_tips1')}
    ${this.i18n.transform('example_config_tips2')}
    ${this.i18n.transform('example_config_tips3')}
    ${this.i18n.transform('example_config_tips4')}`;

  public showModelSetRef = false;

  public modelParams = {
    top_p: 0.5,
    temperature: 0.5,
    max_tokens: null,
  };

  public modelSetTipCtx = {
    param: this.modelParams,
    selectedModel: {},
    outputs: {
      updatedParam: ($event: IModelParam) => {
        this.modelParams = { ...$event };
        this.showModelSetRef = false;
        this.modelSettingsRef.hide();
        this.onSave();
      },
    },
  };

  public modeButtons = [
    { text: this.i18n.transform('effect_priority'), value: 'effect' },
    { text: this.i18n.transform('speed_priority'), value: 'speed' },
  ];

  public selectMode = this.modeButtons[0];

  public selectedModelSubscription: Subscription;

  inputForm$: Subscription;
  outputForm$: Subscription;
  configsFormGroup$: Subscription;

  needValid = false;

  updateTimeout: any = null;
  constructor(
    private fb: FormBuilder,
    protected override appFlowServ: AppFlowService,
    private i18n: I18NextEagerPipe,
    protected override nodeServ: NodeService,
    protected agentDataServe: AgentDataService,
    public configServ: AgentConfigService,
    private helpCenterService: HelpCenterService,
    protected commonService: CommonService,
    private nzModal: NzModalService,
    private cdr: ChangeDetectorRef
  ) {
    super(nodeServ, appFlowServ);
  }

  override type2Img = NodeUtils.type2Img;

  onInputValueTypeChange(row: IWorkflowField) {
    row.value.content = NodeUtils.getChangeContent(row.value.type);
    this.onSave();
  }

  getmodelStatus(is_connecting) {
    if (is_connecting) {
      return this.i18n.transform('running');
    }

    return this.i18n.transform('call_failed');
  }

  public override ngOnInit() {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();

    this.validationRules.push(CommonValidation.nameUniquenessVerify(this.names, this.i18n.transform('name_uniqueness'), this.nodeInfo.name) as any);

    const parentNode = this.getParentNodeInfo(this.appFlowServ.getGraph());
    if (parentNode) {
      this.getLoopInnerNodeRefs(parentNode).subscribe(info => {
        this.onRefUpdate(info);
      });
    } else {
      this.getSelfRefs().subscribe(info => {
        this.onRefUpdate(info);
      });
    }

    this.modelListSubscription = this.appFlowServ
      .modelListUpdate()
      .pipe(takeUntil(this.destroy$))
      .subscribe(models => {
        if (models.length > 0) {
          this.modelOptions = models ?? [];
        }
      });

    this.outputParams = this.nodeInfo.outputs.map(output => {
      const validated = !!output.validator.length;

      return {
        expended: false,
        ...output,
        validator: output.validator.map(v => ({
          ...v,
          params: v.params.join(','),
        })),
        validated,
      };
    });

    const { temperature, top_p, enum_visible, max_tokens } = this.nodeInfo.configs;
    this.modelParams = {
      top_p: top_p ?? 0.5,
      temperature,
      max_tokens,
    };

    if (this.nodeInfo.configs?.mode) {
      this.selectMode = this.modeButtons.find(item => item.value === this.nodeInfo.configs.mode);
    }

    let auto_ask_mode;
    if (this.nodeInfo.configs?.template_anthropomorphic) {
      auto_ask_mode = 'ai';
    } else if (this.nodeInfo.configs?.auto_ask_template) {
      auto_ask_mode = 'template';
    } else {
      auto_ask_mode = 'default';
    }

    this.selectedModelSubscription = this.configsFormGroup.get('model').valueChanges.subscribe(value => {
      this.modelSetTipCtx.selectedModel = this.modelOptions.find(model => model.model_deployment_id === value);
    });

    // 初始化模型信息
    setTimeout(() => {
      this.configsFormGroup.patchValue({
        model: this.nodeInfo.configs?.model?.model_deployment_id ?? '',
        extra_prompt_for_fields_extraction: this.nodeInfo.configs.extra_prompt_for_fields_extraction,
        question_content: this.nodeInfo.configs.question_content,
        with_chat_history: this.nodeInfo.configs.with_chat_history,
        extract_fields_from_response: this.nodeInfo.configs.extract_fields_from_response,
        max_response: this.nodeInfo.configs.max_response,
        example_content: this.nodeInfo.configs?.example_content ?? '',
        allow_node_break: this.nodeInfo.configs?.allow_node_break ?? false,
        allow_node_confirm: this.nodeInfo.configs?.allow_node_confirm ?? false,
        auto_ask_template: this.nodeInfo.configs?.auto_ask_template || this.i18n.transform('ques_custom_inquiry_template_placeholder'),
        auto_ask_mode,
        enum_visible: enum_visible ?? false,
      });
    });

    this.rewrite_chain = (this.nodeInfo.configs.rewrite_chain || []).filter(item => this.rewriteChainInfoMap[item.type]);
    if (!this.rewrite_chain.length && this.isPOC) {
      this.rewrite_chain.push({
        type: 'rewrite_api',
        index: 1,
        enabled: false,
        args: { url: '' },
      });
    }

    NodeUtils.checkModelExistence(this.nodeInfo.name, this.nodeInfo.configs?.model?.model_deployment_id ?? '', this.modelOptions);

    // 若开启参数提取，question_content非必填
    if (this.configsFormGroup.controls.extract_fields_from_response.value) {
      this.configsFormGroup.controls.question_content.removeValidators(Validators.required);
    }

    this.configsFormGroup.controls.extract_fields_from_response.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(value => {
      if (value) {
        this.configsFormGroup.controls.question_content.removeValidators(Validators.required);
      } else {
        this.configsFormGroup.controls.question_content.addValidators(Validators.required);

        this.outputParams = this.outputParams
          .filter(item => item.name.toLowerCase() === 'user_response' || item.name.toLowerCase() === 'status')
          .map(item => Object.assign({}, item));
      }
    });
    // 设置提示词优化的参数
    this.agentDataServe.setPromptAndModelConfig({
      modelConfig: this.nodeInfo.configs?.model,
    });
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.inputForm$ = this.inputForm.form.valueChanges.pipe(debounceTime(NODE_SAVE_DEBOUNCE_TIME)).subscribe(value => {
        this.onSave();
      });
      this.outputForm$ = this.outputForm.form.valueChanges.pipe(debounceTime(NODE_SAVE_DEBOUNCE_TIME)).subscribe(value => {
        this.onSave();
      });
      this.configsFormGroup$ = this.configsFormGroup.valueChanges.pipe(debounceTime(NODE_SAVE_DEBOUNCE_TIME)).subscribe(value => {
        this.onSave();
      });
    }, 100);
    if (this.appFlowServ.testRunVerificationError) {
      setTimeout(() => {
        this.validateNode();
      });
    }
  }

  public onRefUpdate(info: IParamRef[]) {
    this.nameRefOptions = info;

    if (this.isInit) {
      this.inputParams = NodeUtils.initInputs(this.nodeInfo.inputs, this.nameRefOptions);
    } else {
      NodeUtils.reSelectRefsWithNewOps(this.inputParams, this.nameRefOptions);
    }

    this.isInit = false;
  }

  override ngOnDestroy(): void {
    this.helpCenterService.hideHelpPanel();
    this.modelCloseSave();
    if (this.modelListSubscription) {
      this.modelListSubscription.unsubscribe();
    }
    if (this.selectedModelSubscription) {
      this.selectedModelSubscription.unsubscribe();
    }
    this.inputForm$.unsubscribe();
    this.outputForm$.unsubscribe();
    this.configsFormGroup$.unsubscribe();
    this.destroy$.next();
    this.destroy$.complete();
  }

  public getCNNamePlaceholder(param, index: number) {
    let { name } = param;
    name = name.toLowerCase();
    if (index > 1 || !['user_response', 'status'].includes(name)) {
      return 'input_placeholder';
    }
    const placeholderMap = {
      user_response: 'user_input',
      status: 'status_placeholder',
    };
    return placeholderMap[name];
  }

  public deleteInputParam(index: number) {
    this.inputParams.splice(index, 1);
    this.onSave();
  }

  public onDeleteOutput(index: number) {
    this.outputParams.splice(index, 1);
    this.onSave();
  }

  public addInputParam() {
    this.inputParams.push({
      ...getInitInputParamConfig(),
      refs: cloneDeep(this.nameRefOptions),
    });
    this.onSave();
  }

  public addOutputParam() {
    this.outputParams.push({
      ...getInitOutputParamConfig(),
      cn_name: '',
      validator: [],
      validated: false,
      required: true,
      reflection: this.selectMode.value === 'effect',
      expended: false,
    });
    this.onSave();
  }

  /** 点击展示模型配置的Tip弹窗 */
  public onShowModelSetTip() {
    if (this.showModelSetRef) {
      this.showModelSetRef = false;
      this.modelSettingsRef.hide();
    } else {
      this.showModelSetRef = true;
      this.modelSetTipCtx.param = this.modelParams;
      window.setTimeout(() => {
        this.modelSettingsRef.show();
      });
    }
  }

  onParamTypeChange(index: number) {
    if (this.outputParams[index].validated) {
      this.changeTypeInit(index);
    }
    this.onSave();
  }

  onChangeValidated(index: number) {
    this.changeTypeInit(index);
    this.onSave();
  }

  private changeTypeInit(index: number) {
    const param = this.outputParams[index];
    const { type } = this.outputParams[index];

    if (type === 'boolean') {
      param.validated = false;
    }

    if (type === 'string') {
      this.outputParams[index].validator = [
        {
          type: 'length_limit_validate',
          params: '',
        },
      ];
    }

    if (['integer', 'number'].includes(type)) {
      this.outputParams[index].validator = [
        {
          type: 'number_range_validate',
          params: '',
        },
      ];
    }
  }

  dismiss(): void {}

  close(): void {}

  validateNode() {
    const checkForm = (form: any) => {
      if (form && form.controls) {
        Object.values(form.controls).forEach((control: any) => {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        });
      }
    };
    checkForm(this.inputForm?.form);
    checkForm(this.outputForm?.form);
    checkForm(this.configsFormGroup);
  }

  getInputNames(index: number): {
    existingValues: string[];
    notAllowedValues: string[];
  } {
    const notAllowedValues = ['STATUS', 'query'];
    const names = this.variants;
    names.splice(index, 1);
    return { existingValues: names, notAllowedValues };
  }
  get variants() {
    return this.inputParams.map(param => param.name);
  }
  public outputValidityValidator(index: number) {
    const notAllowedValues = ['STATUS', 'status', 'USER_RESPONSE', 'user_response'];
    const names = this.outputParams.map(p => p.name);
    names.splice(index, 1);
    return { existingValues: names, notAllowedValues };
  }

  getOutputNames(index: number): {
    existingValues: string[];
  } {
    const names = this.outputParams.map(p => p.name);
    names.splice(index, 1);
    return { existingValues: names };
  }

  onNameChange() {
    window.setTimeout(() => {
      const checkForm = (form: any) => {
        if (form && form.controls) {
          Object.values(form.controls).forEach((control: any) => {
            control.markAsDirty();
            control.updateValueAndValidity({ onlySelf: true });
          });
        }
      };
      checkForm(this.inputForm?.form);
      checkForm(this.outputForm?.form);
    });
  }

  onOutputNameChange() {
    window.setTimeout(() => {
      const checkForm = (form: any) => {
        if (form && form.controls) {
          Object.values(form.controls).forEach((control: any) => {
            control.markAsDirty();
            control.updateValueAndValidity({ onlySelf: true });
          });
        }
      };
      checkForm(this.outputForm?.form);
    });
  }

  public handlePluginUpdated($event: IPluginRefTipUpdatedValue): void {
    if (this.showPluginRefTip) {
      if ($event.isSelect && $event.pluginArgs) {
        $event.pluginArgs.forEach(arg => {
          const { name, name_cn, type, description, required, validate_rule, validate_type, validated, default: default_value } = arg;

          const validator: IQuesValidatorExt[] = [];
          if (validated) {
            validator.push({
              type: this.plugin2QuesActionMap[validate_type],
              params: validate_rule,
            });
          }

          this.outputParams.push({
            name,
            type: type as IWorkflowFieldType,
            description,
            required,
            value: {
              type: 'generated',
              content: null,
              hint: '',
              default: default_value,
            },
            source: 'user',
            cn_name: name_cn,
            validator,
            validated,
            reflection: this.selectMode.value === 'effect',
            expended: false,
          });
        });
      }

      this.showPluginRefTip = false;
      this.pluginRefTipRef.hide();
    }
    this.onSave();
  }

  onShowPluginRefTip() {
    if (this.showPluginRefTip) {
      this.showPluginRefTip = false;
      this.pluginRefTipRef.hide();
    } else {
      this.showPluginRefTip = true;
      this.pluginRefTipRef.show();
    }
  }

  isNil(value: any) {
    return CommonUtils.isNil(value);
  }

  public onIntelligentAdd() {
    if (!this.configsFormGroup.controls.extra_prompt_for_fields_extraction.value || this.isFlowReadonly) return;
    this.nzModal.create({
      nzContent: OptimizePromptModalComponent,
      nzWidth: 600,
      nzData: {
        instruct: this.configsFormGroup.controls.extra_prompt_for_fields_extraction.value,
        isWorkflow: true,
        tipsChange: value => {
          this.configsFormGroup.controls.extra_prompt_for_fields_extraction.setValue(value);
          this.cdr.markForCheck();
        },
      },
    });
  }

  public openRefModal() {
    const myModal = this.nzModal.create({
      nzContent: RefPromptComponent,
      nzWidth: 1000,
      nzData: {
        select: (tmpl: ITmpl) => {
          this.configsFormGroup.controls.extra_prompt_for_fields_extraction.setValue(tmpl.content);
          this.cdr.markForCheck();
        },
      },
    });
  }

  modelCloseSave() {
    if (!this.tagCompareNoChange()) {
      this.appFlowServ.setNodeModalCloseMonitor({ id: this.nodeInfo.id });
    }
    this.handelSave();
  }

  expendMore(row: IQuesWorkflowFieldExt) {
    row.expended = !row.expended;
  }

  onSave() {
    this.changeUpdateTime();
    if (this.appFlowServ.testRunVerificationError) {
      this.handelSave();
    }
  }

  public updateModel(modelInfo: any) {
    this.configsFormGroup.controls.model.setValue(modelInfo.id);
    // 设置提示词优化的参数
    this.agentDataServe.setPromptAndModelConfig({
      modelConfig: modelInfo.modelInfo,
    });
  }

  treeSelect() {
    setTimeout(() => {
      this.onSave();
    });
  }

  protected readonly changeUrl = cdnAssetUrl;

  handelSave() {
    if (this.tagCompareNoChange()) {
      return;
    }
    const selectedModel = this.modelOptions.find(model => model.model_deployment_id === this.configsFormGroup.controls.model.value);
    let hasTimeValidator = false;

    const nodeData: IQuestionerNode = {
      id: this.nodeInfo.id,
      name: this.nodeInfo.name,
      type: this.nodeInfo.type,
      inputs: NodeUtils.getDtoInputs(this.inputParams),
      outputs: this.outputParams.map(outputParam => {
        outputParam.reflection = this.selectMode.value === 'effect';
        const { validated, ...restParam } = outputParam;
        let validator = [];

        if (validated) {
          validator = restParam.validator.map(v => {
            const ret: IQuesValidator = {
              type: v.type,
              params: [v.params],
            };

            if (v.type === 'number_range_validate') {
              ret.params = v.params.split(',');
            }

            if (v.type === 'enum_legality_validate') {
              ret.params = [v.params.replace(/，/g, ',')];
            }

            if (v.type === 'date_time_format') {
              hasTimeValidator = true;
            }

            return ret;
          });
        }

        return {
          ...restParam,
          validator,
        };
      }),
      configs: {
        mode: this.selectMode.value,
        temperature: Number(this.modelParams.temperature),
        top_p: Number(this.modelParams.top_p),
        extra_prompt_for_fields_extraction: this.configsFormGroup.controls.extra_prompt_for_fields_extraction.value,
        question_content: this.configsFormGroup.controls.question_content.value,
        with_chat_history: this.configsFormGroup.controls.with_chat_history.value,
        extract_fields_from_response: this.configsFormGroup.controls.extract_fields_from_response.value,
        max_response: this.configsFormGroup.controls.max_response.value,
        model: {
          model_name: selectedModel?.model_name ?? '',
          model_type: selectedModel?.model_type ?? '',
          model_deployment_id: selectedModel?.model_deployment_id ?? '',
          ...(selectedModel?.model && { model: selectedModel.model }),
        },
        template_anthropomorphic: this.configsFormGroup.controls.auto_ask_mode.value === 'ai',
        auto_ask_template: this.configsFormGroup.controls.auto_ask_mode.value === 'template' ? this.configsFormGroup.controls.auto_ask_template.value : '',
        rewrite_chain: this.rewrite_chain,
        example_content: this.configsFormGroup.controls.example_content.value,
        allow_node_break: this.configsFormGroup.controls.allow_node_break.value,
        allow_node_confirm: this.configsFormGroup.controls.allow_node_confirm.value,
        enum_visible: this.configsFormGroup.value.enum_visible,
        max_tokens: Number(this.modelParams.max_tokens),
      },
    };
    this.appFlowServ.setNodeSaveMonitor({
      nodeData,
    });
    if (this.updateTimeout) {
      clearTimeout(this.updateTimeout);
      this.updateTimeout = null;
    }
    this.updateTimeout = setTimeout(() => {
      this.updateChangeAndInitTime();
    }, 200);
  }

  inputOnSave() {
    if (this.appFlowServ.testRunVerificationError) {
      this.handelSave();
    }
  }
}
