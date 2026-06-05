import { Component, ViewChild, inject, Input } from '@angular/core';
import { MODULES } from '@shared/modules';
import { filter, take } from 'rxjs';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { DynamicNodeParamsComponent } from '@shared/components/dynamic-node-params/dynamic-node-params.component';
import { agentCommonLogic } from '@routes/agent-center/app-agent/common-logic-agent';
import { MonacoEditorLoaderService } from '@materia-ui/ngx-monaco-editor';
import { AppPluginRepoService } from '@services/agent-center/app-plugin-repo.service';
import { JsonViewerComponent } from '@shared/components/json-viewer/json-viewer.component';
import { Clipboard } from '@angular/cdk/clipboard';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { isArray, isInteger } from 'lodash';
import { AssetSuccessIconComponent } from '@shared/components/assets/asset-success-icon/asset-success-icon.component';
import { AssetErrorIconComponent } from '@shared/components/assets/asset-error-icon/asset-error-icon.component';
import { AssetCopyIconComponent } from '@shared/components/assets/asset-copy-icon/asset-copy-icon.component';
import { AssetCopySuccessIconComponent } from '@shared/components/assets/asset-copy-success-icon/asset-copy-success-icon.component';
import { AssetLoadingIconComponent } from '@shared/components/assets/asset-loading-icon/asset-loading-icon.component';
import { AssetGenerateLoadingIconComponent } from '@shared/components/assets/asset-generate-loading-icon/asset-generate-loading-icon.component';
import { NoDataIconComponent } from '@shared/components/no-data-icon/no-data-icon.component';
import { AssetAlertIconComponent } from '@shared/components/assets/asset-alert-icon/asset-alert-icon.component';
import { initializeReqItem, pluginSchemaStr2Path } from '@routes/agent-center/app-plugin/utils';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzAlertModule } from 'ng-zorro-antd/alert';

@Component({
  selector: 'plugin-debug-modal',
  templateUrl: './plugin-debug-modal.component.html',
  styleUrls: ['./plugin-debug-modal.component.scss'],
  standalone: true,
  imports: [
    MODULES,
    DynamicNodeParamsComponent,
    JsonViewerComponent,
    AssetSuccessIconComponent,
    AssetErrorIconComponent,
    AssetCopyIconComponent,
    AssetCopySuccessIconComponent,
    AssetLoadingIconComponent,
    AssetGenerateLoadingIconComponent,
    NoDataIconComponent,
    AssetAlertIconComponent,
    NzButtonModule,
    NzIconModule,
    NzToolTipModule,
    NzAlertModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class PluginDebugModalComponent {
  readonly modalRef = inject(NzModalRef);

  @Input() requestArgs = [];

  @Input() hideParseBtn = false;

  @Input() pluginBodyParams;

  @ViewChild('dynamicFormIns')
  dynamicFormIns!: DynamicNodeParamsComponent;

  public requestArgToDynamicForm = [];

  public tabs: string[] = ['Body', 'URL', 'Headers'];

  public activeTab: string = 'Body';

  // 调测接口的结果
  public debugResult = null;

  public rawRequestBody;

  public rawResponse;

  // 记录输入区域的不同标签的复制按钮状态
  public inputCopyState: Record<string, boolean> = {};

  // 记录输出区域的复制按钮状态
  public isOutputCopying = false;

  public isLoading = false;

  public showResponseJson=false;
  constructor(
    private i18n: I18NextEagerPipe,
    private commonLogic: agentCommonLogic,
    private monacoLoader: MonacoEditorLoaderService,
    private appPluginRepoServe: AppPluginRepoService,
    private clipboard: Clipboard,
    private agentDataServe: AgentDataService,
  ) {}

  ngOnInit() {
    //插件路径参数
    let pluginPathArgs = pluginSchemaStr2Path(this.pluginBodyParams?.request_info?.basic_info?.input_schema)
    pluginPathArgs = pluginPathArgs.map((item) => initializeReqItem(item))
    this.requestArgToDynamicForm = [...this.requestArgs,...pluginPathArgs];
    this.registerJSONValidationSchema();
  }

  /** 按钮禁用：rawResponse为空 或者 errorMessage为空 */
  get isDebugBtnDisabled() {
    return (
      (this.debugResult?.success && !this.rawResponse) ||
      (!this.debugResult?.success && !this.debugResult?.errorMessage)
    );
  }

  public async onDebugging() {
    this.isLoading = false;
    let dynamicFormPass = true;
    // 检查基础类型入参
    dynamicFormPass = this.dynamicFormIns
      ? this.dynamicFormIns.validatePrimitiveInputs()
      : true;
    // 检查复杂类型入参
    const validationResults = this.dynamicFormIns?.validateJsonSchemas() || [];
    for (const item of validationResults) {
      if (item.type === 'object' || item.type.startsWith('array')) {
        if (item.required) {
          if (item.isError || item.isEmpty) {
            dynamicFormPass = false;
          }
        } else {
          if (item.isError) {
            dynamicFormPass = false;
          }
        }
      }
    }

    if (dynamicFormPass) {
      this.debugResult = null;
      this.isLoading = true;

      try {
        let tool_id = '';
        if (this.pluginBodyParams.tool_id) {
          const res = await this.appPluginRepoServe.modifyDebugPluginId(
            this. pluginBodyParams.plugin_id,
            this.pluginBodyParams.tool_id,
          );
          tool_id = res.data||res.tool_id||this.pluginBodyParams.tool_id;
        } else {
          const req = {...this.pluginBodyParams}
          if(req?.request_info?.basic_info?.input_schema){
            req.request_info.input_schema = req.request_info.basic_info.input_schema
          }
          const res = await this.appPluginRepoServe.getDebugPluginId(
            req
          );
          tool_id = res.tool_id;
        }

        const params = this.getDebugBodyParams() || {};
        const jsonStr = JSON.stringify(params);
        this.debugResult = await this.appPluginRepoServe.debugPluginById(
          tool_id,
          jsonStr
        );
        this.rawRequestBody =
          this.debugResult?.raw_request_body &&
          this.safeJsonParse(this.debugResult.raw_request_body);

        // 超长json格式化会卡死，检测到超长直接以文本展示并提示不能格式化
        if (this.debugResult?.raw_response) {
          if (this.debugResult.raw_response.length>10000){
            this.rawResponse = this.debugResult.raw_response;
          }
          else{
            this.rawResponse =
              this.debugResult?.raw_response &&
              this.safeJsonParse(this.debugResult.raw_response);
            this.showResponseJson=true;
          }
        }
      } finally {
        this.isLoading = false;
      }
    }
  }

  public onParseRes() {
    let resSchema;
    if (this.debugResult?.success) {
      resSchema = this.resStr2Schema(this.debugResult?.raw_response);
    } else {
      resSchema = this.resStr2Schema(this.debugResult?.errorMessage);
    }
    if (resSchema) {
      this.agentDataServe.setPluginDebugRes(JSON.stringify(resSchema));
    } else {
      this.agentDataServe.setPluginDebugRes('');
    }
    this.close();
  }

  public setActiveTab(tab: string): void {
    this.activeTab = tab;
    if (!this.inputCopyState[tab]) {
      this.inputCopyState[tab] = false;
    }
  }

  public copyInput() {
    let text = '';
    switch (this.activeTab) {
      case 'Body':
        text = this.debugResult.raw_request_body;
        break;
      case 'URL':
        text = this.debugResult.raw_request_url;
        break;
      case 'Headers':
        text = JSON.stringify(this.debugResult.raw_request_headers);
        break;
      default:
        return;
    }

    const currentTab = this.activeTab;
    if (this.clipboard.copy(text)) {
      this.inputCopyState[currentTab] = true;
      setTimeout(() => {
        this.inputCopyState[currentTab] = false;
      }, 3000);
    }
  }

  public copyOutput() {
    let text = '';
    if (this.debugResult?.success) {
      text = this.debugResult.raw_response || '';
    } else {
      text = this.debugResult.errorMessage || '';
    }

    if (this.clipboard.copy(text)) {
      this.isOutputCopying = true;
      setTimeout(() => {
        this.isOutputCopying = false;
      }, 3000);
    }
  }

  public isSuccess(code: number) {
    return code >= 200 && code < 300;
  }

  public isValidJson(rawResponse: string) {
    try {
      JSON.parse(rawResponse);
      return true;
    } catch (e) {
      return false;
    }
  }

  private safeJsonParse(s: any) {
    try {
      return JSON.parse(s);
    } catch {
      return s;
    }
  }

  /** 点击【开始调测】，获取表单key-value，作为插件调测接口的入参 */
  private getDebugBodyParams() {
    return this.dynamicFormIns?.getDynamicParams();
  }

  private registerJSONValidationSchema() {
    this.monacoLoader.isMonacoLoaded$
      .pipe(
        filter((isLoaded) => isLoaded),
        take(1),
      )
      .subscribe(() => {
        // 为每个请求参数添加 modelUri 字段
        this.requestArgToDynamicForm.forEach((input) => {
          input.modelUri = this.commonLogic.getModelUriByType(
            input.type,
            input.name,
          );
          input.isError = false;
          input.isEmpty = false;
        });
        // 收集所有 JSON Schema
        const schemas = this.requestArgToDynamicForm.map((input) => ({
          uri: input.modelUri?.toString(),
          schema: this.commonLogic.getJSONSchemaByType(input.type)?.schema,
          fileMatch: [input.modelUri?.toString()],
        }));
        // 避免覆盖之前设置的 JSON Schema
        monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
          validate: true,
          schemas: schemas,
          schemaValidation: 'error',
        });
      });
  }

  private close() {
    this.modalRef.destroy();
  }

  private resStr2Schema(jsonStr: string) {
    let obj;

    try {
      obj = JSON.parse(jsonStr);
      if (!obj || typeof obj !== 'object') {
        return '';
      }
    } catch {
      return '';
    }
    return this.resObject2Schema(obj);
  }

  /** 将插件调测结果对应的object对象转换成JSON Schema */
  private resObject2Schema(data) {
    const schema: any = {};

    if (isArray(data)) {
      if (data.length === 0) {
        return undefined; // 过滤空数组，无法知道具体属于Array<...>
      }
      schema.type = 'array';
      const elmTypes = [...new Set(data.map((item) => this.getType(item)))];

      if (elmTypes.length === 1) {
        schema.items =
          elmTypes[0] === 'object'
            ? this.resObject2Schema(data[0]) // array<object>类型，递归转换JSON Schema
            : { type: elmTypes[0] }; // 单一类型数组
      } else {
        schema.items = { type: 'mixed' }; // 混合类型数组
      }
    } else if (this.getType(data) === 'object') {
      const properties = {};

      Object.entries(data || {}).forEach(([key, value]) => {
        if (value === null) {
          return; // 过滤null，无法知道null属于某个类型
        }

        const propSchema = this.resObject2Schema(value);
        if (
          propSchema !== null &&
          propSchema !== undefined &&
          Object.keys(propSchema).length > 0
        ) {
          properties[key] = propSchema;
        }
      });

      if (Object.keys(properties).length === 0) {
        return undefined; // 过滤空对象，无法解析出对象属性，可能导致工作流运行失败
      }

      schema.type = 'object';
      schema.properties = properties;
      schema.required = Object.keys(properties);
    } else {
      // 基础类型
      schema.type = this.getType(data);
    }

    return schema;
  }

  /** 获取入参的具体数据类型 */
  private getType(value): string {
    if (isArray(value)) {
      return 'array';
    }

    if (typeof value === 'number') {
      return isInteger(value) ? 'integer' : 'number';
    }

    if (['boolean', 'string', 'object'].includes(typeof value)) {
      return typeof value;
    }

    return 'unknown';
  }
}
