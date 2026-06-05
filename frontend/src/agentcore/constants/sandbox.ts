import { signal } from '@angular/core';
import { customPropTransfer, prop } from '@agentcore/shared/decorators/prop';

class Tag {
  @prop()
  key: string;

  @prop()
  value?: string;
}

class Env {
  @prop()
  key: string;

  @prop()
  value: string;
}

class LogConfig {
  @prop()
  group_id: string;

  @prop()
  stream_id: string;
}

export class SandboxBasicInfo {
  // 工具id
  @prop()
  id: string;

  // 运行时 ID
  @prop()
  code_interpreter_id: string;

  // 工具名称
  @prop()
  name: string;

  // 工具描述
  @prop()
  description: string;

  // 是否启用日志服务
  @prop()
  lts_enable: boolean;

  // 创建时间
  @prop()
  created_at: string;

  // 更新时间
  @prop()
  updated_at: string;

  // 环境变量列表
  @prop({ instances: Env })
  envs: Env[];

  // 标签列表
  @prop({ instances: Tag })
  tags: Tag[];

  // 日志服务配置
  @prop({instances: LogConfig})
  lts_config: LogConfig;

  @prop()
  environment_id: string;

  @prop()
  environment_name: string;

  @prop()
  execution_role_agency: string;

  @prop()
  iam_name: string;

  @prop()
  auth_type: string;

  @prop()
  endpoint: string;

  @prop()
  access_mode: string;

  @prop()
  auth_key: string;

}

