/** 记忆库策略类型  */
export enum MemoryStrategyType {
  SEMANTIC_MEMORY = 'semantic_memory',
  USER_PROFILE = 'user_profile',
}

/** 记忆库表格列  */
export enum MemoryLibColKey {
  MEMORY_REPO_ID = 'memory_repo_id',
  WORKSPACE_ID = 'workspace_id',
  NAME = 'name',
  DESCRIPTION = 'description',
  STRATEGY = 'strategy',
  CREATED_USER_ID = 'created_user_id',
  CREATED_USER_NAME = 'created_user_name',
  CREATE_TIME = 'create_time',
  LAST_UPDATE_USER_ID = 'last_update_user_id',
  LAST_UPDATE_USER_NAME = 'last_update_user_name',
  UPDATE_TIME = 'update_time',
}

/** 记忆库引用资源类型  */
export enum MemoryLibReferenceType {
  AGENT = 'agent',
  WORKFLOW = 'workflow',
  CONTROLLER = 'controller',
}

/** 卡片模式操作类型  */
export enum MemoryLibCardOperationType {
  OPERATION = 'operation',
}

/** 记忆库选择组件的提示展示形式  */
export enum MemoryLibSelectorTipType {
  ICON = 'icon',
  DESCRIPTION = 'description',
}
