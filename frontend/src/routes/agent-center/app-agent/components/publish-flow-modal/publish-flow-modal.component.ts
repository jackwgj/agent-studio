import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
  QueryList,
  ViewChildren
} from "@angular/core";
import { NgForm } from "@angular/forms";
import { I18nNamespace } from "@i18n";
import { I18NEXT_NAMESPACE } from "angular-i18next";
import { CommonUtils } from "src/utils/common.util";
import { AppFlowRepoService } from "@services/agent-center/app-flow-repo.service";
import {
  IWorkflowApp,
  IWorkflowAppField
} from "@routes/agent-center/app-flow/app-flow.types";
import { NonEmptyValidatorDirective } from "@shared/directives/variable-name-validator.directive";
import { MODULES } from "@shared/modules";

export interface IWorkflowAppFieldWithExpand extends IWorkflowAppField {
  expand: boolean;
}

@Component({
  selector: "meta-publish-flow-modal",
  templateUrl: "./publish-flow-modal.component.html",
  styleUrls: ["./publish-flow-modal.component.less"],
  imports: [
    MODULES,
    NonEmptyValidatorDirective
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER]
    }
  ]
})
export class PublishFlowModalComponent implements OnInit {
  @ViewChild("inputForm") inputForm: NgForm;

  @ViewChildren("outputForm") outputForm!: QueryList<NgForm>;

  @Input() workflowId: string;

  @Input() tagList: any;

  @Input() versionId: string;

  @Output("confirm") confirm = new EventEmitter();

  public lang = CommonUtils.getLanguage();

  public name = "";

  public description = "";

  public tagsSelected: string[] = [];

  public expandStart = true;

  public expandEnd = true;

  public inputParams: IWorkflowAppFieldWithExpand[] = [];

  public outputParams: IWorkflowAppFieldWithExpand[] = [];

  public prologue: string;

  public suggest_queries: string[];

  constructor(private flowRepoServe: AppFlowRepoService) {
  }

  ngOnInit() {
    this.tagsSelected = this.tagList
      .filter((v) => v.selected)
      .map((v) => v.tag_id);
    this.getDetail();
  }

  async getDetail() {
    const res = (await this.flowRepoServe.getFrontParams(
      this.workflowId,
      this.versionId
    )) as IWorkflowApp;
    this.name = res.name;
    this.description = res.description;
    this.inputParams = res.inputs.map((item: IWorkflowAppField) => ({
      ...item,
      expand: false
    }));
    this.prologue = res.prologue;
    this.suggest_queries = res.suggest_queries;
    this.outputParams = res.outputs.map(
      (item: IWorkflowAppField, index: number) => {
        if (index === 0) {
          return {
            ...item,
            expand: true
          };
        } else {
          return {
            ...item,
            expand: false
          };
        }
      }
    );
  }

  chooseCategory(id: string) {
    if (this.tagsSelected.includes(id)) {
      const index = this.tagsSelected.findIndex((v) => v === id);
      this.tagsSelected.splice(index, 1);
    } else if (this.tagsSelected.length < 2) {
      this.tagsSelected.push(id);
    }
  }

  tagsClass(id: string) {
    return {
      selected: this.tagsSelected.includes(id),
      disabled:
        !this.tagsSelected.includes(id) && this.tagsSelected.length >= 2,
      unselected:
        !this.tagsSelected.includes(id) && this.tagsSelected.length < 2
    };
  }

  expandParam(item: IWorkflowAppFieldWithExpand, form: NgForm) {
    item.expand = !item.expand;
    form.resetForm();
  }

  onComfirmExpandNode(): void {
    this.expandStart = true;
    this.expandEnd = true;
    this.inputParams.forEach((item) => {
      item.expand = true;
    });
    this.outputParams.forEach((item) => {
      item.expand = true;
    });
  }

  onConfirm() {
    const inputs = this.inputParams.map((item) => {
      const { expand, ...rest } = item;
      return rest;
    });
    const outputs = this.outputParams.map((item) => {
      const { expand, ...rest } = item;
      return rest;
    });
    const data = {
      tagsSelected: this.tagsSelected,
      inputs,
      outputs,
      prologue: this.prologue,
      suggest_queries: this.suggest_queries
    };
    this.confirm.emit(data);
  }

  dismiss() {
  }

  close() {
  }
}
