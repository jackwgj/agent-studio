import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ITmpl } from '@services/prompt.service';
import { CommonNoDataComponent } from '@shared/components/common-no-data/common-no-data.component';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { CommonUtils } from 'src/utils/common.util';
import { InformationTemplateService } from '@routes/information-template/information-template.service';
import { MonacoEditorModule } from '@materia-ui/ngx-monaco-editor';
import { FormsModule } from '@angular/forms';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';

@Component({
  selector: 'meta-ex-temp',
  template: `
    <div class="h-full flex flex-col">
      <div class="flex flex-col gap-[16px]">
        <div class="flex w-full items-center gap-2">
          <input
            nz-input
            class="!w-full"
            [placeholder]="'extempcomponent_334' | i18nextEager"
            [(ngModel)]="searchValue"
            (keyup.enter)="getPrompts()"
          />
          <button
            nz-button
            nzType="default"
            (click)="getPrompts()"
          >
            <span nz-icon nzType="reload" nzTheme="outline"></span>
          </button>
        </div>

        <div
          class="flex h-[400px] w-full rounded-[8px] border-[1px] border-solid border-[#dfdfdf]"
        >
          <ng-container *ngIf="tmpls.length">
            <div
              class="border-r-solid h-full w-[310px] border-r-[1px] border-r-[#dfdfdf] p-[20px] overflow-auto"
            >
              <div
                class="flex flex-col gap-[8px] overflow-auto h-full"
              >
                <div
                  [ngStyle]="{
                    'border-color':
                      tmpl.id === curTmpl?.id
                        ? '#1476ff'
                        : 'transparent'
                  }"
                  class="flex w-full cursor-pointer flex-col gap-[4px] rounded-[6px] border-[1px] border-solid bg-[#fafafa] p-[16px]"
                  *ngFor="let tmpl of tmpls"
                  (click)="onSelectTmpl(tmpl)"
                >
                  <div class="flex items-center gap-[8px]">
                    <div
                      class="max-w-[calc(100%-40px)] shrink-0 font-bold text-[#191919] overflow-text"
                    >
                      {{ tmpl.name }}
                    </div>
                  </div>
                  <div class="text-[12px] text-desc">
                    {{ 'extempcomponent_332' | i18nextEager }}:
                    {{ tmpl.category }}
                    |
                    {{ 'extempcomponent_333' | i18nextEager }}:
                    {{ tmpl.visibility }}
                  </div>
                </div>
                <div *ngIf="isLoading" class="flex w-full items-center justify-center">
                  <nz-spin></nz-spin>
                </div>
              </div>
            </div>
            <div class="h-[375px] w-[calc(100%-310px)] p-[20px]">
              <div *ngIf="curTmpl" class="flex flex-col gap-[8px] text-desc h-full">
                <ngx-monaco-editor
                  class="h-full"
                  [options]="editorOptions"
                  [(ngModel)]="curTmpl.content"
                ></ngx-monaco-editor>
              </div>
            </div>
          </ng-container>
          <ng-container *ngIf="!tmpls.length">
            <div class="flex h-full w-full items-center justify-center">
              <app-common-no-data *ngIf="!isLoading" />
              <nz-spin *ngIf="isLoading" />
            </div>
          </ng-container>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .editor-wrapper {
        border: 1px solid #c2c2c2;
        border-radius: 8px;
        width: 100%;
        height: 100%;
        padding: 14px;
      }
      .overflow-text {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    `,
  ],
  standalone: true,
  imports: [
    MODULES,
    CommonNoDataComponent,
    MonacoEditorModule,
    FormsModule,
    NzInputModule,
    NzButtonModule,
    NzIconModule,
    NzSpinModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ExTempComponent implements OnInit {
  @Output() select = new EventEmitter<ITmpl>();

  public isLoading = false;

  public curTmpl: any | null = null;

  public offset = 0;

  public limit = 10;

  public hasNextPage = true;

  public controller = new AbortController();

  public tmpls: any[] = [];

  public lang = CommonUtils.getLanguage();

  public searchValue = '';

  editorOptions = {
    theme: 'vs',
    language: 'json',
    minimap: {
      enabled: false,
    },
    readOnly: true,
  };

  constructor(
    private i18n: I18NextEagerPipe,
    private informationTemplateService: InformationTemplateService,
  ) {}

  async ngOnInit() {
    this.getPrompts();
  }

  public async getPrompts(conf: { isScroll: boolean } = { isScroll: false }) {
    try {
      if (this.controller) {
        this.controller.abort();
      }

      this.controller = new AbortController();
      this.isLoading = true;

      const query: any = {
        name: this.searchValue ?? '',
        offset: conf?.isScroll ? this.offset : 0,
        limit: 10,
      };

      let { message_list, count } =
        await this.informationTemplateService.getStructuredMessagesListAsync(
          query,
          this.controller.signal,
        );

      message_list = message_list.map((m) => {
        return {
          ...m,
          content: JSON.stringify(m.content, null, 2),
        };
      });

      this.hasNextPage = this.tmpls.length < count;
      if (conf?.isScroll) {
        this.tmpls.push(...message_list);
      } else {
        this.tmpls = message_list;
      }

      this.offset += count;

      if (this.tmpls.length) {
        this.curTmpl = this.tmpls[0];
      }
    } finally {
      this.isLoading = false;
    }
  }
  public onSelectTmpl(tmpl: ITmpl) {
    this.curTmpl = tmpl;
  }
  public onScroll(event: Event): void {
    const container = event.target as HTMLElement;
    const isBottom =
      container.scrollTop + container.clientHeight >= container.scrollHeight;

    if (isBottom && !this.isLoading && this.hasNextPage) {
      this.getPrompts({ isScroll: true });
    }
  }



  dismiss(): void {}

  close(): void {}

  public onSelect() {
    this.select.emit(this.curTmpl);
    this.close();
  }
}
