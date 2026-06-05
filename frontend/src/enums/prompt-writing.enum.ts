export enum EWritingStatus {
  /** 撰写 */
  WRITING = 'WRITING',

  /** 候选编辑(write) */
  WCANDIDATE = 'WCANDIDATE',

  /** 候选查看(read) */
  RCANDIDATE = 'RCANDIDATE',
}

export enum ECandSaveType {
  SAVE = 'SAVE',
  SAVE_AS = 'SAVE_AS',
}
