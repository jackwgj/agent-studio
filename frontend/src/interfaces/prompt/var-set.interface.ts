export interface IVarSetRes {
  datasets: IDataset[];
}

export interface IDataset {
  description: string;
  dataset_id: string;
  dataset_name: string;
  dataset_type: string;
  project_id: string;
  obs_path: string;
  create_time?: string;
}

export interface IVarSet {
  [key: string]: string;
}

export interface IDatasetRes {
  dataset_values: IVarSet[];
}
