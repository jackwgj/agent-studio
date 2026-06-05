import { Component } from '@angular/core';
import { IMemoParam } from '../create-memo/types';

@Component({
  selector: 'meta-preview-memos',
  templateUrl: './preview-memos.component.html',
  styleUrls: ['./preview-memos.component.less'],
  standalone: true,
  imports: [],
  providers: [],
})
export class PreviewMemosComponent {}

export interface IMemoParamExt extends IMemoParam {
  update_time: string;
  value: string;
}
