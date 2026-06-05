import { Component, OnInit, Input, SimpleChanges } from '@angular/core';
import { MODULES } from '@shared/modules';
import { FlowUtils } from '@routes/agent-center/app-flow/utils/flow-utils';

@Component({
  selector: 'node-card-description',
  templateUrl: './node-card-description.component.html',
  styleUrls: ['./node-card-description.component.less'],
  standalone: true,
  imports: [MODULES],
  providers: [],
})
export class NodeCardDescriptionComponent implements OnInit {
  @Input() nodeInfo!: any;
  @Input() description?: string;
  @Input() originDescription?: string;

  public descriptionText = '';
  public showDescription = false;

  constructor() {}

  ngOnInit() {}

  initDescription() {
    const defaultDescription = FlowUtils.nodeDesci18nMap(this.nodeInfo?.type);
    this.showDescription = Boolean(this.description);
    this.descriptionText = this.description || this.originDescription || defaultDescription;
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.initDescription();
  }
}
