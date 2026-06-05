import { HttpClientModule } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nService } from '@agentcore/core/i18n.service';
import { EvaluationTab } from '@agentcore/evaluation/evaluation.interface';
import { I18nNamespace } from '@i18n';
import { COMMON_MODULES, MODULES } from '@shared/modules';
import { cdnAssetUrl } from '../../../single-spa/assets-url';
import { CommonUtils } from '../../../utils/common.util';
import { deepClone } from '../../../utils/utils';

@Component({
  selector: 'app-no-data-guide',
  templateUrl: './no-data-guide.component.html',
  styleUrls: ['./no-data-guide.component.less'],
  imports: [COMMON_MODULES, MODULES, HttpClientModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.GUIDE],
    },
    { provide: I18nService },
  ],
  standalone: true,
})
export class NoDataGuideComponentComponent implements OnInit {
  @Input() isSmall = false;
  // false 代表空数据向导  true代表小向导
  @Input() type = '';
  // 判断是哪个模块的向导
  @Output() clickDesc = new EventEmitter<any>();

  public lang = CommonUtils.getLanguage();

  public guideConfig = null;
  public baseUrl = `assets/images/no-data-guide/${this.lang === 'zh-cn' ? 'zh' : 'en'}`;

  public options: any = { ns: I18nNamespace.GUIDE };
  public agentNewConfig = {
    // byc test
    title: this.i18n.transform('agentNewConfig.title', this.options),
    desc: this.i18n.transform('agentNewConfig.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('agentNewConfig.stepList.one.title', this.options),
        descList: [
          { type: 'text', content: this.i18n.transform('agentNewConfig_split1', this.options) },
          { type: 'link', content: this.i18n.transform('agentNewConfig_split2', this.options), clickData: 'create' },
          { type: 'text', content: this.i18n.transform('agentNewConfig_split3', this.options) },
        ],
        icon: 'agent-new-1.svg',
      },
      {
        title: this.i18n.transform('agentNewConfig.stepList.two.title', this.options),
        desc: this.i18n.transform('agentNewConfig.stepList.two.desc', this.options),
        icon: 'agent-new-2.svg',
      },
      {
        title: this.i18n.transform('agentNewConfig.stepList.three.title', this.options),
        desc: this.i18n.transform('agentNewConfig.stepList.three.desc', this.options),
        icon: 'agent-new-3.svg',
      },
    ],
  };
  public agentConfig = {
    title: this.i18n.transform('agentConfig.title', this.options),
    desc: this.i18n.transform('agentConfig.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('agentConfig.stepList.one.title', this.options),
        descList: [
          {
            type: 'text',
            content: this.i18n.transform('agentConfig_split1', this.options),
          },
          {
            type: 'link',
            content: this.i18n.transform('agentConfig_split2', this.options),
            clickData: 'create',
          },
          {
            type: 'text',
            content: this.i18n.transform('agentConfig_split3', this.options),
          },
        ],
        icon: 'agent-1.svg',
      },
      {
        title: this.i18n.transform('agentConfig.stepList.two.title', this.options),
        desc: this.i18n.transform('agentConfig.stepList.two.desc', this.options),
        icon: 'agent-2.svg',
      },
      {
        title: this.i18n.transform('agentConfig.stepList.three.title', this.options),
        desc: this.i18n.transform('agentConfig.stepList.three.desc', this.options),
        icon: 'agent-3.svg',
      },
      {
        title: this.i18n.transform('agentConfig.stepList.four.title', this.options),
        desc: this.i18n.transform('agentConfig.stepList.four.desc', this.options),
        icon: 'agent-4.svg',
      },
    ],
  };
  public workflowConfig = {
    title: this.i18n.transform('workflowConfig.title', this.options),
    desc: this.i18n.transform('workflowConfig.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('workflowConfig.stepList.one.title', this.options),
        descList: [
          {
            type: 'text',
            content: this.i18n.transform('agentConfig_split1', this.options),
          },
          {
            type: 'link',
            content: this.i18n.transform('workflowConfig_split2', this.options),
            clickData: 'create',
          },
          {
            type: 'text',
            content: this.i18n.transform('workflowConfig_split3', this.options),
          },
        ],
        icon: 'workflow-1.svg',
      },
      {
        title: this.i18n.transform('workflowConfig.stepList.two.title', this.options),
        desc: this.i18n.transform('workflowConfig.stepList.two.desc', this.options),
        icon: 'workflow-2.svg',
      },
      {
        title: this.i18n.transform('workflowConfig.stepList.three.title', this.options),
        desc: this.i18n.transform('workflowConfig.stepList.three.desc', this.options),
        icon: 'workflow-3.svg',
      },
      {
        title: this.i18n.transform('workflowConfig.stepList.four.title', this.options),
        desc: this.i18n.transform('workflowConfig.stepList.four.desc', this.options),
        icon: 'workflow-4.svg',
      },
    ],
  };

  public multiConfig = {
    title: this.i18n.transform('multiConfig.title', this.options),
    desc: this.i18n.transform('multiConfig.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('multiConfig.stepList.one.title', this.options),
        descList: [
          {
            type: 'text',
            content: this.i18n.transform('agentConfig_split1', this.options),
          },
          {
            type: 'link',
            content: this.i18n.transform('multiConfig_split2', this.options),
            clickData: 'create',
          },
          {
            type: 'text',
            content: this.i18n.transform('multiConfig_split3', this.options),
          },
        ],
        icon: 'multi-1.svg',
      },
      {
        title: this.i18n.transform('multiConfig.stepList.two.title', this.options),
        desc: this.i18n.transform('multiConfig.stepList.two.desc', this.options),
        icon: 'multi-2.svg',
      },
      {
        title: this.i18n.transform('multiConfig.stepList.three.title', this.options),
        desc: this.i18n.transform('multiConfig.stepList.three.desc', this.options),
        icon: 'multi-3.svg',
      },
      {
        title: this.i18n.transform('multiConfig.stepList.four.title', this.options),
        desc: this.i18n.transform('multiConfig.stepList.four.desc', this.options),
        icon: 'multi-4.svg',
      },
    ],
  };
  public mcpConfig = {
    title: this.i18n.transform('mcpConfig.title', this.options),
    desc: this.i18n.transform('mcpConfig.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('mcpConfig.stepList.one.title', this.options),
        desc: this.i18n.transform('mcpConfig.stepList.one.desc', this.options),
        icon: 'mcp-1.svg',
      },
      {
        title: this.i18n.transform('mcpConfig.stepList.two.title', this.options),
        desc: this.i18n.transform('mcpConfig.stepList.two.desc', this.options),
        icon: 'mcp-2.svg',
      },
    ],
  };
  public pluginConfig = {
    title: this.i18n.transform('pluginConfig.title', this.options),
    desc: this.i18n.transform('pluginConfig.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('pluginConfig.stepList.one.title', this.options),
        desc: this.i18n.transform('pluginConfig.stepList.one.desc', this.options),
        icon: 'plugin-1.svg',
      },
      {
        title: this.i18n.transform('pluginConfig.stepList.two.title', this.options),
        desc: this.i18n.transform('pluginConfig.stepList.two.desc', this.options),
        icon: 'plugin-2.svg',
      },
      {
        title: this.i18n.transform('pluginConfig.stepList.three.title', this.options),
        desc: this.i18n.transform('pluginConfig.stepList.three.desc', this.options),
        icon: 'plugin-3.svg',
      },
    ],
  };
  public promptConfig = {
    title: this.i18n.transform('promptConfig.title', this.options),
    desc: this.i18n.transform('promptConfig.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('promptConfig.stepList.one.title', this.options),
        descList: [
          {
            type: 'text',
            content: this.i18n.transform('agentConfig_split1', this.options),
          },
          {
            type: 'link',
            content: this.i18n.transform('promptConfig_split2', this.options),
            clickData: 'create',
          },
          {
            type: 'text',
            content: this.i18n.transform('promptConfig_split3', this.options),
          },
          {
            type: 'link',
            content: this.i18n.transform('promptConfig_split4', this.options),
            clickData: 'import',
          },
          {
            type: 'text',
            content: this.i18n.transform('promptConfig_split5', this.options),
          },
        ],
        icon: 'prompt-1.svg',
      },
      {
        title: this.i18n.transform('promptConfig.stepList.two.title', this.options),
        descList: [
          {
            type: 'text',
            content: this.i18n.transform('promptConfig_split6', this.options),
          },
          {
            type: 'link',
            content: this.i18n.transform('promptConfig_split7', this.options),
            clickData: 'createTask',
          },
          {
            type: 'text',
            content: this.i18n.transform('promptConfig_split8', this.options),
          },
        ],
        icon: 'prompt-2.svg',
      },
      {
        title: this.i18n.transform('promptConfig.stepList.three.title', this.options),
        descList: [
          {
            type: 'text',
            content: this.i18n.transform('promptConfig_split9', this.options),
          },
          {
            type: 'link',
            content: this.i18n.transform('promptConfig_split10', this.options),
            clickData: 'taskList',
          },
        ],
        icon: 'prompt-3.svg',
      },
    ],
  };

  public knowledgeConfig = {
    title: this.i18n.transform('knowledgeConfig.title', this.options),
    desc: this.i18n.transform('knowledgeConfig.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('knowledgeConfig.stepList.one.title', this.options),
        desc: this.i18n.transform('knowledgeConfig.stepList.one.desc', this.options),
        icon: 'knowledge-1.svg',
      },
      {
        title: this.i18n.transform('knowledgeConfig.stepList.two.title', this.options),
        desc: this.i18n.transform('knowledgeConfig.stepList.two.desc', this.options),
        icon: 'knowledge-2.svg',
      },
      {
        title: this.i18n.transform('knowledgeConfig.stepList.three.title', this.options),
        desc: this.i18n.transform('knowledgeConfig.stepList.three.desc', this.options),
        icon: 'knowledge-3.svg',
      },
    ],
  };
  public cardConfig = {
    title: this.i18n.transform('cardConfig.title', this.options),
    desc: this.i18n.transform('cardConfig.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('cardConfig.stepList.one.title', this.options),
        desc: this.i18n.transform('cardConfig.stepList.one.desc', this.options),
        icon: 'card-1.svg',
      },
      {
        title: this.i18n.transform('cardConfig.stepList.two.title', this.options),
        desc: this.i18n.transform('cardConfig.stepList.two.desc', this.options),
        icon: 'card-2.svg',
      },
      {
        title: this.i18n.transform('cardConfig.stepList.three.title', this.options),
        desc: this.i18n.transform('cardConfig.stepList.three.desc', this.options),
        icon: 'card-3.svg',
      },
      {
        title: this.i18n.transform('cardConfig.stepList.four.title', this.options),
        desc: this.i18n.transform('cardConfig.stepList.four.desc', this.options),
        icon: 'card-4.svg',
      },
    ],
  };
  public modelConfig = {
    title: this.i18n.transform('customManage.title', this.options),
    desc: this.i18n.transform('customManage.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('customManage.stepList.one.title', this.options),
        desc: this.i18n.transform('customManage.stepList.one.desc', this.options),
        icon: 'model-1.svg',
      },
      {
        title: this.i18n.transform('customManage.stepList.two.title', this.options),
        desc: this.i18n.transform('customManage.stepList.two.desc', this.options),
        icon: 'model-2.svg',
      },
      {
        title: this.i18n.transform('customManage.stepList.three.title', this.options),
        desc: this.i18n.transform('customManage.stepList.three.desc', this.options),
        icon: 'model-3.svg',
      },
    ],
  };
  public policyConfig = {
    title: this.i18n.transform('routeManage.title', this.options),
    desc: this.i18n.transform('routeManage.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('routeManage.stepList.one.title', this.options),
        desc: this.i18n.transform('routeManage.stepList.one.desc', this.options),
        icon: 'policy-1.svg',
      },
      {
        title: this.i18n.transform('routeManage.stepList.two.title', this.options),
        desc: this.i18n.transform('routeManage.stepList.two.desc', this.options),
        icon: 'policy-2.svg',
      },
      {
        title: this.i18n.transform('routeManage.stepList.three.title', this.options),
        desc: this.i18n.transform('routeManage.stepList.three.desc', this.options),
        icon: 'policy-3.svg',
      },
    ],
  };
  public intentConfig = {
    title: this.i18n.transform('intentManage.title', this.options),
    desc: this.i18n.transform('intentManage.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('intentManage.stepList.one.title', this.options),
        desc: this.i18n.transform('intentManage.stepList.one.desc', this.options),
        icon: 'intent-1.svg',
      },
      {
        title: this.i18n.transform('intentManage.stepList.two.title', this.options),
        desc: this.i18n.transform('intentManage.stepList.two.desc', this.options),
        icon: 'intent-2.svg',
      },
    ],
  };
  public messageConfig = {
    title: this.i18n.transform('messageTemplate.title', this.options),
    desc: this.i18n.transform('messageTemplate.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('messageTemplate.stepList.one.title', this.options),
        desc: this.i18n.transform('messageTemplate.stepList.one.desc', this.options),
        icon: 'message-1.svg',
      },
      {
        title: this.i18n.transform('messageTemplate.stepList.two.title', this.options),
        desc: this.i18n.transform('messageTemplate.stepList.two.desc', this.options),
        icon: 'message-2.svg',
      },
      {
        title: this.i18n.transform('messageTemplate.stepList.three.title', this.options),
        desc: this.i18n.transform('messageTemplate.stepList.three.desc', this.options),
        icon: 'message-3.svg',
      },
    ],
  };
  public objectConfig = {
    title: this.i18n.transform('objectManage.title', this.options),
    desc: this.i18n.transform('objectManage.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('objectManage.stepList.one.title', this.options),
        desc: this.i18n.transform('objectManage.stepList.one.desc', this.options),
        icon: 'object-1.svg',
      },
      {
        title: this.i18n.transform('objectManage.stepList.two.title', this.options),
        desc: this.i18n.transform('objectManage.stepList.two.desc', this.options),
        icon: 'object-2.svg',
      },
    ],
  };
  public evaluationSet = {
    title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_set.title'),
    desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_set.desc'),
    stepList: [
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_set.step.one.title'),
        desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_set.step.one.desc'),
        icon: 'evaluation-set-1.svg',
      },
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_set.step.two.title'),
        desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_set.step.two.desc'),
        icon: 'evaluation-set-2.svg',
      },
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_set.step.three.title'),
        desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_set.step.three.desc'),
        icon: 'evaluation-set-3.svg',
      },
    ],
  };
  public skillConfig = {
    title: this.coreI18n.transform('skill.guide.title'),
    desc: this.coreI18n.transform('skill.guide.desc'),
    stepList: [
      {
        title: this.coreI18n.transform('skill.guide.step1.title'),
        desc: this.coreI18n.transform('skill.guide.step1.des'),
        icon: 'skill-new-1.svg',
      },
      {
        title: this.coreI18n.transform('skill.guide.step2.title'),
        desc: this.coreI18n.transform('skill.guide.step2.des'),
        icon: 'skill-new-2.svg',
      },
    ],
  };

  public evaluationTask = {
    title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.title'),
    desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.desc'),
    stepList: [
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.step.one.title'),
        descList: [
          {
            type: 'text',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.click'),
          },
          {
            type: 'link',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.step.one.title'),
            clickData: EvaluationTab.EVALUATION_SET_TAB,
          },
          {
            type: 'text',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.step.one.desc'),
          },
        ],
        icon: 'evaluation-task-1.svg',
      },
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.step.two.title'),
        descList: [
          {
            type: 'text',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.click'),
          },
          {
            type: 'link',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.step.two.title'),
            clickData: EvaluationTab.EVALUATOR_TAB,
          },
          {
            type: 'text',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.step.two.desc'),
          },
        ],
        icon: 'evaluation-task-2.svg',
      },
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.step.three.title'),
        descList: [
          {
            type: 'text',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.click'),
          },
          {
            type: 'link',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.step.three.title'),
            clickData: EvaluationTab.EVALUATION_TASK_TAB,
          },
          {
            type: 'text',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluation_task.step.three.desc'),
          },
        ],
        icon: 'evaluation-task-3.svg',
      },
    ],
  };

  public tagMgr = {
    title: this.coreI18n.transform('evaluation.newbie_guide.nodata.tag-mgr.title'),
    desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.tag-mgr.desc'),
    stepList: [
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.tag-mgr.step.one.title'),
        descList: [
          {
            type: 'text',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.tag-mgr.click'),
          },
          {
            type: 'link',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.tag-mgr.step.one.title'),
            clickData: EvaluationTab.TAG_MGR_TAB,
          },
          {
            type: 'text',
            content: this.coreI18n.transform('evaluation.newbie_guide.nodata.tag-mgr.step.one.desc'),
          },
        ],
        icon: 'tag-mgr-1.svg',
      },
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.tag-mgr.step.two.title'),
        desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.tag-mgr.step.two.desc'),
        icon: 'tag-mgr-2.svg',
      },
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.tag-mgr.step.three.title'),
        desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.tag-mgr.step.three.desc'),
        icon: 'tag-mgr-3.svg',
      },
    ],
  };

  public evaluator = {
    title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluator.title'),
    desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluator.desc'),
    stepList: [
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluator.step.one.title'),
        desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluator.step.one.desc'),
        icon: 'evaluator-1.svg',
      },
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluator.step.two.title'),
        desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluator.step.two.desc'),
        icon: 'evaluator-2.svg',
      },
      {
        title: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluator.step.three.title'),
        desc: this.coreI18n.transform('evaluation.newbie_guide.nodata.evaluator.step.three.desc'),
        icon: 'evaluator-3.svg',
      },
    ],
  };

  public optimizeConfig = {
    title: this.i18n.transform('optimizeConfig.title', this.options),
    desc: this.i18n.transform('', this.options),
    stepList: [
      {
        title: this.i18n.transform('optimizeConfig.stepList.one.title', this.options),
        descList: [
          {
            type: 'text',
            content: this.i18n.transform('agentConfig_split1', this.options),
          },
          {
            type: 'link',
            content: this.i18n.transform('optimizeConfig_split2', this.options),
            clickData: 'create',
          },
          {
            type: 'text',
            content: this.i18n.transform('optimizeConfig_split3', this.options),
          },
        ],
        icon: 'optimize-1.svg',
      },
      {
        title: this.i18n.transform('optimizeConfig.stepList.two.title', this.options),
        desc: this.i18n.transform('optimizeConfig.stepList.two.desc', this.options),
        icon: 'optimize-2.svg',
      },
      {
        title: this.i18n.transform('optimizeConfig.stepList.three.title', this.options),
        desc: this.i18n.transform('optimizeConfig.stepList.three.desc', this.options),
        icon: 'optimize-3.svg',
      },
    ],
  };

  public sandboxConfig = {
    title: this.i18n.transform('sandbox.guide.title', this.options),
    desc: this.i18n.transform('sandbox.guide.desc', this.options),
    stepList: [
      {
        title: this.i18n.transform('sandbox.guide.step1.title'),
        desc: this.i18n.transform('sandbox.guide.step1.des'),
        icon: 'sandbox-1.svg',
      },
      {
        title: this.i18n.transform('sandbox.guide.step2.title'),
        desc: this.i18n.transform('sandbox.guide.step2.des'),
        icon: 'sandbox-2.svg',
      },
      {
        title: this.i18n.transform('sandbox.guide.step3.title'),
        desc: this.i18n.transform('sandbox.guide.step3.des'),
        icon: 'sandbox-3.svg',
      },
    ],
  };
  public memoryLib = {
    title: this.i18n.transform('memory.create.guide.title', this.options),
    desc: this.i18n.transform('memory.create.guide.describe', this.options),
    stepList: [
      {
        title: this.i18n.transform('memory.create.guide.step1.title', this.options),
        desc: this.i18n.transform('memory.create.guide.step1.des', this.options),
        icon: 'memory-task-1.svg',
      },
      {
        title: this.i18n.transform('memory.create.guide.step2.title', this.options),
        desc: this.i18n.transform('memory.create.guide.step2.des', this.options),
        icon: 'memory-task-2.svg',
      },
    ],
  };

  guideConfigMap = new Map([
    ['agent', this.agentConfig],
    ['workflow', this.workflowConfig],
    ['controller', this.multiConfig],
    ['plugin', this.pluginConfig],
    ['mcp', this.mcpConfig],
    ['prompt', this.promptConfig],
    ['knowledge', this.knowledgeConfig],
    ['card', this.cardConfig],
    ['routeModel', this.modelConfig],
    ['routePolicy', this.policyConfig],
    ['intentPackage', this.intentConfig],
    ['messageTem', this.messageConfig],
    ['objectTem', this.objectConfig],
    ['agent_new', this.agentNewConfig],
    ['skill', this.skillConfig],
    ['sandbox', this.sandboxConfig],
    [EvaluationTab.EVALUATION_TASK_TAB, this.evaluationTask],
    [EvaluationTab.EVALUATION_SET_TAB, this.evaluationSet],
    [EvaluationTab.EVALUATOR_TAB, this.evaluator],
    [EvaluationTab.TAG_MGR_TAB, this.tagMgr],
    ['optimize', this.optimizeConfig],
    ['memoryLib', this.memoryLib],
  ]);

  constructor(
    public i18n: I18NextEagerPipe,
    private coreI18n: I18nService
  ) {
    this.guideConfig = null;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.type) {
      this.guideConfig = null;
      const guideConfig = this.guideConfigMap.get(this.type);
      this.guideConfig = deepClone(guideConfig);
      this.guideConfig.stepList.forEach(item => (item.icon = this.getIcon(item.icon)));
    }
  }

  ngOnInit(): void {}

  public clickHandler(e) {
    this.clickDesc.emit(e);
  }

  // 处理步骤显示
  public getStep(index) {
    return this.i18n.transform('guide_step', { index });
  }

  public getIcon(name) {
    return cdnAssetUrl(`${this.baseUrl}/${this.isSmall ? 'small' : 'large'}/${name}`);
  }
}
