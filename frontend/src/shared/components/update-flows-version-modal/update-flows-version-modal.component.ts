import { Component, Input, Output, EventEmitter } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import * as I18next from 'angular-i18next';
import { NodeDependencies } from '@routes/agent-center/app-flow/components/modules';
import { AppFlowService } from '@routes/agent-center/app-flow/app-flow.service';
import type {
  IIntentContainerNode,
  IIntentContainerBranch,
} from '@routes/agent-center/app-flow/node.type';

@Component({
  selector: 'update-flows-version-modal',
  templateUrl: './update-flows-version-modal.component.html',
  styleUrls: ['./update-flows-version-modal.component.scss'],
  standalone: true,
  imports: [MODULES, NodeDependencies],
  providers: [
    {
      provide: I18next.I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class UpdateFlowsVersionModalComponent {
  @Input() boundFlowVersionList = [];

  @Input('nodeInfo') nodeInfo: IIntentContainerNode;

  @Output('confirm') confirm = new EventEmitter<IIntentContainerBranch[]>();

  public table: any = {
    checkedList: [],
    displayedData: [],
    srcData: {
      data: [],
      state: undefined,
    },
    currentPage: 1,
    totalNumber: 0,
  };

  public columns: Array<any> = [
    {
      title: '',
    },
    {
      title: this.i18n.transform('workflow_name'),
    },
    {
      title: this.i18n.transform('related_intention'),
    },
    {
      title: this.i18n.transform('current_version'),
    },
    {
      title: this.i18n.transform('latest_version'),
    },
  ];

  public pageSize: any = {
    options: [6, 10, 20, 50],
    size: 6,
  };

  constructor(private appFlowServ: AppFlowService, private i18n: I18next.I18NextEagerPipe) {}

  ngOnInit(): void {
    this.table.srcData.data = this.boundFlowVersionList;
    this.table.totalNumber = this.boundFlowVersionList.length;
    this.updateDisplayedData();
  }

  private updateDisplayedData(): void {
    const start = (this.table.currentPage - 1) * this.pageSize.size;
    const end = start + this.pageSize.size;
    this.table.displayedData = this.table.srcData.data.slice(start, end);
  }

  public trackByFn(index: number, item: any): number {
    return item.branch_id;
  }

  public isAllChecked(): boolean {
    return this.table.displayedData.length > 0 && this.table.displayedData.every(row => this.table.checkedList.indexOf(row) !== -1);
  }

  public isIndeterminate(): boolean {
    return this.table.checkedList.length > 0 && this.table.checkedList.length < this.table.displayedData.length;
  }

  public onAllCheckedChange(checked: boolean): void {
    if (checked) {
      this.table.displayedData.forEach(row => {
        if (this.table.checkedList.indexOf(row) === -1 && !row.disabled) {
          this.table.checkedList.push(row);
        }
      });
    } else {
      this.table.checkedList = [];
    }
  }

  public onItemCheckedChange(row: any, checked: boolean): void {
    if (checked) {
      if (this.table.checkedList.indexOf(row) === -1) {
        this.table.checkedList.push(row);
      }
    } else {
      this.table.checkedList = this.table.checkedList.filter(item => item !== row);
    }
  }

  public onPageIndexChange(page: number): void {
    this.table.currentPage = page;
    this.updateDisplayedData();
  }

  public onPageSizeChange(size: number): void {
    this.pageSize.size = size;
    this.table.currentPage = 1;
    this.updateDisplayedData();
  }

  public updateVersions() {
    // 用户手动选上的意图分支绑定工作流的最新版本信息
    const branchesSelected = this.table.checkedList.map((item) => {
      return {
        id: item.index,
        configs: {
          branch_id: item.branch_id,
          category: item.category,
          index: item.index,
          intent_id: item.intent_id,
          workflow_id: item.workflow_id,
          workflow_name: item.workflow_name,
          workflow_version_id: item.last_version_id,
          workflow_version_name: item.last_version_name,
        },
      };
    });

    const newBranches = this.nodeInfo.branches.map((item) => {
      const matchingItem = branchesSelected.find(
        (subItem) =>
          `${item.configs.index}_${item.configs.workflow_id}` ===
          `${subItem.configs.index}_${subItem.configs.workflow_id}`,
      );
      return matchingItem || item;
    });
    this.appFlowServ.setContainerDsl({
      ...this.nodeInfo,
      branches: newBranches,
    });
    this.confirm.emit(newBranches);
    this.close();
  }

  close(): void {}

  dismiss() {}
}
