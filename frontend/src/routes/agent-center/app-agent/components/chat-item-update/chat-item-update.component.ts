import {
  Component,
  Input,
  OnDestroy,
  SimpleChanges,
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MODULES} from '@shared/modules';
import {NzIconModule} from 'ng-zorro-antd/icon';
import {NzCollapseModule} from 'ng-zorro-antd/collapse';
import {NzButtonModule} from 'ng-zorro-antd/button';
import {NzToolTipModule} from 'ng-zorro-antd/tooltip';
import {I18nNamespace} from '@i18n';
import {I18NEXT_NAMESPACE} from 'angular-i18next';
import {
  ConversationContent,
  FuncCallItem,
  TimeConsumption,
  TimeConsumptionStr,
} from '@routes/agent-center/types/my-workspace.types';
import {AppMarkdownAnswerComponent} from '@shared/components/app-markdown-answer/app-markdown-answer.component';
import {AssetQuoteIconComponent} from '@shared/components/assets/asset-quote-icon/asset-quote-icon.component';
import {CustomPopconfirmComponent} from '@shared/components/custom-popconfirm/custom-popconfirm.component';
import {ToolCallIOComponent} from '@shared/components/tool-call-IO/tool-call-IO.component';
import {ClickOutsideDirective} from '@shared/directives/click-outside.directive';
import {UploadFileIconComponent} from '@shared/components/upload-file-icon/upload-file-icon.component';
import {TtsPlayerService} from '@services/tts-player.service';
import {ChatItemComponent} from "@routes/agent-center/app-agent/components/chat-item/chat-item.component";
import {SafeHtmlPipe} from "../../../../../pipes/safehtml.pipe";

@Component({
  selector: 'chat-item-update',
  templateUrl: './chat-item-update.component.html',
  styleUrls: ['./chat-item-update.component.scss'],
  imports: [
    CommonModule,
    MODULES,
    ToolCallIOComponent,
    CustomPopconfirmComponent,
    AppMarkdownAnswerComponent,
    AssetQuoteIconComponent,
    ClickOutsideDirective,
    UploadFileIconComponent,
    SafeHtmlPipe,
    NzIconModule,
    NzCollapseModule,
    NzButtonModule,
    NzToolTipModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.TRACE],
    },
    TtsPlayerService,
  ],
})
export class ChatItemUpdateComponent extends ChatItemComponent implements OnDestroy {

  public curAnswer: (ConversationContent | FuncCallItem)

  public collapsed: boolean = true

  public thoughtFinished: boolean = false
  public dialogHistoryUpdate: any;

  public hasTool:boolean=false


  public printText:string=''

  public dislikeTipTitle: string = '';
  @Input() isFinished: boolean = false;

  @Input() isLast: boolean = false;
  override ngOnChanges(changes: SimpleChanges): void {
    if (changes.data) {

      const data = changes.data.currentValue;
      // 过滤掉 role 等于 "followUpQuestions" 的对象
      const filteredData = data?.filter(
        (item) => item.role !== 'followUpQuestions',
      );
      this.dialogHistoryUpdate = this.dealCvList(filteredData);
      this.aiAnswerListService.setFileSourceWithNewData(this.dialogHistoryUpdate);
      this.sourceFiles = this.aiAnswerListService.filterSourceFile(this.dialogHistoryUpdate);
    }
  }



  override dealCvList(list: any[]) : any{
    list = [...new Set(list)];
    let userList: Array<
      (ConversationContent | FuncCallItem) & {
      timeConsumptionStr: TimeConsumptionStr;
    }
    > = [];
    let answerList: Array<
      (ConversationContent | FuncCallItem) & {
      timeConsumptionStr: TimeConsumptionStr;
    }> = [];
    let summaryList: Array<
      (ConversationContent | FuncCallItem) & {
      timeConsumptionStr: TimeConsumptionStr;
    }> = [];
    for (let i = 0; i < list.length; i++) {
      const item = list[i];
      const {event, plugin, type, latency} = item || {};
      if (event === 'plugin_start') {
        const data = {} as FuncCallItem & {
          timeConsumptionStr: TimeConsumptionStr;
        };
        data.pluginName = plugin?.name;
        data.toolType = type;
        data.collapsed = false;
        if (plugin) {
          const {memory_variables, icon, ...rest} = item.plugin;
          data.request = rest;
          data.icon = icon
        }

        if(data.toolType === 'skill'){
          data.title = `SKILL - ${this.i18n.transform('used')}${
            data.pluginName
          }`;
          data.tipTitle = `${this.i18n.transform('used')} ${data.pluginName} SKILL`;
        } else{
          //mcp 和 插件的type 都是plugin
          data.title = `${this.i18n.transform('llmselectcomponent_426')} - ${this.i18n.transform('used')}${data.pluginName}`;
          data.tipTitle = `${this.i18n.transform('used')}${data.pluginName}${this.i18n.transform('llmselectcomponent_426')}`;
        }

        const time_consumption: TimeConsumption & { totalEx?: number } =
          latency;
        const next = list[i + 1];
        if (next && next.role === 'function') {
          data.isRunning = false;
          data.response = next?.content;
          data.status = next?.status;
          time_consumption.plugin_latency = next.latency?.plugin || 0;
          time_consumption.overall_latency = next.latency?.overall || 0;
        } else if (item.role === 'memorySet') {
          data.title = this.i18n.transform('setting_memory_completed');
          data.memory_variables = Object.keys(plugin?.memory_variables).map(
            (key) => ({key, value: plugin?.memory_variables[key]}),
          );
        } else {
          if (data.toolType !== 'plugin') {
            data.title = `${this.i18n.transform('workflow')} - ${this.i18n.transform('used')}${
              data.pluginName
            }`;
            data.tipTitle = `${this.i18n.transform('used')}${data.pluginName}${this.i18n.transform('workflow')}`;
          } else {
            if (data.pluginName === 'retrieval') {
              data.title = `${this.i18n.transform('knowledge')} - ${this.i18n.transform('searched')}${this.i18n.transform('knowledge')}`;
              data.tipTitle = `${this.i18n.transform('searched')}${this.i18n.transform('knowledge')}`;
            } else {
              data.title = `${this.i18n.transform('llmselectcomponent_426')} - ${this.i18n.transform('searched')}${
                data.pluginName
              }`;
              data.tipTitle = `${this.i18n.transform('searched')}${data.pluginName}${this.i18n.transform('llmselectcomponent_426')}`;
            }
          }
          data.isRunning = true;
        }
        if (item.role === 'memorySet') {
          data.role = 'memorySet';
        } else {
          data.role = 'functionRole';
        }
        data.time_consumption = time_consumption;
        data.timeConsumptionStr = this.getTimeDelayStr(time_consumption);
        answerList.push(data)
      } else if (item?.role === 'user') {
        const data = item as ConversationContent & {
          timeConsumptionStr: TimeConsumptionStr;
        };
        data.content = item.content;
        userList[i] = data
      } else {
        if (
          item.event !== 'function_call_end' &&
          item.event !== 'api_exec_data'
        ) {
          const data = item as ConversationContent & {
            timeConsumptionStr: TimeConsumptionStr;
          };
          data.timeConsumptionStr = this.getTimeDelayStr(data.time_consumption);
          if (item?.role?.startsWith('think')) {
            data.collapsed = false;
            data.title = this.i18n.transform('think.tips1')
          }

          if (item?.role?.startsWith('model')) {
            data.collapsed = false;
            data.title = this.i18n.transform('think.tips')
          }
          if (data.role !== 'assistant') {
            answerList.push(data)
          } else  {
            summaryList.push(data)
          }
        }
      }
    }

    answerList = answerList.filter((item) => item?.role !== 'function' && !item?.role?.startsWith('think'))
    if (!this.isFinished) {
      this.curAnswer = answerList[answerList.length - 1]
    }
    if (answerList.length) {
      this.hasTool = true
    }
    if(this.isFinished && summaryList.length){
      this.thoughtFinished = true
      this.collapsed = true;
    }
    return [...userList, {thought: answerList}, ...summaryList];
  }


  override changeCollapsedState(answer?:any) {
    if(answer){
      answer.collapsed = !answer.collapsed
    }else{
      this.collapsed = !this.collapsed
    }

  }


  override getTimeDelayStr(
    time_consumption: (TimeConsumption & { totalEx?: number }) | undefined,
  ) {
    let timeDelayRes = {} as TimeConsumptionStr;
    if (time_consumption === undefined) {
      return timeDelayRes;
    }
    if (
      time_consumption.overall_latency !== undefined &&
      time_consumption.overall_latency !== null
    ) {
      timeDelayRes.total_latency_str = `${this.i18n.transform(
        'total_latency',
      )} ${time_consumption.overall_latency}s `;
    } else {
      timeDelayRes.total_latency_str = '';
    }

    if (
      time_consumption.overall !== undefined &&
      time_consumption.overall !== null
    ) {
      timeDelayRes.overall_str = `${this.i18n.transform('run_time')} <span style='color:#000'>${
        time_consumption.overall
      }s</span>`;
    } else {
      timeDelayRes.overall_str = '';
    }

    if (
      time_consumption.plugin_latency !== undefined &&
      time_consumption.plugin_latency !== null
    ) {
      timeDelayRes.plugin_latency_str = `${time_consumption.plugin_latency}s `;
    } else {
      timeDelayRes.plugin_latency_str = '';
    }

    return timeDelayRes;
  }

}
