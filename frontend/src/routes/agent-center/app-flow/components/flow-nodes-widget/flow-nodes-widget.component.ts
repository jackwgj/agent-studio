import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { Graph } from '@antv/x6';
import { I18nNamespace } from '@i18n';
import { FormsModule } from '@angular/forms';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { NodeInfo } from '../../node.type';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { WORKFLOW_SVGS } from '../../flow.const';
import { AppFlowService } from '../../app-flow.service';
import { Subject, takeUntil } from 'rxjs';
import { NodeService } from '../../node.service';

@Component({
  selector: 'meta-flow-nodes-widget',
  templateUrl: './flow-nodes-widget.component.html',
  styleUrls: ['./flow-nodes-widget.component.less'],
  standalone: true,
  imports: [MODULES, FormsModule, NzInputModule, NzToolTipModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class FlowNodesWidgetComponent implements OnInit, OnDestroy {
  @Input() graph: Graph;

  @Output() zoomIn = new EventEmitter<void>();

  public nodes: NodeInfo[] = [];

  public searchedNodes: NodeInfo[] = [];

  public searchNodeName = '';

  constructor(
    private appFlowServ: AppFlowService,
    private nodeServ: NodeService,
  ) {}

  public nodeIcons = {
    Move: cdnAssetUrl('assets/agent-center/flow/move.svg'),
    ...WORKFLOW_SVGS,
  };

  public currFocusedId = '';

  private destroy$ = new Subject<void>();

  ngOnInit() {
    this.nodeServ
      .currSelectedNodeUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe((nodeId: string) => {
        if (nodeId) {
          this.currFocusedId = nodeId;
        }
      });

    this.nodes = this.graph
      .getNodes()
      .map((node) => node.data?.ngArguments?.nodeInfo as NodeInfo)
      .filter(
        (node: NodeInfo) =>
          !['IntentDetectionContainer', 'LoopInput', 'LoopOutput'].includes(
            node.type,
          ),
      );

    this.searchedNodes = this.nodes;
  }

  onClickNode(nodeId: string) {
    this.appFlowServ.setNodeIdClicked(nodeId);
    this.zoomIn.emit();
  }

  onSearch(name: string) {
    this.searchedNodes = this.nodes.filter((node) => node.name.includes(name));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
