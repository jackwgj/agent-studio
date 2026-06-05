import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { MODULES } from '@shared/modules';
import { SHARE_PAGE, SHARE_TOOL_TYPE } from '../edit-share-modal/edit-share-modal.config';
import { CardService } from '@routes/agent-center/my-card/card.service';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';


@Component({
    selector: 'share-usage-table',
    templateUrl: './share-usage-table.component.html',
    styleUrls: ['./share-usage-table.component.scss'],
    standalone: true,
    imports: [MODULES, NzTableModule, NzEmptyModule],
    providers: [
      {
        provide: I18NEXT_NAMESPACE,
        useValue: [I18nNamespace.COMMON, I18nNamespace.PLATFORM_MANAGEMENT, I18nNamespace.TRACE],
      },
    ],
})
export class ShareUsageTableComponent implements OnInit {
    @Input() toolType: SHARE_TOOL_TYPE;
    @Input() toolId = '';
    @Input() versionId = '';
    @Input() pageType: SHARE_PAGE;
    @Input() shareData: any;
    public pluginName: string;
    public title = '';
    public columns = [
        {
            title: 'space_name',
            width: '120px',
            field: 'app_workspace_name',
        },
        {
            title: 'deployment_name',
            width: '120px',
            field: 'app_name',
        },
        {
            title: 'ref_version',
            width: '100px',
            field: 'resource_version',
        },
    ];

    public totalNumber = 0;
    public displayedData: any[] = [];

    public currentPage: number = 1;
    public pageSize: { options: Array<number>; size: number } = {
        options: [10, 20, 50, 100],
        size: 10,
    };

    constructor(
        public router: Router,
        private cardService: CardService,
    ) {
    }

    ngOnInit() {
    }

    close(): void { }

    dismiss(): void { }


    public onStateUpdate(): void {
        this.queryUsage();
    }

    public queryUsage(type?: string): void {
        // 删除的查全量
        // 有toolType类型的查类型
        const param: any = {
            offset: ((this.currentPage || 1) - 1) * this.pageSize.size,
            limit: this.pageSize.size,
            reference_type: 'share',
            resource_type: this.shareData.resource_type === SHARE_PAGE.plugin ? 'tool':  this.shareData.resource_type,
          };
          if(this.versionId){
            param.version= this.versionId;
          }
          if(type) {
            param.app_type = type;
          }
          this.cardService
            .selectQuoteList(this.shareData.resource_id, param)
            .then((res) => {
              this.totalNumber = res.count;
              this.displayedData = res.relations;
            })
            .finally(() => {
            });


    }

    public init(type: SHARE_TOOL_TYPE) {
        this.totalNumber = 0;
        this.currentPage = 1;
        this.displayedData = [];
        this.queryUsage(type);
    }
}
