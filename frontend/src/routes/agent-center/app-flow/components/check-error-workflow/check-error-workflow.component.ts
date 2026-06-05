import {
  Component,
  OnInit,
  Input,
  EventEmitter,
  Output,
  ViewChild,
  SimpleChanges,
  Renderer2,
} from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { CardService } from '@routes/agent-center/my-card/card.service';
import { copyToClipboard } from 'src/utils/commonFn';
import { PipesModule } from 'src/pipes/pipes.module';
import { I18nNamespace } from '@i18n';
import { WORKFLOW_SVGS } from 'src/routes/agent-center/app-flow/flow.const';
import { AppFlowService } from '@routes/agent-center/app-flow/app-flow.service';
import * as platformBrowser from '@angular/platform-browser';

@Component({
  selector: 'check-error-workflow',
  templateUrl: './check-error-workflow.component.html',
  styleUrls: ['./check-error-workflow.component.less'],
  standalone: true,
  imports: [
    MODULES,
    PipesModule,
    NzDrawerModule,
    NzIconModule,
    NzAlertModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class CheckErrorWorkflowModal implements OnInit {
  @Input('validateWorkflowList') validateWorkflowList: any;
  @Input('workflowDetail') workflowDetail: any;
  @Input() isShow: boolean;
  @Input() errorTip: platformBrowser.SafeHtml;

  @Output() clickNodeEmit = new EventEmitter<any>();
  @Output() dismissEmit = new EventEmitter<any>();
  modalTitle = '';
  errorList = [];
  visible = false;
  clickItem = {
    index: -1,
    id: '',
  };
  foldStatus = {};

  constructor(
    private i18n: I18NextEagerPipe,
    private cardService: CardService,
    public appFlowServ: AppFlowService,
    private renderer: Renderer2,
  ) {}

  ngOnInit() {
    this.setData(this.validateWorkflowList);
    this.appFlowServ
      .updataValidateWorkflowList()
      .pipe()
      .subscribe((res) => {
        this.setData(res);
      });
  }

  setData(validateWorkflowList) {
    const nodes = this.workflowDetail?.workflow_details.nodes;
    const errorNode = new Set(validateWorkflowList.map((error) => error.id));
    this.errorList = [...errorNode].map((item: any) => {
      const nodeItem = nodes?.find((itemn: any) => itemn.id === item) || {};
      nodeItem.img = WORKFLOW_SVGS[nodeItem.type];
      let errorItem = validateWorkflowList.filter(
        (itemn, index, self) =>
          itemn.id === item &&
          index ===
            self.findIndex(
              (obj) =>
                obj.id === itemn.id &&
                obj.reason === itemn.reason &&
                obj.type === itemn.type,
            ),
      );
      errorItem = errorItem.map((itemn: any) => {
        itemn.errtext = itemn.reason.replace(itemn.id, nodeItem.name);
        return itemn;
      });
      this.foldStatus[nodeItem.id] = true;
      return {
        error: errorItem,
        node: nodeItem,
        expand: true,
      };
    });

    if (this.clickItem.index >= 0 && this.clickItem.id) {
      const findNodeIndex = this.errorList.findIndex(
        (item) => item.node.id === this.clickItem.id,
      );
      if (findNodeIndex >= 0) {
        this.clickItem.index = findNodeIndex;
      } else {
        this.clickItem.index = -1;
        this.clickItem.id = '';
      }
    }
    this.modalTitle = `问题清单  (${errorNode.size})`;
  }

  cardClick(item, i) {
    this.clickItem.index = i;
    this.clickItem.id = item.node.id;
    this.clickNodeEmit.emit(item.node);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.isShow) {
      this.visible = this.isShow;
      if (this.isShow) {
        setTimeout(() => {
          this.adjustTopForNodeDrawer();
        }, 0);
      }
    }
  }

  ngOnDestroy(): void {}

  onClose() {
    this.visible = false;
    this.isShow = false;
    this.dismissEmit.emit();
  }

  dismiss(): void {}

  close(): void {}

  onConfirm(): void {}

  private adjustTopForNodeDrawer() {
    const container = document.getElementById('app-flow-container');
    const top = container?.getBoundingClientRect()?.top ?? 113;

    const drawer = document.querySelector('.check-error-workflow-drawer');

    if (drawer) {
      this.renderer.setStyle(drawer, 'top', `${top}px`);
    }
  }
  setFoldStatus(e: MouseEvent, id: string, status: boolean) {
    e.stopPropagation();
    this.foldStatus[id] = status;
  }
}
