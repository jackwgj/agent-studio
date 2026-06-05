import { Component, Input, OnInit } from '@angular/core';
import type { IPromptChunk } from '@interfaces/prompt/prompt-editor.interface';
import { NgStyle } from '@angular/common';
import { PromptEditorService } from '@services/prompt-editor.service';
import {
  VARIANT_NAME_ERROR_POS,
  VARIANT_PADDING_X,
} from '@constants/exp-tmpl-config.const';

@Component({
  selector: 'tmpl-variant-tag',
  templateUrl: './tmpl-variant-tag.component.html',
  standalone: true,
  imports: [NgStyle],
})
export class TmplVariantTagComponent implements OnInit {
  @Input() varChunkProps: IPromptChunk;

  /** 渲染时是否去除大括号 */
  @Input() isRemoveBrackets: boolean;

  /** 是否渲染 X 轴方向内边距 */
  @Input() shouldRenderPaddingX: boolean;

  public bgColor: string;

  public value: string;

  public paddingX = '0px';

  public variantFontColor: string;

  constructor(private promptEditorServ: PromptEditorService) {}

  public ngOnInit(): void {
    const index = this.varChunkProps.pos ?? 0;

    this.value = this.isRemoveBrackets
      ? this.varChunkProps.value.replace(/{{|}}/g, '')
      : this.varChunkProps.value;

    this.variantFontColor =
      this.varChunkProps.pos === VARIANT_NAME_ERROR_POS ? 'red' : 'black';
    this.bgColor = this.promptEditorServ.getVarBgColor(index);
    this.paddingX = this.shouldRenderPaddingX ? VARIANT_PADDING_X : '0px';
  }
}
