import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { NzDrawerService } from "ng-zorro-antd/drawer";
import { NzMessageService } from "ng-zorro-antd/message";
import { CollapseTitleComponent } from "../collapse-title/collapse-title.component";
import { ScenarioGuideModalComponent } from "@routes/knowledge-center/components/scenario-guide-modal/scenario-guide-modal.component";
import { cloneDeep } from "lodash";
import { cdnAssetUrl } from "src/single-spa/assets-url";
import {
  ISceneData,
  ISkillOption,
  MAX_GUIDE_COUNT
} from "@routes/knowledge-center/components/scenario-guide-modal/scenario-guide-modal.constant";
import { AgentDataService } from "@services/agent-center/agent-data.service";
import { Subject, takeUntil } from "rxjs";
import { ScenarioGuideModalService } from "@routes/knowledge-center/components/scenario-guide-modal/scenario-guide-modal.service";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { I18nNamespace } from "@i18n";
import { MODULES } from "@shared/modules";

@Component({
  selector: "task-planning",
  templateUrl: "./task-planning.component.html",
  styleUrls: ["./task-planning.component.scss"],
  standalone: true,
  imports: [MODULES, CommonModule, CollapseTitleComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER]
    }
  ]
})
export class TaskPlanningComponent implements OnInit, OnChanges, OnDestroy {
  @Input() mcpServersAdded;
  @Input() pluginAdded;
  @Input() flowAdded;
  @Input() sceneList: ISceneData[] = [];
  @Input() isFlowReadonly: boolean;

  @Output() doTriggerSaveScenes = new EventEmitter<any>();

  public MAX_GUIDE_COUNT = MAX_GUIDE_COUNT;
  public changeUrl = cdnAssetUrl;
  public cardConfig = {
    title: "",
    subTitle: "",
    description: "",
    helpTip: ""
  };
  public collapseLoading = false;
  public collapsed = true;
  public resetGuideCount = MAX_GUIDE_COUNT;
  public skillSetOptions: ISkillOption[];
  private ngUnsubscribe = new Subject<void>();

  constructor(
    private i18n: I18NextEagerPipe,
    private agentDataServe: AgentDataService,
    private scenarioGuideModalServ: ScenarioGuideModalService,
    private nzDrawerService: NzDrawerService,
    private nzMessageServ: NzMessageService
  ) {
  }

  ngOnInit() {
    this.cardConfig = {
      title: this.i18n.transform("task_planning_label"),
      subTitle: this.i18n.transform("scenario_guide_label"),
      description: this.i18n.transform("scenario_guide_desc"),
      helpTip: this.i18n.transform("scenario_guide_tip")
    };
    this.agentDataServe
      .pluginAndFlowListUpdate$()
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe((list: any) => {
        this.skillSetOptions = this.scenarioGuideModalServ.initSkillOptions({
          pluginAdded: {
            ...this.pluginAdded,
            list: list.plugins
          },
          flowAdded: {
            ...this.flowAdded,
            list: list.flows
          }
        });
        this.updateSceneData();
      });
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.sceneList) {
      this.collapsed = !changes.sceneList.currentValue.length;
      (changes.sceneList.currentValue as ISceneData[])?.forEach((item) => {
        item.isNoTool = item.guidelines?.some((item) => !item.tools?.length);
      });
      this.updateSceneData();
    }
  }

  public addOrEditBtnClick(originData?: ISceneData): void {
    const drawerRef = this.nzDrawerService.create({
      nzTitle: "",
      nzWidth: 700,
      nzPlacement: "right",
      nzMaskClosable: true,
      nzClosable: true,
      nzBodyStyle: { padding: 0 },
      nzContent: ScenarioGuideModalComponent,
      nzContentParams: {
        originData: cloneDeep(originData),
        skillSetOptions: this.skillSetOptions,
        curSceneMaxGuideCount: this.resetGuideCount + (originData?.guidelines?.length || 0),
        saveData: () => {
          const modalInstance = drawerRef.getContentComponent();
          if (!modalInstance) return;
          modalInstance.confirmBtnLoading = true;
          const curSceneList = cloneDeep(this.sceneList);
          const sceneIndex = this.sceneList.findIndex(
            (item) => item.id === modalInstance.modalData.id
          );
          if (modalInstance.modalData.id && sceneIndex !== -1) {
            curSceneList[sceneIndex] = modalInstance.modalData;
          } else {
            curSceneList.push(modalInstance.modalData);
          }
          const data = { scenes: curSceneList };
          this.doTriggerSaveScenes.emit({
            data,
            successCb: () => {
              drawerRef.close();
              this.nzMessageServ.success(
                this.i18n.transform(
                  modalInstance.modalData.id
                    ? "update_scene_success_tip"
                    : "add_scene_success_tip"
                )
              );
            },
            finallyCb: () => {
              modalInstance.confirmBtnLoading = false;
            }
          });
        }
      }
    });
  }

  public deleteBtnClick(rowData: ISceneData): void {
    const data = {
      scenes: this.sceneList.filter((item) => item.id !== rowData.id)
    };
    this.collapseLoading = true;
    this.doTriggerSaveScenes.emit({
      data,
      successCb: () => {
        this.nzMessageServ.success(
          this.i18n.transform("delete_scene_success_tip")
        );
      },
      finallyCb: () => {
        this.collapseLoading = false;
      }
    });
  }

  private updateSceneData(): void {
    if (!this.skillSetOptions) {
      return;
    }
    let totalGuide = 0;
    this.sceneList?.forEach((scene) => {
      let sceneExistToolInvalid = false;
      let sceneIsNoTool = false;
      totalGuide = totalGuide + (scene.guidelines?.length || 0);
      scene.guidelines?.forEach((guide) => {
        const deletedSkills = [];
        const effectiveSkills = [];
        guide?.tools?.forEach((tool) => {
          const matchGroup = this.skillSetOptions.find(
            (group) => group.skillType === tool.type
          );
          const matchOption = matchGroup?.children?.find(
            (option) => option.id === tool.id
          );
          if (matchOption) {
            tool.skillOption = matchOption;
            if (tool.skillOption.disabled) {
              effectiveSkills.push(tool.skillOption);
            }
          } else {
            tool.skillOption = undefined;
            deletedSkills.push(tool);
          }
        });
        guide.deletedSkills = deletedSkills;
        guide.effectiveSkills = effectiveSkills;
        guide.guideExistToolInvalid = !!(
          guide.deletedSkills.length || guide.effectiveSkills.length
        );
        guide.isNoTool =
          !guide.tools?.length ||
          guide.deletedSkills.length + guide.effectiveSkills.length ===
          guide.tools.length;
        sceneExistToolInvalid =
          sceneExistToolInvalid ||
          (!guide.isNoTool &&
            (!!guide.deletedSkills.length || !!guide.effectiveSkills.length));
        sceneIsNoTool = sceneIsNoTool || guide.isNoTool;
      });
      scene.sceneExistToolInvalid = sceneExistToolInvalid;
      scene.isNoTool = sceneIsNoTool;
    });
    this.resetGuideCount = this.MAX_GUIDE_COUNT - totalGuide;
  }
}
