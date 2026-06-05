import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { NgForm } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import {
  IntegerStrValidatorDirective,
  NumberStrValidatorDirective,
  PositiveIntValidatorDirective,
  RefSelectedRequireDirective,
} from '@shared/directives/common-validator.directive';
import { NonEmptyValidatorDirective } from '@shared/directives/variable-name-validator.directive';
import { MODULES } from '@shared/modules';
import { CommonValidation } from '@shared/validation/commonValidation';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { cloneDeep } from 'lodash';
import { Subject, takeUntil } from 'rxjs';
import { ParamLabelPipe } from 'src/pipes/param-label.pipe';
import { AppFlowService } from '../../app-flow.service';
import { WORKFLOW_SVGS } from '../../flow.const';
import { NodeService } from '../../node.service';
import type {
  IBranch,
  IBranchCondition,
  IBranchNode,
  IParamRef,
  IWorkflowFieldType,
} from '../../node.type';
import { AccBlockComponent } from '../acc-block/acc-block.component';
import { ModalBaseComponent } from '../base/modal-base.component';
import { BranchUtils } from '../branch.utils';
import { ParamTreeSelectedComponent } from '../param-tree/param-tree-selected.component';
import { ParamTreeComponent } from '../param-tree/param-tree.component';
import { NodeUtils } from '../utils';
import { EditNameComponent } from '@routes/agent-center/app-flow/components/edit-name/edit-name.component';
import { NodeDescriptionComponent } from '../node-description/node-description.component';
import { NodeTypeTopic } from '@routes/agent-center/types/common.types';
import { HelpCenterService } from '@services/help-center.service';
import { CommonService } from '@services/common.service';
import { InputTreeSelect } from 'src/routes/agent-center/app-flow/components/input-tree-select/input-tree-select';
@Component({
  selector: 'meta-branch-modal',
  templateUrl: './branch-modal.component.html',
  styleUrls: ['./branch-modal.component.less', '../common-styles.less'],
  standalone: true,
  imports: [
    MODULES,
    AccBlockComponent,
    NonEmptyValidatorDirective,
    ParamLabelPipe,
    ParamTreeComponent,
    PositiveIntValidatorDirective,
    IntegerStrValidatorDirective,
    NumberStrValidatorDirective,
    ParamTreeSelectedComponent,
    RefSelectedRequireDirective,
    EditNameComponent,
    NodeDescriptionComponent,
    InputTreeSelect
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class BranchModalComponent extends ModalBaseComponent implements OnInit {
  @Input('names') names: string[];

  @Input('nodeInfo') nodeInfo: IBranchNode;

  @Output('confirm') confirm = new EventEmitter<IBranchNode>();

  @ViewChild('inputForm') inputForm: NgForm;

  @ViewChild('elseifForm') elseifForm: NgForm;

  @ViewChild('nameForm') nameForm: NgForm;

  protected override destroy$ = new Subject<void>();

  public branchImgUrl = WORKFLOW_SVGS.Branch;

  public nameRefOptions: IParamRef[] = [];

  public emptyModel = '';

  public paramSourceOptions = [
    { label: this.i18n.transform('ref'), value: 'ref' },
    { label: this.i18n.transform('literal'), value: 'literal' },
  ];

  public strLenOptions = [
    { label: this.i18n.transform('ref'), value: 'ref' },
    { label: 'String', value: 'string' },
    { label: 'Integer', value: 'integer' },
  ];

  public emptyOps = [];

  public strConditionOptions = BranchUtils.getStrConditionOptions(this.i18n);

  public arrConditionOptions = BranchUtils.getArrConditionOptions(this.i18n);

  public numConditionOptions = BranchUtils.getNumConditionOptions(this.i18n);

  public boolConditionOptions = BranchUtils.getBoolConditionOptions(this.i18n);

  public objConditionOptions = BranchUtils.getObjConditionOptions(this.i18n);

  public booleanOps = BranchUtils.getBooleanOps();

  public ifCondition: IBranch = {
    id: 'if',
    configs: BranchUtils.getNewBranchConfigs(this.nameRefOptions),
  };

  public elseIfConditions: IBranch[] = [];

  public iconClassNameMap = BranchUtils.getConditionOpIconClassName;

  codeTimeout: any = null;

  constructor(
    private i18n: I18NextEagerPipe,
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    private helpCenterService: HelpCenterService,
    protected commonService: CommonService,
  ) {
    super(nodeServ, appFlowServ);
  }

  onTypeChange = BranchUtils.onTypeChange;

  public override ngOnInit() {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();

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

  ngAfterViewInit(): void {
    if (this.appFlowServ.testRunVerificationError) {
      setTimeout(() => {
        this.validateNode();
      });
    }
  }

  public onRefUpdate(info: IParamRef[]) {

    this.nameRefOptions = info;

    if (this.isInit) {
      this.nodeInfo.branches.forEach((branch) => {
        if (branch.id !== 'default') {
          const branchCopy = cloneDeep(branch);
          branchCopy.configs.conditions = branchCopy.configs.conditions.map(
            (condition) => {
              return BranchUtils.buildViewCondition(
                condition,
                this.nameRefOptions,
              );
            },
          );

          if (branch.id.includes('if')) {
            this.ifCondition = branchCopy;
          }

          if (branch.id.includes('elseIf-')) {
            this.elseIfConditions.push(branchCopy);
          }
        }
      });
    } else {
      this.ifCondition.configs.conditions.forEach((con) => {
        NodeUtils.reSelectRefWithNewOps(con.left, this.nameRefOptions);

        if (con.right?.value?.type === 'ref') {
          NodeUtils.reSelectRefWithNewOps(con.right, this.nameRefOptions);
        }
      });

      this.elseIfConditions.forEach((elseIfCon) => {
        elseIfCon.configs.conditions.forEach((con) => {
          NodeUtils.reSelectRefWithNewOps(con.left, this.nameRefOptions);

          if (con.right?.value?.type === 'ref') {
            NodeUtils.reSelectRefWithNewOps(con.right, this.nameRefOptions);
          }
        });
      });
    }

    this.isInit = false;
  }

  override ngOnDestroy(): void {
    this.helpCenterService.hideHelpPanel();
    this.modelCloseSave();
    this.destroy$.next();
    this.destroy$.complete();
  }

  public addIfCondition() {
    this.ifCondition.configs.conditions.push(this.getBranchConditionObj());
    this.onSave();
  }

  public addElseIfCondition(index: number) {
    this.elseIfConditions[index].configs.conditions.push(
      this.getBranchConditionObj(),
    );
    this.onSave();
  }

  public deleteIfExp(index: number): void {
    if (index === 0 && this.ifCondition.configs.conditions.length === 1) {
      return;
    }

    this.ifCondition.configs.conditions.splice(index, 1);
    this.onSave();
  }

  public deleteElseIfExp(blockIndex: number, expIndex: number): void {
    if (
      expIndex === 0 &&
      this.elseIfConditions[blockIndex].configs.conditions.length === 1
    ) {
      return;
    }

    this.elseIfConditions[blockIndex].configs.conditions.splice(expIndex, 1);
    this.onSave();
  }

  public onIfExpChange(): void {
    BranchUtils.onExpChange(this.ifCondition.configs);
    this.onSave();
  }

  public onElseIfExpChange(index: number): void {
    BranchUtils.onExpChange(this.elseIfConditions[index].configs);
    this.onSave();
  }

  public onElseIfBlockDelete(index: number): void {
    this.elseIfConditions.splice(index, 1);
    this.onSave();
  }

  public addElseIfBlock(): void {
    this.elseIfConditions.push({
      id: 'elseIf',
      configs: BranchUtils.getNewBranchConfigs(this.nameRefOptions),
    });
    this.onSave();
  }

  public originalOrder(): number {
    return 0;
  }

  public trackByFn(index: number) {
    return index;
  }

  dismiss(): void { }

  close(): void { }

  public buildSendBranchesData(): IBranch[] {
    const ifBranch: IBranch = {
      id: `if`,
      configs: BranchUtils.branchConfigsMapper(this.ifCondition.configs),
    };

    const elseBranch: IBranch[] = this.elseIfConditions.map(
      (elseIfCondition, index) => {
        return {
          id: `elseIf-${index + 1}`,
          configs: BranchUtils.branchConfigsMapper(elseIfCondition.configs),
        };
      },
    );

    return [ifBranch, ...elseBranch, { id: 'default' }];
  }

  public leftTypeChange = BranchUtils.leftTypeChange;

  public opChange = BranchUtils.opChange;


  validateNode() {
    this.inputForm.form.markAllAsTouched();
    this.elseifForm.form.markAllAsTouched();
    if (this.inputForm.form.invalid || this.elseifForm.form.invalid) {
      return;
    }
  }

  public isStrType(type: IWorkflowFieldType) {
    if (type === 'string' || type?.startsWith('file')) {
      return true;
    }

    return false;
  }

  public canFillStr(type: IWorkflowFieldType) {
    if (
      ['string', 'object'].includes(type) ||
      type.toLowerCase().startsWith('file')
    ) {
      return true;
    }

    return false;
  }

  private getBranchConditionObj(): IBranchCondition {
    return BranchUtils.getBranchConditionObj(this.nameRefOptions);
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
    this.appFlowServ.setNodeSaveMonitor({
      nodeData: {
        ...this.nodeInfo,
        branches: this.buildSendBranchesData(),
      }
    });
    if (this.codeTimeout) {
      clearTimeout(this.codeTimeout);
      this.codeTimeout = null;
    }
    this.codeTimeout = setTimeout(() => {
      this.updateChangeAndInitTime();
    }, 200);
  }

  modelCloseSave() {
    if (!this.tagCompareNoChange()) {
      this.appFlowServ.setNodeModalCloseMonitor({ id: this.nodeInfo.id });
    }
    this.handelSave();
  }

  onSave() {
    this.changeUpdateTime();
    if (this.appFlowServ.testRunVerificationError) {
      this.handelSave();
    }
  }

  inputOnSave() {
    if (this.appFlowServ.testRunVerificationError) {
      this.handelSave();
    }
  }
}
