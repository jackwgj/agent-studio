export interface ProviderInfo {
  auth_config_status: string;
  auth_configs?: Array<any>;
  created_date: number;
  description: string;
  id: string;
  last_updated_date: number;
  logo: string;
  provider_name: string;
  provider_name_en: string;
  provider_url?: string;
  tags?: string;
  auth_url?:string;
}
