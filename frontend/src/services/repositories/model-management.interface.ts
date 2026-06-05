interface ModelSquareDetail {
  id: string;
  provider_id: string;
  service_name: string;
  service_key: string;
  model_name: string;
  logo: string;
  is_reasoning: boolean;
  is_support_close_reasoning: boolean;
  model_type: string;
  model_deploy_type: string;
  domain_id: string;
  project_id: string;
  workspace_id: string;
  created_by_user: string;
  created_date: number;
  last_updated_date: number;
  api_url: string;
  is_support_function: boolean;
  interface_protocol: string;
  auth_metadata_id: string;
  publish_status: string;
  is_support_stream: boolean;
  throttling_policy: number;
  status: string;
  is_subscribed: boolean;
  context_length: string;
  is_in_free_trial: boolean;
  template_id: string;
  is_support_deep_thinking: boolean;
  resident_model_id: string;
}

export interface ModelSquareListRes {
  total: number;
  data: Array<ModelSquareDetail>;
  all_model_type: string[];
  all_model_series: string[];
}