import { Component, Input, OnInit } from '@angular/core';

import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzTypographyModule } from 'ng-zorro-antd/typography';

import { MODULES } from '@shared/modules';
import { SkillSource, SkillStatus } from '@enums/skill.enum';
import { I18nService } from '@agentcore/core/i18n.service';
import { I18nPipe } from '@agentcore/shared/pipes/i18n.pipe';
import { PipesModule } from 'src/pipes/pipes.module';

@Component({
  selector: 'app-skill-card',
  templateUrl: './skill-card.component.html',
  styleUrls: ['./skill-card.component.scss'],
  standalone: true,
  imports: [MODULES, I18nPipe, PipesModule, NzIconModule, NzTagModule, NzToolTipModule, NzTypographyModule],
  providers: [I18nService],
})
export class SkillCardComponent implements OnInit {
  @Input() skillItem: any = {};

  statusMap = {
    [SkillStatus.DEVELOPED]: this.i18n.transform('skill.detail.statusTag.developed'),
    [SkillStatus.DEVELOPING]: this.i18n.transform('skill.detail.statusTag.developing'),
  };
  sourceMap = {
    [SkillSource.CUSTOM]: this.i18n.transform('skill.detail.sourceTag.custom'),
    [SkillSource.IMPORT]: this.i18n.transform('skill.list.card.outImport'),
  };

  get isDeveloping() {
    return this.skillItem?.status === SkillStatus.DEVELOPING;
  }

  constructor(private i18n: I18nService) {}

  ngOnInit(): void {}

  ngOnDestroy() {}
}
