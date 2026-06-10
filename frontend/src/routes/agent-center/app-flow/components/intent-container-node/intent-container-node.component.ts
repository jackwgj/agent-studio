import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnInit,
  NgZone,
} from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { AppFlowService } from '../../app-flow.service';
import type {
  IIntentContainerBranch,
  IIntentContainerNode,
} from '../../node.type';
import { NodeService } from '../../node.service';
import { NodeBaseComponent } from '../base/node-base.component';
import { NodeDependencies } from '../modules';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { NzModalService } from 'ng-zorro-antd/modal';
import { takeUntil } from 'rxjs';
import { UpdateFlowsVersionModalComponent } from '@shared/components/update-flows-version-modal/update-flows-version-modal.component';

@Component({
  selector: 'meta-intent-container-node',
  templateUrl: './intent-container-node.component.html',
  styleUrls: [
    '../common-styles.less',
    './intent-container-node.component.scss',
  ],
  standalone: true,
  imports: [NodeDependencies],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class IntentContainerNodeComponent
  extends NodeBaseComponent
  implements OnInit
{
  @Input('nodeInfo') nodeInfo: IIntentContainerNode;

  public changeUrl = cdnAssetUrl;

  public updateFlowTip = '';

  public refWorkflows = [];

  public boundFlowVersionList = [];

  constructor(
    protected override appFlowServ: AppFlowService,
    protected override nodeServ: NodeService,
    protected override cdr: ChangeDetectorRef,
    protected override elementRef: ElementRef<HTMLDivElement>,
    private i18n: I18NextEagerPipe,
    private ngZone: NgZone,
    private nzModal: NzModalService,
  ) {
    super(nodeServ, appFlowServ, cdr, elementRef);
  }

  override ngOnInit(): void {
    this.setNodeBase(this.nodeInfo);
    super.ngOnInit();

    this.appFlowServ
      .appRefsUpdate()
      .pipe(takeUntil(this.destroy$))
      .subscribe((list) => {
        this.refWorkflows = list;
        // 初始化画布数据 或 切换意图包，都会订阅到流的变化
        // （1）在初始化画布数据，使用setTimeout计算容器节点的Tip提示和待更新的列表信息
        // （2）切换意图包后，branches分支信息恢复成空数组，入参是[]
        this.getBoundFlowVersionList([]);
      });

    setTimeout(() => {
      this.getBoundFlowVersionList(this.nodeInfo.branches);
    });
  }

  get numOfBoundFlows() {
    return Array.from(
      new Set(
        this.nodeInfo.branches
          .map((item) => item?.configs?.workflow_id)
          .filter((item) => item && item.trim() !== ''),
      ),
    ).length;
  }

  getBoundFlowsTip() {
    return this.numOfBoundFlows > 0
      ? `${this.numOfBoundFlows} ${this.i18n.transform('workflow')}`
      : this.i18n.transform('unbound_workflow');
  }

  /** 在容器节点里，打开意图分支绑定的工作流最新版本信息的弹窗 */
  public openUpdateFlowListModal() {
    const { branches, configs, id, inputs, name, outputs, type } =
      this.nodeInfo;

    this.ngZone.run(() => {
      this.nzModal.create({
        nzContent: UpdateFlowsVersionModalComponent,
        nzClassName: 'tp-modal-lg',
        nzMaskClosable: true,
        nzData: {
          boundFlowVersionList: this.boundFlowVersionList,
          nodeInfo: {
            branches,
            configs,
            id,
            inputs,
            name,
            outputs,
            type,
          },
        },
        nzFooter: null,
      });
    });
  }

  /** 比较ref_workflows列表和意图分支绑定的工作流版本，获取Tip提示和待更新的列表信息 */
  private getBoundFlowVersionList(branches: IIntentContainerBranch[]) {
    const list = [];

    branches.forEach((item) => {
      const match = this.refWorkflows.find(
        (subItem) => subItem.workflow_id === item.configs.workflow_id,
      );

      if (
        match &&
        Number(item.configs.workflow_version_id) < Number(match.last_version_id)
      ) {
        list.push({
          ...item.configs,
          last_version_name: match.last_version_name,
          last_version_id: match.last_version_id,
        });
      }
    });

    this.updateFlowTip = `${list.length}${this.i18n.transform(
      'intent_update_flow_tip',
    )}`;
    this.boundFlowVersionList = list;
  }
}
