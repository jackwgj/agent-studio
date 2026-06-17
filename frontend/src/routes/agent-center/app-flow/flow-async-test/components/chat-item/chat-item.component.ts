import {
  ChangeDetectorRef,
  Component, EventEmitter,
  Input,
  OnDestroy, Output, output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { AppMarkdownAnswerComponent } from '@shared/components/app-markdown-answer/app-markdown-answer.component';
import { AssetColorUserIconComponent } from '@shared/components/assets/asset-color-user-icon/asset-color-user-icon.component';
import { AssetQuoteIconComponent } from '@shared/components/assets/asset-quote-icon/asset-quote-icon.component';
import { UploadFileIconComponent } from '@shared/components/upload-file-icon/upload-file-icon.component';
import { InputNodeParamsComponent } from '@shared/components/input-node-params/input-node-params.component';
import { cdnAssetUrl } from "../../../../../../single-spa/assets-url";
import { ConversationContent } from "@routes/agent-center/types/my-workspace.types";

@Component({
  selector: 'model-chat-async-item',
  templateUrl: './chat-item.component.html',
  styleUrls: ['./chat-item.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    AppMarkdownAnswerComponent,
    AssetColorUserIconComponent,
    AssetQuoteIconComponent,
    UploadFileIconComponent,
    InputNodeParamsComponent
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ModelChatAsyncItemComponent  {
  @Input() agentIcon: string;

  @Input() dialogHistory:any[];

  @Output('runTask') runTask = new EventEmitter<any>();

  @ViewChild('inputNodeParams')
  inputNodeParamsComp!: InputNodeParamsComponent;

  public changeUrl = cdnAssetUrl;

  public isConversationContent(
    item: ConversationContent ,
  ): item is ConversationContent {
    return typeof item === 'object' && item !== null;
  }

  public getTrimmedQuestion(question: string) {
    return question.trim();
  }

  /** 点击输入节点的确定按钮 */
  public onConfirm(ans: any) {
    // 根据输入节点必填 非必填，动态进行校验
    let isInputNodeParamsPass = false;
    //
    let inputs;
    if (ans?.inputList && ans.inputList.length > 0) {
      inputs = ans.inputList;
    }
    isInputNodeParamsPass = this.inputNodeParamsComp
      ? this.inputNodeParamsComp.checkInput(inputs)
      : true;

    if (!isInputNodeParamsPass) {
      return;
    }

    const inputData = this.inputNodeParamsComp?.getDynamicParams('');

    this.runTask.emit({inputs: {query: this.getFormattedData(inputData)}})
  }

  /** 获取输入节点的表单key-value，并转换为包含:和\n的字符串，作为run接口的入参 */
  protected getFormattedData(inputData: any) {
    const entries = Object.entries(inputData ?? {}).map(([key, value]) => {
      const formattedValue = value === null ? '' : value;
      return `${key}:${formattedValue}`;
    });
    return entries.join('\n');
  }
}
