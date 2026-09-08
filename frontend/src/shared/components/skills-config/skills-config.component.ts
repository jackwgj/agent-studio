import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';

import { catchError, finalize, of, Subject, takeUntil } from 'rxjs';

import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { MessageComponent } from '@shared/services/cfdata.service';

import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { MODULES } from '@shared/modules';
import { SkillInputPromptService } from '@services/agent-center/skill/skill-input-prompt.service';
import { AddSkillHalfModalComponent } from '@shared/components/skills-config/add-skill-modal/add-skill-halfmodal.component';
import { SKILL_MAX_COUNT } from '@enums/skill.enum';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { SIESkillApi } from '@agentcore/api/sie-skill.api';
import { I18nService } from '@agentcore/core/i18n.service';
import { I18nPipe } from '@agentcore/shared/pipes/i18n.pipe';

import { SkillUpdateModalComponent } from './skill-update-modal/skill-update-modal.component';
import { AssetIntelligentAddComponent } from '../assets/asset-intelligent-add/asset-intelligent-add.component';

@Component({
  selector: 'skills-config',
  templateUrl: './skills-config.component.html',
  styleUrls: ['./skills-config.component.scss'],
  standalone: true,
  imports: [AddSkillHalfModalComponent, MODULES, AssetIntelligentAddComponent, I18nPipe, NzIconModule, NzToolTipModule, NzModalModule],
  providers: [I18nService, NzModalService],
})
export class SkillsConfigComponent implements OnInit {
  @Input() agentId = '';
  @Input() isFlowReadonly = false;
  @Input() selectedSkills = [];
  @Output() doTriggerSaveSkill = new EventEmitter<any>();
  @ViewChild(AddSkillHalfModalComponent) addSkillModal: any;

  // 技能集
  public skillsAdded: any = {
    title: this.i18n.transform('skill.title'),
    collapsed: true,
    skillsSwitchEnabled: false,
    list: [],
  };
  public skillsLoading = false;
  public skillsEmptyText = this.i18n.transform('skill.limit', {
    number: this.skillLimit,
  });
  public changeUrl = cdnAssetUrl;
  private receivedInput = '';
  private destroy$ = new Subject<void>();
  private addTip = this.i18n.transform('skill.add.success.tip');
  private deleteTip = this.i18n.transform('skill.delete.success.tip');
  private skillsRequestFailed = false;

  get skillLimit() {
    return this.configService.getConfigs()?.agent_skill_limit ?? SKILL_MAX_COUNT;
  }

  constructor(
    private i18n: I18nService,
    private configService: AgentConfigService,
    private nzModal: NzModalService,
    private inputService: SkillInputPromptService,
    private skillApi: SIESkillApi
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.skillsAdded.list = changes?.selectedSkills?.currentValue || [];
    this.skillsAdded.collapsed = !this.skillsAdded.list.length;
  }

  ngOnInit(): void {
    this.inputService.input$.pipe(takeUntil(this.destroy$)).subscribe(value => {
      this.receivedInput = value;
    });
  }

  public changeCollapseEvent() {
    this.skillsAdded.collapsed = !this.skillsAdded.collapsed;
  }

  // 点击自动添加
  public autoAddClickedEvent() {
    this.skillsLoading = true;
    const params = {
      instructions: this.receivedInput,
      type: 'skill',
      limit: this.skillLimit - this.skillsAdded.list.length,
    };

    this.skillApi
      .getSkillResource(params, this.agentId)
      .pipe(
        catchError(() => {
          this.skillsRequestFailed = true;
          return of([]);
        }),
        finalize(() => {
          this.skillsLoading = false;
          this.skillsAdded.collapsed = false;
        })
      )
      .subscribe({
        next: data => {
          if (this.skillsRequestFailed) {
            this.skillsRequestFailed = false;
            return;
          }
          const skills = data?.skills || [];
          if (skills.length) {
            this.batchAddSkill([...skills, ...this.skillsAdded.list], this.addTip);
          } else {
            MessageComponent.showPrompt(this.i18n.transform('skill.auto.add.no.tip'));
          }
        },
      });
  }

  // 点击手动添加按钮
  public addBtnClickedEvent() {
    if (this.isFlowReadonly) {
      return;
    }
    this.addSkillModal.show(this.skillsAdded.list);
  }

  public deleteOneSkills(skill) {
    const remainedData = this.skillsAdded.list.filter(item => item.skill_id !== skill.skill_id);
    this.batchAddSkill(remainedData, this.deleteTip);
  }

  public isShowUpdateIcon(item: any): boolean {
    const appLastVersionObj = item?.app_last_version_obj;
    const resourceVersion = item?.resource_version;

    if (this.isFlowReadonly) {
      return false;
    }

    if (!appLastVersionObj || !resourceVersion) {
      return false;
    }

    if (!appLastVersionObj.valid) {
      return false;
    }

    const latestVersion = appLastVersionObj.resource_latest_version;
    return latestVersion !== resourceVersion;
  }

  public isShowDeleteIcon(item: any): boolean {
    if (this.isFlowReadonly) {
      return false;
    }

    const appLastVersionObj = item?.app_last_version_obj;

    if (!appLastVersionObj) {
      return false;
    }

    return !appLastVersionObj.valid;
  }

  public onConfirmUpdateSkill(item): void {
    this.nzModal.create({
      nzTitle: this.i18n.transform('skill.update.title'),
      nzContent: SkillUpdateModalComponent,
      nzData: {
        skillItem: item,
      },
      nzOnOk: () => {},
      nzOnCancel: () => {},
    });
  }

  public batchAddSkill(addList, tip = this.addTip) {
    const skillDetail = {
      skill_details: addList.map(item => ({
        skill_id: item.skill_id,
        skill_version:
          item.app_last_version_obj?.resource_version ||
          item.app_last_version_obj?.resource_latest_version ||
          item.latest_version ||
          item.last_version_id ||
          '',
      })),
    };
    this.dealSkill(skillDetail, tip);
  }

  public dealSkill(data, tip) {
    this.skillsLoading = true;
    this.addSkillModal?.close();

    this.doTriggerSaveSkill.emit({
      data,
      successCb: () => {
        MessageComponent.showSuccess(tip, 3000);
      },
      finallyCb: () => {
        this.skillsLoading = false;
      },
    });
  }
}
