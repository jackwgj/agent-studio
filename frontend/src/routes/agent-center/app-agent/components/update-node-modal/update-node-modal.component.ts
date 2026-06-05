import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MODULES } from "@shared/modules";
import { I18nNamespace } from "@i18n";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { cdnAssetUrl } from "../../../../../single-spa/assets-url";


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
  @Input() title: "";
  @Input() context: "";
  public changeUrl = cdnAssetUrl;
  authsList = [];

  constructor(
    private i18n: I18NextEagerPipe) {
  }

  ngOnInit() {
  }

  close(): void {
  }

  dismiss(): void {
  }
}
