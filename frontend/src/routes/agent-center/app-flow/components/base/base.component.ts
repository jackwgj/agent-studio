/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable import/no-cycle */
import { Injectable, OnDestroy } from '@angular/core';
import i18next from 'i18next';
import { NODE_ACTIONS } from '@routes/agent-center/constants/workflow-common.const';
import { Subject, takeUntil } from 'rxjs';
import { AppFlowService } from '../../app-flow.service';
import { NodeService } from '../../node.service';
import { INodeBase, NodeInfo } from '../../node.type';
import { NodeUtils } from '../utils';
import { I18nNamespace } from '@i18n';

@Injectable()
export abstract class BaseComponent implements OnDestroy {
  protected isFlowReadonly = false;

  protected actions = NODE_ACTIONS;

  protected destroy$ = new Subject<void>();

  protected nodeBase: INodeBase = {
    id: '',
    name: '',
    type: 'Start',
  };

  protected type2Img = NodeUtils.type2Img;

  tokenDataFrom = '';

  constructor(
    protected nodeServ: NodeService,
    protected appFlowServ: AppFlowService,
  ) {
    this.nodeServ
      .isReadonlyFlowUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        this.isFlowReadonly = value;
      });
  }

  protected nodeDesci18nMap(type) {
    if (type === 'IntentDetectionContainer') {
      return '';
    }

    return `nodeDesc.${type}`;
  }

  protected setNodeBase(nodeInfo: NodeInfo) {

    // 临时处理创建时默认生成的大模型节点，创建模板添加 isDefaultName 后可删
    if (nodeInfo.name === i18next.getResource('zh-CN', I18nNamespace.AGENT_CENTER, 'llm') || nodeInfo.name === 'LLM') {
      nodeInfo.configs = {
        ...nodeInfo.configs,
        isDefaultName: true
      }
    }

    const map = this.appFlowServ.nodeDefaultNameMap;
    if (nodeInfo.configs?.isDefaultName && map.has(nodeInfo.type)) {
      const suffixMatch = nodeInfo.name.match(/(_\d+)+$/);
      const suffix = suffixMatch ? `${suffixMatch[0]}` : '';
      const defaultName = map.get(nodeInfo.type);
      nodeInfo.name = defaultName + suffix;
    }

    const constantMap = this.appFlowServ.nodeConstantNameMap;
    if (constantMap.has(nodeInfo.type)) {
      nodeInfo.name = constantMap.get(nodeInfo.type);
    }

    const { id, name, type, configs } = nodeInfo;
    this.nodeBase = { id, name, type, configs };
  }

  protected onClickAction(action: any) {
    this.appFlowServ.setNodeAction({
      type: action.id,
      id: this.nodeBase.id,
    });
  }

  protected onClickTest() {
    // 重新调试清除上次调试的信息
    this.appFlowServ.setSaveNodeTestInfo({
      id: this.nodeBase.id
    });
    this.appFlowServ.setNodeAction({
      type: 'test',
      id: this.nodeBase.id,
    });
  }

  protected onClickTestInfo($event: any) {
    $event.stopPropagation();
    // 打开调试弹窗并回显结果
    this.appFlowServ.setNodeTestInfoAction({
      id: this.nodeBase.id,
      info: true,
      tokenDataFrom:this.tokenDataFrom
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
