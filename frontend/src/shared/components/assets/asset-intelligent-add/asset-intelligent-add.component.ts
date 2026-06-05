import { Component } from '@angular/core';
import { MODULES } from '@shared/modules';
import { cdnAssetUrl } from 'src/single-spa/assets-url';

@Component({
  selector: 'asset-intelligent-add',
  templateUrl: './asset-intelligent-add.component.html',
  imports: [MODULES],
  standalone: true,
})
export class AssetIntelligentAddComponent {
  public changeUrl = cdnAssetUrl;
}
