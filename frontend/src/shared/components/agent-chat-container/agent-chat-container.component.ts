import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { I18NextEagerPipe } from 'angular-i18next';

import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { IAnswerItem, IFlowChatItem, IInputItem } from '@routes/agent-center/types/common.types';
import { SessioMgnService } from '@services/agent-observatory/session-service';
import { MODULES } from '@shared/modules';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { AppMarkdownAnswerComponent } from '../app-markdown-answer/app-markdown-answer.component';
import { InputNodeParamsComponent } from '../input-node-params/input-node-params.component';

@Component({
  selector: 'agent-chat-container',
  templateUrl: './agent-chat-container.component.html',
  styleUrl: './agent-chat-container.component.scss',
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    AppMarkdownAnswerComponent,
    InputNodeParamsComponent,
  ],
})
export class AgentChatContainerComponent {
  @Input() chatLoop: IFlowChatItem[];
  @Output() onResendQuestion: EventEmitter<number> = new EventEmitter();
  @Output() onSubmitInput: EventEmitter<Object> = new EventEmitter();

  @ViewChild('inputNodeParams') inputNodeParamsComp!: InputNodeParamsComponent;
  @ViewChild('kbConfRef') kbConfRef;

  public cdnUrl = cdnAssetUrl;

  public tipContext: any = {};

  constructor(
    private i18n: I18NextEagerPipe,
    private cdr: ChangeDetectorRef,
    private configServ: AgentConfigService,
    private sessioMgnService : SessioMgnService,
  ) { }

  ngOnInit(): void {
  }

  public onResend(index: number): void {
    this.onResendQuestion.emit(index);
  }

  public handleCollapsedStep(item) {
    item.collapsed = !item?.collapsed
  }

  public changeCollapsedState(pluginForm: any) {
    const collapsed = pluginForm?.collapsed;
    pluginForm.collapsed = !collapsed;
    this.cdr.markForCheck();
  }

  public onFileUploadStatus(state: string, ans: IAnswerItem) {
    if (state === 'loading') {
      ans.inputStatus = 'not-prepared';
    } else if (state === 'failed') {
      ans.inputStatus = 'failed';
    } else {
      ans.inputStatus = 'not-confirmed';
    }
  }

  public onConfirm(ans: IAnswerItem, chatItem: IFlowChatItem) {
    let inputs: IInputItem[];
    if (ans?.inputList.length > 0) {
      inputs = ans.inputList;
    }
    const isInputNodeParamsPass = this.inputNodeParamsComp?.checkInput(inputs);
    if (!isInputNodeParamsPass) {
      return;
    }

    const inputData = this.inputNodeParamsComp?.getDynamicParams('');
    Object.entries(inputData).forEach(([key, data]) => {
      if (data instanceof Array) {
        inputData[key] = JSON.stringify(inputData[key].map((obj) => obj.url));
      }
    });

    this.onSubmitInput.emit(inputData);
  }

  public onShowKbConfTip(e, row, i) {
    let arr = this.chatLoop.filter(
      (item) => item.execution_id && !item.showAnswer.some((ele) => ele.isError),
    );
    let tipIndex;
    arr.forEach((item, index) => {
      if (item.execution_id === row.execution_id && item.dialogId === row.dialogId) {
        tipIndex = index;
      }
    });
    this.kbConfRef._results.forEach(element => {
      element.hide();
    });
    e.stopPropagation();
    this.tipContext = {
      traceId: row.execution_id,
      outputs: {
        ok: ($event: string): void => {
          row.tagNum = Number($event);
          this.kbConfRef._results[tipIndex].hide();
        },
      },
    };
    setTimeout(() => {
      this.kbConfRef._results[tipIndex].show();
    });
  }

  public onClickLike(e, row) {
    e.stopPropagation();
    let param = {
      span_Id: row.execution_id,
      vote: row.vote === 1 ? 0 : 1,
      data_type: 'trace',
    };
    this.sessioMgnService.vote(param).then((res) => {
      row.vote = row.vote === 1 ? 0 : 1;
    });
  }

  public onClickDislike(e, row) {
    e.stopPropagation();
    let param = {
      span_Id: row.execution_id,
      vote: row.vote === -1 ? 0 : -1,
      data_type: 'trace',
    };
    this.sessioMgnService.vote(param).then((res) => {
      row.vote = row.vote === -1 ? 0 : -1;
    });
  }
}
