import { Component } from '@angular/core';
import { NodeBaseComponent } from '../base/node-base.component';

@Component({
  selector: 'stream-transform-node',
  templateUrl: './stream-transform-node.component.html',
  styleUrls: ['../common-styles.less', './stream-transform-node.component.less'],
  standalone: true,
  imports: [],
  providers: [],
})
export class StreamTransformNodeComponent extends NodeBaseComponent {}
