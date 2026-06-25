import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MODULES } from "@shared/modules";
import { I18nNamespace } from "@i18n";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { cdnAssetUrl } from "../../../../../single-spa/assets-url";
import { NZ_MODAL_DATA, NzModalRef } from "ng-zorro-antd/modal";


@Component({
  selector: "meta-auth-modal",
  imports: [CommonModule, MODULES],
  templateUrl: "./update-node-modal.component.html",
  styleUrls: ["./update-node-modal.component.scss"],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MODEL_ACCESS]
    }
  ]
})


export class UpdateNodeModalComponent {
  readonly nzModalData = inject<any>(NZ_MODAL_DATA);
  public changeUrl = cdnAssetUrl;
  authsList = [];

  constructor(
    private i18n: I18NextEagerPipe,
    private modalRef: NzModalRef) {
  }

  ngOnInit() {
  }

  close(): void {
    this.modalRef.close(true);
  }

  dismiss(): void {
    this.modalRef.close(false);
  }
}
