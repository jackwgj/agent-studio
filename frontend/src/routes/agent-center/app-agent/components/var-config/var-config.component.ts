import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  QueryList,
  SimpleChanges,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { NgForm } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import {
  MonacoEditorComponent,
  MonacoEditorLoaderService,
  MonacoEditorModule,
} from '@materia-ui/ngx-monaco-editor';
import {
  EditorOptions,
  NodeUtils,
} from '@routes/agent-center/app-flow/components/utils';
import { IWorkflowField } from '@routes/agent-center/app-flow/node.type';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { AssetCopyIconComponent } from '@shared/components/assets/asset-copy-icon/asset-copy-icon.component';
import { AssetCopySuccessIconComponent } from '@shared/components/assets/asset-copy-success-icon/asset-copy-success-icon.component';
import { AssetFormatIconComponent } from '@shared/components/assets/asset-format-icon/asset-format-icon.component';
import { FlexibleTextFieldComponent } from '@shared/components/flexible-text-field/flexible-text-field.component';
import { ArrTypeValidatorDirective } from '@shared/directives/array-type-validator.directive';
import { ObjTypeValidatorDirective } from '@shared/directives/obj-type-validator.directive';
import { NonEmptyValidatorDirective } from '@shared/directives/variable-name-validator.directive';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { cloneDeep } from 'lodash';
import { filter, take } from 'rxjs';

export type MonacoUri = monaco.Uri;

type PlainType = string | number | boolean | Record<string, any>;

export type AgentVal = PlainType | Array<PlainType>;

export interface IAgentVarCachedVal {
  val: AgentVal;
}

export interface IAgentVar extends IWorkflowField {
  val: AgentVal;
  uri?: MonacoUri;
}

@Component({
  selector: 'meta-var-config',
  templateUrl: './var-config.component.html',
  styleUrls: ['./var-config.component.scss'],
  imports: [
    MODULES,
    MonacoEditorModule,
    FlexibleTextFieldComponent,
    NonEmptyValidatorDirective,
    AssetFormatIconComponent,
    ArrTypeValidatorDirective,
    ObjTypeValidatorDirective,
    AssetCopyIconComponent,
    AssetCopySuccessIconComponent,
    NzButtonModule,
    NzFormModule,
    NzIconModule,
    NzInputModule,
    NzInputNumberModule,
    NzSwitchModule,
    NzToolTipModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
  standalone: true
})
export class VarConfigComponent implements OnChanges {
  @Input() params: IWorkflowField[] = [];

  @Output() confirm = new EventEmitter<IAgentVar[]>();

  @ViewChild('varForm') varForm: NgForm;

  @ViewChildren('jsonEditor') jsonEditors: QueryList<any>;

  public vars: IAgentVar[] = [];

  public editorOps = EditorOptions;

  public getTypeFunc = NodeUtils.getFieldTypeView;

  public isFile = NodeUtils.isFileType;

  public isStr = NodeUtils.isStringType;

  public isMultiples = NodeUtils.isMultiplesType;

  public isNum = NodeUtils.isNumType;

  public isInt = NodeUtils.isIntType;

  public isBool = NodeUtils.isBooleanType;

  public isArr = NodeUtils.isArrayType;

  public isObj = NodeUtils.isObjectType;

  public isComplex = NodeUtils.isComplexType;

  constructor(
    private agentDataServe: AgentDataService,
    private monacoLoader: MonacoEditorLoaderService,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes?.params?.currentValue) {
      this.monacoLoader.isMonacoLoaded$
        .pipe(
          filter((isLoaded) => isLoaded),
          take(1),
        )
        .subscribe(() => {
          this.vars = this.initVars(changes.params.currentValue);
        });
    }
  }

  initVars(params: IWorkflowField[]): IAgentVar[] {
    const cachedParams = this.agentDataServe.getAgentCachedVarMap();

    const complexCtx: { val: string; uri: MonacoUri }[] = [];

    const vars = cloneDeep(params).map((param) => {
      const cacheVal = cachedParams[param.name];
      let val: AgentVal = null;

      if (cacheVal) {
        val = cacheVal.val;
      } else if (param.value?.default) {
        val = param.value.default;
      } else {
        // do nth
      }

      let uri = null;
      if (['array', 'object'].includes(param.type)) {
        uri = this.generateUri(param.name);
        complexCtx.push({
          val: val as string,
          uri: uri,
        });
      }

      return {
        ...param,
        val,
        uri,
      };
    });

    if (complexCtx.length) {
      setTimeout(() => {
        const editors = this.jsonEditors.toArray() as MonacoEditorComponent[];

        complexCtx.forEach((ctx, index) => {
          try {
            const model = monaco.editor.createModel(ctx.val, 'json', ctx.uri);
            editors[index].editor.setModel(model);
          } catch {
            // do nth
          }
        });
      });
    }

    return vars;
  }

  public getComplexErr(i: number): string | null {
    return (
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      this.varForm?.controls[`param${i}`]?.errors?.arrErr?.msg ||
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      this.varForm?.controls[`param${i}`]?.errors?.serviceName?.tiErrorMessage
    );
  }

  onConfirm() {
    if (this.varForm?.invalid) {
      return;
    }

    const cache: Record<string, IAgentVar> = {};
    this.vars.forEach((param) => {
      cache[param.name] = {
        val: param.val,
      };

      if (['object', 'array'].includes(param.type)) {
        monaco.editor.getModel(param.uri)?.dispose();
      }
    });
    this.agentDataServe.setAgentCachedVarMap(cache);
    this.confirm.emit(this.vars);
  }

  public formatDoc(uri: MonacoUri) {
    const editors = this.jsonEditors.toArray() as MonacoEditorComponent[];
    const editorCmp = editors.find((e) => e.uri === uri);

    if (editorCmp) {
      editorCmp.editor.getAction('editor.action.formatDocument').run();
    }
  }

  public isFormValid() {
    return this.varForm?.form?.valid;
  }

  close() {
    this.confirm.emit();
  }

  private generateUri(id: string) {
    return monaco.Uri.parse(`agent://agent/var/${id}.json`);
  }
}