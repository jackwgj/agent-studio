import {
  Component, EventEmitter, Input, Output
} from '@angular/core';


@Component({
  selector: 'meta-param-extraction-log',
  standalone: true,
  imports: [],
  providers: [],
  templateUrl: './param-extraction-log.component.html',
  styleUrls: [
    './param-extraction-log.component.scss',
    '../flow-log-modal.component.scss',
  ],
})
export class ParamExtractionLogComponent {
  @Input() callItem: any;
  @Output('showChildEvent') showChildEvent = new EventEmitter<any>();
}
