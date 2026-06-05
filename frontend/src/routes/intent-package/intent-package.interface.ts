export interface PackageListResponse {
  count: number;
  intent_list: IntentInfo[];
}

export interface IntentInfo {
  branch_id?: string;
  name: string;
  examples?: string[];
}
