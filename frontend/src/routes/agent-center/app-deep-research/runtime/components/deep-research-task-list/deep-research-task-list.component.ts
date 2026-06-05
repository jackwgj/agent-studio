/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable @typescript-eslint/no-this-alias */
/* eslint-disable no-underscore-dangle */
/* eslint-disable @typescript-eslint/no-unsafe-argument */
/* eslint-disable @typescript-eslint/no-unsafe-member-access */
/* eslint-disable @typescript-eslint/no-unsafe-call */
import {Component, EventEmitter, Input, Output, SimpleChanges,} from '@angular/core';

import {I18nNamespace} from '@i18n';

import {MODULES} from '@shared/modules';
import {I18NEXT_NAMESPACE, I18NextEagerPipe, I18NextModule} from 'angular-i18next';
import {NgForOf} from "@angular/common";
import {NzModalService} from "ng-zorro-antd/modal";
import {MessageComponent} from '@shared/services/cfdata.service';
import {NoDataIconComponent} from "@shared/components/no-data-icon/no-data-icon.component";
import {ActivatedRoute} from "@angular/router";
import {Subject, takeUntil} from "rxjs";
import {cdnAssetUrl} from "../../../../../../single-spa/assets-url";
import i18next from "i18next";
import {
  UpdateNameModalComponent
} from "@routes/agent-center/app-deep-research/runtime/components/update-name-modal/update-name-modal.component";
import {
  DeleteTaskModalComponent
} from "@routes/agent-center/app-deep-research/runtime/components/delete-task-modal/delete-task-modal.component";
import {DeepResearchService} from "@services/agent-center/deep-research.service";
import {handleTime} from "@routes/agent-center/utils";
import {DeepResearchSubService} from "@routes/agent-center/app-deep-research/runtime/deep-research-sub.service";


export interface taskItem {
  id?: string;
  type: string;
  name: string;
  editName?: string;
  mode: string;
  inputs: { query: string; };
  timeout: number;
  version: number;
  isEdit?: boolean;
  status: string;
  statusTitle?: string;
  versionId?: string;
  message?: string;
  is_published?: boolean;
}

@Component({
  selector: 'deep-research-task-list',
  templateUrl: './deep-research-task-list.component.html',
  styleUrls: ['./deep-research-task-list.component.less'],
  imports: [
    MODULES,
    NgForOf,
    I18NextModule,
    MODULES,
    NoDataIconComponent
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT],
    },
  ],
})


export class DeepResearchTaskListComponent {
  private destroy$ = new Subject<void>();

  @Input() shortCode: string;

  @Output() selectTask = new EventEmitter<any>();

  public changeUrl = cdnAssetUrl;

  public taskList: taskItem[] = []

  public taskGroupModel = [
    {
      name: this.i18n.transform('deep_research_task.category_1'),
      tasks: []
    },
    {
      name: this.i18n.transform('deep_research_task.category_2'),
      tasks: []
    },
    {
      name: this.i18n.transform('deep_research_task.category_3'),
      tasks: []
    },
    {
      name: this.i18n.transform('deep_research_task.category_4'),
      tasks: []
    },
    {
      name: this.i18n.transform('deep_research_task.category_5'),
      tasks: []
    },
    {
      name: this.i18n.transform('category_6'),
      tasks: []
    },
    {
      name: this.i18n.transform('category_7'),
      tasks: []
    }
  ]
  public taskGroupList = [
    {
      name: this.i18n.transform('deep_research_task.category_1'),
      tasks: []
    },
    {
      name: this.i18n.transform('deep_research_task.category_2'),
      tasks: []
    },
    {
      name: this.i18n.transform('deep_research_task.category_3'),
      tasks: []
    },
    {
      name: this.i18n.transform('deep_research_task.category_4'),
      tasks: []
    },
    {
      name: this.i18n.transform('deep_research_task.category_5'),
      tasks: []
    },
    {
      name: this.i18n.transform('deep_research_task.category_6'),
      tasks: []
    },
    {
      name: this.i18n.transform('deep_research_task.category_7'),
      tasks: []
    }
  ]


  public search: string = ''
  public actions = [
    {
      icon: 'edit',
      id: 'edit',
      label: i18next.t('rename'),
    },
    {
      icon: 'delete',
      id: 'delete',
      label: i18next.t('delete'),
    },
  ];
  public selectId: number = 1
  public showBox: boolean = false

  public statusMap = {
    0: 'pending',
    10: 'running',
    30: 'running',
    20: 'waiting',
    40: 'completed',
    50: 'finished',
    60: 'finished',
    70: 'finished',
    80: 'finished',
  }
  public isloading = true
  public versionId: string = ''

  constructor(
    private deepResearchServ: DeepResearchService,
    private route: ActivatedRoute,
    private deepResearchSubService: DeepResearchSubService,
    private nzModalService: NzModalService,
    private i18n: I18NextEagerPipe,
  ) {
    /** 任务状态是否发生改变 */
    this.deepResearchSubService
      .taskCreate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(async (task: object) => {
        if (task && this.shortCode) {
          this.createTask(task)
        }
      });
  }

  ngOnChanges(changes: SimpleChanges) {
    // 每次点击试运行按钮，清空上一次的【运行结果】Tab页数据
    this.getTaskList()
  }

  tabActiveChange(id: string) {
  }


  ngOnInit(): void {}

  ngAfterViewInit(): void {
    this.init();
  }

  public init(): void {
    this.route.queryParams.subscribe((params) => {
      const { versionId } = params;
      this.versionId = versionId;
    });
  }

  editTask(item) {
    item.isEdit = true
  }

  handelTask(type, item, indexObj) {
    this.selectId = item.id
    switch (type) {
      case 'edit':
        item.editName = item.name;
        item.isEdit = true;
        this.showChangeNameModal(item);
        return;
      case 'delete':
        this.deleteChat(item);
        return;
      case 'select':
        this.doSelectTask(item, indexObj);
        return;
      default:
        return;
    }
  }

  onClick(e){
    e.stopPropagation();
  }

  deleteChat(item) {
    const modal = this.nzModalService.create({
      nzTitle: '',
      nzFooter: null,
      nzWidth: 400,
      nzClosable: false,
      nzMaskClosable: false,
      nzContent: DeleteTaskModalComponent,
    });
    modal.componentInstance.name = item.name;
    modal.componentInstance.deleteChat.subscribe(() => {
      this.operateTask({task_id: this.selectId, operate_type: 1});
      modal.close();
    });
  }

  showChangeNameModal(item) {
    const modal = this.nzModalService.create({
      nzTitle: '',
      nzFooter: null,
      nzWidth: 400,
      nzClosable: false,
      nzMaskClosable: false,
      nzContent: UpdateNameModalComponent,
    });
    modal.componentInstance.name = String(JSON.parse(JSON.stringify(item.editName)));
    modal.componentInstance.nameChange.subscribe((name: string) => {
      this.renameTask({name: name, task_id: this.selectId});
      modal.close();
    });
  }

  doSelectTask(item, indexObj) {
    this.selectId = item.id;
    this.deepResearchServ.getTaskDetail({task_id: this.selectId}).then((task) => {
      item.status = task.data.status
      if(indexObj){
        this.getTaskList('select')
      }
      this.deepResearchSubService.setTaskChange(item)
    })
  }

  createTask(task) {
    const params = {
      task_name: this.i18n.transform('deep_research_task.task_4'),
      run_type: 1,
      agent_type: 6,
      agent_id: this.shortCode
    }
    let query = {
      task_id: '',
      name: task.query,
      query: task.query,
      inputs: task.inputs,
    }
    this.deepResearchServ.creatTask(params).then((res) => {
      query.task_id = res.data;
      this.selectId = res.data;
      MessageComponent.showSuccess(this.i18n.transform('deep_research_task.task_1'));
      this.getTaskList('create')
      this.deepResearchSubService.setChat({...query,id:res.data})
    })
  }

  renameTask(taskInfo) {
    this.deepResearchServ.renameTask(taskInfo).then((res) => {
      MessageComponent.showSuccess(this.i18n.transform('deep_research_task.task_2'));
      this.getTaskList()
    })
  }


  operateTask(taskInfo) {
    this.deepResearchServ.operateTask(taskInfo).then((res) => {
      MessageComponent.showSuccess(this.i18n.transform('deep_research_task.task_3'));
      this.getTaskList()
    })
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getTaskList(from?: string) {
    if (this.taskList.length <= 0) {
      this.isloading = true
    }
    this.deepResearchServ.getTaskList({
      run_type: 1,
      agent_id: this.shortCode,
      num: 1,
      size: 1000,
      search_key: this.search
    }).then((res) => {
      this.taskGroupList = JSON.parse(JSON.stringify(this.taskGroupModel));
      this.isloading = false
      this.taskList = res.data.task_list;
      if (this.taskList.length) {
        this.taskList.forEach(item => {
          item.statusTitle = this.statusMap[item.status]
          this.groupTasks(item)
        })
        if (!from) {
          this.doSelectTask(this.taskList[0],null)
        }

      }
    })
  }

  groupTasks = (task) => {
    if (task.display_info?.is_pin) {
      this.taskGroupList[0].tasks.push(task);
    } else {
      let res = handleTime(task.create_time);
      if (res === 'today') {
        this.taskGroupList[1].tasks.push(task);
      } else if (res === '1day') {
        this.taskGroupList[2].tasks.push(task);
      } else if (res === '1week') {
        this.taskGroupList[3].tasks.push(task);
      } else if (res === '1month') {
        this.taskGroupList[4].tasks.push(task);
      } else if (res === '3month') {
        this.taskGroupList[5].tasks.push(task);
      } else if (res === '1year') {
        this.taskGroupList[6].tasks.push(task);
      }
    }
  };

  onSearch(e) {

  }

  onClear(e) {

  }

  public showCreate() {
    this.deepResearchSubService.setTaskPage(true)
  }

  showSearchBox() {
    this.showBox = !this.showBox
    if (!this.showBox) {
      this.search = ''
    }
  }

  searchTask() {
    this.getTaskList()
  }
}
