import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewEncapsulation } from "@angular/core";
import { CommonModule } from "@angular/common";
import { I18NextEagerPipe } from "angular-i18next";
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzTooltipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: "meta-tool-card",
  standalone: true,
  imports: [CommonModule, NzButtonModule, NzTooltipModule],
  templateUrl: "./tool-card.component.html",
  styleUrl: "./tool-card.component.scss",
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToolCardComponent implements OnChanges {
  @Input() tool: any;
  @Input() hasBorder = false;
  @Output() select = new EventEmitter();

  isRiskBtn: boolean = false;
  public isHover = false;
  public btnConfig: any = {
    text: this.i18n.transform("add"),
    color: "default",
    class: ""
  };

  constructor(
    private i18n: I18NextEagerPipe
  ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.updateBtnConfig();
  }

  private updateBtnConfig(isHover = false) {
    if (isHover && this.tool.selected) {
      this.btnConfig.text = this.i18n.transform("remove");
      this.isRiskBtn = true;
      this.btnConfig.class = "";
    } else if (this.tool.selected) {
      this.btnConfig.text = this.i18n.transform("added");
      this.btnConfig.color = "default";
      this.btnConfig.class = "selected-btn";
      this.isRiskBtn = false;
    } else {
      this.btnConfig.text = this.i18n.transform("add");
      this.btnConfig.color = "default";
      this.btnConfig.class = "";
      this.isRiskBtn = false;
    }
  }

  public selectTool() {
    if (!this.tool.canAdd) {
      return;
    }
    this.tool.selected = !this.tool.selected;
    this.updateBtnConfig(this.isHover);
    this.select.emit(this.tool);
  }

  public updateBtn(isHover = true) {
    this.isHover = isHover;
    if (!this.tool.selected || !this.tool.canAdd) {
      return;
    }
    this.updateBtnConfig(isHover);
  }
}
