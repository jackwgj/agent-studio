import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { FormBuilder, FormControl, Validators} from '@angular/forms';
import { MessageComponent } from '@shared/services/cfdata.service';
import {
  NzSelectModule
} from 'ng-zorro-antd/select';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { I18nNamespace } from '@i18n';
import { AccBlockComponent } from '@routes/agent-center/app-flow/components/acc-block/acc-block.component';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { Subject, takeUntil } from 'rxjs';
import { cloneDeep } from 'lodash';
import { NodeUtils } from "@routes/agent-center/app-flow/components/utils";
import { IParamRef, IWFView, IWorkflowField } from "@routes/agent-center/app-flow/node.type";
import {
  SwaggerTreeConvertService
} from "@routes/agent-center/app-plugin/plugin-action-detail/swagger-tree-convert.service";
import { AppFlowRepoService } from "@services/agent-center/app-flow-repo.service";
import { MonacoEditorLoaderService } from "@materia-ui/ngx-monaco-editor";

@Component({
  selector: 'meta-plugin-function',
  templateUrl: './plugin-function.component.html',
  styleUrls: ['./plugin-function.component.scss'],
  standalone: true,
  imports: [
    MODULES,
    AccBlockComponent,
    NzSelectModule,
    NzFormModule,
    NzButtonModule,
    NzIconModule,
    FormsModule,
    CommonModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
    NzMessageService,
  ],
})
export class PluginFunctionComponent implements OnInit {
  @Input() selectedFunction: string = '';
  @Output() update = new EventEmitter<any>();

  private destroy$ = new Subject<void>();

  constructor(
    private i18n: I18NextEagerPipe,
    private fb: FormBuilder,
    private agentDataServe: AgentDataService,
    private nzMessage: NzMessageService,
    private appFlowRepoServ: AppFlowRepoService,
    private swaggerTreeConvertService: SwaggerTreeConvertService,
    private monacoLoader: MonacoEditorLoaderService,

  ) {
    this.agentDataServe
      .pluginDebugResUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe((resSchemaStr: string) => {

      });
  }


  public pluginName: string;

  public headers: { key: string; value: string }[] = [];

  public selectFormFG = this.fb.group({
    selectFunctionGraph: new FormControl('', [Validators.required]),
  });

  public selectFunctionGraphLable = this.i18n.transform('codemodalcomponent_282');

  public selectFunctionOptions = [];

  public selectFunctionOptionsTotal: number = 0;

  public selectFunctionOptionsPages: number = 1;

  public selectSearchWord: string = '';

  public outputParamsFG: IWorkflowField[] = [];

  public outputParamsShowFG: IWorkflowField[] = [];

  public inputParamsFG: IWorkflowField[] = [];

  public nameRefOptions: IParamRef[] = [];

  public inputParams: IWorkflowField[] = [];

  public outputParams: IWFView[] = [];

  public expanded = false;

  public inputSourceOptions = [
    { label: this.i18n.transform('ref'), value: 'ref' },
    { label: this.i18n.transform('literal'), value: 'literal' },
  ];

  pluginInfo={
    outputs: [],
    configs:{
      fg_id:''
    }
  }

  getType = NodeUtils.getFieldTypeView;

  ngOnInit() {
    try {
      this.monacoLoader.loadMonaco()
    }catch (e){

    }
    this.outputParams = [];
    this.outputParamsShowFG = NodeUtils.fields2Views(this.pluginInfo.outputs);
    this.outputParamsFG = this.pluginInfo.outputs;
    if(this.selectedFunction){
      this.selectFormFG?.controls?.selectFunctionGraph?.setValue(this.selectedFunction)
      this.showFunctionList(true, true);
    }else{
      this.showFunctionList(true, false);
    }
  }

  ngOnDestroy(): void {
    this.agentDataServe.setPluginDebugRes('');
  }

  async onConfirm() {

  }

  isObjectLikeType(type: string) {
    return ['object', 'array<object>'].includes(type);
  }

  public showFunctionWindow(type: string) {
    const id =
      type === 'add'
        ? ''
        : this.selectFormFG?.controls?.selectFunctionGraph?.value;
    if (
      type === 'edit' &&
      !this.selectFormFG?.controls?.selectFunctionGraph?.value
    ) {
      this.nzMessage.warning(this.i18n.transform('codemodalcomponent_287'));
      return;
    }
  }

  public showFunctionList(isnew, isSetSelect) {
    return new Promise(async (resolve) => {
      if (isnew) {
        this.selectFunctionOptions = [];
        this.selectFunctionOptionsPages = 1;
        if (this.selectFormFG?.controls?.selectFunctionGraph?.value) {
          await this.appFlowRepoServ.getFunctionsdetail(
              this.selectFormFG?.controls?.selectFunctionGraph?.value,
            )
            .then((resData: any) => {
              if (resData?.data) {
                const selectVal = {
                  label: resData.data?.function_name,
                  value: resData.data.id,
                  item: resData.data,
                };
                this.selectFunctionOptions?.push(selectVal);
                if (isSetSelect) {
                  this.selectFunctionGraphLableOnSelect(selectVal);
                }
              }
            });
        }
      }

      const pageSize = 10;
      await this.appFlowRepoServ
        .getFunctionsList(
          pageSize,
          (this.selectFunctionOptionsPages - 1) * pageSize,
          this.selectSearchWord,
        )
        .then((resData: any) => {
          if (resData.data) {
            let resList = resData.data?.list.map((item) => {
              return {
                label: item.function_name,
                value: item.id,
                item: item,
              };
            });
            if (this.selectedFunction) {
              resList = resList.filter(
                (item) => item.value !== this.selectedFunction
              );
            }
            const allList = [...this.selectFunctionOptions, ...resList];
            this.selectFunctionOptions = allList;
            this.selectFunctionOptionsTotal = resData.data?.total;
          }
          this.selectFunctionOptionsPages += 1;
        })
        .finally(() => {
          resolve(null);
        });
    });
  }

  public selectFunctionGraphLableOnSelect(event: any) {
    const newInput = this.swaggerTreeConvertService.getInputswagger(
      event.item.function_input,
      cloneDeep(this.nameRefOptions),
    );
    this.inputParamsFG = newInput;

    const newOutput = this.swaggerTreeConvertService.getOutputswagger(
      event.item?.function_output,
    );
    this.outputParamsFG = newOutput;
    this.outputParamsShowFG = NodeUtils.fields2Views(newOutput);
  }

  public loadMoreFunctionOptions(): void {
    if (this.selectFunctionOptions.length >= this.selectFunctionOptionsTotal) {
      return;
    }

    this.showFunctionList(false, false);
  }

  onInputValueTypeChange(row: IWorkflowField) {
    row.value.content = NodeUtils.getChangeContent(row.value.type);
  }

  public seachMoreFunctionOptions(): void {
    this.showFunctionList(true, false);
  }

  changeFunctionGraph(event: any){
    this.update.emit({
      id: event,
    })
  }

  deleteFunction(event: any,e){
    event.stopPropagation();
    this.appFlowRepoServ.deleteFunction(e.item.id).then((res) => {
      if (res) {
        if (this.selectedFunction !== e.item.id) {
          this.selectFormFG?.controls?.selectFunctionGraph?.setValue(
            this.selectedFunction,
          );
          this.showFunctionList(true, true);
          this.update.emit({
            id: this.selectedFunction,
          });
        } else {
          this.selectFormFG?.controls?.selectFunctionGraph?.setValue('');
          this.showFunctionList(true, false);
          this.update.emit({
            id: null,
          });
        }
        MessageComponent.showSuccess(
          this.i18n.transform('pluginfunctioncomponent_727'),
          3000,
        );
      }
    })
  }
}
