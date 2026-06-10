import { CommonModule } from '@angular/common';
import { Component, DestroyRef, ElementRef, inject, ViewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { finalize, fromEvent, sampleTime, Subscription, tap } from 'rxjs';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { SkillApi } from '@agentcore/api/skill.api';
import { I18N_NAMESPACE } from '@agentcore/constants/common';
import { SKILL_RESOURCE_TYPE_CONFIG, SkillDetail, SkillDetailFromPage, SkillResourceType } from '@agentcore/constants/skill.model';
import { i18nProviderValue, I18nService } from '@agentcore/core/i18n.service';
import { I18nPipe } from '@agentcore/shared/pipes/i18n.pipe';

@Component({
  selector: 'skill-market',
  templateUrl: './skill-market.component.html',
  styleUrls: ['./skill-market.component.less'],
  standalone: true,
  imports: [
    NzIconModule,
    NzEmptyModule,
    NzTabsModule,
    NzInputModule,
    NzButtonModule,
    NzTagModule,
    NzSpinModule,
    NzSelectModule,
    CommonModule,
    FormsModule,
    I18nPipe,
  ],
  providers: [
    I18nService,
    {
      provide: I18NEXT_NAMESPACE,
      useValue: i18nProviderValue([I18N_NAMESPACE.Skill]),
    },
  ],
})
export class SkillMarketComponent {
  @ViewChild('scrollContainer') scrollContainer: ElementRef;
  resourceConfig = SKILL_RESOURCE_TYPE_CONFIG;
  tabs = [
    {
      id: SkillResourceType.ALL,
      title: this._i18n.transform(SKILL_RESOURCE_TYPE_CONFIG.all.i18key),
      active: true,
    },
    {
      id: SkillResourceType.GENERAL,
      title: this._i18n.transform(SKILL_RESOURCE_TYPE_CONFIG.general.i18key),
      active: false,
    },
    {
      id: SkillResourceType.EFFENCY,
      title: this._i18n.transform(SKILL_RESOURCE_TYPE_CONFIG.effency.i18key),
      active: false,
    },
    {
      id: SkillResourceType.TOOL,
      title: this._i18n.transform(SKILL_RESOURCE_TYPE_CONFIG.tool.i18key),
      active: false,
    },
    {
      id: SkillResourceType.DESIGN,
      title: this._i18n.transform(SKILL_RESOURCE_TYPE_CONFIG.design.i18key),
      active: false,
    },
    {
      id: SkillResourceType.CREATE,
      title: this._i18n.transform(SKILL_RESOURCE_TYPE_CONFIG.create.i18key),
      active: false,
    },
  ];

  selectedTab = this.tabs.find(e => e.active) ?? this.tabs[0];
  tags: Array<{ label: string; value: string }> = [];
  searchValue = '';
  cards: SkillDetail[] = [];
  loading = false;
  destroyRef = inject(DestroyRef);
  filter: Record<string, any> = {};
  hasFilter = false;
  loadSub: Subscription;
  total = 0;
  public activeTabIndex = 0;
  constructor(
    private _router: Router,
    private _skillApi: SkillApi,
    private _i18n: I18nService
  ) {}

  ngAfterViewInit() {
    this.watchLatest();
    this.loadMore();
  }

  onActiveChange(index: number): void {
    const tab = this.tabs[index];
    this.selectedTab = this.tabs.find(e => e.id === tab.id);
    this.refresh();
  }

  loadMore() {
    if (this.cards.length === 0 || this.cards.length < this.total) {
      this.loading = true;
      this.loadSub && this.loadSub?.unsubscribe();
      this.loadSub = this._skillApi
        .queryResourceSkill({
          offset: this.cards.length,
          limit: 100,
          tagId: SKILL_RESOURCE_TYPE_CONFIG[this.selectedTab!.id]?.id,
          name: this.filter?.name,
          description: this.filter?.description,
        })
        .pipe(
          tap(res => {
            this.total = res.total;
            this.cards = this.cards.concat(res.items);
          }),
          finalize(() => {
            this.loading = false;
          }),
          takeUntilDestroyed(this.destroyRef)
        )
        .subscribe();
    }
  }

  watchLatest() {
    fromEvent(this.scrollContainer.nativeElement, 'scroll')
      .pipe(sampleTime(100), takeUntilDestroyed(this.destroyRef))
      .subscribe((event: any) => {
        this.checkScroll(event.target);
      });
  }

  getTitleLabel() {
    return this.cards.length
      ? this._i18n.transform('skill.resource.title.count', { title: this.selectedTab.title, count: this.cards.length })
      : this.selectedTab.title;
  }

  onClear() {
    this.searchValue = '';
    this.filter = {};
    this.hasFilter = false;
    this.refresh();
  }

  onNgModelChange(e) {
    this.filter = {};
    if (this.searchValue) {
      this.filter.name = this.searchValue;
    }
    this.hasFilter = Object.keys(this.filter).length > 0;
    this.refresh();
  }

  gotoDetail(card) {
    this._router.navigate(['/home/skill/detail'], {
      queryParams: {
        from: SkillDetailFromPage.RESOURCE,
        id: card.skillId,
      },
    });
  }

  refresh() {
    this.cards = [];
    this.loadMore();
  }

  private checkScroll(target: HTMLElement) {
    // 关键公式：可视高度 + 滚动距离 >= 实际内容总高度
    const threshold = 50; // 预留 50px 缓冲区，提前加载
    const isAtBottom = target.scrollTop + target.clientHeight >= target.scrollHeight - threshold;
    if (isAtBottom) {
      this.loadMore();
    }
  }
}
