import { ServiceId } from '@agentcore/constants/request';
import { BaseApi } from '@agentcore/core/base.api';
import { environment } from 'src/environment/environment';

export class JiuwenBaseApi extends BaseApi {
  override serviceId: string = environment.serviceId || ServiceId.JIUWEN;

  override baseQuery: Record<string, any> = {
    workspace_id: '{workspaceId}',
  };
}
