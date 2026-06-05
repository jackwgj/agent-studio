import { ChangeDetectorRef, Component, Renderer2 } from '@angular/core';
import { KbTagItem } from '@routes/knowledge-center/knowledge.types';
import { I18NextModule } from 'angular-i18next';
import { NzTagModule } from 'ng-zorro-antd/tag';
import {NzTooltipDirective, NzToolTipModule} from 'ng-zorro-antd/tooltip';
import { NzIconModule } from 'ng-zorro-antd/icon';

export interface FileTagCustomRendererParams extends ICellRendererParams {
  clickCallback: (params: any) => void;
}

@Component({
  selector: 'file-tag-renderer',
  standalone: true,
  template: `
    <div (mouseenter)="enter(true)" (mouseleave)="enter(false)" class="relative">
      <div class="tag-container relative" nz-tooltip [nzTooltipTitle]="displayValue?.length ? tagTip : null">
        @for (tag of displayValue; track $index) {
          <nz-tag>
            {{ tag }}
          </nz-tag>
        } @empty {
          --
        }
        <ng-template #tagTip>
          <div class="tip-container max-[320px] flex flex-wrap gap-[8px]">
            @for (tag of displayValue; track $index) {
              <nz-tag>
                {{ tag }}
              </nz-tag>
            }
          </div>
        </ng-template>
        <div class="edit-container absolute right-0 flex items-center h-full top-0 text-[16px] bg-[#f5f5f5]"
             [hidden]="!isEnter">
          <span
            nz-icon
            nzType="edit"
            nzTheme="outline"
            class="cursor-pointer"
            (click)="editClick()">
          </span>
        </div>
      </div>
    </div>
  `,
  styles: `
    .tag-container {
      text-overflow: ellipsis;
      overflow: hidden;
      display: block;
      white-space: nowrap;
      max-width: 100%;
      width: fit-content;
      padding-right: 20px;
    }
  `,
  host: {
    '[class.ti-render-container]': 'true',
    '[class.config-render-wrapper]': 'true',
  },
  imports: [
    I18NextModule,
    NzTagModule,
    NzToolTipModule,
    NzIconModule,
    NzTooltipDirective,
  ],
})
export class FileTagCustomRenderer extends GridPipeRenderTemplate {
  isEnter: boolean = false;
  displayValue?: KbTagItem[];

  public override defaultKeepRenderer: boolean = true;

  clickCallback?: (params: any) => void;

  constructor(
    renderer: Renderer2,
    private renderUtilService: TiGridKitRenderUtilService,
    private cdr: ChangeDetectorRef
  ) {
    super(renderer as any);
  }

  override agInit(params: FileTagCustomRendererParams): void {
    super.agInit(params);
    this.displayValue = this.params.value || [];
    this.clickCallback = params.clickCallback;
  }

  enter(open: boolean): void {
    this.isEnter = open;
  }

  updateBusinessData(newValue: KbTagItem[]): void {
    this.displayValue = newValue || [];
    this.renderUtilService.updateRowData(this.params, newValue);
  }

  editClick() {
    this.clickCallback?.({ row: this.params?.data });
  }
}
