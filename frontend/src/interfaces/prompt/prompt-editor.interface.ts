import { EChunkType, EVarSyncSrc } from '@enums/prompt-editor.enum';
import { IModel } from './model.interface';
import { IFileInfo } from '@interfaces/prompt/candidate-template-list.interface';

export interface IPromptChunk {
  type: EChunkType;
  value: string;

  /**
   * 仅 EChunkType 为 VARIANT 时才具有该属性,
   * 表示当前 VARIANT 在所有 VARIANT 中的顺序
   */
  pos?: number;
}

export interface FileItem {
  type?: string;
  progress?: string;
  name?: string;
  url?: string;
  file?: File;
  fileId?: string;
  isImage?: boolean;
  controller?: AbortController;
}

export interface IVariant {
  key: string;
  value: string;
  name: string;
}

export interface ITmplPrompt {
  prompt: string;
  variants: IVariant[];
  answer?: string;
  file?: FileItem;
}

/** 变量同步信息 */
export interface IVarSyncInfo {
  src: EVarSyncSrc;
  variant: IVariant;
}

export interface IWritingHistory {
  id: string;
  timestamp: string;
  modelInfo: IModel;
  answer: string;
  prompt: string;
  variants: IVariant[];
  file: FileItem;
}
