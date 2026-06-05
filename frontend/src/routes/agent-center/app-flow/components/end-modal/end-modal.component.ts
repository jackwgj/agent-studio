import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { NgForm } from '@angular/forms';
import { NodeTypeTopic } from '@routes/agent-center/types/common.types';
import { HelpCenterService } from '@services/help-center.service';
import { I18nNamespace } from '@i18n';
import { RefSelectedRequireDirective } from '@shared/directives/common-validator.directive';
import {
  NonEmptyValidatorDirective,
  ValueValidityValidatorDirective,
  VariableNameValidatorDirective,
} from '@shared/directives/variable-name-validator.directive';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { cloneDeep } from 'lodash';
import { BehaviorSubject, takeUntil } from 'rxjs';
import { ParamLabelPipe } from 'src/pipes/param-label.pipe';
import { AppFlowService } from '../../app-flow.service';
import { getInitInputParamConfig } from '../../flow.const';
import { NodeService } from '../../node.service';
import type { IEndNode, IParamRef, IWorkflowField } from '../../node.type';
import { AccBlockComponent } from '../acc-block/acc-block.component';
import { ModalBaseComponent } from '../base/modal-base.component';
import { ParamTreeComponent } from '../param-tree/param-tree.component';
import { NodeUtils } from '../utils';
import { CmdTextareaComponent } from '../cmd-textarea/cmd-textarea.component';
import { StructOutputComponent } from '@routes/agent-center/app-flow/components/struct-output/struct-output.component';
import { NodeDescriptionComponent } from '../node-description/node-description.component';
import { InputTreeSelect } from 'src/routes/agent-center/app-flow/components/input-tree-select/input-tree-select';
import { CommonService } from '@services/common.service';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'meta-end-modal',
  templateUrl: './end-modal.component.html',
  styleUrls: ['./end-modal.component.less', '../common-styles.less'],
  standalone: true,
  imports: [
    MODULES,
    CmdTextareaComponent,
    AccBlockComponent,
    VariableNameValidatorDirective,
    NonEmptyValidatorDirective,
    ValueValidityValidatorDirective,
    ParamLabelPipe,
    RefSelectedRequireDirective,
    ParamTreeComponent,
    StructOutputComponent,
    NodeDescriptionComponent,
    InputTreeSelect,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
    NzModalService,
    NzMessageService,
  ],
})
export class EndModalComponent extends ModalBaseComponent implements OnInit {
  @Input('nodeInfo') nodeInfo: IEndNode;

  @Output('confirm') confirm = new EventEmitter<any>();

  @ViewChild('inputForm') inputForm: NgForm;

  @ViewChild('outputForm') outputForm: NgForm;

  @ViewChild('structOutput') structOutput: StructOutputComponent;

  public responseTemplate = '';

  public inputParams: IWorkflowField[] = [];

  public outputParams: IWorkflowField[] = [];

  public sourceOptions = [
    { label: this.i18n.transform('ref'), value: 'ref' },
    { label: this.i18n.transform('literal'), value: 'literal' },
  ];

  public nameRefOptions: IParamRef[] = [];
  isFirstInit = 0;
  codeTimeout: any = null;
  get tipVals() {
    return this.inputParams.map((param) => param.name);
  }
  readonly showVarListSubject$ = new BehaviorSubject<boolean>(false);
  constructor(
    private i18n: I18NextEagerPipe,
    protected override nodeServ: NodeService,
    protected override appFlowServ: AppFlowService,
    private helpCenterService: HelpCenterService,
    protected commonService: CommonService,
    protected agentDataServe: AgentDataService,
    private nzModal: NzModalService,
    private nzMessage: NzMessageService,
  ) {
    super(nodeServ, appFlowServ);
  }

  public override ngOnInit() {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();

    this.nodeServ
      .refInfoUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe((info) => {

        const info_list = NodeUtils.refInfo2Tree(info);
        const requestVariables = this.appFlowServ.requestVariablesFn(info_list);

        this.nameRefOptions = [...info_list, ...requestVariables];

        if (this.isInit) {
          this.inputParams = NodeUtils.initInputs(
            this.nodeInfo.inputs,
            this.nameRefOptions,
          );

          this.outputParams = NodeUtils.initInputs(
            this.nodeInfo.outputs,
            this.nameRefOptions,
          );
        } else {
          NodeUtils.reSelectRefsWithNewOps(
            this.inputParams,
            this.nameRefOptions,
          );

          NodeUtils.reSelectRefsWithNewOps(
            this.outputParams,
            this.nameRefOptions,
          );
        }

        this.isInit = false;
      });

    this.responseTemplate = (
      this.nodeInfo as IEndNode
    ).configs.response_template;
  }

  public addEndNodeParam(params: IWorkflowField[]): void {
    params.push({
      ...getInitInputParamConfig(),
      refs: cloneDeep(this.nameRefOptions),
    });
    this.onSave();
  }

  public deleteParam(arr: IWorkflowField[], index: number): void {
    arr.splice(index, 1);
    this.onSave();
  }

  getOutputOptions(options) {
    return options.filter(item => item.type !== 'LLM')
  }

  public onTypeChange(row: IWorkflowField) {
    //输入类型初始化值为string
    if (row.value.type === 'literal') {
      row.type = 'string';
    }
    row.value.content = NodeUtils.getChangeContent(row.value.type);
    this.onSave();
  }

  dismiss(): void { }

  close(): void { }

  ngAfterViewInit() {
    if (this.appFlowServ.testRunVerificationError) {
      setTimeout(() => {
        this.validateNode();
      });
    }
  }

  validateNode() {
    this.inputForm?.form?.markAllAsTouched();
    this.outputForm?.form?.markAllAsTouched();
  }

  getNames(
    arr: IWorkflowField[],
    index: number,
  ): {
    existingValues: string[];
  } {
    const names = arr.map((p) => p.name);
    names.splice(index, 1);
    return { existingValues: names };
  }

  onOutputNameChange() {
    this.outputForm?.form?.markAsDirty();
  }

  onNameChange() {
    this.inputForm?.form?.markAsDirty();
  }

  modelCloseSave() {
    if (!this.tagCompareNoChange()) {
      this.appFlowServ.setNodeModalCloseMonitor({ id: this.nodeInfo.id });
    }
    this.handelSave();
  }

  handelSave() {
    if (this.tagCompareNoChange()) {
      return;
    }
    const struct_output_template = this.structOutput.struct_output_template;
    const isStructMessage = this.structOutput.structEnable;
    this.appFlowServ.setNodeSaveMonitor({
      nodeData: {
        ...this.nodeInfo,
        inputs: NodeUtils.getDtoInputs(this.inputParams),
        outputs: NodeUtils.getDtoInputs(this.outputParams),
        configs: {
          ...(this.nodeInfo as IEndNode).configs,
          response_template: this.responseTemplate,
          struct_output_template,
          enable_history: true,
          isStructMessage
        },
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

  treeSelect() {
    setTimeout(() => {
      this.onSave();
    });
  }

  inputOnSave() {
    if (this.appFlowServ.testRunVerificationError) {
      this.handelSave();
    }
  }

  onSave() {
    this.changeUpdateTime();
    if (this.appFlowServ.testRunVerificationError) {
      this.handelSave();
    }
  }

  override ngOnDestroy(): void {
    super.ngOnDestroy();
    this.modelCloseSave();
    this.helpCenterService.hideHelpPanel();
  }

  structOnSave (){
    if (this.isFirstInit <= 0) {
      this.isFirstInit += 1;
      return;
    }
    this.onSave();
  }
}
