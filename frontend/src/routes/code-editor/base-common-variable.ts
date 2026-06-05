import { Injectable } from "@angular/core";
import i18next from 'i18next';
/**
 * @description 该文件主要用于创建项目中所需要的静态变量  静态变量的含义: 变量在定义的时候就被赋值，赋值后 该值不允许二次修改。
 * **/

export interface BlockPosition {
  contextRefPos: string;
  functionRefPos: string;
  customFunctionRefPos: string;
  variableRefPos: string;
  triggerRefPos: string;
  globalVariableRefPos: string;
}

interface FlowFieldDataRefData {
  contextRefBlockArr: string;
  functionRefBlockArr: string;
  customFunctionRefBlockArr: string;
  writeArr: string;
  triggerRefBlockArr: string;
  globalVariableRefBlockArr: string;
}

interface FlowInputDataTypes {
  reference: string;
  function: string;
  custom: string;
  variable: string;
  write: string;
  triggerRefer: string;
  global: string;
}

interface FieldTypes {
  array: string;
  boolean: string;
  integer: string;
  number: string;
  object: string;
  string: string;
}

@Injectable({
  providedIn: "root",
})
export class BaseCommonVariable {
  private readonly maxNameLen: number = 60;
  private readonly contextReferencePrefix: string = "200/props/";
  private readonly zeroWidthSpaceString: string = "​"; //dom中的特殊字符
  private readonly zeroWidthSpaceStringUnicodeValue: string = "200b"; // ​的unicode编码
  private readonly maxArrayComponentItemLen: number = 20;
  private readonly outermostArraySwitchToInputFlag: string = "outermostArraySwitchToInput"; // 判断数组组件是否切换为输入框
  private readonly changeSelectComponentToInputFlag: string = "selectionSwitchToInput";
  private readonly rootArrayFieldIsUpgradeFlag: string = "rootArrayFieldIsUpgrade"; // 当该参数的值为true代表，数组根节点字段升级，需要添加该标识符，让其最初显示为input
  private readonly flowCallInputBlockPosition: BlockPosition = {
    contextRefPos: "contextRefPos",
    functionRefPos: "functionRefPos",
    customFunctionRefPos: "customFunctionRefPos",
    variableRefPos: "variableRefPos",
    triggerRefPos: "triggerRefPos",
    globalVariableRefPos: "globalVariableRefPos",
  };
  private editFlowCallInputRef: any[] = [
    {
      label: "contextRefPos",
      value: "@{context[",
      includes: [],
    },
    {
      label: "functionRefPos",
      value: "@{function[",
      includes: ["@{trigger["], // 连接器中：函数支持trigger 的引用
      express: /(@\{function)(?:\[.\s*.+\])+\s*}\s*.*$/,
    },
    {
      label: "customFunctionRefPos",
      value: "@{customfunction[",
      includes: [],
    },
    {
      label: "variableRefPos",
      value: "@{variable[",
      includes: [],
    },
    {
      label: "triggerRefPos",
      value: "@{trigger[",
      includes: [], // 与其他表达式的引用能力
      express: /(@\{trigger\[)\s*.+\]\s*}\s*.*$/,
    },
    {
      label: "globalVariableRefPos",
      value: "@{globalvariable[",
      includes: [],
    },
  ]; // 数组顺序勿动
  private readonly splitSign: string = "/";
  private readonly arrayName: string = "array";
  private readonly arraySign: string = "[]";
  private isViewFlowHistorySidebarNodeRunningData: boolean = false; // 是否渲染流历史页面的侧边栏组件
  readonly paramTypes: any[] = [
    {
      label: "boolean",
      value: "boolean",
    },
    {
      label: "int",
      value: "int",
    },
    {
      label: "long",
      value: "long",
    },
    {
      label: "string",
      value: "string",
    },
    {
      label: "double",
      value: "double",
    },
    {
      label: "float",
      value: "float",
    },
    {
      label: "object",
      value: "object",
    },
  ];
  readonly authenticationOptions: string[] = ["NON_AUTH", "basic", "apiKey"];
  readonly richEditorBlockStyle: string =
    "display: inline-block; background-color: #f2f5fc;padding-left: 2px; padding-right: 2px; margin-left: 2px; margin-right: 2px; height: 20px; color: #575d6c; font-size: 12px; padding-top: 2px;";
  readonly richEditorImageStyle: string = "width: 18px; vertical-align: middle;";
  readonly errorRefBorderStyle: string = "border: 0.25px solid red";
  private readonly responseBodyAndHeaderName: string[] = ["body", "header"];
  private readonly code_editor_max_height: number = 500;
  private readonly code_editor_min_height: number = 200;
  private readonly v2variableConnectorStepId: string = "variableV2StepId";
  private readonly fieldDataRefData: FlowFieldDataRefData = {
    contextRefBlockArr: "contextRefBlockArr",
    functionRefBlockArr: "functionRefBlockArr",
    customFunctionRefBlockArr: "customFunctionRefBlockArr",
    writeArr: "writeArr",
    triggerRefBlockArr: "triggerRefBlockArr",
    globalVariableRefBlockArr: "globalVariableRefBlockArr",
  }; // flow的fieldData中的数据
  private readonly flowInputDataTypes: FlowInputDataTypes = {
    reference: "reference",
    triggerRefer: "triggerRefer",
    function: "function",
    custom: "custom",
    variable: "variable",
    write: "write",
    global: "global",
  }; // flow input的数据类型
  private readonly ref_stepId: string = "ref_stepId"; // 用于合并flow编排页面上下文引用变量连接器，复制其节点原始stepId
  private readonly polling_ref_stepId: string = "polling_ref_stepId";
  private readonly noRootDataInPropsPresentKeys: string[] = ["form", "header", "path", "query"]; // props_present中没有根节点的key
  private readonly fieldTypes: FieldTypes = {
    // 连接器字段类型
    array: "array",
    boolean: "boolean",
    integer: "integer",
    number: "number",
    object: "object",
    string: "string",
  };
  readonly changeBlockPosObjectToArray: string = "changeBlockPosObjectToArray"; // 保存flow时，将blockPosition由对象改为array
  readonly connectorPropsRootKeys: string[] = ["body", "header", "form", "path", "query", "trigger"];
  readonly defaultFlowEditConfigPageWidth: number = 425;
  readonly defaultFlowConfigPageResizeMaxWidth: number = 80;

  constructor() {}

  getEditFlowCallInputRef(): any[] {
    return this.editFlowCallInputRef;
  }

  getCodeEditorMaxHeight(): number {
    return this.code_editor_max_height;
  }

  getCodeEditorMinHeight(): number {
    return this.code_editor_min_height;
  }

  getMaxNameLen(): number {
    return this.maxNameLen;
  }

  getContextReferencePrefix(): string {
    return this.contextReferencePrefix;
  }

  getZeroWidthSpaceString(): string {
    return this.zeroWidthSpaceString;
  }

  getZeroWidthSpaceStringUnicodeValue(): string {
    return this.zeroWidthSpaceStringUnicodeValue;
  }

  getMaxArrayComponentItemLen(): number {
    return this.maxArrayComponentItemLen;
  }

  getOutermostArraySwitchToInputFlag(): string {
    return this.outermostArraySwitchToInputFlag;
  }

  getChangeSelectComponentToInputFlag(): string {
    return this.changeSelectComponentToInputFlag;
  }

  getRootArrayFieldIsUpgradeFlag(): string {
    return this.rootArrayFieldIsUpgradeFlag;
  }

  getFlowCallInputBlockPosition() {
    return this.flowCallInputBlockPosition;
  }

  getViewFlowHistorySidebarNodeRunningData(): boolean {
    return this.isViewFlowHistorySidebarNodeRunningData;
  }

  setViewFlowHistorySidebarNodeRunningData(result: boolean) {
    this.isViewFlowHistorySidebarNodeRunningData = result;
  }

  getSplitSign(): string {
    return this.splitSign;
  }

  getArrayName(): string {
    return this.arrayName;
  }

  getArraySign(): string {
    return this.arraySign;
  }

  getResponseBodyAndHeaderName(): string[] {
    return this.responseBodyAndHeaderName;
  }

  getV2variableConnectorStepId(): string {
    return this.v2variableConnectorStepId;
  }

  getFieldDataRefData(): FlowFieldDataRefData {
    return this.fieldDataRefData;
  }

  getFlowInputDataTypes(): FlowInputDataTypes {
    return this.flowInputDataTypes;
  }

  getRefStepId(): string {
    return this.ref_stepId;
  }

  getNoRootDataInPropsPresentKeys(): string[] {
    return this.noRootDataInPropsPresentKeys;
  }

  getFieldTypes(): FieldTypes {
    return this.fieldTypes;
  }

  getPollingStepId() {
    return this.polling_ref_stepId;
  }

  getGlobalVariableIcon() {
    return "theme/default/images/global-variable.png";
  }
}
