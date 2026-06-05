/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { Graph } from '@antv/x6';
import { AppFlowService } from '../../app-flow.service';
import { NodeService } from '../../node.service';
import type { ICommentNode } from '../../node.type';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeUtils } from '../utils';
import { NgClass } from '@angular/common';
import { MODULES } from '@shared/modules';

@Component({
  selector: 'meta-comment-node',
  templateUrl: './comment-node.component.html',
  styleUrls: ['../common-styles.less', './comment-node.component.less'],
  standalone: true,
  imports: [NgClass, MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class CommentNodeComponent
  extends NodeBaseComponent
  implements OnInit, OnChanges, AfterViewInit
{
  @Input('nodeInfo') nodeInfo: ICommentNode;

  // 编辑区
  @ViewChild('annotationEl') annotationEl: ElementRef<HTMLDivElement>;
  // 通用节点容器
  @ViewChild('nodeWrapper') nodeWrapperEl: ElementRef<HTMLDivElement>;

  public getType = NodeUtils.getFieldTypeView;

  content: string = '';

  isEditing = false;

  graph: Graph;

  constructor(
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    protected override cdr: ChangeDetectorRef,
    protected override elementRef: ElementRef<HTMLDivElement>,
  ) {
    super(nodeServ, appFlowServ, cdr, elementRef);
  }

  override ngOnInit(): void {
    this.setNodeBase(this.nodeInfo);
    this.graph = this.appFlowServ.getGraph();
    this.content = this.nodeInfo?.content;
    super.ngOnInit();
    if (this.graph) {
      // 撤销和重做只会更新节点大小，不会更新视图，需要手动更新视图
      // 监听执行撤销操作的事件
      this.graph.on('history:undo', (event) => {
        // 使用setTimeout确保在DOM更新后执行
        const data = event.cmds[0].data;
        if (data.id === this.nodeInfo.id) {
          if ('key' in data && data.key === 'size') {
            const { width, height } = data.prev.size;
            this.setNodeSize(width, height);
          } else if ('key' in data && data.key === 'data') {
            this.syncNodeData(data.prev.data.ngArguments.nodeInfo);
          } else {
            // do nth
          }
        }
      });

      // 监听执行重做操作的事件
      this.graph.on('history:redo', (event) => {
        const data = event.cmds[0].data;
        if (data.id === this.nodeInfo.id) {
          if ('key' in data && data.key === 'size') {
            const { width, height } = data.next.size;
            this.setNodeSize(width, height);
          } else if ('key' in data && data.key === 'data') {
            this.syncNodeData(data.next.data.ngArguments.nodeInfo);
          } else {
            // do nth
          }
        }
      });
    }
  }

  override ngAfterViewInit() {
    super.ngAfterViewInit();
    // 视图加载完成后用节点实际数据刷新
    this.syncNodeData(this.nodeInfo);
    // 首次需要加载size
    const node: any = this.graph.getCellById(this.nodeInfo.id);
    const { width, height } = this.nodeInfo;
    node.resize(width + 4, height + 4);
  }

  override ngOnChanges(changes: SimpleChanges): void {
    // 该节点手动管理size,避免执行父组件中的高度管理方法
  }

  handleDbClick(event: MouseEvent) {
    this.isEditing = true;
    this.setNodeEditing();
    this.bindStopPropagation();
  }

  stopMouseBubbleEvent(e) {
    e.stopPropagation();
  }

  stopKeyShot(e) {
    const key = e.metaKey || e.ctrlKey;
    const editKeys = ['c', 'v', 'x', 'a', 'z', 'y'];
    if (key && editKeys.includes(e.key.toLowerCase())) {
      e.stopPropagation();
    }
  }

  bindStopPropagation() {
    const currNodeEle = this.annotationEl.nativeElement;
    currNodeEle.addEventListener('mousedown', this.stopMouseBubbleEvent);
    currNodeEle.addEventListener('mousemove', this.stopMouseBubbleEvent);
    currNodeEle.addEventListener('mouseup', this.stopMouseBubbleEvent);
    currNodeEle.addEventListener('wheel', this.stopMouseBubbleEvent);
    currNodeEle.addEventListener('keydown', this.stopKeyShot);
  }

  unbindStopPropagation() {
    const currNodeEle = this.annotationEl.nativeElement;
    currNodeEle.removeEventListener('mousedown', this.stopMouseBubbleEvent);
    currNodeEle.removeEventListener('mousemove', this.stopMouseBubbleEvent);
    currNodeEle.removeEventListener('mouseup', this.stopMouseBubbleEvent);
    currNodeEle.removeEventListener('wheel', this.stopMouseBubbleEvent);
    currNodeEle.removeEventListener('keydown', this.stopKeyShot);
  }

  setNodeEditing() {
    const { id } = this.nodeInfo;
    const node = this.graph.getCellById(id);
    if (node) {
      node.setData(
        {
          isEditing: this.isEditing,
        },
        { ignoreHistory: true },
      );
    }
  }

  /**
   * 保存内容
   */
  onBlur() {
    this.isEditing = false;

    this.setNodeEditing();
    this.unbindStopPropagation();
    const selection = document.getSelection();
    selection.removeAllRanges();

    const text = this.annotationEl.nativeElement.innerText;
    this.appFlowServ.setNodeSaveMonitor({
      nodeData: {
        ...this.nodeInfo,
        content: text,
      },
    });
  }
  // 拖拽改变大小相关

  // 触发拖拽的初始位置
  startX: number;
  startY: number;
  // 开始拖拽时的宽高
  startWidth: number;
  startHeight: number;
  // 控制是否修改宽高
  isResizeHeight = false;
  isResizeWidth = false;
  /**
   * 拖拽时实时计算并更新节点的视图（没有真正保存）
   * @param event
   */
  handleMouseMove = (event: MouseEvent) => {
    //计算偏移
    const offsetX = event.clientX - this.startX;
    const offsetY = event.clientY - this.startY;
    // 计算新的宽高
    const scale = this.appFlowServ.getLocalScale() / 100;
    const newWidth = (this.startWidth + offsetX) / scale;
    const newHeight = (this.startHeight + offsetY) / scale;

    if (this.isResizeWidth) {
      this.annotationEl.nativeElement.parentElement.style.width = `${newWidth}px`;
      this.annotationEl.nativeElement.style.width = `${newWidth}px`;
      this.nodeWrapperEl.nativeElement.style.width = `${newWidth + 4}px`;
    }
    if (this.isResizeHeight) {
      this.annotationEl.nativeElement.parentElement.style.height = `${newHeight}px`;
      this.annotationEl.nativeElement.style.height = `${newHeight}px`;
      this.nodeWrapperEl.nativeElement.style.height = `${newHeight + 4}px`;
    }
  };

  /**
   * 鼠标松开时保存新的宽高
   */
  handleMouseUp = () => {
    this.isResizeHeight = false;
    this.isResizeWidth = false;
    document.removeEventListener('mousemove', this.handleMouseMove);
    document.removeEventListener('mouseup', this.handleMouseUp);
    this.endResize();
  };

  /**
   * 拖拽改变宽度
   * @param event
   */
  startResizeWidth(event: MouseEvent) {
    this.isResizeWidth = true;
    this.startResize(event);
  }

  /**
   * 拖拽改变高度
   * @param event
   */
  startResizeHeight(event: MouseEvent) {
    this.isResizeHeight = true;
    this.startResize(event);
  }

  /**
   * 拖拽改变宽高
   * @param event
   */
  startResizeBoth(event: MouseEvent) {
    this.isResizeWidth = true;
    this.isResizeHeight = true;
    this.startResize(event);
  }

  /**
   * 开始设置size
   * @param event
   */
  startResize(event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    const el = this.annotationEl.nativeElement;
    // 记录当前编辑器大小
    const { width, height } = el.getBoundingClientRect();
    this.startWidth = width;
    this.startHeight = height;
    // 记录鼠标点击位置
    this.startX = event.clientX;
    this.startY = event.clientY;
    document.addEventListener('mousemove', this.handleMouseMove);
    document.addEventListener('mouseup', this.handleMouseUp);
  }

  /**
   * 拖拽结束时保存宽高到工作流，从视图到节点数据
   */
  setSize() {
    const nodeId = this.nodeInfo.id;
    const node: any = this.graph.getCellById(nodeId);
    let { width, height } =
      this.annotationEl.nativeElement.getBoundingClientRect();
    const scale = this.appFlowServ.getLocalScale() / 100;
    width = width / scale;
    height = height / scale;

    // 吸附到网格
    width = Math.round(width / 18) * 18;
    height = Math.round(height / 18) * 18;
    // 设置节点视图尺寸
    this.setNodeSize(width, height);
    // 保存到工作流
    node.resize(width + 4, height + 4);
  }

  /**
   * 根据输入的宽高设置节点视图
   * @param width
   * @param height
   */
  setNodeSize(width: number, height: number) {
    const el = this.annotationEl.nativeElement;
    el.style.width = `${width}px`;
    el.style.height = `${height}px`;
    el.parentElement.style.width = `${width}px`;
    el.parentElement.style.height = `${height}px`;
    this.nodeWrapperEl.nativeElement.style.height = `${height + 4}px`;
    this.nodeWrapperEl.nativeElement.style.width = `${width + 4}px`;
    this.nodeInfo.width = width;
    this.nodeInfo.height = height;
  }

  /**
   * 结束拖拽时更新宽高到工作流,并触发工作流的保存
   */
  endResize() {
    this.setSize();
    this.appFlowServ.updateFlowData();
  }

  /**
   * 同步节点数据,从节点数据到视图
   * @param nodeInfo
   */
  syncNodeData(nodeInfo?: ICommentNode) {
    const { width, height } = nodeInfo;
    this.setNodeSize(width, height);
    this.content = this.nodeInfo.content;
    this.cdr.markForCheck();
  }

  handleEnter(event) {
    event.preventDefault();
    (event.target as HTMLElement).blur(); // 失焦保存
  }
}
