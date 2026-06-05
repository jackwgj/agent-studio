/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable @typescript-eslint/no-unsafe-call */
/* eslint-disable @typescript-eslint/no-unsafe-member-access */
import { Component, Input } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { FormateTimePipe } from 'src/pipes/formate-time.pipe';

@Component({
  selector: 'right-share-panel',
  templateUrl: './right-share-panel.component.html',
  styleUrls: ['./right-share-panel.component.less'],
  imports: [MODULES],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT],
    },
    FormateTimePipe,
  ],
})
export class RightSharePanelComponent {
  constructor(
    private i18n: I18NextEagerPipe,
    private formateTimePipe: FormateTimePipe,
  ) { }

  @Input() headInfo: any;

  public isExpanded = true;

  public show = false;

  public shareTime = '';

  public shareSource = '';

  public onToggle() {
    this.isExpanded = !this.isExpanded;
  }

  shareWorkspace = '';
  title = '';

  ngOnChanges(changes) {
    const list = this.headInfo?.workspace_list || [];
    if (list.length) {
      this.shareWorkspace = list.map(v => {
        return v.workspace_name;
      })?.join(' | ');
    } else {
      this.shareWorkspace = this.i18n.transform('edit_share_modal_15');
    }
    this.title = this.headInfo?.resource_name || '';
    this.shareTime = this.headInfo?.share_time ? this.formateTimePipe.transform(this.headInfo?.share_time) : '';
    const name = this.headInfo.resource_workspace_name ? `(${this.headInfo.resource_workspace_name})` : '';
    this.shareSource = `${this.headInfo.creator}${name}`;
  }

  ngOnInit() {

  }
}
