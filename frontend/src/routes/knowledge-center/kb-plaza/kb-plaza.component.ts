import { CommonModule } from "@angular/common";
import { Component, computed, OnInit, signal, ViewChild } from "@angular/core";
import { I18nNamespace } from "@i18n";
import { KbCardDisplayComponent } from "@routes/knowledge-center/components/kb-card-display/kb-card-display.component";
import { ShareKbComponent } from "@routes/knowledge-center/components/share-kb/share-kb.component";
import { KbCardDisplayConfig, KbShareTargetType, KbTagType, KBType } from "@routes/knowledge-center/knowledge-base.interface";
import { KBScope } from "@routes/knowledge-center/knowledge.types";
import { KnowledgeRepoService } from "@services/agent-center/knowledge.service";
import { KbAbilitiesService } from "@services/knowledge-center/kb-abilities.service";
import { NewCommonNoDataWithBtnComponent } from "@shared/components/new-common-no-data-with-btn/new-common-no-data-with-btn.component";
import { ElIntersectionObsDirective } from "@shared/directives/el-intersection-obs.directive";
import { MODULES } from "@shared/modules";
import { SetSidebarVisibilityService } from "@shared/services/set-sidebar-visibility.service";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { CommonUtils } from "src/utils/common.util";

@Component({
  selector: "kb-plaza",
  templateUrl: "./kb-plaza.component.html",
  styleUrls: ["./kb-plaza.component.less"],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    NewCommonNoDataWithBtnComponent,
    ShareKbComponent,
    KbCardDisplayComponent,
    ElIntersectionObsDirective
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.AGENT_CENTER
      ]
    }
  ]
})
export class KbPlazaComponent implements OnInit {
  @ViewChild("cardContainer") cardContainer;

  kbList = [];

  currentPage = 1;

  totalNumber = 0;

  pageSize = 24;

  loading = false;

  searchedTags: string = "";

  cardDisplayConfig: KbCardDisplayConfig = {
    cardSize: "middle",
    descriptionConfig: {
      enable: true,
      maxLine: 2
    },
    tagsConfig: {
      enable: [KbTagType.INVALID, KbTagType.TYPE]
    },
    footerConfig: {
      userShow: true,
      timeShow: true,
      kbOperation: true
    },
    detailLink: {
      enable: true
    }
  };

  kbTypeSearchValue = "";

  kbTypeOptions: Array<any> = [
    {
      label: this.i18n.transform("all"),
      value: ""
    },
    {
      label: this.i18n.transform("default"),
      value: KBType.INTERNAL
    },
    {
      label: this.i18n.transform("third_party"),
      value: KBType.EXTERNAL
    }
  ];

  protected readonly KBScope = KBScope;

  public appTabs: Array<{ title: string; id: KbShareTargetType; active: number }> = [
    {
      title: this.i18n.transform("other_share"),
      id: KbShareTargetType.OTHER_SHARED,
      active: 0
    },
    {
      title: this.i18n.transform("my_share"),
      id: KbShareTargetType.MY_SHARED,
      active: 1
    }
  ];
  public shareType = signal(KbShareTargetType.OTHER_SHARED);

  pageTitle = computed(() => {
    const tabName = this.appTabs.find(a => a.id === this.shareType()).title ?? this.appTabs[0].title;
    const suffix = CommonUtils.getLanguage() === "zh-cn" ? "" : " ";
    return [tabName, suffix].join("");
  });

  get totalCount() {
    if (this.searchedTags.length === 0 && this.totalNumber === 0) {
      return "";
    }
    return ` (${this.totalNumber})`;
  }

  constructor(
    private i18n: I18NextEagerPipe,
    private knowledgeRepoService: KnowledgeRepoService,
    public kbAbilitiesService: KbAbilitiesService,
    private sidebarServ: SetSidebarVisibilityService
  ) {
  }

  ngOnInit(): void {
    this.sidebarServ.setSidebarsVisibilityByState("hideSidebar");
    this.getKBInSearch();
  }

  ngOnDestroy(): void {
    this.sidebarServ.setSidebarsVisibilityByState("destroy");
  }

  public changeAppTab(selectedIndex: number) {
    const item = this.appTabs[selectedIndex];
    this.shareType.set(item.id);
    this.getKBInSearch();
  }

  filterClearSearch() {
    this.searchedTags = "";
    this.getKBInSearch();
  }

  getKBInSearch() {
    this.currentPage = 1;
    this.getKbs();
  }

  get isCardScroll() {
    return this.cardContainer?.nativeElement?.scrollHeight > this.cardContainer?.nativeElement?.clientHeight;
  }

  public getKbs(isLoadMore = false) {
    if ((isLoadMore && this.kbList.length >= this.totalNumber) || this.loading) {
      return;
    }
    this.loading = true;
    this.totalNumber = 0;
    const searchParams = {
      name: this.searchedTags
    };
    this.knowledgeRepoService.getKnowledgeBaseList({
      ...searchParams,
      limit: this.pageSize,
      offset: (this.currentPage - 1) * this.pageSize,
      share_type: this.shareType(),
      type: this.kbTypeSearchValue || null
    }).then(result => {
      if (isLoadMore) {
        this.kbList = [...this.kbList, ...(result.items ?? [])];
      } else {
        this.cardContainer?.nativeElement?.scroll?.(0, 0);
        this.kbList = result.items ?? [];
      }
      this.totalNumber = result.total;
      if (this.kbList.length < this.totalNumber) {
        this.currentPage++;
      }
      this.loading = false;
    }).catch(() => {
      this.loading = false;
    });
  }

  shareSuccess() {
    this.getKBInSearch();
  }
}
