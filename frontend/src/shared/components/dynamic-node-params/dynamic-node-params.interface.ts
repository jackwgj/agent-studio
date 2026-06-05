import { IWorkflowField } from "@routes/agent-center/app-flow/node.type";

export interface DynamicNodeParam extends IWorkflowField{
  front_name?: string;
  controlValue?: any;
  file?: File;
  uploadData?: {
    url: string;
    name: string;
    progress: string;
  };
  uploadDatas?: Array<{ 
    url: string;
    fileId: string;
    name: string;
    progress: string;
    isHover: boolean;
  }>;
  isError?: boolean;
  isEmpty?: boolean;
}