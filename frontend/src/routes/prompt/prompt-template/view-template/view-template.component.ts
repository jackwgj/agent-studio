import { ChangeDetectionStrategy, Component, Input, OnInit, inject } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import * as I18next from 'angular-i18next';
import { PromptEditorService } from '@services/prompt-editor.service';
import { IPromptChunk } from '@interfaces/prompt/prompt-editor.interface';
import { TmplVariantTagComponent } from '@shared/components/tmpl-variant-tag/tmpl-variant-tag.component';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzModalModule, NzModalRef } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { Clipboard } from '@angular/cdk/clipboard';
import type { ITmplCard } from '@interfaces/prompt/prompt-template.interface';
import { MessageComponent } from '@shared/services/cfdata.service';
import { ConsoleFrameworkService } from '@core/services';
import { cdnAssetUrl } from 'src/single-spa/assets-url';

@Component({
  selector: 'meta-view-template-modal',
  templateUrl: './view-template.component.html',
  styleUrls: ['./view-template.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [MODULES, TmplVariantTagComponent, NzTagModule, NzModalModule, NzButtonModule],
  providers: [
    {
      provide: I18next.I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.PROMPT_PLATFORM],
    },
  ],
})
export class ViewTemplateComponent implements OnInit {
  @Input() currentTmpl: ITmplCard;

  public templateName = '';

  public modalContent: IPromptChunk[];
  readonly modalRef = inject(NzModalRef);
  public tags: string[];
  protected readonly iconList = iconList;
  protected readonly industryTagColor = industryTagColor;
  public typeImages = {
    text: cdnAssetUrl('assets/prompt/type-text-green.svg'),
    multi: cdnAssetUrl('assets/prompt/type-multi.svg'),
  };
  public language = this.consoleFrameworkService?.dataService?.getLocale() ?? 'zh-cn';

  constructor(
    private promptEditorServ: PromptEditorService,
    private clipboard: Clipboard,
    private i18n: I18next.I18NextEagerPipe,
    private readonly consoleFrameworkService: ConsoleFrameworkService
  ) {}

  ngOnInit(): void {
    this.templateName = this.currentTmpl.template_name;
    this.modalContent = this.promptEditorServ.processVar(this.currentTmpl.content);
    this.tags = this.currentTmpl.tag_list?.map(item => (this.language === 'en-us' ? item.name_en : item.name));
  }

  public close(): void {}

  public dismiss(): void {
    this.modalRef.destroy();
  }

  public onCopy(): void {
    const copyStatus = this.clipboard.copy(this.currentTmpl.content);

    if (copyStatus) {
      MessageComponent.showSuccess(this.i18n.transform('base_copy_success'));
    } else {
      MessageComponent.showSuccess(this.i18n.transform('base_copy_error'));
    }
  }
}

export const iconList = {
  行业: 'ind-industry',
  金融: 'ind-finance',
  医疗: 'ind-medical',
  政务: 'ind-government',
  教育: 'ind-education',
  制造: 'ind-make',
  互联网: 'ind-it',
};
export const industryTagColor = {
  行业: '#e6f8f7',
  通用: '#e6f8f7',
  金融: '#f6f4e6',
  医疗: '#e5f2e9',
  政务: '#e7f1ff',
  教育: '#e7f1ff',
  制造: '#e8f5fa',
  互联网: '#e8f5fa',
};
