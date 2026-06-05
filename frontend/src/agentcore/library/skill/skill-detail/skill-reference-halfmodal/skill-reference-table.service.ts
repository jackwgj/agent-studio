import { inject } from '@angular/core';
import { SkillApi } from '@agentcore/api/skill.api';
import { I18nService } from '@agentcore/core/i18n.service';
import { CoreUtilService } from '@agentcore/shared/services/core-util.service';
export class SkillReferenceTableService {
  skillId;
  _skillApi: SkillApi = inject(SkillApi);
  _i18n: I18nService = inject(I18nService);
  _coreUtilService: CoreUtilService = inject(CoreUtilService);
}
