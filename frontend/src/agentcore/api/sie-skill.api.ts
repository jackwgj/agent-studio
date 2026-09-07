  import { Injectable } from '@angular/core';

  import { catchError, map, Observable, of } from 'rxjs';

  import { ServiceId } from '@agentcore/constants/request';
  import { SkillDetailRes, SkillListRes, SkillVersionDetail, SkillVersionListRes } from '@agentcore/constants/skill.model';
  import { customPropTransfer } from '@agentcore/shared/decorators/prop';
  import { MessageConfig } from '@agentcore/types/request';

  import { JiuwenBaseApi } from './jiuwen-base.api';

  @Injectable({
    providedIn: 'root',
  })
  export class SIESkillApi extends JiuwenBaseApi {
    override serviceId = (window as any).iamTargetId || ServiceId.JIUWEN;
    prefix = '/v1/{projectId}/agent-manager/sie/skills';


    querySkillList(params = {}): Observable<any> {
      return this.request.get({
        url: `${this.prefix}`,
        params,
      } as any);
    }
    querySkillDetail(skillId: string, messageConfig?: MessageConfig) {
      return this.request
        .get(
          {
            url: `${this.prefix}/${skillId}`,
          } as any,
          messageConfig
        )
        .pipe(map(res => customPropTransfer(res, SkillDetailRes).skillInfo));
    }

    queryReference(skillId: string, offset = 0, limit = 100, messageConfig?: MessageConfig) {
      return this.request
        .get({
          url: this.isHc() ? `/v1/studio/resources/${skillId}/referenced-by` : `/v1/{projectId}/agent-manager/relation/resources/${skillId}`,
          params: {
            offset,
            limit,
            resource_type: 'skill',
          },
          messageConfig,
        } as any)
        .pipe(catchError(() => of({ relations: [] })));
    }

    queryResourceSkill({ offset, limit, tagId, name, description }: { offset: number; limit: number; tagId?: string; name?: string; description?: string }) {
      return this.request
        .get({
          url: this.prefix,
          params: {
            offset,
            limit,
            tag_id: tagId,
            name: name || undefined,
            description,
            published_asset: 1,
          },
        } as any)
        .pipe(map(res => customPropTransfer(res, SkillListRes)));
    }

    // 获取自动推荐的skill
    getSkillResource(params: any, agent_id: string) {
      return this.request.post({
        url: this.isHc() ? `/v1/studio/agents/${agent_id}/resources` : `/v1/{projectId}/agent-manager/agents/${agent_id}/resources?workspace_id={workspaceId}`,
        params,
      } as any);
    }
  }
