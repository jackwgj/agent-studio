/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import {
  CdkDrag,
  CdkDragDrop,
  CdkDropList,
  DragDropModule,
  moveItemInArray
} from "@angular/cdk/drag-drop";
import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild
} from "@angular/core";
import { NgForm, Validators } from "@angular/forms";
import { I18nNamespace } from "@i18n";
import { RefSelectedRequireDirective } from "@shared/directives/common-validator.directive";
import { MODULES } from "@shared/modules";
import { CommonValidation } from "@shared/validation/commonValidation";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { cloneDeep } from "lodash";
import { ParamLabelPipe } from "src/pipes/param-label.pipe";
import { AppFlowService } from "../../app-flow.service";
import {
  getInitOutputParamConfig,
  getInitRefParamConfig,
  WORKFLOW_SVGS
} from "../../flow.const";
import { NodeService } from "../../node.service";
import {
  IAggregationGroups,
  IParamRef,
  IRefContentType,
  IWorkflowField,
  IWorkflowFieldType,
  type IAggregationNode
} from "../../node.type";
import { AccBlockComponent } from "../acc-block/acc-block.component";
import { ModalBaseComponent } from "../base/modal-base.component";
import { NodeUtils } from "../utils";
import { EditNameComponent } from "@routes/agent-center/app-flow/components/edit-name/edit-name.component";
import { NodeDescriptionComponent } from "../node-description/node-description.component";
import { NodeTypeTopic } from "@routes/agent-center/types/common.types";
import { HelpCenterService } from "@services/help-center.service";
import { CommonService } from "@services/common.service";
import { InputTreeSelect } from "src/routes/agent-center/app-flow/components/input-tree-select/input-tree-select";

export interface IAggGroup {
  name: string;
  type: IWorkflowFieldType;
  fields: IWorkflowField[];
  nameRefOptions?: IParamRef[];
}

@Component({
  selector: "meta-aggregation-modal",
  templateUrl: "./aggregation-modal.component.html",
  styleUrls: ["./aggregation-modal.component.less", "../common-styles.less"],
  standalone: true,
  imports: [
    MODULES,
    AccBlockComponent,
    ParamLabelPipe,
    RefSelectedRequireDirective,
    DragDropModule,
    EditNameComponent,
    NodeDescriptionComponent,
    InputTreeSelect
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER]
    }
  ]
})
export class AggregationModalComponent
  extends ModalBaseComponent
  implements OnInit {
  @Input("names") names: string[];

  @Input("nodeInfo") nodeInfo: IAggregationNode;

  @Output("confirm") confirm = new EventEmitter<any>();

  @ViewChild("groupsForm") groupsForm: NgForm;

  @ViewChild("nameForm") nameForm: NgForm;

  public icon = WORKFLOW_SVGS.Aggregation;

  public validationRules = [Validators.required];

  public aggOps = [
    { label: this.i18n.transform("first-non-null"), value: "first-non-null" }
  ];

  public mode = "first-non-null";

  public groups: IAggGroup[] = [];

  public nameRefOptions: IParamRef[] = [];

  public needValid = false;
  public errorMsg = "";
  codeTimeout: any = null;

  constructor(
    protected override nodeServ: NodeService,
    protected override appFlowServ: AppFlowService,
    private i18n: I18NextEagerPipe,
    private helpCenterService: HelpCenterService,
    protected commonService: CommonService
  ) {
    super(nodeServ, appFlowServ);
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

    this.mode = this.nodeInfo.configs.mode;

    const parentNode = this.getParentNodeInfo(this.appFlowServ.getGraph());
    if (parentNode) {
      this.getLoopInnerNodeRefs(parentNode).subscribe((info) => {
        this.onRefUpdate(info);
      });
    } else {
      this.getSelfRefs().subscribe((info) => {
        this.onRefUpdate(info);
      });
    }
  }

  override ngOnDestroy(): void {
    this.helpCenterService.hideHelpPanel();
    super.ngOnDestroy();
    this.modelCloseSave();
  }

  public onRefUpdate(info: IParamRef[]) {

    this.nameRefOptions = info;

    if (this.isInit) {
      this.initGroups();

      this.groups.forEach((group) => {
        NodeUtils.initSelectedTreeNode(group.fields);
      });
    } else {
      this.groups.forEach((group) => {
        if (!group.type) {
          NodeUtils.reSelectRefsWithNewOps(group.fields, this.nameRefOptions);
        } else {
          this.shuffleGroupRefs(group);
          NodeUtils.reSelectRefsWithNewOps(group.fields, group.nameRefOptions);
        }
      });
    }

    this.isInit = false;
  }

  dismiss(): void {
  }

  close(): void {
  }

  validateNode() {
    const { invalidList } = this.getNodeCoreData();
    this.needValid = true;
    if (invalidList?.length) {
      this.errorMsg = this.i18n.transform("aggregationmodalcomponent_264", { invalidList: invalidList.join(",") });
    } else {
      this.errorMsg = "";
    }
  }

  getNodeCoreData() {
    const inputs: IWorkflowField[] = [];
    const outputs: IWorkflowField[] = [];
    const configs = {
      mode: this.mode,
      groups: {} as IAggregationGroups
    };
    let invalidList: string[] = [];
    this.groups.filter(this.isGroupNotEmpty).forEach((group, index) => {
      configs.groups[group.name] = [];
      let firstSchema = null;
      group.fields
        .filter((f) => f.value.content)
        .forEach((field, fieldIndex) => {
          const dtoInput = NodeUtils.getDtoInput(cloneDeep(field));

          if (fieldIndex === 0) {
            const { type, schema } = dtoInput;
            const output: IWorkflowField = {
              ...getInitOutputParamConfig(),
              name: group.name,
              type
            };

            if (schema) {
              output.schema = schema;
            }

            outputs.push(output);
          }

          const fieldName = `group${index + 1}_${fieldIndex + 1}`;
          configs.groups[group.name].push({
            name: fieldName,
            index: fieldIndex
          });

          inputs.push({
            ...dtoInput,
            name: fieldName
          });

          if (dtoInput.schema) {
            if (!firstSchema) {
              firstSchema = dtoInput.schema;
            } else {
              const valid = this.judgeSchemaSame(firstSchema, dtoInput.schema);
              if (!valid) {
                invalidList.push(`group${index + 1}`);
              }
            }
          }
        });
    });

    return {
      inputs,
      outputs,
      configs,
      invalidList
    };
  }

  judgeSchemaSame(s1: any, s2: any) {
    return this.isEqualNameTypeWithArray(s1, s2);
  }

  isEqualNameTypeWithArray(obj1, obj2) {
    if (obj1 === obj2) return true;
    if (obj1 === null || obj2 === null) return obj1 === obj2;

    // 处理数组
    if (Array.isArray(obj1) && Array.isArray(obj2)) {
      if (obj1.length !== obj2.length) return false;
      return obj1.every((item, index) =>
        this.isEqualNameTypeWithArray(item, obj2[index])
      );
    }

    if (typeof obj1 !== "object" || typeof obj2 !== "object") return false;

    // 比较 name 和 type
    if (obj1.name !== obj2.name || obj1.type !== obj2.type) {
      return false;
    }

    const keys1 = Object.keys(obj1);
    const keys2 = Object.keys(obj2);

    for (let key of new Set([...keys1, ...keys2])) {
      if (key === "name" || key === "type") continue;

      const val1 = obj1[key];
      const val2 = obj2[key];

      if (
        (typeof val1 === "object" && val1 !== null) ||
        (typeof val2 === "object" && val2 !== null)
      ) {
        if (!this.isEqualNameTypeWithArray(val1, val2)) {
          return false;
        }
      }
    }

    return true;
  }

  initGroups() {
    this.groups = NodeUtils.fields2Views(this.nodeInfo.outputs).map((o) => {
      const disabledRefIds: string[] = [];
      const fields = this.nodeInfo.configs.groups[o.name].map((group) => {
        const field = cloneDeep(
          this.nodeInfo.inputs.find((input) => input.name === group.name)
        );

        const { ref_node_id, ref_var_name, source } = field.value
          .content as IRefContentType;
        disabledRefIds.push(`${ref_node_id}.${ref_var_name}.${source}`);

        return field;
      });

      const sharedRefs = NodeUtils.narrowRefOption(this.nameRefOptions, {
        type: o.type,
        disabledRefIds
      });

      fields.forEach((field) => {
        field.refs = cloneDeep(sharedRefs);
      });
      const emptyField: IWorkflowField = {
        ...getInitRefParamConfig(),
        name: `${o.name}_${fields.length + 1}`,
        refs: cloneDeep(sharedRefs)
      };
      fields.push(emptyField);

      return {
        name: o.name,
        type: o.type,
        fields,
        nameRefOptions: cloneDeep(sharedRefs)
      };
    });

    if (!this.groups.length) {
      this.groups = [
        {
          name: "group1",
          type: null,
          fields: [
            {
              ...getInitRefParamConfig(),
              name: "group1_1",
              refs: cloneDeep(this.nameRefOptions)
            }
          ],
          nameRefOptions: cloneDeep(this.nameRefOptions)
        }
      ];
    }
  }

  fieldTrackBy(_, f: IWorkflowField) {
    return f.name;
  }

  dropGroupItem(index: number, event: CdkDragDrop<IWorkflowField[]>) {
    moveItemInArray(
      this.groups[index].fields,
      event.previousIndex,
      event.currentIndex
    );

    this.groups[index].fields.forEach((f, i) => {
      f.name = `${this.groups[index].name}_${i + 1}`;
    });
    this.groups[index] = cloneDeep(this.groups[index]);
    this.onSave();
  }

  sortGroupItemPredicate(
    index: number,
    _: CdkDrag<IWorkflowField>,
    groups: CdkDropList<IWorkflowField[]>
  ) {
    const targetItem = groups.data[index];

    if (!targetItem.value.content) {
      return false;
    }

    return true;
  }

  deleteGroupItem(groupIndex: number, itemIndex: number) {
    this.groups[groupIndex].fields.splice(itemIndex, 1);

    if (this.groups[groupIndex].fields.length === 1) {
      this.groups[groupIndex].type = null;
      this.groups[groupIndex].fields[0].refs = cloneDeep(this.nameRefOptions);
    } else {
      this.shuffleGroupRefs(this.groups[groupIndex]);
      NodeUtils.reSelectRefsWithNewOps(
        this.groups[groupIndex].fields,
        this.groups[groupIndex].nameRefOptions
      );
    }

    this.groups[groupIndex].fields.forEach((f, i) => {
      f.name = `${this.groups[groupIndex].name}_${i + 1}`;
    });
    this.groups[groupIndex] = cloneDeep(this.groups[groupIndex]);
    this.onSave();
  }

  addGroup() {
    const index = this.groups.length;
    this.groups.push({
      name: `group${index + 1}`,
      type: null,
      fields: [
        {
          ...getInitRefParamConfig(),
          name: `group${index + 1}_1`,
          refs: cloneDeep(this.nameRefOptions)
        }
      ],
      nameRefOptions: cloneDeep(this.nameRefOptions)
    });
    this.onSave();
  }

  deleteGroup(index: number) {
    this.groups.splice(index, 1);

    for (let i = index; i < this.groups.length; ++i) {
      this.groups[i].name = `group${i + 1}`;

      this.groups[i].fields.forEach((field) => {
        const nameFragment = field.name.split("_");

        if (nameFragment.length > 1) {
          field.name = `${this.groups[i].name}_${nameFragment[1]}`;
        }
      });
    }
    this.onSave();
  }

  onFieldChange(
    selectedRef: IParamRef[],
    groupIndex: number,
    fieldIndex: number
  ) {
    selectedRef[0].disabled = true;
    const group = this.groups[groupIndex];
    const fieldView = group.fields[fieldIndex];
    const fieldViewType = (fieldView.value.content as IParamRef[])[0]
      .type as IWorkflowFieldType;
    const fieldViewName = (fieldView.value.content as IParamRef[])[0]
      .name as IWorkflowFieldType;

    group.type = fieldViewType;

    if (group.fields[group.fields.length - 1].value.content) {
      this.shuffleGroupRefs(group);

      const nextField = {
        ...getInitRefParamConfig(),
        name: `group${groupIndex + 1}_${fieldIndex + 2}`
      };

      nextField.refs = cloneDeep(group.nameRefOptions);
      group.fields.push(nextField);

      NodeUtils.reSelectRefsWithNewOps(group.fields, group.nameRefOptions);
    }
    const groupVars = [];
    group.fields.forEach((f,idx) => {
      if (idx !== fieldIndex && Array.isArray(f?.value?.content) && f?.value?.content?.length === 1) {
        groupVars.push({ ...f?.value?.content[0] });
      }
    });
    if (groupVars.some(itm => itm.name === fieldViewName && itm.type === fieldViewType)) {
      this.needValid = true;
      this.errorMsg = this.i18n.transform("aggregationmodalcomponent_265", { invalidList: [group.name] });
    }
    if (groupVars.some(itm => itm.type !== fieldViewType)) {
      this.needValid = true;
      const typeValidMsg= this.i18n.transform("aggregationmodalcomponent_264", { invalidList: [group.name] });
      this.errorMsg = this.errorMsg.length ? `${this.errorMsg};${typeValidMsg}` : typeValidMsg;
    }
  }

  isGroupNotEmpty(group: IAggGroup) {
    return !(group.fields.length === 1 && !group.fields[0].value.content);
  }

  isGroupsNotEmpty() {
    return this.groups.some(this.isGroupNotEmpty);
  }

  modelCloseSave() {
    if (!this.tagCompareNoChange()) {
      this.appFlowServ.setNodeModalCloseMonitor({ id: this.nodeInfo.id });
    }
    this.handelSave();
  }

  private shuffleGroupRefs(group: IAggGroup) {
    const disabledRefIds: string[] = [];

    group.fields.forEach((field) => {
      if (Array.isArray(field.value.content)) {
        const { ref_node_id, ref_var_name, source } = (
          field.value.content as IRefContentType[]
        )[0];
        disabledRefIds.push(`${ref_node_id}.${ref_var_name}.${source}`);
      }
    });
    const sharedRefs = NodeUtils.narrowRefOption(this.nameRefOptions, {
      type: group.type,
      disabledRefIds
    });
    group.nameRefOptions = cloneDeep(sharedRefs);
  }


  onSave() {
    this.validateNode();
    this.changeUpdateTime();
    if (this.appFlowServ.testRunVerificationError) {
      this.handelSave();
    }
  }

  treeSelect() {
    setTimeout(() => {
      this.onSave();
    });
  }

  handelSave() {
    if (this.tagCompareNoChange()) {
      return;
    }
    const { inputs, outputs, configs, invalidList } = this.getNodeCoreData();
    const nodeData: IAggregationNode = {
      id: this.nodeInfo.id,
      name: this.nodeInfo.name,
      type: this.nodeInfo.type,
      inputs,
      outputs,
      configs
    } as IAggregationNode;
    this.appFlowServ.setNodeSaveMonitor({
      nodeData
    });
    if (this.codeTimeout) {
      clearTimeout(this.codeTimeout);
      this.codeTimeout = null;
    }
    this.codeTimeout = setTimeout(() => {
      this.updateChangeAndInitTime();
    }, 200);
  }
}
