/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable @typescript-eslint/naming-convention */
export type UploadStatus = 'ready' | 'uploading' | 'success' | 'fail';

export interface UploadRawFile extends File {
  uid: number;
}

export interface DocType extends UploadFile {
  knowledgeId?: string;
  fileSize?: string;
}

export interface LinkInfo {
  value?: any;
  knowledgeName: string;
  url: string;
  status: 'ready' | 'uploading' | 'success' | 'fail';
  id: string;
}

export interface UploadFile {
  name: string;
  percentage?: number;
  status: UploadStatus;
  size?: number;
  response?: unknown;
  uid: number;
  url?: string;
  raw?: UploadRawFile;
  _file?: UploadRawFile;
}

export interface FileListType {
  name: string;
  size: string;
}

export interface knowledgeContent {
  knowledgeId?: string;
  knowledgeName: string;
  knowledgeCount: number;
  status?: string;
}
