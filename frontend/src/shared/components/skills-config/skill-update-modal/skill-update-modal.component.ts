import { Component, Input, OnInit } from '@angular/core';

import { NzIconModule } from 'ng-zorro-antd/icon';

import { MODULES } from '@shared/modules';
import { I18nService } from '@agentcore/core/i18n.service';
import { I18nPipe } from '@agentcore/shared/pipes/i18n.pipe';

@Component({
  selector: 'app-skill-update-modal',
  templateUrl: './skill-update-modal.component.html',
  standalone: true,
  imports: [MODULES, I18nPipe, NzIconModule],
  providers: [I18nService],
})
export class SkillUpdateModalComponent implements OnInit {
  @Input() skillItem: any;

  constructor(public i18n: I18nService) {}

  ngOnInit(): void {}
}
