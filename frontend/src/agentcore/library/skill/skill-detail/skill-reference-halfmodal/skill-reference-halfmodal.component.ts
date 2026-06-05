import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { NzTableModule } from 'ng-zorro-antd/table';
import { MODULES } from '@shared/modules';
import { I18nService } from '@agentcore/core/i18n.service';
import { I18nPipe } from '@agentcore/shared/pipes/i18n.pipe';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { SkillReferenceTableService } from './skill-reference-table.service';
import { SkillApi } from '@agentcore/api/skill.api';
@Component({
  selector: 'skill-reference-halfmodal',
  templateUrl: './skill-reference-halfmodal.component.html',
  styleUrls: ['./skill-reference-halfmodal.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    I18nPipe,
    NzTableModule,
    MODULES
  ],
  providers: [I18nService, SkillReferenceTableService],
})
export class SkillReferenceHalfmodalComponent extends SkillReferenceTableService {
  override skillId: string;
  readonly modalRef = inject(NzModalRef);
  total = 0;
  ListData: any[] = [];
  loading = true;
  pageSize = 12;
  pageIndex = 1;
  public cardPageSizeoptions: [12, 24, 48, 64]
  title = [this._i18n.transform('skill.detail.reference.header.appName'), this._i18n.transform('skill.detail.reference.header.appVersion')
  ]

  constructor(private router: Router, private skillApi: SkillApi,) {
    super();
  }

  dismiss() { }

  close() { }

  ngOnInit() {
  }

  public onQueryParamsChange(e) {
    const nextOffset = (this.pageIndex - 1) * this.pageSize;
    const limit = this.pageSize;
    this._skillApi.queryReference(this.skillId, nextOffset, limit).subscribe((response) => {
      this.total = response.const;
      this.ListData = response.relations;
    });
  }


  public linkto(params) {
    this.router.navigate([`/home/agent-center/app-agent/detail`], {
      queryParams: { agentId: params.data.app_id },
    });
  }
}
