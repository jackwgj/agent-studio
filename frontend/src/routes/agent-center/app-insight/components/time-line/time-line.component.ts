/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable @typescript-eslint/restrict-template-expressions */
/* eslint-disable @typescript-eslint/no-unsafe-argument */
/* eslint-disable @typescript-eslint/no-unused-vars */
/* eslint-disable no-self-assign */
/* eslint-disable @typescript-eslint/restrict-plus-operands */
/* eslint-disable @typescript-eslint/no-unsafe-call */
/* eslint-disable @typescript-eslint/no-unsafe-member-access */
import {
  Component,
  ChangeDetectionStrategy,
  Input,
  OnChanges,
  SimpleChanges,
  ViewChild,
  ElementRef,
  AfterViewInit,
  OnDestroy,
  EventEmitter,
  Output,
} from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import {
  TimelineInvokeType,
  type Invoke,
} from '@routes/agent-center/interfaces/insight/app-insight.interface';
import { cdnAssetUrl } from 'src/single-spa/assets-url';

type DataType = Invoke & { startX: number; width: number };
const colorList: { [key: string]: string[] } = {
  llm: Array(4).fill('rgba(222, 236, 255, 0.96)'),
  plugin: Array(4).fill('rgba(222, 236, 255, 0.96)'),
  default: Array(4).fill('rgba(222, 236, 255, 0.96)'),
};

@Component({
  selector: 'time-line',
  templateUrl: './time-line.component.html',
  styleUrls: ['./time-line.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class TimeLineComponent implements AfterViewInit, OnDestroy, OnChanges {
  @Input() currentInvodeId?: string;

  @Input() data: any;

  @Output() handleClick = new EventEmitter<string>();

  @ViewChild('timelineRef') timelineRef!: ElementRef;

  public state: any = {
    startX: 0, // 画图区域的开始位置离画布左侧的距离X
    width: 700, // 画图区域的宽度，相当于画布宽度-2*(startX)

    sliderStartX: 4, // 时间轴滑动区域的起始位置离画布左侧的距离X，相当于dragSideWidth/2
    sliderWidth: 692, // 时间轴滑动区域宽度，相当于画布宽度-2*(sliderStartX)
    timelineHeight: 50, // 时间轴高度，等于timelineTickHeight+timelineSliderHeight
    timelineTickHeight: 30, // 时间轴上刻度区域高度
    timelineSliderHeight: 20, // 时间轴上滑块区域高度

    selectedStartX: -1, // 选中区域在画布中的左侧x坐标
    selectedEndX: -1, // 选中区域在画布中的右侧x坐标
    isDragging: false, // 是否正在拖动选中区域
    isDraggingLeft: false, // 是否正在拖动选中区域左端
    isDraggingRight: false, // 是否正在拖动选中区域右端
    dragStartX: 0, // 开始拖动的clientX，用于计算dragOffsetX
    dragOffsetLeft: 0, // 拖动时的偏移量（就是拖动结束的clientX-dragStartX）
    dragOffsetRight: 0, // 拖动时的偏移量（就是拖动结束的clientX-dragStartX）
    dragSideWidth: 8, // 左右拖拽条的宽度

    tickInterval: 50, // 刻度间隔，单位像素
    scale: 10, // 每个刻度代表的单位时间

    totalTime: 0, // 时间轴总时长
    minTime: 0, // 时间轴0ms位置对应的数据的实际开始时间

    isCursorOverSelection: false,

    swimmingItemHeight: 36, // 单个泳道高度
    swimmingItemGap: 10, // 泳道间隔高度
    swimmingHeight: 0, // 泳道总高度

    startImg: null as any,
    codeImg: null as any,
    branchImg: null as any,
    intentDetectionImg: null as any,
    questionerImg: null as any,
    pluginImg: null as any,
    mcpImg: null as any,
    endImg: null as any,
    messageImg: null as any,
    llmImg: null as any,
    knowledgeImg: null as any,
    aggregationImg: null as any,
    workflowImg: null as any,
    inputImg: null as any,
    httpImg: null as any,
    qaImg: null as any,
    sqlImg: null as any,
  };

  private isDrawing = false;

  private canvas: any;

  private ctx: any;

  private drawTimeline(swimmingHeight: number) {
    this.ctx.fillStyle = '#F5F5F5';
    this.ctx.fillRect(
      this.state.sliderStartX,
      swimmingHeight + this.state.timelineTickHeight,
      this.state.sliderWidth,
      this.state.timelineSliderHeight,
    );
    this.drawTicks(swimmingHeight);

    if (this.state.selectedStartX !== -1 && this.state.selectedEndX !== -1) {
      const cornerRadius = 4;
      this.ctx.fillStyle = 'rgba(20, 118, 255, 0.2)';
      this.ctx.fillRect(
        Math.min(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) + cornerRadius,
        swimmingHeight + this.state.timelineTickHeight,
        Math.abs(
          this.state.dragOffsetRight +
            this.state.selectedEndX -
            (this.state.dragOffsetLeft + this.state.selectedStartX),
        ) - cornerRadius,
        this.state.timelineSliderHeight,
      );

      this.ctx.fillStyle = '#1476FF';
      let startSliderX =
        Math.min(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) -
        this.state.dragSideWidth / 2;
      let startSliderY = swimmingHeight + this.state.timelineTickHeight;
      this.ctx.beginPath();
      this.ctx.moveTo(startSliderX + cornerRadius, startSliderY);
      this.ctx.arc(
        startSliderX + cornerRadius,
        startSliderY + cornerRadius,
        cornerRadius,
        Math.PI * 1.5,
        Math.PI,
        true,
      );
      this.ctx.lineTo(
        startSliderX,
        startSliderY + this.state.timelineSliderHeight - cornerRadius,
      );
      this.ctx.arc(
        startSliderX + cornerRadius,
        startSliderY + this.state.timelineSliderHeight - cornerRadius,
        cornerRadius,
        Math.PI,
        Math.PI * 0.5,
        true,
      );
      this.ctx.lineTo(
        startSliderX + this.state.dragSideWidth,
        startSliderY + this.state.timelineSliderHeight,
      );
      this.ctx.lineTo(startSliderX + this.state.dragSideWidth, startSliderY);
      this.ctx.closePath();
      this.ctx.fill();

      startSliderX =
        Math.max(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) +
        this.state.dragSideWidth / 2;
      startSliderY = swimmingHeight + this.state.timelineTickHeight;
      this.ctx.beginPath();
      this.ctx.moveTo(startSliderX, startSliderY + cornerRadius);
      this.ctx.arc(
        startSliderX - cornerRadius,
        startSliderY + cornerRadius,
        cornerRadius,
        0,
        Math.PI * 1.5,
        true,
      );
      this.ctx.lineTo(startSliderX - this.state.dragSideWidth, startSliderY);
      this.ctx.lineTo(
        startSliderX - this.state.dragSideWidth,
        startSliderY + this.state.timelineSliderHeight,
      );
      this.ctx.lineTo(
        startSliderX - cornerRadius,
        startSliderY + this.state.timelineSliderHeight,
      );
      this.ctx.arc(
        startSliderX - cornerRadius,
        startSliderY + this.state.timelineSliderHeight - cornerRadius,
        cornerRadius,
        Math.PI * 0.5,
        0,
        true,
      );
      this.ctx.closePath();
      this.ctx.fill();

      this.ctx.strokeStyle = '#FFFFFF';
      this.ctx.setLineDash([]);
      this.ctx.beginPath();
      this.ctx.moveTo(
        Math.min(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) -
          this.state.dragSideWidth / 2 +
          3,
        swimmingHeight + this.state.timelineTickHeight + 8,
      );
      this.ctx.lineTo(
        Math.min(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) -
          this.state.dragSideWidth / 2 +
          3,
        swimmingHeight + this.state.timelineTickHeight + 12,
      );
      this.ctx.stroke();
      this.ctx.beginPath();
      this.ctx.moveTo(
        Math.min(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) -
          this.state.dragSideWidth / 2 +
          5,
        swimmingHeight + this.state.timelineTickHeight + 6,
      );
      this.ctx.lineTo(
        Math.min(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) -
          this.state.dragSideWidth / 2 +
          5,
        swimmingHeight + this.state.timelineTickHeight + 14,
      );
      this.ctx.stroke();
      this.ctx.beginPath();
      this.ctx.moveTo(
        Math.max(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) -
          this.state.dragSideWidth / 2 +
          5,
        swimmingHeight + this.state.timelineTickHeight + 8,
      );
      this.ctx.lineTo(
        Math.max(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) -
          this.state.dragSideWidth / 2 +
          5,
        swimmingHeight + this.state.timelineTickHeight + 12,
      );
      this.ctx.stroke();
      this.ctx.beginPath();
      this.ctx.moveTo(
        Math.max(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) -
          this.state.dragSideWidth / 2 +
          3,
        swimmingHeight + this.state.timelineTickHeight + 6,
      );
      this.ctx.lineTo(
        Math.max(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) -
          this.state.dragSideWidth / 2 +
          3,
        swimmingHeight + this.state.timelineTickHeight + 14,
      );
      this.ctx.stroke();
    }
  }

  private drawTicks(swimmingHeight: number) {
    const bili =
      Math.abs(
        this.state.dragOffsetRight +
          this.state.selectedEndX -
          (this.state.dragOffsetLeft + this.state.selectedStartX),
      ) / this.state.sliderWidth;
    if (bili > 0) {
      const startXN =
        ((this.state.sliderStartX -
          Math.min(
            this.state.dragOffsetLeft + this.state.selectedStartX,
            this.state.dragOffsetRight + this.state.selectedEndX,
          )) /
          this.state.sliderWidth) *
          (this.state.width / bili) +
        this.state.startX;
      const endXN =
        this.state.startX +
        this.state.width -
        ((Math.max(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) -
          (this.state.sliderStartX + this.state.sliderWidth)) /
          this.state.sliderWidth) *
          (this.state.width / bili);

      const selectedTime = this.state.totalTime * bili;
      const nextScale = this.getScale(selectedTime)[1];
      let newTickInterval = this.state.tickInterval / bili;
      newTickInterval = (newTickInterval * nextScale) / this.state.scale;
      const scale = nextScale;

      for (let x = startXN; x <= endXN; x += newTickInterval) {
        const timeValue = `${Math.round(
          (x - startXN) / (newTickInterval / scale),
        )}ms`;
        this.ctx.strokeStyle = '#F0F0F0';
        this.ctx.setLineDash([5, 3]);
        this.ctx.beginPath();
        this.ctx.moveTo(x, 0);
        this.ctx.lineTo(x, swimmingHeight + 5);
        this.ctx.stroke();
        this.ctx.fillStyle = '#808080';
        this.ctx.fillText(timeValue, x, swimmingHeight + 20);
      }
    }
  }

  private drawSwimming(propData: Invoke, e?: any) {
    if (!propData) {
      return;
    }
    if (!this.canvas || this.isDrawing) {
      return;
    }
    this.isDrawing = true;
    const swimmingData = this.getSwimmingData(propData);
    this.state.swimmingHeight =
      swimmingData.length *
      (this.state.swimmingItemHeight + this.state.swimmingItemGap);
    this.canvas.height = this.state.timelineHeight + this.state.swimmingHeight;
    this.timelineRef.nativeElement.style.height = `${this.canvas.height}'px'`;

    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    this.drawTimeline(this.state.swimmingHeight);

    if (e) {
      const rect = this.canvas.getBoundingClientRect();
      const mouseX = e.clientX - rect.left;
      const mouseY = e.clientY - rect.top;
      swimmingData.forEach((data: DataType, i: number) => {
        const x1 = data.startX;
        const x2 = x1 + data.width;
        const y1 =
          (this.state.swimmingItemHeight + this.state.swimmingItemGap) * i;
        const y2 = y1 + this.state.swimmingItemHeight;

        const positionCon =
          mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
        if (this.currentInvodeId === data.invokeId) {
          this.ctx.fillStyle =
            colorList[data.invokeType]?.[2] || colorList.default[2];
          this.ctx.strokeStyle =
            colorList[data.invokeType]?.[3] || colorList.default[3];
          this.ctx.setLineDash([]);
          this.ctx.strokeRect(
            data.startX,
            (this.state.swimmingItemHeight + this.state.swimmingItemGap) * i,
            data.width,
            this.state.swimmingItemHeight,
          );
        } else if (positionCon) {
          this.ctx.fillStyle =
            colorList[data.invokeType]?.[1] || colorList.default[1];
        } else {
          this.ctx.fillStyle =
            colorList[data.invokeType]?.[0] || colorList.default[0];
        }
        this.ctx.fillRect(
          data.startX,
          (this.state.swimmingItemHeight + this.state.swimmingItemGap) * i,
          data.width,
          this.state.swimmingItemHeight,
        );
        const startX = this.getTextStartX(data);
        if (startX >= 0) {
          this.drawNodeByInvokeType(
            data.invokeType as TimelineInvokeType,
            startX,
            i,
          );
          this.ctx.fillStyle = '#191919';
          this.ctx.font = `12px sans-serif`;
          this.ctx.fillText(
            this.getInfoName(data),
            startX + 42,
            (this.state.swimmingItemHeight + this.state.swimmingItemGap) * i +
              24,
          );
        }
      });
    } else {
      swimmingData.forEach((data: DataType, i: number) => {
        if (this.currentInvodeId === data.invokeId) {
          this.ctx.fillStyle =
            colorList[data.invokeType]?.[2] || colorList.default[2];
          this.ctx.strokeStyle =
            colorList[data.invokeType]?.[3] || colorList.default[3];
          this.ctx.setLineDash([]);
          this.ctx.strokeRect(
            data.startX,
            (this.state.swimmingItemHeight + this.state.swimmingItemGap) * i,
            data.width,
            this.state.swimmingItemHeight,
          );
        } else {
          this.ctx.fillStyle =
            colorList[data.invokeType]?.[0] || colorList.default[0];
        }
        this.ctx.fillRect(
          data.startX,
          (this.state.swimmingItemHeight + this.state.swimmingItemGap) * i,
          data.width,
          this.state.swimmingItemHeight,
        );
        const textStartX = this.getTextStartX(data);
        if (textStartX >= 0) {
          this.drawNodeByInvokeType(
            data.invokeType as TimelineInvokeType,
            textStartX,
            i,
          );
          this.ctx.fillStyle = '#191919';
          this.ctx.font = `12px sans-serif`;
          this.ctx.fillText(
            this.getInfoName(data),
            textStartX + 42,
            (this.state.swimmingItemHeight + this.state.swimmingItemGap) * i +
              24,
          );
        }
      });
    }
    this.isDrawing = false;
  }

  private drawNodeByInvokeType(
    invokeType: TimelineInvokeType,
    startX: number,
    lineIndex: number,
  ) {
    switch (invokeType) {
      case TimelineInvokeType.Start:
        this.drawAndGetStartImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Code:
        this.drawAndGetCodeImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Branch:
        this.drawAndGetBranchImg(startX, lineIndex);
        break;
      case TimelineInvokeType.IntentDetection:
      case TimelineInvokeType.ComplexIntentDetection:
      case TimelineInvokeType.IntentDetectionContainer:
        this.drawAndGetIntentDetectionImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Questioner:
        this.drawAndGetQuestionerImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Plugin:
        this.drawAndGetPluginImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Mcp:
      case TimelineInvokeType.mcp:
        this.drawAndGetMcpImg(startX, lineIndex);
        break;
      case TimelineInvokeType.End:
        this.drawAndGetEndImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Message:
        this.drawAndGetMessageImg(startX, lineIndex);
        break;
      case TimelineInvokeType.LLM:
        this.drawAndGetLLMImg(startX, lineIndex);
        break;
      case TimelineInvokeType.LTM:
        this.drawAndGetLTMImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Knowledge:
        this.drawAndGetKnowledgeImg(startX, lineIndex);
        break;
      case TimelineInvokeType.KnowledgeRepo:
        this.drawAndGetKnowledgeRepoImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Aggregation:
        this.drawAndGetAggregationImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Workflow:
        this.drawAndGetWorkflowImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Input:
        this.drawAndGetInputImg(startX, lineIndex);
        break;
      case TimelineInvokeType.SetVariable:
        this.drawAndGetSetVariableImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Loop:
        this.drawAndGetLoopImg(startX, lineIndex);
        break;
      case TimelineInvokeType.LoopInput:
        this.drawAndGetLoopInputImg(startX, lineIndex);
        break;
      case TimelineInvokeType.LoopOutput:
        this.drawAndGetLoopOutputImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Http:
        this.drawAndGetHttpImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Agent:
        this.drawAndGetAgentImg(startX, lineIndex);
        break;
      case TimelineInvokeType.QA:
        this.drawAndGetQAImg(startX, lineIndex);
        break;
      case TimelineInvokeType.Sql:
        this.drawAndGetSqlImg(startX, lineIndex);
        break;
      case TimelineInvokeType.StreamTransform:
        this.drawAndGetStreamTransformImg(startX, lineIndex);
        break;
      default:
        break;
    }
  }

  private drawAndGetStartImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'startImg',
      cdnAssetUrl('assets/agent-center/flow/Start.svg'),
    );
  }

  private drawAndGetCodeImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'codeImg',
      cdnAssetUrl('assets/agent-center/flow/Code.svg'),
    );
  }

  private drawAndGetBranchImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'branchImg',
      cdnAssetUrl('assets/agent-center/flow/Branch.svg'),
    );
  }

  private drawAndGetIntentDetectionImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'intentDetectionImg',
      cdnAssetUrl('assets/agent-center/flow/Intent.svg'),
    );
  }

  private drawAndGetQuestionerImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'questionerImg',
      cdnAssetUrl('assets/agent-center/flow/Questioner.svg'),
    );
  }

  private drawAndGetPluginImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'pluginImg',
      cdnAssetUrl('assets/agent-center/flow/Plugin.svg'),
    );
  }

  private drawAndGetMcpImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'mcpImg',
      cdnAssetUrl('assets/agent-center/images/service-default.svg'),
    );
  }

  private drawAndGetEndImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'endImg',
      cdnAssetUrl('assets/agent-center/flow/End.svg'),
    );
  }

  private drawAndGetMessageImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'messageImg',
      cdnAssetUrl('assets/agent-center/flow/message.svg'),
    );
  }

  private drawAndGetStreamTransformImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'messageImg',
      cdnAssetUrl('assets/agent-center/flow/stream-transform.svg'),
    );
  }

  private drawAndGetLLMImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'llmImg',
      cdnAssetUrl('assets/agent-center/flow/LLM.svg'),
    );
  }

  private drawAndGetKnowledgeImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'knowledgeImg',
      cdnAssetUrl('assets/agent-center/flow/Knowledge.svg'),
    );
  }

  private drawAndGetKnowledgeRepoImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'knowledgeImg',
      cdnAssetUrl('assets/agent-center/flow/KnowledgeRepo.svg'),
    );
  }

  private drawAndGetAggregationImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'aggregationImg',
      cdnAssetUrl('assets/agent-center/flow/Aggregation.svg'),
    );
  }

  private drawAndGetWorkflowImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'workflowImg',
      cdnAssetUrl('assets/agent-center/flow/ChildFlow.svg'),
    );
  }

  private drawAndGetInputImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'inputImg',
      cdnAssetUrl('assets/agent-center/flow/Input.svg'),
    );
  }

  private drawAndGetSetVariableImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'setVariableImg',
      cdnAssetUrl('assets/agent-center/flow/SetVariable.svg'),
    );
  }

  private drawAndGetLoopImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'loopImg',
      cdnAssetUrl('assets/agent-center/flow/Loop.svg'),
    );
  }

  private drawAndGetLoopInputImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'loopInputImg',
      cdnAssetUrl('assets/agent-center/flow/Start.svg'),
    );
  }

  private drawAndGetLoopOutputImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'loopOutputImg',
      cdnAssetUrl('assets/agent-center/flow/End.svg'),
    );
  }

  private drawAndGetHttpImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'httpImg',
      cdnAssetUrl('assets/agent-center/flow/Http.svg'),
    );
  }

  private drawAndGetAgentImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'agentImg',
      cdnAssetUrl('assets/agent-center/flow/Agent.svg'),
    );
  }

  private drawAndGetLTMImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'ltmImg',
      cdnAssetUrl('assets/agent-center/flow/LTM.svg'),
    );
  }

  private drawAndGetQAImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'qaImg',
      cdnAssetUrl('assets/agent-center/flow/QA.svg'),
    );
  }

  private drawAndGetSqlImg(startX: number, lineIndex: number) {
    this.drawAndGetImage(
      startX,
      lineIndex,
      'sqlImg',
      cdnAssetUrl('assets/agent-center/flow/Sql.svg'),
    );
  }

  private drawAndGetImage(
    startX: number,
    lineIndex: number,
    imgKey: string,
    imgSrc: string,
  ) {
    if (this.state[imgKey]) {
      this.ctx.drawImage(
        this.state[imgKey],
        startX + 12,
        (this.state.swimmingItemHeight + this.state.swimmingItemGap) *
          lineIndex +
          8,
        20,
        20,
      );
    } else {
      const img = new Image();
      img.src = imgSrc;
      img.onload = () => {
        this.state[imgKey] = img;
        this.ctx.drawImage(
          img,
          startX + 12,
          (this.state.swimmingItemHeight + this.state.swimmingItemGap) *
            lineIndex +
            8,
          20,
          20,
        );
      };
    }
  }

  private getTextStartX(data: DataType) {
    if (data.startX + data.width < this.state.startX) {
      return -1;
    }

    if (data.startX < this.state.startX) {
      return this.state.startX;
    }

    return data.startX;
  }

  private isCursorOverSelectionFunc = (e: any) => {
    const selectionLeft =
      Math.min(this.state.selectedStartX, this.state.selectedEndX) -
      this.state.dragSideWidth / 2;
    const selectionRight =
      Math.max(this.state.selectedStartX, this.state.selectedEndX) +
      this.state.dragSideWidth / 2;
    const rect = this.canvas.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;
    return (
      selectionLeft <= mouseX &&
      mouseX <= selectionRight &&
      mouseY >= this.state.swimmingHeight &&
      mouseY <= this.state.swimmingHeight + this.state.timelineHeight
    );
  };

  private updateCursorStyle = (e: any) => {
    if (this.state.isDraggingLeft || this.state.isDraggingRight) {
      this.canvas.style.cursor = 'e-resize';
      return;
    }
    const rect = this.canvas.getBoundingClientRect();
    const mouseY = e.clientY - rect.top;
    if (
      mouseY < this.state.swimmingHeight ||
      mouseY > this.state.swimmingHeight + this.state.timelineHeight
    ) {
      this.canvas.style.cursor = '';
      return;
    }
    const mouseX = e.clientX - rect.left;

    const selectPos =
      (mouseX >= this.state.selectedStartX - this.state.dragSideWidth / 2 &&
        mouseX < this.state.selectedStartX + this.state.dragSideWidth / 2) ||
      (mouseX <= this.state.selectedEndX + this.state.dragSideWidth / 2 &&
        mouseX > this.state.selectedEndX - this.state.dragSideWidth / 2);
    if (selectPos) {
      this.canvas.style.cursor = 'e-resize';
      return;
    }
    if (this.state.isCursorOverSelection) {
      this.canvas.style.cursor = 'all-scroll';
      return;
    }
    this.canvas.style.cursor = '';
  };

  private handleResize = () => {
    const newWidth =
      this.timelineRef.nativeElement.getBoundingClientRect().width -
      this.state.startX * 2;
    this.canvas.width = newWidth + this.state.startX * 2;
    if (!this.state.width) {
      this.state.width = newWidth;
      this.state.sliderWidth = newWidth - this.state.sliderStartX * 2;
      this.initDrawSwiming(this.data);
      return;
    }

    const beishu = this.state.width ? newWidth / this.state.width : 1;
    this.state.width = newWidth;
    this.state.sliderWidth = newWidth - this.state.sliderStartX * 2;
    const newTickInterval = this.state.tickInterval * beishu;
    this.state.tickInterval = newTickInterval;
    this.state.scale = this.state.scale;

    this.state.selectedStartX =
      (this.state.selectedStartX - this.state.sliderStartX) * beishu +
      this.state.sliderStartX;
    this.state.selectedEndX =
      (this.state.selectedEndX - this.state.sliderStartX) * beishu +
      this.state.sliderStartX;

    this.drawSwimming(this.data);
  };

  private onMousedown = (e: any) => {
    this.state.isCursorOverSelection = this.isCursorOverSelectionFunc(e);
    if (this.state.isCursorOverSelection) {
      const rect = this.canvas.getBoundingClientRect();
      const mouseX = e.clientX - rect.left;
      if (
        mouseX >= this.state.selectedStartX - this.state.dragSideWidth / 2 &&
        mouseX < this.state.selectedStartX + this.state.dragSideWidth / 2
      ) {
        this.state.isDraggingLeft = true;
        this.state.dragStartX = e.clientX;
      } else if (
        mouseX <= this.state.selectedEndX + this.state.dragSideWidth / 2 &&
        mouseX > this.state.selectedEndX - this.state.dragSideWidth / 2
      ) {
        this.state.isDraggingRight = true;
        this.state.dragStartX = e.clientX;
      } else if (
        mouseX >=
          Math.min(this.state.selectedStartX, this.state.selectedEndX) +
            this.state.dragSideWidth / 2 &&
        mouseX <=
          Math.max(this.state.selectedStartX, this.state.selectedEndX) -
            this.state.dragSideWidth / 2
      ) {
        this.state.isDragging = true;
        this.state.dragStartX = e.clientX;
      } else {
        // do nth
      }
    }
    window.addEventListener('mousemove', this.onMousemove);
    window.addEventListener('mouseup', this.onMouseup);
  };

  private onMousemove = (e: any) => {
    if (this.state.isDraggingLeft) {
      let deltaX = e.clientX - this.state.dragStartX;
      if (
        Math.abs(this.state.selectedStartX + deltaX - this.state.selectedEndX) <
        1
      ) {
        deltaX = this.state.selectedEndX - 1 - this.state.selectedStartX;
      } else if (this.state.selectedStartX + deltaX < this.state.sliderStartX) {
        deltaX = this.state.sliderStartX - this.state.selectedStartX;
      } else if (
        this.state.selectedStartX + deltaX >
        this.state.sliderStartX + this.state.sliderWidth
      ) {
        if (
          this.state.selectedEndX ===
          this.state.sliderStartX + this.state.sliderWidth
        ) {
          deltaX =
            this.state.sliderStartX +
            this.state.sliderWidth -
            this.state.selectedStartX -
            1;
        } else {
          deltaX =
            this.state.sliderStartX +
            this.state.sliderWidth -
            this.state.selectedStartX;
        }
      } else {
        // do nth
      }
      this.state.dragOffsetLeft = deltaX;
      this.state.dragOffsetRight = 0;
      this.drawSwimming(this.data, e);
      this.state.isCursorOverSelection = true;
    } else if (this.state.isDraggingRight) {
      let deltaX = e.clientX - this.state.dragStartX;
      if (
        Math.abs(this.state.selectedEndX + deltaX - this.state.selectedStartX) <
        1
      ) {
        deltaX = 1 + this.state.selectedStartX - this.state.selectedEndX;
      } else if (
        this.state.selectedEndX + deltaX >
        this.state.sliderStartX + this.state.sliderWidth
      ) {
        deltaX =
          this.state.sliderStartX +
          this.state.sliderWidth -
          this.state.selectedEndX;
      } else if (this.state.selectedEndX + deltaX < this.state.sliderStartX) {
        if (this.state.sliderStartX === this.state.selectedStartX) {
          deltaX = this.state.sliderStartX - this.state.selectedEndX + 1;
        } else {
          deltaX = this.state.sliderStartX - this.state.selectedEndX;
        }
      } else {
        // do nth
      }
      this.state.dragOffsetLeft = 0;
      this.state.dragOffsetRight = deltaX;
      this.drawSwimming(this.data, e);
      this.state.isCursorOverSelection = true;
    } else if (this.state.isDragging) {
      let deltaX = e.clientX - this.state.dragStartX;
      if (this.state.selectedStartX + deltaX < this.state.sliderStartX) {
        deltaX = this.state.sliderStartX - this.state.selectedStartX;
      } else if (
        this.state.selectedEndX + deltaX >
        this.state.sliderStartX + this.state.sliderWidth
      ) {
        deltaX =
          this.state.sliderStartX +
          this.state.sliderWidth -
          this.state.selectedEndX;
      } else {
        // do nth
      }
      this.state.dragOffsetLeft = deltaX;
      this.state.dragOffsetRight = deltaX;
      this.drawSwimming(this.data, e);
      this.state.isCursorOverSelection = true;
    } else {
      this.drawSwimming(this.data, e);
      this.state.isCursorOverSelection = this.isCursorOverSelectionFunc(e);
    }
    this.updateCursorStyle(e);
  };

  private onMouseup = (e: any) => {
    const rect = this.canvas.getBoundingClientRect();
    if (
      this.state.isDraggingLeft ||
      this.state.isDraggingRight ||
      this.state.isDragging
    ) {
      this.state.selectedStartX += this.state.dragOffsetLeft;
      this.state.selectedEndX += this.state.dragOffsetRight;
      this.state.dragOffsetLeft = 0;
      this.state.dragOffsetRight = 0;
    }
    if (this.state.selectedStartX > this.state.selectedEndX) {
      [this.state.selectedStartX, this.state.selectedEndX] = [
        this.state.selectedEndX,
        this.state.selectedStartX,
      ];
    }
    if (this.state.selectedStartX < this.state.sliderStartX) {
      this.state.selectedEndX +=
        this.state.sliderStartX - this.state.selectedStartX;
      this.state.selectedStartX = this.state.sliderStartX;
    }
    if (
      this.state.selectedEndX >
      this.state.sliderStartX + this.state.sliderWidth
    ) {
      this.state.selectedStartX -=
        this.state.selectedEndX -
        (this.state.sliderStartX + this.state.sliderWidth);
      this.state.selectedEndX =
        this.state.sliderStartX + this.state.sliderWidth;
    }
    if (this.state.selectedStartX < this.state.sliderStartX) {
      this.state.selectedStartX = this.state.sliderStartX;
    }
    if (
      this.state.selectedEndX >
      this.state.sliderStartX + this.state.sliderWidth
    ) {
      this.state.selectedEndX =
        this.state.sliderStartX + this.state.sliderWidth;
    }
    this.state.isDragging = false;
    this.state.isDraggingLeft = false;
    this.state.isDraggingRight = false;
    this.drawSwimming(this.data, e);
    window.removeEventListener('mousemove', this.onMousemove);
    window.removeEventListener('mouseup', this.onMouseup);
  };

  private findClosestElement(array, target) {
    let minDiff = Infinity;
    let closestElement = null;

    array.forEach((element) => {
      const diff = Math.abs(element - target);
      if (diff < minDiff) {
        minDiff = diff;
        closestElement = element;
      }
    });

    return closestElement;
  }

  private getScale(realTotalTime: number) {
    const strLen = realTotalTime.toString().length;
    const scaleList = [];
    for (let i = 0; i <= strLen; i++) {
      scaleList.push(...[10 ** i, 10 ** i * 2, 10 ** i * 5, 10 ** i * 8]);
    }
    const tickNum = this.state.width <= 500 ? 6 : 10;
    const scale = this.findClosestElement(scaleList, realTotalTime / tickNum);
    const totalTime = Math.ceil(realTotalTime / scale) * scale;
    const tickInterval = (scale / totalTime) * this.state.width;
    return [totalTime, scale, tickInterval];
  }

  private getTotalTimeAndScale(data: Invoke) {
    const { startTime, endTime } = data;
    const minTime = new Date(startTime).getTime();
    const maxTime = new Date(endTime).getTime();
    const realTotalTime = maxTime - minTime;
    this.state.minTime = minTime;

    const [totalTime, scale, tickInterval] = this.getScale(realTotalTime);
    this.state.scale = scale;
    this.state.totalTime = totalTime;
    this.state.tickInterval = tickInterval;
  }

  private getSwimmingData(data: Invoke): DataType[] {
    let startXN = this.state.startX;
    let endXN = this.state.startX + this.state.width;
    const bili =
      Math.abs(
        this.state.dragOffsetRight +
          this.state.selectedEndX -
          (this.state.dragOffsetLeft + this.state.selectedStartX),
      ) / this.state.width;
    if (bili > 0) {
      startXN =
        ((this.state.sliderStartX -
          Math.min(
            this.state.dragOffsetLeft + this.state.selectedStartX,
            this.state.dragOffsetRight + this.state.selectedEndX,
          )) /
          this.state.sliderWidth) *
          (this.state.width / bili) +
        this.state.startX;
      endXN =
        this.state.startX +
        this.state.width -
        ((Math.max(
          this.state.dragOffsetLeft + this.state.selectedStartX,
          this.state.dragOffsetRight + this.state.selectedEndX,
        ) -
          (this.state.sliderStartX + this.state.sliderWidth)) /
          this.state.sliderWidth) *
          (this.state.width / bili);
    }

    let swimmingData: DataType[] = [];
    const { childInvokes } = data;
    if (childInvokes && childInvokes.length > 0) {
      childInvokes.forEach((item) => {
        const invokeType = item.invokeType;
        if (
          invokeType in TimelineInvokeType &&
          (!item.childInvokes || item.childInvokes.length === 0)
        ) {
          const { startTime, endTime } = item;
          const startTimeMill =
            new Date(startTime).getTime() - this.state.minTime;
          const endTimeMill = new Date(endTime).getTime() - this.state.minTime;
          const itemStartX =
            ((endXN - startXN) * startTimeMill) / this.state.totalTime +
            startXN;
          const itemEndX =
            ((endXN - startXN) * endTimeMill) / this.state.totalTime + startXN;
          const width = itemEndX - itemStartX;
          swimmingData.push({ ...item, startX: itemStartX, width });
        }
        const childSwimmingData = this.getSwimmingData(item);
        swimmingData = [...swimmingData, ...childSwimmingData];
      });
    }
    return swimmingData;
  }

  public onHandleClick = (e: any) => {
    const rect = this.canvas.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;
    const swimmingData = this.getSwimmingData(this.data);
    const item = swimmingData.find((data: DataType, i: number) => {
      const x1 = data.startX;
      const x2 = x1 + data.width;
      const y1 =
        (this.state.swimmingItemHeight + this.state.swimmingItemGap) * i;
      const y2 = y1 + this.state.swimmingItemHeight;
      return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
    });
    if (item) {
      this.handleClick.emit(item.node_uuid);
    }
  };

  private onMouseLeave = () => {
    this.state.isCursorOverSelection = false;
    this.canvas.style.cursor = 'default';
    this.drawSwimming(this.data);
  };

  private initDrawSwiming = (data: Invoke) => {
    if (!this.timelineRef) {
      return;
    }
    this.state.width =
      this.timelineRef.nativeElement.getBoundingClientRect().width -
      this.state.startX * 2;
    this.state.sliderWidth = this.state.width - this.state.sliderStartX * 2;
    if (!this.state.width) {
      return;
    }
    this.canvas.width = this.state.width + this.state.startX * 2;
    this.canvas.height = this.state.timelineHeight;
    this.state.selectedStartX = this.state.sliderStartX;
    this.state.selectedEndX = this.state.sliderStartX + this.state.sliderWidth;
    this.getTotalTimeAndScale(data);
  };

  ngAfterViewInit(): void {
    this.canvas = document.getElementById('timelineCanvas') as any;
    this.ctx = this.canvas.getContext('2d');

    this.canvas.addEventListener('mousedown', this.onMousedown);
    this.canvas.addEventListener('mousemove', this.onMousemove);
    this.canvas.addEventListener('mouseup', this.onMouseup);
    this.canvas.addEventListener('mouseenter', this.updateCursorStyle);
    this.canvas.addEventListener('mouseleave', this.onMouseLeave);
    this.canvas.addEventListener('click', this.onHandleClick);

    window.addEventListener('resize', this.handleResize);
    this.listenTimelineRefWidth();
    this.initDrawSwiming(this.data);
  }

  private listenTimelineRefWidth() {
    const resizeObserver = new ResizeObserver((entries) => {
      for (const entry of entries) {
        this.handleResize();
      }
    });

    resizeObserver.observe(this.timelineRef.nativeElement);
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.handleResize);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.data) {
      const data = changes.data.currentValue;
      if (data) {
        this.initDrawSwiming(data);
      }
    }
    if (changes.currentInvodeId) {
      const currentInvodeId = changes.currentInvodeId.currentValue;
      if (currentInvodeId && this.data) {
        this.drawSwimming(this.data);
      }
    }
  }

  public getInfoName = (info: any) => {
    const { node_name, node_id } = info || {};
    return node_name || node_id;
  };
}
