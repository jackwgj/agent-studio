import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { DataSourceManagementRepoService } from '@services/repositories/datasource-management-repo.service';
import { DeleteRefsService } from '@shared/services/delete-refs.service';
import { CommonService } from '@services/common.service';
import { FormateTimePipe } from 'src/pipes/formate-time.pipe';
import {
  NewCommonNoDataWithBtnComponent
} from '@shared/components/new-common-no-data-with-btn/new-common-no-data-with-btn.component';
import { PipesModule } from '../../pipes/pipes.module';
import {
  BatchDeleteRefsModalComponent
} from '@routes/datasource-management/components/batch-delete-refs-modal/batch-delete-refs-modal.component';
import {
  EditableDatasourceHalfmodalComponent
} from '@routes/datasource-management/components/editable-datasource-halfmodal/editable-datasource-halfmodal.component';
import { HttpService } from '@services/http.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzDrawerService } from 'ng-zorro-antd/drawer';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'datasource-management',
  templateUrl: './datasource-management.component.html',
  styleUrls: ['./datasource-management.component.scss'],
  standalone: true,
  imports: [MODULES, NewCommonNoDataWithBtnComponent, PipesModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.AGENT_CENTER],
    },
    DeleteRefsService,
    FormateTimePipe,
    NzModalService,
    NzDrawerService,
    NzMessageService
  ],
  encapsulation: ViewEncapsulation.None,
})
export class DatasourceManagementComponent implements OnInit {
  subscribeBtnStatus = this.commonService.getSubscribeStatus();
  is_white_list = false;

  constructor(
    private i18n: I18NextEagerPipe,
    private dataSourceRepoServe: DataSourceManagementRepoService,
    private deleteRefsServe: DeleteRefsService,
    private nzDrawer: NzDrawerService,
    private nzModal: NzModalService,
    private nzMessage: NzMessageService,
    private commonService: CommonService,
    private readonly http: HttpService,
  ) {
    this.http.getResourceWhiteList().subscribe(res => {
      this.is_white_list = res;
    });
  }

  ngOnInit() {
    this.getDatasourceListData();
  }

  loading = false;
  srcData: { data: any[] } = {
    data: [],
  };
  checkedList: any[] = [];
  allChecked = false;
  indeterminate = false;

  listColumns: Array<{ title: string; width?: string; fixed?: string }> = [
    { title: '' },
    { title: this.i18n.transform('data_source_name') },
    { title: this.i18n.transform('database_type') },
    { title: this.i18n.transform('description') },
    { title: this.i18n.transform('operator1') },
    { title: this.i18n.transform('operation_time') },
    {
      title: this.i18n.transform('operation'),
      fixed: 'right',
      width: '120px',
    },
  ];

  public trackByFn(index: number) {
    return index;
  }
  updateCheckStatus(): void {

  }

  isChecked(row: any): boolean {
    return false;
  }

  onItemChecked(row: any, checked: boolean): void {

  }

  onAllChecked(checked: boolean): void {

  }

  listAction(key: string, row: any) {

  }

  searchValue = '';
  get searchNameIsEmpty(): boolean {
    return this.searchValue.length === 0;
  }

  currentPage = 1;
  pageSize = {
    options: [10, 20, 50, 100],
    size: 10,
  };
  totalNumber = 0;

  pageIndexChange(index: number) {
    this.currentPage = index;
    this.getDatasourceListData();
  }

  pageSizeChange(size: number) {}

  getDatasourceListData() {

  }

  // 单个删除
  private deleteDatasource(id: string): any {

  }

  // 批量删除
  deleteData(){

  }

  public createDatasource() {

  }

  private openHalfModel(param?) {

  }

  handleClickClearSearch() {

  }

  searchList() {

  }
}
