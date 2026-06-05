import { Component, EventEmitter, Input, Output } from "@angular/core";
import { CommonModule } from "@angular/common";
import { NzIconModule } from "ng-zorro-antd/icon";
import { NzToolTipModule } from "ng-zorro-antd/tooltip";
import { NzButtonModule } from "ng-zorro-antd/button";
import { NzSpinModule } from "ng-zorro-antd/spin";
import { MODULES } from "@shared/modules";

@Component({
  selector: "collapse-title",
  templateUrl: "./collapse-title.component.html",
  styleUrls: ["./collapse-title.component.scss"],
  standalone: true,
  imports: [MODULES, CommonModule, NzIconModule, NzToolTipModule, NzButtonModule, NzSpinModule]
})
export class CollapseTitleComponent {
  @Input() cardConfig: {
    title: string;
    subTitle: string;
    description?: string;
    helpTip?: string
  };
  @Input() collapseLoading: boolean;
  @Input() isFlowReadonly: boolean;
  @Input() addDisabled: boolean;
  @Input() addDisabledTip: string;
  @Input() collapsed: boolean;
  @Output() addBtnClickEvent = new EventEmitter<void>();

  constructor() {
  }

  public changeCollapsedState(): void {
    this.collapsed = !this.collapsed;
  }

  public onAddBtnClick(event: Event): void {
    event?.stopPropagation();
    if (this.addDisabled) {
      return;
    }
    this.addBtnClickEvent.emit();
  }
}
