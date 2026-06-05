import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild
} from "@angular/core";
import { MODULES } from "@shared/modules";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { I18nNamespace } from "@i18n";
import { ModalBaseComponent } from "../base/modal-base.component";
import { getInitInputParamConfig, WORKFLOW_SVGS } from "../../flow.const";
import { AppFlowService } from "../../app-flow.service";
import { NodeService } from "../../node.service";
import { AccBlockComponent } from "../acc-block/acc-block.component";
import { ReadonlyParamsTreeComponent } from "../readonly-params/readonly-params-tree.component";
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  NgForm,
  Validators
} from "@angular/forms";
import { TextFieldModule } from "@angular/cdk/text-field";
import { Subject, takeUntil } from "rxjs";
import type { IParamRef, IQANode, IWorkflowField } from "../../node.type";
import { NodeUtils } from "../utils";
import { CommonValidation } from "@shared/validation/commonValidation";
import {
  NonEmptyValidatorDirective,
  ValueValidityValidatorDirective
} from "@shared/directives/variable-name-validator.directive";
import {
  RefSelectedRequireDirective,
  PositiveIntIndexValidatorDirective
} from "@shared/directives/common-validator.directive";
import { cloneDeep } from "lodash";
import { EValidationMode } from "@routes/agent-center/types/common.types";
import { CmdTextareaComponent } from "../cmd-textarea/cmd-textarea.component";
import { StructOutputComponent } from "@routes/agent-center/app-flow/components/struct-output/struct-output.component";
import { EditNameComponent } from "@routes/agent-center/app-flow/components/edit-name/edit-name.component";
import { NodeDescriptionComponent } from "../node-description/node-description.component";
import { InputTreeSelect } from "src/routes/agent-center/app-flow/components/input-tree-select/input-tree-select";
import { NodeTypeTopic } from "@routes/agent-center/types/common.types";
import { HelpCenterService } from "@services/help-center.service";
import { CommonService } from "@services/common.service";
import { NzDrawerService } from "ng-zorro-antd/drawer";

interface StrategyType {
  text: string;
  id: string;
}

interface WriteType {
  text: string;
  id: boolean;
}

interface ResponseType {
  text: string;
  id: string;
}

@Component({
  selector: "meta-QA-modal",
  templateUrl: "./QA-modal.component.html",
  styleUrls: [
    "./QA-modal.component.scss",
    "../common-styles.less",
    "../../../../../styles/text-field-prebuilt.css"
  ],
  standalone: true,
  imports: [
    MODULES,
    AccBlockComponent,
    ReadonlyParamsTreeComponent,
    TextFieldModule,
    NonEmptyValidatorDirective,
    RefSelectedRequireDirective,
    PositiveIntIndexValidatorDirective,
    ValueValidityValidatorDirective,
    CmdTextareaComponent,
    StructOutputComponent,
    EditNameComponent,
    NodeDescriptionComponent,
    InputTreeSelect
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER]
    },
    NzDrawerService
  ]
})
export class QAModalComponent
  extends ModalBaseComponent
  implements OnInit, OnDestroy {
  @Input("names") names: string[];

  @Input("nodeInfo") nodeInfo: IQANode;

  @Output("confirm") confirm = new EventEmitter<any>();

  @ViewChild("inputForm") inputForm: NgForm;

  @ViewChild("structOutput") structOutput: StructOutputComponent;

  public iconUrl = WORKFLOW_SVGS.QA;

  public validationRules = [Validators.required];

  public strategyTip = this.i18n.transform("qamodalcomponent_564");

  public writeTip = this.i18n.transform("qamodalcomponent_565");

  public strategyTypes: StrategyType[] = [
    {
      text: this.i18n.transform("qamodalcomponent_557"),
      id: "random"
    },
    {
      text: this.i18n.transform("qamodalcomponent_558"),
      id: "index"
    }
  ];

  public writeTypes: WriteType[] = [
    {
      text: this.i18n.transform("qamodalcomponent_559"),
      id: true
    },
    {
      text: this.i18n.transform("qamodalcomponent_560"),
      id: false
    }
  ];
  public writeTypeSelected: WriteType = this.writeTypes[0];
  public strategyTypeSelected: StrategyType = this.strategyTypes[0];

  public responseTypes: ResponseType[] = [
    {
      text: this.i18n.transform("qamodalcomponent_561"),
      id: "userReply"
    },
    {
      text: this.i18n.transform("qamodalcomponent_562"),
      id: "noReply"
    }
  ];

  public responseTypeSelected: ResponseType = this.responseTypes[0];

  public questionListForm: FormGroup;

  private isCompositionActive = false;

  public inputChanged = new Subject<string>();

  public inputParams: IWorkflowField[] = [];

  public nameRefOptions: IParamRef[] = [];

  public sourceOptions = [
    { label: this.i18n.transform("ref"), value: "ref" },
    { label: this.i18n.transform("literal"), value: "literal" }
  ];

  public indexSourceOptions = [
    { label: this.i18n.transform("ref"), value: "ref" },
    { label: "Integer", value: "literal" }
  ];

  public EValidationMode = EValidationMode;

  needValid = false;

  isFirstInit = 0;
  updateTimeout: any = null;

  constructor(
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    private fb: FormBuilder,
    private i18n: I18NextEagerPipe,
    private helpCenterService: HelpCenterService,
    protected commonService: CommonService
  ) {
    super(nodeServ, appFlowServ);

    this.questionListForm = this.fb.group({
      headers: fb.array([])
    });
  }

  override ngOnInit() {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();

    this.validationRules.push(
      CommonValidation.nameUniquenessVerify(
        this.names,
        this.i18n.transform("name_uniqueness"),
        this.nodeInfo.name
      )
    );

    this.updateRefs();

    this.setQAFormData(this.nodeInfo, this.questionListForm);
    if (this.getHeadersControls().controls.length < 50) {
      this.addOneQuestion();
    }
  }

  get questionLimit() {
    const controls = this.getHeadersControls().controls;
    const lastControlValue = controls[controls.length - 1]?.get("key")?.value;
    if (lastControlValue === "") {
      return controls.length - 1;
    }

    return controls.length;
  }

  get tipVals() {
    return this.inputParams.map((param) => param.name);
  }

  ngAfterViewInit() {
    if (this.appFlowServ.testRunVerificationError) {
      setTimeout(() => {
        this.validateNode();
      });
    }
  }

  validateNode() {
    const inputErr = this.inputForm?.form
      ? this.checkForm()
      : null;
    const controls = this.getHeadersControls().controls;
    this.needValid = true;
  }

  checkForm() {
    if (this.inputForm?.form) {
      this.inputForm.form.markAllAsTouched();
      this.inputForm.form.updateValueAndValidity();
    }
  }

  dismiss(): void {
  }

  close(): void {
  }

  public onRefUpdate(info: IParamRef[]) {
    this.nameRefOptions = info;
    if (this.isInit) {
      this.inputParams = NodeUtils.initInputs(
        this.nodeInfo.inputs,
        this.nameRefOptions
      );
    } else {
      NodeUtils.reSelectRefsWithNewOps(this.inputParams, this.nameRefOptions);
    }

    this.isInit = false;
  }

  public onTypeChange(row: IWorkflowField) {
    row.value.content = NodeUtils.getChangeContent(row.value.type);
    this.onSave();
  }

  public getHeadersControls(): FormArray {
    return this.questionListForm.get("headers") as FormArray;
  }

  public deleteOneQuestion(control: AbstractControl<string, string>) {
    if (this.isTextareaEmpty(control)) return;

    const headersControls = this.getHeadersControls().controls;

    const index = headersControls.indexOf(control);
    if (index !== -1) {
      this.getHeadersControls().removeAt(index);
    }
    this.onBlur(control);
  }

  public onBlur(control: AbstractControl<string, string>) {
    const controls = this.getHeadersControls().controls;
    const emptyControls = controls.filter((item) => !item.get("key").value);

    const index = controls.indexOf(control);
    if (emptyControls.length > 1) {
      (this.questionListForm.get("headers") as FormArray).removeAt(index);
    }
    this.reorderTextarea();
    this.onSave();
  }

  public isTextareaEmpty(control: AbstractControl<string, string>): boolean {
    return !control.get("key").value;
  }

  private reorderTextarea(): void {
    const formArray = this.questionListForm.get("headers") as FormArray;
    const controls = formArray.controls;
    const nonEmptyControls = controls.filter((item) => item.get("key").value);
    const emptyControls = controls.filter((item) => !item.get("key").value);

    formArray.clear();
    [...nonEmptyControls, ...emptyControls].forEach((item) =>
      formArray.push(item)
    );
  }

  public addOneQuestion() {
    (this.questionListForm.get("headers") as FormArray).push(
      this.fb.group({
        key: new FormControl("")
      })
    );
  }

  public changeStrategyType(type: StrategyType): void {
    const indexParam = this.inputParams.find((i) => i.name === "index");

    if (type.id === "index") {
      if (!indexParam) {
        const updatedInputParams = cloneDeep(this.inputParams);
        updatedInputParams.unshift({
          ...getInitInputParamConfig(),
          refs: cloneDeep(this.nameRefOptions),
          name: "index",
          type: "integer"
        });
        this.inputParams = [...updatedInputParams];
      } else {
        if (!indexParam.value) {
          return;
        }

        const { type, content } = indexParam.value;

        if (type === "literal" && isNaN(Number(content))) {
          indexParam.value.content = "";
        }

        if (type === "ref" && Array.isArray(content)) {
          const isIntType = content.some((item) => item.type === "integer");
          if (!isIntType) {
            indexParam.value.content = "";
          }
        }
      }
    } else {
      let updatedInputParams = cloneDeep(this.inputParams);
      updatedInputParams = updatedInputParams.filter((v) => {
        return v.name !== "index";
      });
      this.inputParams = [...updatedInputParams];
    }

    this.checkForm();
    this.onSave();
  }

  public isControlInvalid(control: any): boolean {
    const keyControl = control.get("key");
    return (
      keyControl &&
      keyControl?.invalid &&
      (keyControl?.dirty || keyControl?.touched)
    );
  }

  public onCompositionStart(index: number) {
    if (
      !this.isLastTextarea(index) ||
      this.getHeadersControls().controls.length === 50
    ) {
      return;
    }
    this.isCompositionActive = true;
    this.addOneQuestion();
  }

  public onCompositionEnd() {
    this.isCompositionActive = false;
  }

  public onInputChange(event: any, index: number): void {
    this.inputChanged.next(event);
    if (this.inputShouldAddQuestion(index)) {
      this.addOneQuestion();
    }
  }

  public onNameChange() {
    window.setTimeout(() => {
      this.checkForm();
    });
  }

  public getInputNames(index: number): {
    existingValues: string[];
  } {
    const names = this.inputParams.map((p) => p.name);
    names.splice(index, 1);
    return { existingValues: names };
  }

  public addInputParam() {
    this.inputParams.push({
      ...getInitInputParamConfig(),
      refs: cloneDeep(this.nameRefOptions)
    });
  }

  public deleteInputParam(index: number): void {
    if (this.isIndexParam(index)) {
      return;
    }
    this.inputParams.splice(index, 1);
  }

  public isIndexParam(i: number) {
    return this.strategyTypeSelected.id === "index" && i === 0;
  }

  private setQAFormData(nodeInfo: IQANode, questionListForm: FormGroup) {
    if (nodeInfo.configs?.qaStrategy) {
      this.strategyTypeSelected = this.strategyTypes.find(
        (item) => item.id === nodeInfo.configs.qaStrategy
      );
    }

    if (nodeInfo.configs?.needReply !== undefined) {
      this.responseTypeSelected = this.responseTypes.find(
        (item) =>
          item.id === (nodeInfo.configs.needReply ? "userReply" : "noReply")
      );
    } else {
      this.responseTypeSelected = this.responseTypes[0];
    }

    if (nodeInfo.configs?.enable_history !== undefined) {
      this.writeTypeSelected = this.writeTypes.find(
        (item) => item.id === !!nodeInfo.configs.enable_history
      );
    } else {
      this.writeTypeSelected = this.writeTypes[0];
    }

    if (nodeInfo.configs?.options?.length) {
      (questionListForm.controls.headers as FormArray).clear();
      nodeInfo.configs?.options?.forEach((qHistory) => {
        (questionListForm.controls.headers as FormArray).push(
          this.fb.group({
            key: new FormControl(qHistory)
          })
        );
      });
    }
  }

  private updateRefs() {
    const parentNode = this.getParentNodeInfo(this.appFlowServ.getGraph());
    if (parentNode) {
      this.getLoopInnerNodeRefs(parentNode, { strOnly: true }).subscribe(
        (info) => {
          this.onRefUpdate(info);
        }
      );
    } else {
      this.getSelfRefs().subscribe((info) => {
        this.onRefUpdate(info);
      });
    }
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    this.modelCloseSave();
    this.helpCenterService.hideHelpPanel();
  }

  private isLastTextarea(index: number): boolean {
    const controls = this.getHeadersControls().controls;
    return index === controls.length - 1;
  }

  modelCloseSave() {
    if (!this.tagCompareNoChange()) {
      this.appFlowServ.setNodeModalCloseMonitor({ id: this.nodeInfo.id });
    }
    this.handelSave();
  }

  treeSelect() {
    setTimeout(() => {
      this.onSave();
    });
  }

  private inputShouldAddQuestion(index: number): boolean {
    return (
      !this.isCompositionActive &&
      this.isLastTextarea(index) &&
      this.getHeadersControls().controls.length < 50
    );
  }

  onSave() {
    this.changeUpdateTime();
    if (this.appFlowServ.testRunVerificationError) {
      this.handelSave();
    }
  }

  handelSave() {
    if (this.tagCompareNoChange()) {
      return;
    }
    let questionList: string[] = [];
    const headers = (this.questionListForm.get("headers") as FormArray)
      .controls;
    if (headers) {
      headers.forEach((item: AbstractControl<string, string>) => {
        const qItemValue = item.get("key")?.value;

        if (qItemValue !== "") {
          questionList.push(qItemValue);
        }
      });
    }
    const struct_output_template = this.structOutput.struct_output_template;
    const isStructMessage = this.structOutput.structEnable;
    this.appFlowServ.setNodeSaveMonitor({
      nodeData: {
        ...this.nodeInfo,
        inputs: NodeUtils.getDtoInputs(this.inputParams),
        configs: {
          ...this.nodeInfo.configs,
          qaStrategy: this.strategyTypeSelected.id,
          options: questionList,
          needReply: this.responseTypeSelected.id === "noReply" ? false : true,
          enable_history: this.writeTypeSelected.id,
          struct_output_template,
          isStructMessage
        }
      }
    });
    if (this.updateTimeout) {
      clearTimeout(this.updateTimeout);
      this.updateTimeout = null;
    }
    this.updateTimeout = setTimeout(() => {
      this.updateChangeAndInitTime();
    }, 200);
  }

  inputOnSave() {
    if (this.appFlowServ.testRunVerificationError) {
      this.handelSave();
    }
  }

  structOnSave() {
    if (this.isFirstInit <= 0) {
      this.isFirstInit += 1;
      return;
    }
    this.onSave();
  }
}
