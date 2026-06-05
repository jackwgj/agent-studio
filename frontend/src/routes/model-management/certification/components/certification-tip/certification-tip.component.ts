import { Component, ElementRef, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { FormGroup, FormsModule } from "@angular/forms";
import { cdnAssetUrl } from "../../../../../single-spa/assets-url";
import { Clipboard } from "@angular/cdk/clipboard";
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageService } from 'ng-zorro-antd/message';
@Component({
  selector: 'meta-certification-tip',
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    FormsModule,
    NzAlertModule,
    NzInputModule,
    NzButtonModule
  ],
  templateUrl: './certification-tip.component.html',
  styleUrls: ['./certification-tip.component.scss'],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MODEL_ACCESS],
    },
  ],
})
export class CertificationTipComponent implements OnInit {
  @Input() apiKey: string;

  configWord = '';
  confirmText = 'DELETE';
  loading = false;
  public changeUrl = cdnAssetUrl;
  myForm: FormGroup;

  constructor(
    private modalRef: NzModalRef,
    private i18n: I18NextEagerPipe,
    private clipboard: Clipboard,
    public elementRef: ElementRef,
    private message: NzMessageService,
  ) {}

  ngOnInit() {}

  copy() {
    this.clipboard.copy(this.apiKey);
    this.message.success(this.i18n.transform('api_key_copied_successfully'));
  }

  close(): void {
    this.modalRef.destroy();
  }

  dismiss(): void {
    this.modalRef.destroy();
  }
}
