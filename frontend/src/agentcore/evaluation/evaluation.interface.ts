/**
 * 评估标签页枚举
 */
export enum EvaluationTab {
  EVALUATION_SET_TAB = 'evaluation_set_tab',
  EVALUATOR_TAB = 'evaluator_tab',
  EVALUATION_TASK_TAB = 'evaluation_task_tab',
  TAG_MGR_TAB = 'tag_mgr_tab',
}

export type TabKey = EvaluationTab;

/**
 * 活跃标签页变更回调函数类型
 */
export type ActiveTabChange = (isActive: boolean, tab: Tab) => void;

export interface Steps {
  id: number;
  index: string;
  title: string;
  description: string;
  imageUrl: string;
}

export interface Tab {
  id: TabKey;
  title: string;
  active: boolean;
  label: string;
  steps?: Steps[];
  alert?: {
    type: 'success' | 'error' | 'warn' | 'prompt' | 'simple';
    content: string;
  };
  onActiveChange: ActiveTabChange;
}
