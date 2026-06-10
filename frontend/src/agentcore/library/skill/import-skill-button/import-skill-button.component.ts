import { IMPORT_TYPE } from '@agentcore/constants/skill';
import { I18nService } from '@agentcore/core/i18n.service';
import { I18nPipe } from '@agentcore/shared/pipes/i18n.pipe';
import { CoreUtilService } from '@agentcore/shared/services/core-util.service';
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MODULES } from '@shared/modules';
import { NzModalService } from 'ng-zorro-antd/modal';
import { ImportSkillModalComponent } from '../import-skill-modal/import-skill-modal.component';
import { SkillCommonService } from '../services/skill.common.service';
@Component({
  selector: 'meta-import-skill-button',
  templateUrl: './import-skill-button.component.html',
  styleUrls: ['./import-skill-button.component.scss'],
  standalone: true,
  imports: [MODULES, CommonModule, I18nPipe],
  providers: [I18nService],
})
export class ImportSkillButtonComponent implements OnInit {
  @Input() type: 'default' | 'danger' | 'primary' = 'default';
  @Input() source: 'skill' | 'agent' = 'skill';
  // 区分使用场景，skill为列表页，agent为单智能体
  @Output() importCompleted = new EventEmitter<any>();
  importType = IMPORT_TYPE;

  public butDisabled = false;
  constructor(
    private nzModal: NzModalService,
    private skillCommonService: SkillCommonService,
    private coreUtilService: CoreUtilService
  ) {
    this.coreUtilService.subscriptionCallback(sub => {
      this.butDisabled = !sub;
    });
  }

  ngOnInit() { }

  importZip(importType: IMPORT_TYPE) {
    const thisNzModal: any = this.nzModal.create<ImportSkillModalComponent, any>({
      nzContent: ImportSkillModalComponent,
      nzWidth: '700px',
    });
    const instance = thisNzModal.getContentComponent();
    instance.importType = importType;
    instance.importSuccess.subscribe(() => {
      if (this.source === 'skill') {
        this.skillCommonService.refreshSkillSub.next(true);
      } else {
        this.importCompleted.emit();
      }
    });
  }
}
