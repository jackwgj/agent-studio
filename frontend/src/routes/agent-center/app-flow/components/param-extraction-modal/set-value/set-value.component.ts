import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { MODULES } from '@shared/modules';
import { takeUntil } from 'rxjs';
import { NodeService } from '../../../node.service';
import { SetValueTipComponent } from '@routes/agent-center/app-flow/components/param-extraction-modal/set-value-tip/set-value-tip.component';
import { SetDefaultComponent } from '@routes/agent-center/app-flow/components/set-default/set-default.component';
import * as nodeType from '@routes/agent-center/app-flow/node.type';
import { isNil } from 'lodash';

const CMP_MAX_WIDTH = '655px';

@Component({
  selector: 'meta-set-value',
  templateUrl: './set-value.component.html',
  styleUrls: ['./set-value.component.less'],
  standalone: true,
  imports: [MODULES, SetValueTipComponent],
})
export class SetValueComponent
  extends SetDefaultComponent
  implements OnInit, OnDestroy
{

  @Input('nameRefOptions') nameRefOptions: nodeType.IParamRef[] = [];

  constructor(public override nodeServ: NodeService) {
    super(nodeServ);
  }

  public override tipContext = {
    param: null,
    isNormalWidth: this.isNormalWidth,
    isShowDesc: false,
    host: this.host,
    cmpWidth: this.cmpWidth,
    nameRefOptions: null,
    outputs: {
      close: (state: {
        value?: string;
        desc?: string;
        isClose: boolean;
        display?: string;
      }): void => {
        if (!isNil(state?.value)) {
          this.param.value.content = state.value;
          if (!Array.isArray(this.param.type) && !isNil(state?.desc)) {
            this.param.description = state.desc;
          }
        }
        if (state.isClose) {
          this.changeParam.emit();
        }
      },
      valueChange: () => {
      },
    },
  };

  public override onSetDefault() {
    this.nodeServ.setCloseAllSetDefaultTip();
  }
}
