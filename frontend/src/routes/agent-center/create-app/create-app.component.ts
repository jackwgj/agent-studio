import { Location } from '@angular/common';
import { Component, ElementRef, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageComponent, StorageService } from '@shared/services/cfdata.service';
import { ApplicationType } from '@enums/agent-center.enum';
import { I18nNamespace } from '@i18n';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { ModelImageDataService } from '@routes/agent-center/app-agent/components/info-and-prompt-builder/model-image-src.service';
import { generateFlowCode } from '@routes/agent-center/utils';
import { IconUploadComponent } from '@routes/knowledge-center/knowledge/icon-upload.component';
import { AppAgentHighCodeService } from '@services/agent-center/app-agent-high-code.service';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { CommonService } from '@services/common.service';
import { HttpService } from '@services/http.service';
import { InputFileTextComponent } from '@shared/components/input-file-text/input-file-text.component';
import { MODULES } from '@shared/modules';
import { SetSidebarVisibilityService } from '@shared/services/set-sidebar-visibility.service';
import { CommonValidation } from '@shared/validation/commonValidation';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { CommonUtils } from '../../../utils/common.util';

enum mapKeys {
  from = 'from',
  code = 'code',
}

@Component({
  selector: 'agent-create-app',
  templateUrl: './create-app.component.html',
  styleUrls: ['./create-app.component.less'],
  standalone: true,
  imports: [MODULES, IconUploadComponent, InputFileTextComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT, I18nNamespace.TOOL],
    },
  ],
})
export class CreateAppComponent implements OnInit {
  public isShowErr: boolean = false;
  public initialValue = { pureText: '', tags: [] };
  public createTypes = [
    {
      type: ApplicationType.SINGLE_AGENT,
      name: 'single_agent',
      desc: 'single_agent_app_desc',
      info: 'single_agent_app_info',
      icon: cdnAssetUrl('assets/agent-center/create-app/single-icon.svg'),
      image_en: cdnAssetUrl('assets/agent-center/create-app/single-agent-app-en.svg'),
      imageUrl: cdnAssetUrl('assets/agent-center/create-app/single-agent-app.svg'),
    },
    {
      type: ApplicationType.MULTI_AGENT,
      name: 'multiple_agent',
      desc: 'multi_agent_app_desc',
      info: 'multi_agent_app_info',
      icon: cdnAssetUrl('assets/agent-center/create-app/multi-icon.svg'),
      image_en: cdnAssetUrl('assets/images/config/multi-agent-en.png'),
      imageUrl: cdnAssetUrl('assets/agent-center/create-app/multi-agent.png'),
    },
    {
      type: ApplicationType.CHAT_WORKFLOW,
      name: 'dialogue_based_workflow',
      desc: 'workflow_chat_desc',
      info: 'workflow_chat_info',
      icon: cdnAssetUrl('assets/agent-center/create-app/chat-icon.svg'),
      image_en: cdnAssetUrl('assets/images/config/workflow-con-en.png'),
      imageUrl: cdnAssetUrl('assets/agent-center/create-app/workflow-chat.png'),
    },
    {
      type: ApplicationType.TASK_WORKFLOW,
      name: 'task_based_workflow',
      desc: 'workflow_task_desc',
      info: 'workflow_task_info',
      icon: cdnAssetUrl('assets/agent-center/create-app/task-icon.svg'),
      image_en: cdnAssetUrl('assets/images/config/workflow-task-en.png'),
      imageUrl: cdnAssetUrl('assets/agent-center/create-app/workflow-task.png'),
    },
  ];
  public highCodeTypes = [];
  public iconConfig = {
    defaultIcon: this.defaultIcon,
    accept: '.png,.jpeg,.gif,.jpg',
    upload_size: 1024 * 200,
    desc: this.i18n.transform('createappcomponent_1214'),
  };

  public selectedType = ApplicationType.SINGLE_AGENT;

  ApplicationType = ApplicationType;

  get backTitle() {
    let str = '';
    if (this.selected.type === ApplicationType.CHAT_WORKFLOW) {
      str = this.i18n.transform('create_based_workflow');
    }

    if (this.selected.type === ApplicationType.TASK_WORKFLOW) {
      str = this.i18n.transform('create_task_workflow');
    }

    if (this.selected.type === ApplicationType.SINGLE_AGENT) {
      str = this.i18n.transform('homecomponent_1215');
    }

    if (this.selected.type === ApplicationType.MULTI_AGENT) {
      str = this.i18n.transform('homecomponent_1217');
    }

    if (this.selected.type === ApplicationType.AGENT_NEW) {
      str = this.i18n.transform('homecomponent_1230');
    }

    return str;
  }

  public get selected() {
    return [...this.createTypes, ...this.highCodeTypes].find(item => item.type === this.selectedType);
  }

  public get isAgent() {
    return [ApplicationType.SINGLE_AGENT, ApplicationType.MULTI_AGENT].includes(this.selectedType);
  }

  public get isAi() {
    return [ApplicationType.AGENT_NEW].includes(this.selectedType);
  }

  public get defaultIcon() {
    switch (this.selectedType) {
      case ApplicationType.SINGLE_AGENT:
        return cdnAssetUrl('assets/agent-center/create-app/single-icon-default.svg');
      case ApplicationType.MULTI_AGENT:
        return cdnAssetUrl('assets/agent-center/create-app/multi-icon-default.svg');

      default:
        return cdnAssetUrl('assets/agent-center/create-app/workflow-default.svg');
    }
  }

  public get formLabels() {
    switch (this.selectedType) {
      case ApplicationType.SINGLE_AGENT:
        return ['name', '', 'description', 'single_agent_icon'];
      case ApplicationType.MULTI_AGENT:
        return ['name', '', 'description', 'multi_agent_icon'];

      default:
        return ['name', 'name', 'description', 'workflow_icon'];
    }
  }

  public get placeholders() {
    if (this.isAgent) {
      return ['app_name_placeholder', '', 'app_desc_placeholder'];
    }
    return ['workflow_name_placeholder', 'workflow_code_input_placeholder', 'workflow_desc_placeholder'];
  }

  // 是否来自于开发者空间
  private isFromDevelopSpace: boolean = false;

  // 是否来自单智能体
  public isFromEditFlow: boolean = false;

  // 是否来自agent builder
  public isFromAgentBuilder: boolean = false;

  public loading = false;

  public form: FormGroup;

  public aiForm: FormGroup;

  from: string = '';
  workflowId: string = '';
  subscribeBtnStatus = this.commonService.getSubscribeStatus();
  lang: string = '';
  private isFromOverview: boolean = false;
  private activeOverview = '';

  // 图标选项数组
  iconOptions: any[] = [];
  iconLoading = false;
  isShowCardSelect = false;
  MAXLENGHT = 1024;
  type = 'agent';

  constructor(
    private fb: FormBuilder,
    private i18n: I18NextEagerPipe,
    private appService: AppAgentRepoService,
    private flowService: AppFlowRepoService,
    private router: Router,
    private route: ActivatedRoute,
    private elementRef: ElementRef,
    private sidebarServ: SetSidebarVisibilityService,
    public commonService: CommonService,
    private location: Location,
    private readonly http: HttpService,
    private modelImageDataService: ModelImageDataService,
    private configServ: AgentConfigService,
    private highCodeService: AppAgentHighCodeService
  ) {
    this.lang = CommonUtils.getLanguage();
    this.route.queryParams.subscribe(params => {
      const state = this.router.getCurrentNavigation()?.extras.state as {
        name: string;
        description: string;
      };
      if (state) {
        this.aiForm.patchValue(state);
        return;
      }
      let { type, from, workflowId } = params;
      this.type = type || ApplicationType.SINGLE_AGENT;
      if (type === 'chat') {
        this.MAXLENGHT = 1024;
      } else {
        this.MAXLENGHT = 256;
      }
      // 兼容开发者空间的逻辑
      if (from && from === 'develop-space') {
        this.isFromDevelopSpace = true;
      }

      if (from === 'agentBuilder') {
        this.isFromAgentBuilder = true;
      }

      if (from && from === 'edit-flow') {
        this.from = from;
        this.workflowId = workflowId;
        this.createTypes = this.createTypes.slice(2);
        this.highCodeTypes = [];
        type = ApplicationType.CHAT_WORKFLOW;
        this.isFromEditFlow = true;
      }

      this.isShowCardSelect = !type || type === 'chat';
      if (type === 'chat') {
        this.createTypes = this.createTypes.slice(-2);
      }
      this.handleTypeSelect(type ?? ApplicationType.SINGLE_AGENT);
    });
  }

  public ngOnInit() {
    this.form = this.fb.group({
      name: ['', [Validators.required, CommonValidation.wfNameVerify(this.i18n.transform('variable_validator5'))]],
      code: [''],
      description: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(this.MAXLENGHT)]],
      icon: [''],
    });
    this.aiForm = this.fb.group({
      ai_name: ['', [Validators.required, CommonValidation.wfNameVerify(this.i18n.transform('variable_validator5'))]],
      description: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(this.MAXLENGHT)]],
    });

    this.route.queryParams.subscribe(params => {
      if (params.active) {
        this.activeOverview = params.active;
      }
      if (params.from && params.from === 'overview') {
        this.isFromOverview = true;
        if (!this.isAi) {
          const str = this.isAgent ? this.configServ.getConfigs()?.demo_agent_id : this.configServ.getConfigs()?.demo_workflow_id;
          if (str) {
            let strList = str.split(';');
            if (this.isAgent) {
              this.form.controls.name.setValue(strList[0]);
              this.form.controls.description.setValue(strList[1]);
            } else {
              this.form.controls.name.setValue(strList[0]);
              this.form.controls.code.setValue(strList[1]);
              this.form.controls.description.setValue(strList[2]);
            }
            this.generationImage();
          }
        }
      }
    });

    this.sidebarServ.setSidebarsVisibilityByState('init', true);
  }

  ngOnDestroy() {
    this.sidebarServ.setSidebarsVisibilityByState('destroy');
  }

  public initForm() {
    if (this.isAi) {
      this.isShowErr = false;
      return;
    } else {
      this.initialValue = { pureText: '', tags: [] };
    }
    this.form?.reset();
    if (!this.isAgent) {
      return;
    }
  }

  public handleTypeSelect(type: ApplicationType) {
    this.selectedType = type;
    this.initForm();
  }

  public back() {
    if (this.isFromOverview) {
      this.router.navigate(['/home/overview'], {
        queryParams: {
          active: this.activeOverview,
        },
      });
      return;
    }
    if (this.isFromDevelopSpace) {
      this.router.navigate(['/home/develop-space']);
      return;
    }
    if (this.isFromEditFlow) {
      this.location.back();
      return;
    }
    const routeQueryParams = this.route.snapshot.queryParams;
    let _queryParams: Record<string, string> = {};
    if (routeQueryParams.from === 'agentBuilder') {
      Object.keys(routeQueryParams).forEach((key: string) => {
        if (key === 'from' || key === 'work_space_id') {
          _queryParams[key] = routeQueryParams[key];
        }
      });
    }
    switch (this.selectedType) {
      case ApplicationType.CHAT_WORKFLOW:
        this.router.navigate(['/home/agent-center/workflow'], {
          queryParams: { ..._queryParams },
        });
        break;
      case ApplicationType.MULTI_AGENT:
        this.router.navigate(['/home/agent-center/multi']);
        break;
      case ApplicationType.SINGLE_AGENT:
        this.router.navigate(['/home/agent-center/single']);
        break;
      case ApplicationType.AGENT_NEW:
        this.router.navigate(['/home/agent-center/high']);
        break;
      default:
        this.router.navigate(['/home/agent-center/single']);
    }
  }

  isPNG(str) {
    return str.toLowerCase().endsWith('.png');
  }

  public async createAppNew() {
    const { agent_id } = await this.highCodeService.createHighAgent({
      description: this.initialValue.pureText,
      type: 'agent_new',
    });
    if (!agent_id) {
      return;
    }
    StorageService.setSessionStorage(`firstMessage_${agent_id}`, this.initialValue.pureText);
    this.router.navigate([`/home/spec-driven/${agent_id}`]);
  }

  public async createApp() {
    const params = this.form.value;
    if (params.icon && !this.isPNG(params.icon)) {
      params.icon = this.findIcon(params.icon)[0]?.icon;
    }
    if (!params.icon) {
      delete params.icon;
    }
    if (this.selectedType === ApplicationType.MULTI_AGENT) {
      params.type = ApplicationType.MULTI_AGENT;
    }
    const agentRes = await this.appService.createAgent(params);
    const { agent_id } = agentRes;
    if (!agent_id) {
      return;
    }
    let queryParams = {};
    if (this.selectedType === ApplicationType.MULTI_AGENT) {
      queryParams = {
        ...queryParams,
        id: agent_id,
        type: 'multi',
      };
      if (this.isFromDevelopSpace) {
        queryParams[mapKeys.from] = 'develop-space';
      }
      this.router.navigate(['/home/agent-center/app-flow/flow'], {
        queryParams: queryParams,
      });
      return;
    }
    queryParams = {
      ...queryParams,
      agentId: agent_id,
    };
    if (this.isFromDevelopSpace) {
      queryParams[mapKeys.from] = 'develop-space';
    }
    this.router.navigate(['/home/agent-center/app-agent/detail'], {
      queryParams: queryParams,
    });
  }

  /** 检查是否有base64字符串 */
  private containsBase64Image(text: string) {
    // 匹配常见的 Base64 图片格式
    const pattern =
      /data:image\/(?:png|jpeg|jpg|gif|bmp|webp|svg\+xml);base64,[A-Za-z0-9+/=]+/i;
    return pattern.test(text);
  }

  public async createFlow() {
    const { icon, ...params } = this.form.value;
    params[mapKeys.code] = generateFlowCode(this.form.get('name')?.value);
    // ai生成base64字符串
    if (icon && this.containsBase64Image(icon)) {
      params.avatar = this.findIcon(icon)[0]?.icon;
    } else if (icon) {
      params.avatar = icon;
    }
    const workflowRes = await this.flowService.createFlow({
      ...params,
      type: this.selectedType,
    });
    const { workflow_id } = workflowRes;
    if (!workflow_id) {
      return;
    }

    let queryParams: any = {};
    queryParams = {
      ...queryParams,
      id: workflow_id,
    };
    if (this.isFromDevelopSpace) {
      queryParams[mapKeys.from] = 'develop-space';
    }

    if (this.isFromAgentBuilder) {
      const { work_space_id = '', from = '' } = this.route.snapshot.queryParams;
      if (work_space_id) {
        queryParams.work_space_id = work_space_id;
      }
      if (from) {
        queryParams.from = from;
      }
    }

    let extrayParams: any = {};

    if (this.from === 'edit-flow') {
      const key = 'workflowId';
      extrayParams[key] = this.workflowId;
      const { multiFlowType = '' } = this.route.snapshot.queryParams;
      if (multiFlowType) {
        extrayParams.multiFlowType = multiFlowType;
      }
    }

    this.router.navigate([`/home/agent-center/app-flow/flow`], {
      queryParams: {
        id: workflow_id,
        from: this.getQueryParams(),
        ...extrayParams,
        ...queryParams,
      },
    });
  }

  private getQueryParams() {
    if (this.from) {
      return this.from;
    }
    if (this.isFromDevelopSpace) {
      return 'develop-space';
    }
    return '';
  }

  public async handleCreate() {

    const subscribeBtnStatus = this.commonService.getSubscribeStatus();
    if (!subscribeBtnStatus && this.isFromDevelopSpace) {
      return;
    }

    if (this.isAi) {
      if (!this.initialValue.pureText && !this.initialValue.tags.length) {
        this.isShowErr = true;
        this.elementRef.nativeElement.querySelector(`[contenteditable=true]`).focus();
        return;
      }
      try {
        this.loading = true;
        await this.createAppNew();
        MessageComponent.showSuccess(this.i18n.transform('created_successfully'));
      } finally {
        this.loading = false;
      }
      return;
    }

    if (!this.form.valid) {
      Object.values(this.form.controls).forEach(control => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
      const firstControlName = Object.keys(this.form.controls).find(key => this.form.get(key)?.invalid);
      if (firstControlName) {
        this.elementRef.nativeElement.querySelector(`[formControlName=${firstControlName}]`).focus();
      }
      return;
    }
    try {
      this.loading = true;
      this.isAgent ? await this.createApp() : await this.createFlow();
      MessageComponent.showSuccess(this.i18n.transform('created_successfully'));
    } finally {
      this.loading = false;
    }
  }

  handleClickToTemplate() {
    this.router.navigate(['/home/app-library']);
  }

  gotoN2() {
    let appType = 'agent';
    if (this.selectedType === 'agent') {
      appType = 'agent';
    } else if (this.selectedType === 'chat') {
      appType = 'workflow';
    }
    this.router.navigate(['/home/ai-assist-create-home'], {
      queryParams: {
        appType: appType,
      },
    });
  }

  isZH(): boolean {
    return this.lang === 'zh-cn';
  }

  // 检查当前选中的图标是否在 iconOptions 中
  getSelectedIconOption(): any | null {
    const currentIcon = this.form.get('icon')?.value;
    if (!currentIcon || !this.iconOptions.length) {
      return null;
    }

    // 查找匹配的图标
    return this.iconOptions.find(option => option.icon_detail === currentIcon || option.icon === currentIcon);
  }

  // 判断图标是否被选中
  isIconSelected(option: any): boolean {
    const selectedOption = this.getSelectedIconOption();
    return selectedOption && (selectedOption.icon_detail === option.icon_detail || selectedOption.icon === option.icon);
  }

  generationImage() {
    this.iconLoading = true;
    this.modelImageDataService
      .getModelImageData({
        name: this.form.get('name')?.value,
        description: this.form.get('description')?.value,
      })
      .then(res => {
        this.iconOptions.push(res.icons);
        this.form.get('icon')?.setValue(res.icons.icon_detail);
      })
      .finally(() => {
        this.iconLoading = false;
      });
  }

  useImage(option: any) {
    // AI生成图标返回对象，icon值传给后台   icon_detail 为base64 作为展示使用
    this.form.get('icon')?.setValue(option.icon_detail);
  }

  findIcon(detail) {
    return this.iconOptions.filter(item => {
      return detail === item.icon_detail;
    });
  }

  generationImageTip() {
    if (!(this.form.get('name')?.value && this.form.get('description')?.value)) {
      return this.i18n.transform('model_image_generate_tip');
    } else if (this.iconOptions.length > 4) {
      return this.i18n.transform('up_fix_generate_tip');
    } else if (this.iconOptions.length > 0 && this.iconOptions.length < 5) {
      return this.i18n.transform('function_optimization4');
    } else {
      return this.i18n.transform('ai_generation_icon_tip');
    }
  }

  protected readonly changeUrl = cdnAssetUrl;

  contentChange(data) {
    this.initialValue = data;
  }
}
