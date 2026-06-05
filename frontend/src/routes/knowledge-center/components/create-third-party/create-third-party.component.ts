import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { TransferItem } from 'ng-zorro-antd/transfer';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalModule, NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';

@Component({
  selector: 'create-third-party',
  templateUrl: './create-third-party.component.html',
  styleUrls: ['./create-third-party.component.less'],
  imports: [MODULES, NzModalModule, NzButtonModule],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.KNOWLEDGE, I18nNamespace.AGENT_CENTER, I18nNamespace.COMMON],
    },
  ],
})
export class CreateThirdPartyComponent implements OnInit {
  isVisible = false;
  @Output() openAccessKnowledgeBase = new EventEmitter<string>();
  @Output() getList = new EventEmitter<string>();
  @Output() kbCreateSuccess = new EventEmitter();
  public placeholder = this.i18n.transform('search_knowledge_base_by_name');
  sourceOptions: Array<any> = [];
  sourceValue: any;
  public myOptions: Array<any> = [];
  public mySelecteds: Array<any> = [];

  public searchable = true;

  public leftQuota = 0;

  public maxCount = 0;

  isNeedHasQuota = false;

  transferDataSource: TransferItem[] = [];

  constructor(
    private knowledgeRepoService: KnowledgeRepoService,
    private kbAbilitiesService: KbAbilitiesService,
    private agentRepoService: AppAgentRepoService,
    private i18n: I18NextEagerPipe,
    private message: NzMessageService
  ) {}

  ngOnInit(): void {}

  showDrawer() {
    this.knowledgeRepoService
      .getThirdPartyKnowledgeBaseList({
        offset: 0,
        limit: 1000,
      })
      .then(res => {
        this.sourceOptions = res.items.map(item => {
          item.label = item.name;
          return item;
        });
        this.isVisible = true;
      });
    this.refreshQuota();
  }

  onSelect(selectedOption: any) {
    this.myOptions = [];
    this.mySelecteds = [];
    this.transferDataSource = [];
    this.refreshQuota();
    if (!selectedOption) {
      return;
    }
    this.knowledgeRepoService
      .getAccessKnowledgeBaseList({
        offset: 0,
        limit: 100,
        knowledge_base_connection_id: selectedOption.id,
      })
      .then(res => {
        this.myOptions = res.knowledge_base_list.map(item => {
          item.label = item.knowledge_base_name;
          item.disabled = item.present;
          if (item.present) {
            item.tip = this.i18n.transform('already_exists');
          }
          return item;
        });
        this.transferDataSource = this.myOptions.map(item => ({
          key: item.knowledge_base_id,
          title: item.label,
          disabled: item.disabled,
          description: item.tip || '',
          direction: item.present ? 'right' : undefined,
        }));
      });
  }

  filterOption = (searchValue: string, item: TransferItem): boolean => {
    return item.title!.toLowerCase().indexOf(searchValue.toLowerCase()) !== -1;
  };

  canMove = (arg: { direction: string; entity: TransferItem }): boolean => {
    if (arg.direction === 'right' && this.isNeedHasQuota) {
      return this.leftQuota > 0;
    }
    return true;
  };

  onTransferChange(ret: { from: string; to: string; list: TransferItem[] }): void {
    const selectedKeys = ret.list.filter(item => item.direction === 'right').map(item => item.key);
    this.mySelecteds = this.myOptions.filter(item => selectedKeys.includes(item.knowledge_base_id));

    if (ret.from === 'right' && ret.to === 'left') {
      if (this.isNeedHasQuota) {
        this.leftQuota = this.leftQuota + ret.list.length;
      }
    } else if (ret.from === 'left' && ret.to === 'right') {
      if (this.isNeedHasQuota) {
        this.leftQuota = this.leftQuota - ret.list.length;
      }
    }
  }

  handleConfirm() {
    if (this.mySelecteds.length === 0) {
      this.message.error(this.i18n.transform('please_select_a_knowledge_base'));
      return;
    }

    let params = {
      knowledge_base_connection_id: this.sourceValue.id,
      knowledge_base_external_ids: this.mySelecteds.map(item => {
        return item.knowledge_base_id;
      }),
    };
    this.knowledgeRepoService.createThirdPartyKnowledgeBase(params).then(res => {
      this.message.success(this.i18n.transform('create_success'));
      this.clearValue();
      this.getList.emit();
      const kbId = this.kbAbilitiesService.getKbId(res.knowledge_base_results?.[0]);
      if (kbId) {
        this.kbCreateSuccess.emit(kbId);
      }
    });
  }

  clearValue() {
    this.sourceOptions = [];
    this.sourceValue = null;
    this.myOptions = [];
    this.mySelecteds = [];
    this.transferDataSource = [];
    this.isVisible = false;
  }

  closeModal() {
    this.isVisible = false;
  }

  async refreshQuota() {
    const res = await this.agentRepoService.getResourceQuantity('thirdPartyKnowledgeBase');
    if (res) {
      const { usedLimit = 0, totalAmount = 0 } = res;
      this.isNeedHasQuota = totalAmount !== -1;
      if (totalAmount === 0) {
        this.leftQuota = 0;
        this.maxCount = 0;
        return;
      }
      this.leftQuota = totalAmount - usedLimit - this.mySelecteds.length;
      this.maxCount = totalAmount - usedLimit;
    }
  }
}
