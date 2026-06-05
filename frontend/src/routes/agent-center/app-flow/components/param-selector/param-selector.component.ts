import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { AccBlockComponent } from '../acc-block/acc-block.component';
import { AppFlowService } from '../../app-flow.service';

interface IModelParam {
  top_p: number;
  temperature: number;
}

@Component({
  selector: 'meta-param-selector',
  templateUrl: './param-selector.component.html',
  styleUrls: ['./param-selector.component.less'],
  standalone: true,
  imports: [MODULES, AccBlockComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ParamSelectorComponent implements OnInit {
  @Input() modelParams: IModelParam;

  @Output() modelParamsChange = new EventEmitter<IModelParam>();
  @Output() paramsChange = new EventEmitter<IModelParam>();

  public modelParamsInfoMap = this.appFlowServe.getModelParamsInfoMap();

  constructor(private appFlowServe: AppFlowService) { }

  ngOnInit() { }

  public originalOrder(): number {
    return 0;
  }

  public trackByFn(index: number) {
    return index;
  }

  public getScales(name: string): number[] {
    const { min, mid, max } = this.modelParamsInfoMap.get(name);

    return [min, mid, max];
  }

  public filterNaN(event: KeyboardEvent): void {
    if (['e', 'E', '+'].includes(event.key)) {
      event.preventDefault();
    }
  }

  public onParamInputBlur(key: string): void {
    const value = Number(this.modelParams[key]);

    if (key === 'max_tokens') {
      this.modelParams[key] = Math.round(value);
    } else {
      this.modelParams[key] = value.toFixed(1);
    }
    this.changeParams();
  }

  changeParams($event?: any) {
    this.paramsChange.emit();
  }
}
