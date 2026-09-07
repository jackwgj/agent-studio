import { ChangeDetectorRef, Component, Input, NgZone } from '@angular/core';
import { MaskComponent } from '../../../../utils/mask.component';
import { I18nNamespace } from '@i18n';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';

@Component({
  selector: 'diff-version-modal',
  templateUrl: './diff-version-modal.component.html',
  styleUrls: ['./diff-version-modal.component.scss'],
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT, I18nNamespace.REVIEW],
    },
  ],
})
export class DiffVersionModalComponent {
  @Input() app_id = '';
  @Input() versionList: any[] = [];
  @Input() leftIndex = 0;
  @Input() rightIndex = 0;
  @Input() type = 'agent';

  config: any = {
    id: 'diff-version',
    titleOptions: {
      original: [],
      modified: [],
    },
    readonly: true,
  };
  lineAdd = 0;
  lineDel = 0;
  height = 500;

  originalValue;
  modifiedValue;

  constructor(
    private agentRepoServe: AppAgentRepoService,
    private appFlowRepoServ: AppFlowRepoService,
    private i18n: I18NextEagerPipe,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone,
  ) {}

  async ngOnInit() {
    this.config.titleOptions.original = [{ id: 'latest', label: this.i18n.transform('current'), default: false }];
    this.config.titleOptions.modified = [{ id: 'latest', label: this.i18n.transform('current'), default: false }];
    this.versionList.forEach((item, index) => {
      this.config.titleOptions.original.push({ id: item.version_id , label: item.version_name, default: false });
      this.config.titleOptions.modified.push({ id: item.version_id , label: item.version_name, default: false });
    });
    this.config.titleOptions.original[this.leftIndex + 1].default = true;
    this.config.titleOptions.modified[this.rightIndex + 1].default = true;

    // 在 Zone 内同步执行，避免异步操作导致的变更检测问题
    this.ngZone.run(() => {
      MaskComponent.show();
    });

    try {
      const [originalData, modifiedData] = await Promise.all([
        this.getVersionData(this.config.titleOptions.original[this.leftIndex + 1].id),
        this.getVersionData(this.config.titleOptions.modified[this.rightIndex + 1].id)
      ]);

      this.ngZone.run(() => {
        this.originalValue = JSON.stringify(originalData, null, 2);
        this.modifiedValue = JSON.stringify(modifiedData, null, 2);
        MaskComponent.hide();
        this.cdr.detectChanges(); // 手动触发变更检测
      });
    } catch (error) {
      this.ngZone.run(() => {
        MaskComponent.hide();
        this.cdr.detectChanges(); // 确保在错误情况下也能正确更新视图
      });
    }
  }

  async originalTitleChange($event) {
    this.ngZone.run(() => {
      MaskComponent.show();
    });

    try {
      const data = await this.getVersionData($event.id);
      this.ngZone.run(() => {
        this.originalValue = JSON.stringify(data, null, 2);
        MaskComponent.hide();
        this.cdr.detectChanges(); // 手动触发变更检测
      });
    } catch (error) {
      this.ngZone.run(() => {
        MaskComponent.hide();
        this.cdr.detectChanges(); // 确保在错误情况下也能正确更新视图
      });
    }
  }

  async modifiedTitleChange($event) {
    this.ngZone.run(() => {
      MaskComponent.show();
    });

    try {
      const data = await this.getVersionData($event.id);
      this.ngZone.run(() => {
        this.modifiedValue = JSON.stringify(data, null, 2);
        MaskComponent.hide();
        this.cdr.detectChanges(); // 手动触发变更检测
      });
    } catch (error) {
      this.ngZone.run(() => {
        MaskComponent.hide();
        this.cdr.detectChanges(); // 确保在错误情况下也能正确更新视图
      });
    }
  }


  private async getVersionData(versionId) {
    let details = {};
    let isAgent = this.type === 'multi' || this.type === 'agent';
    try {
      let value;
      if (versionId !== 'latest') {
        value =
          isAgent
            ? await this.agentRepoServe.rollbackAgentVersion(
                this.app_id,
                versionId,
              )
            : await this.agentRepoServe.rollbackFlowVersion(
                this.app_id,
                versionId,
              );
        if (isAgent) {
          value.workflow_details = value.details;
          value.avatar = value.icon;
        }
      } else if (isAgent) {
        value = await this.agentRepoServe.getMultiAgent(this.app_id);
        value.workflow_details = value.details;
        value.avatar = value.icon;
      } else {
        value = await this.appFlowRepoServ.getFlow(this.app_id);
      }
      details = value.workflow_details;
    } catch {
      // do nth
    }
    return details;
  }

  getLine(data) {
    data?.onDidUpdateDiff(() => {
      let {lineAdd ,lineDel} = this.getChangeLine(data.getLineChanges());
      // 在 Zone 内更新属性，避免变更检测问题
      this.ngZone.run(() => {
        this.height = Math.min((((this.modifiedValue || '').split('\n').length + lineDel) * 19 + 100), document.documentElement.clientHeight - 242);
        this.lineAdd = lineAdd;
        this.lineDel = lineDel;
        this.cdr.detectChanges(); // 手动触发变更检测
      });
    })
  }

  getChangeLine(changes) {
    let lineAdd = 0;
    let lineDel = 0;
    changes.forEach(change => {
      if (change.modifiedEndLineNumber && change.modifiedStartLineNumber) {
        lineAdd += (change.modifiedEndLineNumber - change.modifiedStartLineNumber + 1);
      }
      if (change.originalEndLineNumber && change.originalStartLineNumber) {
        lineDel += (change.originalEndLineNumber - change.originalStartLineNumber + 1);
      }
    });
    return {
      lineAdd,
      lineDel,
    }
  }
}
