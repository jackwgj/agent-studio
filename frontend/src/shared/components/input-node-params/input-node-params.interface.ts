import { FileItem } from "@routes/agent-center/multiUpload.utils";
import { IInputItem } from "@routes/agent-center/types/common.types";

interface inputUploadData extends FileItem {
  isHover?: boolean;
}

export interface InputParamConfig extends IInputItem {
  isEmpty?: boolean;
  value?: any;
  file?: File;
  uploadDatas?: inputUploadData[];
  uploadData?: {
    name?: string;
    progress?: string;
  };
}

export interface InputChatItem {
  dialogId?: string;
}