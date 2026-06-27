import { ValidatorFn } from '@angular/forms';

export const MAX_GUIDE_COUNT = 100;

export const LEAST_GUIDE_COUNT = 1;

export const LEAST_DEPENDENCY_GUIDE_COUNT = 2

export enum SCENARIO_FIELD_ID {
  SCENE_NAME = 'sceneName',
  SCENE_DESC = 'sceneDesc',
  GUIDE_CONFIG_LIST = 'guideConfigList',
  TRIGGER_CONDITION = 'triggerCondition',
  PERFORM_ACTION = 'performAction',
  SKILL_SET = 'skillSet',
  COLLAPSE = 'collapse',
  DEPENDENCY_LIST = 'dependencyList',
  DEPEND_ITEM = 'dependItem',
  DEPEND_SYMBOL = 'dependSymbol',
  BE_DEPENDED_ITEM = 'beDependedItem',
}

export enum FIELD_TYPE {
  TEXT_INPUT = 'textInput',
  SELECT = 'select',
}

export enum SKILL_TYPE {
  PLUGIN = 'tool', // 插件
  WORK_FLOW = 'workflow', // 工作流
  MCP = 'mcp', // mcp
}

export const SCENE_NAME_MAX_LEN = 64;
export const SCENE_DESC_MAX_LEN = 256;
export const TRIGGER_CON_MAX_LEN = 256;
export const PERFORM_ACTION_MAX_LEN = 1000;

export interface FormConfig {
  [key: string]: {
    id: SCENARIO_FIELD_ID;
    label?: string;
    required?: boolean;
    validation?: any;
    validators?: ValidatorFn[];
    defaultValue?: any;
    children?: FormConfig;
    helpTip?: string;
    errorTip?: string;
    type?: FIELD_TYPE;
  };
}

export interface SkillSet {
  mcpServersAdded?: ISkillSet;
  pluginAdded?: ISkillSet;
  flowAdded?: ISkillSet;
}

export interface ISkillSet {
  list: Array<any>;
  title: string;
}

export interface ISkillOption {
  label: string;
  skillType?: SKILL_TYPE;
  children: ISkillChildrenOption[];
}

export interface ISkillChildrenOption {
  id: string;
  label: string;
  avatar: string;
  disabled: boolean;
  skillType: SKILL_TYPE;
  name?: string;
}

export interface ISceneData {
  id?: string;
  name: string;
  description?: string;
  guidelines: IGuide[];
  relations?: IRelation[];
  sceneExistToolInvalid?: boolean;
  isNoTool?: boolean;
}

export interface ITool {
  id: string;
  name: string;
  type: string;
  skillOption?: ISkillChildrenOption;
}

export interface IGuide {
  id: number;
  condition: string;
  action: string;
  tools: ITool[];
  deletedSkills?: ITool[];
  effectiveSkills?: ISkillChildrenOption[];
  guideExistToolInvalid?: boolean;
  isNoTool?: boolean;
}

export interface IRelation {
  type: string;
  src: number;
  tgt: number;
}
