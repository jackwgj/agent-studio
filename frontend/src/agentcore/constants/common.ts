import { HttpErrorResponse } from '@angular/common/http';

import { Observable } from 'rxjs';

export const AGENT_CORE = 'agentcore';

export const enum I18N_NAMESPACE {
  Common = 'common',
  Evaluation = 'evaluation',
  Skill = 'skill',
}

export const enum AppType {
  AGENT = 'agent',
  MULTI = 'multiagents',
  WORKFLOW = 'workflow',
  ALL = 'all',
}

export const enum SpanType {
  MODEL_SPAN = 'model',
  ROOT_SPAN = 'root',
}

export type CommonDeleteTableConfg = any;

export interface RowRelatedInfo {
  /**
   * 行数据标识符
   */
  uniqueId?: string;

  /**
   * 展示更多显示信息
   */
  sourceDisplay: string;

  err?: HttpErrorResponse;
}

export abstract class DeleteBaseParam {
  selectedList: any[];
  loadingText?: string;
  errorDesfn: (row) => RowRelatedInfo;
  finishCallBack?: (successList: RowRelatedInfo[], errorList: RowRelatedInfo[]) => void;
  afterErrorHandle?: (row, err?) => void;
}

export class DeleteMultiParam extends DeleteBaseParam {
  deleteFn: Observable<any>[];
}

export class LoadParam {
  successReq = 0;
  errorReq = 0;
  finish = false;
}
