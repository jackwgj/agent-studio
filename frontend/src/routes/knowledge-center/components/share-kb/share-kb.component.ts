import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { KBScope } from '@routes/knowledge-center/knowledge.types';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { SpaceTeamManagementService } from "@services/space-team-management.service";
import { HttpService } from "@services/http.service";
import { NzDrawerService, NzDrawerRef } from 'ng-zorro-antd/drawer';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'share-kb',
  templateUrl: './share-kb.component.html',
  styleUrls: ['./share-kb.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    FormsModule,
    ReactiveFormsModule,
    NzFormModule,
    NzSelectModule,
    NzRadioModule,
    NzInputModule,
    NzCheckboxModule,
    NzPaginationModule,
    NzButtonModule,
    NzIconModule,
    NzToolTipModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.KNOWLEDGE
      ],
    },
    NzDrawerService,
    NzMessageService
  ],
})
export class ShareKbComponent {
  @ViewChild('shareKbModal', { static: false }) shareTemplateRef: TemplateRef<any>;
  @ViewChild('drawerFooterTpl', { static: false }) drawerFooterTpl: TemplateRef<any>;

  @Output() shareSuccess = new EventEmitter<{
    kbId: string;
  }>();

  private drawerRef: NzDrawerRef;

  public kbOptions = [];
  formGroup = new FormGroup({
    selectedKb: new FormControl(null, [Validators.required]),
    selRangeType: new FormControl('GLOBAL', [Validators.required]),
  });

  public isSelectLoading = false;
  private kbSearchName = '';
  private pageSize = 10;
  private totalNumber = 0;

  public searchKeyword = '';
  public kbSelectPlaceholder1 = this.i18n.transform('share_kb_1');

  public shareModelTypeList = [
    {
      label: this.i18n.transform('can_copy'),
      desc: this.i18n.transform('can_copy_tip'),
      active: false,
      disabled: true,
      disabledTip: this.i18n.transform('share_modal_disable_tip'),
    },
    {
      label: this.i18n.transform('just_use'),
      desc: this.i18n.transform('just_use_tip'),
      active: true,
      disabled: false,
      disabledTip: '',
    },
  ];
  public shareRangeTypeList = [
    {
      id: 'GLOBAL',
      label: this.i18n.transform('all_space_visable'),
      value: 'GLOBAL',
    },
    {
      id: 'PARTIAL',
      label: this.i18n.transform('part_space_visable'),
      value: 'PARTIAL',
    },
  ];

  public allWorkspaceList = [];
  public searchWorkspaceList = [];
  public showWorkspaceList = []
  public shareWorkspaceSelect = [];

  public currentSpacePage: number = 1;
  public spaceTotalNumber: number = 0;
  public spacePageSize: number = 16;

  get selRangeType() {
    return this.formGroup.get('selRangeType')?.value;
  }

  get isAllChecked(): boolean {
    return this.searchWorkspaceList.length > 0 && this.shareWorkspaceSelect.length === this.searchWorkspaceList.length;
  }

  get isIndeterminate(): boolean {
    return this.shareWorkspaceSelect.length > 0 && !this.isAllChecked;
  }

  constructor(
    private nzDrawerService: NzDrawerService,
    private nzMessage: NzMessageService,
    private knowledgeRepoService: KnowledgeRepoService,
    private kbAbilitiesService: KbAbilitiesService,
    private i18n: I18NextEagerPipe,
    private spaceTeamManagementService: SpaceTeamManagementService,
    private readonly http: HttpService,
  ) {}

  public showKbShare() {
    this.searchKeyword = '';
    this.shareWorkspaceSelect = [];
    this.formGroup.get('selRangeType').setValue('GLOBAL');

    this.drawerRef = this.nzDrawerService.create({
      nzTitle: this.i18n.transform('kb_share'),
      nzContent: this.shareTemplateRef,
      nzFooter: this.drawerFooterTpl,
      nzWidth: 500,
      nzPlacement: 'right',
      nzMaskClosable: false
    });

    this.spaceTeamManagementService.getWorkspace().then(res => {
      const list = res.workspaceList || [];
      const allList = list.filter(v => v.id !== this.http.getWorkspaceId());

      this.allWorkspaceList = allList || [];
      this.searchWorkspaceList = JSON.parse(JSON.stringify(this.allWorkspaceList));
      this.spaceTotalNumber = allList.length || 0;
      this.getShowWorkspaceList();
    });
  }

  public closeDrawer() {
    this.drawerRef.close();
    this.formGroup.reset();
  }

  public saveShare() {
    if (this.formGroup.invalid) {
      Object.values(this.formGroup.controls).forEach(control => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
      return;
    }

    this.changeKbScope().then(kb => {
      if (kb) {
        this.drawerRef.close();
        this.shareSuccess.emit(this.kbAbilitiesService.getKbId(kb));
        this.formGroup.reset();
      }
    });
  }

  public getShowWorkspaceList() {
    this.showWorkspaceList = this.getSubArray(
      this.searchWorkspaceList,
      ((this.currentSpacePage || 1) - 1) * this.spacePageSize,
      this.spacePageSize
    );
  }

  getSubArray(arr = [], offset: number, limit: number) {
    const start = Math.max(offset, 0);
    const end = Math.min(offset + limit, arr.length);
    return arr.slice(start, end);
  }

  search(value: string): void {
    this.currentSpacePage = 1;
    if (!value) {
      this.searchWorkspaceList = JSON.parse(JSON.stringify(this.allWorkspaceList));
    } else {
      this.searchWorkspaceList = this.allWorkspaceList.filter(v => v.name.includes(value));
    }
    this.spaceTotalNumber = this.searchWorkspaceList.length;

    if (this.shareWorkspaceSelect.length) {
      this.shareWorkspaceSelect = this.shareWorkspaceSelect.map(v => {
        const select = this.searchWorkspaceList.find(se => se.id === v.id);
        return select || v;
      });
    }
    this.getShowWorkspaceList();
  }

  // NzCheckbox Handlers
  onAllChecked(checked: boolean): void {
    if (checked) {
      this.shareWorkspaceSelect = [...this.searchWorkspaceList];
    } else {
      this.shareWorkspaceSelect = [];
    }
  }

  isItemChecked(item: any): boolean {
    return this.shareWorkspaceSelect.some(v => v.id === item.id);
  }

  onItemChecked(checked: boolean, item: any): void {
    if (checked) {
      this.shareWorkspaceSelect.push(item);
    } else {
      this.shareWorkspaceSelect = this.shareWorkspaceSelect.filter(v => v.id !== item.id);
    }
  }

  // NzSelect Async Search & Scroll
  public onSelectOpen(isOpen: boolean): void {
    if (isOpen) {
      this.kbSearchName = '';
      this.isSelectLoading = true;
      this.getKbs().then(res => {
        this.kbOptions = res;
        this.isSelectLoading = false;
      });
    }
  }

  public onSelectSearch(value: string): void {
    this.kbSearchName = value;
    this.isSelectLoading = true;
    this.getKbs().then(res => {
      this.kbOptions = res;
      this.isSelectLoading = false;
    });
  }

  public onSelectScrollToBottom(): void {
    if (this.kbOptions.length >= this.totalNumber) {
      return;
    }
    this.isSelectLoading = true;
    this.getKbs(this.kbOptions.length).then(res => {
      this.kbOptions = [...this.kbOptions, ...res];
      this.isSelectLoading = false;
    });
  }

  public getKbs(offset?: number): Promise<any[]> {
    return new Promise(resolve => {
      this.knowledgeRepoService.getKnowledgeBaseList({
        name: this.kbSearchName,
        limit: this.pageSize,
        offset: offset ?? 0,
        status: 'OPEN',
      }).then(res => {
        if(res?.items?.length) {
          res.items.forEach(item => {
            item.disabled = item.share_scope !== KBScope.SELF; // 已共享的置灰
          })
        }
        this.totalNumber = res?.total ?? 0;
        resolve(res?.items ?? []);
      }).catch(() => {
        resolve([]);
      });
    });
  }

  private changeKbScope() {
    return new Promise(resolve => {
      const { selectedKb } = this.formGroup.getRawValue();
      if (selectedKb) {
        const isPartial = this.formGroup.get('selRangeType').value === 'PARTIAL';
        const ids = this.shareWorkspaceSelect.map(opt => opt.id) || [];
        let params = {
          share_scope: this.formGroup.get('selRangeType').value,
          auth_workspace_id_list: isPartial ? ids : [],
        }
        if (isPartial && !ids?.length) {
          this.nzMessage.error(this.i18n.transform('select_team_limit_min_tip'), { nzDuration: 3000 });
          resolve(null);
          return;
        }
        if (isPartial && ids?.length > 50) {
          this.nzMessage.error(this.i18n.transform('select_team_limit_max_tip'), { nzDuration: 3000 });
          resolve(null);
          return;
        }
        this.knowledgeRepoService.changeKbScope(this.kbAbilitiesService.getKbId(selectedKb), params)
          .then(() => {
            this.nzMessage.success(`${this.i18n.transform('kb_share_operation')}${this.i18n.transform('success')}`);
            resolve(selectedKb);
          }).catch(() => {
          resolve(null);
        });
      } else {
        resolve(null);
      }
    });
  }
}
