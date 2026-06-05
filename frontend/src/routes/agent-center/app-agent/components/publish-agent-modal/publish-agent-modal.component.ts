import { Component, Input } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { CommonUtils } from 'src/utils/common.util';
import { agentCommonLogic } from '../../common-logic-agent';

@Component({
  selector: 'meta-publish-agent-modal',
  templateUrl: './publish-agent-modal.component.html',
  styleUrls: ['./publish-agent-modal.component.less'],
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class PublishAgentModalComponent {
  @Input() tagList: any;

  public tagsSelected: any = [];

  public lang = CommonUtils.getLanguage();

  constructor(private agentCommonLogic: agentCommonLogic) {}

  ngOnInit() {
    this.tagsSelected = this.tagList
      ?.filter((item: any) => item.selected === true)
      .map((item: any) => item.tag_id);
  }

  public submit() {
    this.close();
  }

  public close() {}

  public dismiss() {}

  /** 自定义设置Prompt builder顶部的tags数组对应的背景色和边框色 */
  public getTagStyle(item: any) {
    if (item.selected) {
      return this.agentCommonLogic.getAppStoreTagStyle('selected');
    } else if (this.tagsSelected.length >= 2) {
      return this.agentCommonLogic.getAppStoreTagStyle('disabled');
    } else {
      return this.agentCommonLogic.getAppStoreTagStyle('unselected');
    }
  }

  public selectTags(item: any) {
    if (!item.selected && this.tagsSelected.length >= 2) {
      return;
    }

    if (item.selected) {
      item.selected = false;
      const index = this.tagsSelected.indexOf(item.tag_id);
      if (index > -1) {
        this.tagsSelected.splice(index, 1);
      }
    } else {
      item.selected = true;
      this.tagsSelected.push(item.tag_id);
    }
  }

  public publishTagsClass(item: any): string {
    if (this.tagsSelected.length < 2 || item.selected) {
      return 'selectable-label';
    } else {
      return 'non-selectable-label';
    }
  }
}
