import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { MCPService } from '@services/agent-center/mcp.service';
import { MCP_DEFAULT_ICON_BASE64_STR } from '@shared/config/default-icons-base64';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { Subscription } from 'rxjs';
import { WindowResizeService } from '@shared/base/window-resize.service';
import type { McpServer } from '@interfaces/mcp/mcp.interfaces';
import { NzTagModule } from 'ng-zorro-antd/tag';

@Component({
  selector: 'mcp-server-card',
  template: `
    <div
      class="card-wrapper"
      [ngClass]="{ selected: isSelected , 'border-disabled': isAddBtnDisabled() && !isFromWorkflow }"
      (click)="handleWorkFlowClickCard()"
      [style]="{
        cursor:
          mcpServerData.fc_instance_status === 'creatingStack'
            ? 'not-allowed'
            : 'pointer'
      }"
    >
      <div class="card-info">
        <img
          [src]="
            !mcpServerData.icon
              ? mcpDefaultIcon
              : mcpServerData.icon.startsWith('data:image/')
              ? mcpServerData.icon
              : 'data:image/png;base64,' + mcpServerData.icon
          "
          alt="icon"
        />
        <div class="content-wrapper">
          <div class="mb-[4px] font-bold" tiOverflow [ngClass]="'medium-draw'">{{ mcpServerData.name }}</div>
          <nz-tag
            *ngIf="type === 'inner'"
            [nzColor]="'orange'"
            class="!rounded-[4px] py-[1px] pl-[2px] pr-[4px] text-[12px]"
          >
            {{ 'platform' | i18nextEager }}
          </nz-tag>
          <nz-tag
            *ngIf="type === 'custom' && false"
            [nzColor]="'blue'"
            class="!rounded-[4px] py-[1px] pl-[2px] pr-[4px] text-[12px]"
          >
            {{ 'user' | i18nextEager }}
          </nz-tag>
          <nz-tag
            *ngIf="type === 'custom'"
            [nzColor]="statusColor"
            class="!rounded-[4px] py-[1px] pl-[2px] pr-[4px] text-[12px]"
          >
            <span *ngIf="mcpToolPendingIds[mcpServerData.id]" class="inline-flex items-center gap-1">
              <span class="inline-block w-[12px] h-[12px] border-[2px] border-current border-t-transparent rounded-full animate-spin"></span>
              {{ addLoadingTip }}
            </span>
            <span *ngIf="!mcpToolPendingIds[mcpServerData.id]">{{ status | i18nextEager }}</span>
          </nz-tag>
          <nz-tag
            *ngIf="type === 'custom' && addNum > 0 && isFromWorkflow"
            [nzColor]="'blue'"
            class="!rounded-[4px] py-[1px] pl-[2px] pr-[4px] text-[12px]"
          >
            + {{ addNum }}
          </nz-tag>
        </div>
        <div style="position: absolute; right: 0; top: 0">
          <button
            *ngIf="isNeed"
            type="button"
            nz-button
            (click)="toggleForm()"
          >
            {{ 'activate_now' | i18nextEager }}
            <nz-icon nzType="right" nzTheme="outline" class="!text-[#1476ff] transition-all" [ngClass]="{ '-rotate-90': !isShowOtherForm && isShowForm }" />
          </button>
          <ng-container *ngIf="!isNeed">
            <ng-container *ngIf="!isFromWorkflow">
              <input
                [id]="mcpServerData.server_id"
                type="checkbox"
                tiCheckbox
                [(ngModel)]="isSelectedCard"
                [disabled]="isAddBtnDisabled()"
                (ngModelChange)="onNgModelChange($event)"
              />
            </ng-container>
            <ng-container *ngIf="isFromWorkflow">
              <div
                *ngIf="!isSelected || isFromWorkflow"
                class="flex cursor-pointer items-center gap-x-[8px]"
                (click)="handleSelectChange('add', $event)"
                [class]="isAddBtnDisabled() ? 'add-btn-wrap-disabled' : ''"
              >
                <nz-icon nzType="plus" nzTheme="outline" class="add-button"
                         [style]="{cursor: isAddBtnDisabled() ? 'not-allowed' : 'pointer'}"
                />
              </div>
              <div
                *ngIf="isSelected && !isFromWorkflow"
                class="flex cursor-pointer items-center gap-x-[8px]"
                (click)="handleSelectChange('remove', $event)"
              >
                <nz-icon nzType="minus" nzTheme="outline" class="remove-button !border-[#1476ff] text-[#1476ff]" />
              </div>
            </ng-container>
          </ng-container>
        </div>
      </div>
      <div
        class="w-[95%] break-words text-[12px] leading-[22px] text-[#808080]" [ngClass]="{ 'description-text' : !isFromWorkflow }"
        nz-tooltip
        [nzTooltipTitle]="mcpServerData.description || '--'"
      ></div>
      <div class="hover-text" *ngIf="!isFromWorkflow">
        <button nz-button (click)="handleClickCard()">{{ 'redeploy' | i18nextEager}}</button>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        width: 100%;
      }
      .add-button,
      .remove-button {
        border-radius: 3.2px;
        padding: 1px;
        border: 1px solid #c2c2c2;
        color: #191919;
        cursor: pointer;
      }
      .card-wrapper {
        background-color: #fafafa;
        border-radius: 8px;
        border: 1px solid transparent;
        transition: border-color 0.3s ease;
        padding: 16px;
        height: 130px;
        &.selected {
          background-color: #f0f7ff;
        }
        &:hover {
          .description-text {
            display: none;
          }
          .hover-text {
            display: inline-block;
          }
          border-color: #1476ff;
        }
        .add-btn-wrap-disabled {
          .add-button {
            border: 1px solid #ddd;
            color: #c2c2c2 !important;
          }
        }
        .hover-text {
          display: none;
        }
        &.border-disabled {
          border-color: transparent;
        }
      }
      .card-info {
        display: flex;
        gap: 0 16px;
        align-items: center;
        position: relative;
        margin-bottom: 12px;
        img {
          width: 40px;
          height: 40px;
        }
        .content-wrapper {
          flex: 1;
        }
      }
      .large-draw{
        max-width: 270px;
      }
      .medium-draw{
        max-width: 200px;
      }
    `,
  ],
  standalone: true,
  imports: [MODULES, NzTagModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT],
    },
  ],
})
export class McpServerCardComponent implements OnInit {
  @Input() mcpServerData!: McpServer;
  @Input() selectedCards: McpServer[] = [];
  @Input() mcpToolPendingIds: any = {};
  @Input() showFormServerId = '';
  @Input() isClickMcpCard: boolean = false; // 用户点击MCP的新增按钮
  @Input() type = ''; // 是自定义MCP还是官方预置MCP inner | custom
  @Input() isFromWorkflow: boolean = false; // 是否是在工作流中添加mcp
  @Input() isClickAddMcpToWorkflow: boolean = false; // 当MCP往工作流中添加时，每2s只能添加一次
  @Input() mcpCreateSub; // 是否是通过MCP创建的提示信息，添加的MCP服务

  @Output() change = new EventEmitter();
  @Output() clickCard = new EventEmitter();

  isSelectedCard = false;
  public addNum: number = 0; // 工作流中添加的MCP的个数
  public mcpDefaultIcon = MCP_DEFAULT_ICON_BASE64_STR;
  public get isNeed() {
    return 'need' === this.mcpServerData.config_status;
  }
  public get isSelected() {
    return (
      -1 !==
      this.selectedCards.findIndex(
        (item) => item.server_id === this.mcpServerData.server_id,
      )
    );
  }
  public get isShowOtherForm() {
    return this.showFormServerId !== this.mcpServerData.server_id;
  }
  public form: FormArray;
  public isShowForm = false;
  public get controls(): FormControl[] {
    return this.form.controls as FormControl[];
  }
  public createLoading = false;
  private resizeSub: Subscription;
  public windowWidth: number;
  addLoadingTip: string = this.i18n.transform('mcp_adding');

  constructor(
    private fb: FormBuilder,
    private mcpService: MCPService,
    private windowResizeService: WindowResizeService,
    private i18n: I18NextEagerPipe,
    ) {}

  ngOnInit() {
    const filterData = this.selectedCards.filter(item => item.id === this.mcpServerData.server_id);
    if (filterData.length > 0) {
      this.addNum = filterData.length;
    }

    this.initPendingToolsLoading();

    this.resizeSub = this.windowResizeService.getWindowWidth().subscribe((width) => {
      this.windowWidth = width;
    });
    if (this.mcpCreateSub) {
      this.mcpCreateSub.asObservable().subscribe((data: any) => {
        if (data.id === this.mcpServerData.id) {
          this.change.emit({
            type: 'add',
            data: data,
            noNeedTool: true,
          });
        }
      });
    }
    this.isSelectedCard = this.isSelected && !this.isFromWorkflow;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.isClickMcpCard) {
      const filterData = this.selectedCards.filter(item => item.id === this.mcpServerData.server_id);
      if (filterData.length > 0) {
        this.addNum = filterData.length;
      }
    }
  }

  // 如果切换了分页，也要有初始化的处理一下pending的loading效果
  private initPendingToolsLoading() {
    if (this.mcpToolPendingIds[this.mcpServerData.id]) {
      this.addLoadingTip = this.i18n.transform('mcp_adding');
    }
  }

  // mock一下数据
  private mockNPXAndUVXData(mcpData: any) {
    if (mcpData.deploy_type === 'stdio') {
      mcpData.fc_instance_status = 'pendingTools';
    }
  }

  ngOnDestroy(): void {
    this.resizeSub?.unsubscribe();
  }

  public toggleForm() {
    if (this.isShowForm) {
      this.handleCancel();
      return;
    }
    this.showMcpForm();
  }

  public showMcpForm() {
    this.isShowForm = true;
    this.change.emit({
      type: 'form',
      data: this.mcpServerData,
    });
  }

  public handleSelectChange(type: string, event?: any) {
    if (event) {
      event.stopPropagation();
    }
    if (type === 'remove') {
      this.handleClickCancelMcpCard(type);
      return;
    }
    if (this.mcpServerData?.isPendingTools) {
      return;
    }
    if (
      this.isFromWorkflow &&
      this.isClickAddMcpToWorkflow &&
      this.type === 'custom'
    ) {
      return;
    }
    if (
      type === 'add' &&
      (
        this.mcpServerData?.fc_instance_status !== 'success' &&
        this.mcpServerData?.fc_instance_status !== 'pendingTools'
      ) &&
      this.type === 'custom'
    ) {
      return;
    }

    this.change.emit({
      type,
      data: this.mcpServerData,
    });
  }

  // 取消mcp卡片的选择
  private handleClickCancelMcpCard(type: string) {
    this.addLoadingTip = this.i18n.transform('mcp_canceling');
    this.change.emit({
      type,
      data: this.mcpServerData,
    });
    setTimeout(() => {
      this.addLoadingTip = this.i18n.transform('mcp_adding');
    }, 1000);
  }

  public async createMcpServer() {
    if (!this.form.valid) {
      this.form.markAllAsTouched();
      return;
    }
    try {
      this.createLoading = true;
      const res = await this.mcpService.provisionService({
        serverId: this.mcpServerData.server_id,
        auth_keys: this.form.value.map((v, i) => {
          const origin = this.mcpServerData.auth.auth_keys[i];
          return {
            ...origin,
            auth_key: v,
          };
        }),
      });
      this.change.emit({
        type: 'create',
        data: res,
      });
    } finally {
      this.createLoading = false;
    }
  }

  public handleCancel() {
    this.isShowForm = false;
  }

  public get status() {
    if (this.mcpServerData?.fc_instance_status === 'success') {
      return 'deployed';
    } else if (this.mcpServerData?.fc_instance_status === 'creatingStack') {
      return 'deploying';
    } else if (this.mcpServerData?.fc_instance_status === 'pendingTools') {
      return 'pendingTools';
    } else {
      return 'deployFailed';
    }
  }

  public get statusColor() {
    if (this.status === 'deployed') {
      return 'green';
    } else if (this.status === 'deploying' || this.status === 'pendingTools') {
      return 'orange';
    } else {
      return 'red';
    }
  }

  public isAddBtnDisabled() {
    if (this.type === 'inner') {
      return false;
    }
    if (this.mcpToolPendingIds[this.mcpServerData.id]) {
      return true;
    }
    return this.mcpServerData?.fc_instance_status !== 'success' && this.mcpServerData?.fc_instance_status !== 'pendingTools';
  }

  public handleWorkFlowClickCard() {
    if(!this.isFromWorkflow) {
      if(!this.isAddBtnDisabled()) {
        this.isSelectedCard = !this.isSelectedCard;
        this.onNgModelChange(this.isSelectedCard);
      }
      return;
    }
    this.handleClickCard();
  }

  public handleClickCard() {
    if (
      this.mcpServerData &&
      this.mcpServerData.fc_instance_status === 'creatingStack'
    ) {
      return;
    }
    this.clickCard.emit(this.mcpServerData);
  }
  public onNgModelChange(e) {
    this.handleSelectChange(e ? 'add' : 'remove');
  }
}
