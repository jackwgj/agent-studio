import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { KnowledgeRepoStatus } from "@enums/knowledge-repo.enum";
import { I18nNamespace } from "@i18n";
import { WORKFLOW_SVGS } from "@routes/agent-center/app-flow/flow.const";
import { knowledgeBaseStatusMap } from "@routes/knowledge-center/knowledge-base-list/knowledge-base-list.map";
import { KbAbilitiesService } from "@services/knowledge-center/kb-abilities.service";
import { MODULES } from "@shared/modules";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { environment } from "src/environment/environment";
import { NzModalService } from "ng-zorro-antd/modal";

@Component({
  selector: "knowledge-card-used",
  templateUrl: "./knowledge-card-used.component.html",
  styleUrls: ["./knowledge-card-used.component.less"],
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER]
    }
  ]
})
export class KnowledgeCardUsed implements OnInit {
  @Input() knowledge;

  @Input() isShowDelPrompt = false;

  @Output() deleteKnowledge = new EventEmitter();
  @Output() valueChange = new EventEmitter();

  public switchState: boolean = false;

  selected = [];

  public isPOC = environment.envType === "poc";

  protected readonly knowledgeBaseStatusMap = knowledgeBaseStatusMap;

  constructor(
    private i18n: I18NextEagerPipe,
    public kbAbilitiesService: KbAbilitiesService,
    private nzModal: NzModalService
  ) {
  }

  public get isClosed() {
    return this.knowledge?.status === KnowledgeRepoStatus.CLOSE;
  }

  public get icon() {
    return this.knowledge?.icon ?? WORKFLOW_SVGS.KnowledgeRepo;
  }

  public get tipContent() {
    if (this.isClosed) {
      return this.i18n.transform("open_kb_tipContent");
    }
    return "";
  }

  public get cardClass() {
    if (!this.isClosed) {
      return "";
    }
    return "unavailable";
  }

  ngOnInit(): void {
  }

  public handleDelete(e: Event) {
    e.stopPropagation();
    if (this.kbAbilitiesService.isResourceDisabled) {
      return;
    }

    if (this.isShowDelPrompt) {
      const modalInstance = this.nzModal.confirm({
        nzTitle: this.i18n.transform("del_tip"),
        nzContent: this.i18n.transform("kb_del_tag_tip"),
        nzOnOk: () => {
          this.deleteKnowledge.emit(this.knowledge);
          modalInstance.close();
          this.valueChange.emit();
        },
        nzOnCancel: () => modalInstance.close()
      });
    } else {
      this.deleteKnowledge.emit(this.knowledge);
      this.valueChange.emit();
    }
  }
}
