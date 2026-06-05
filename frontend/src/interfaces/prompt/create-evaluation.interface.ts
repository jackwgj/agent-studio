export interface IQueryCondition {
  name?: string;
  source?: string;
  model?: string;
  task_id: string;
  type?: number;
}
export interface IEvaluationTaskInfo {
  id?: string;
  name: string;
  method: string;
  task_id: string;
  test_set_id: string;
  description: string;
  prompt_ids: string[];
  regular_expression?: string;
  eval_model_config?: {
    endpoint: string;
    project_id: string;
    deployment_id: string;
    model_source: string;
  };
}
export interface IPromptDatasets {
  dataset_type: string;
  offset: number;
  limit: number;
}
