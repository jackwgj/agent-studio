import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { KbUtils } from '@routes/knowledge-center/kb.utils';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { MODULES } from '@shared/modules';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BytesPipe } from 'src/pipes/bytes.pipe';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { IFile } from '@routes/agent-center/app-knowledge/knowledge.types';
import { PipesModule } from '../../../pipes/pipes.module';
import { KnowledgeChunk } from '../knowledge.types';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { buildChunkSize, buildCombineTitle, buildLevelMode, buildMergeTitle, buildSeparators, buildSplitMode, buildTitleLevel } from '../utils';
import { SetSidebarVisibilityService } from "@shared/services/set-sidebar-visibility.service";
import { KnowledgeType } from "@routes/knowledge-center/knowledge-base-list/knowledge-base-list.map";
import { MarkedPipe } from "../../../pipes/marked.pipe";
import { AsyncPipe } from "@angular/common";
import { SafeHtmlPipe } from "../../../pipes/safehtml.pipe";

import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzModalModule, NzModalService, NzModalRef } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';

@Component({
  selector: 'meta-faq-file',
  templateUrl: './faq-file.component.html',
  styleUrls: ['./faq-file.component.less'],
  standalone: true,
  imports: [
    MODULES,
    BytesPipe,
    MarkedPipe,
    AsyncPipe,
    SafeHtmlPipe,
    PipesModule,
    NzButtonModule,
    NzIconModule,
    NzSpinModule,
    NzDescriptionsModule,
    NzPaginationModule,
    NzEmptyModule,
    NzToolTipModule,
    NzModalModule,
    NzFormModule,
    NzInputModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.KNOWLEDGE, I18nNamespace.COMMON],
    },
  ],
})
export class FaqFileComponent implements OnInit {
  @ViewChild('chunkModal', { static: true }) chunkModal!: TemplateRef<any>;
  @ViewChild('chunkModalFooter', { static: true }) chunkModalFooter!: TemplateRef<any>;

  public knowledge_repo_id = '';

  public fileId = '';

  public isLoadingInfo = false;

  public isLoadingChunks = false;

  public knowledge: any;

  public file: IFile = {
    file_id: '',
    file_name: '',
    file_type: '',
    file_size: 0,
    file_status: '',
    create_time: '',
    update_time: '',
  };

  public chunkList: any = [];

  public currentPage = 1;

  public total = 0;

  public noDataDesc = '';

  public chunkPageSize = {
    options: [12, 24, 48, 64],
    size: 12,
  };

  public modalTitle = '';

  public chunkForm: FormGroup;

  public confirmLoading = false;

  base64 = '';

  isModalOpen = false;

  public chunkModalRef?: NzModalRef;

  private currentModalCb?: (val: KnowledgeChunk) => Promise<void>;

  public get fileType() {
    return this.file.file_name.split('.').slice(-1)[0];
  }

  public get splitMode() {
    return buildSplitMode(this.knowledge);
  }

  public get levelMode() {
    return buildLevelMode(this.knowledge);
  }

  public get combineTitle() {
    return buildCombineTitle(this.knowledge);
  }

  public get mergeTitle() {
    return buildMergeTitle(this.knowledge);
  }

  public get titleLevel() {
    return buildTitleLevel(this.knowledge);
  }

  public get separators() {
    return buildSeparators(this.knowledge, this.i18n);
  }

  public get chunkSize() {
    return buildChunkSize(this.knowledge);
  }

  constructor(
    private route: ActivatedRoute,
    private knowledgeRepoServ: KnowledgeRepoService,
    private fb: FormBuilder,
    private nzModal: NzModalService,
    private nzMessage: NzMessageService,
    private readonly i18n: I18NextEagerPipe,
    private setSidebarVisibilityService: SetSidebarVisibilityService,
    public kbAbilitiesService: KbAbilitiesService
  ) {}

  ngOnInit() {
    this.noDataDesc = this.i18n.transform('no_data');
    this.setSidebarVisibilityService.setSidebarsVisibilityByState('init');
    this.route.params.subscribe(params => {
      this.knowledge_repo_id = params?.id ?? '';
      this.fileId = params?.fileId ?? '';
      this.getInfo();
      this.getChunkList();
    });
  }

  ngOnDestroy(): void {
    this.setSidebarVisibilityService.setSidebarsVisibilityByState('destroy');
  }

  public back() {
    window.history.back();
  }

  public async getInfo() {
    try {
      this.isLoadingInfo = true;
      const [knowledge, fileRes] = await Promise.all([
        this.knowledgeRepoServ.retrieveKb(this.knowledge_repo_id),
        this.knowledgeRepoServ.listFaqFiles(
          this.knowledge_repo_id,
          {
            limit: 1,
            offset: 0,
          },
          {
            ids: [this.fileId],
          }
        ),
      ]);
      this.knowledge = knowledge;
      this.file = fileRes.file_info_list[0];
    } finally {
      this.isLoadingInfo = false;
    }
  }

  public async getChunkList() {
    try {
      this.isLoadingChunks = true;
      const query = {
        offset: ((this.currentPage || 1) - 1) * this.chunkPageSize.size,
        limit: this.chunkPageSize.size,
      };
      const data = await this.knowledgeRepoServ.listFaqChunks(this.knowledge_repo_id, this.fileId, query);
      if (data?.file_chunk_list) {
        this.chunkList = await Promise.all(
          data.file_chunk_list.map(async chunkItem => {
            const resultItem = { ...chunkItem };
            if (chunkItem.image_paths?.length > 0) {
              if (!chunkItem.image_paths[0]?.startsWith('http')) {
                resultItem.kb_type = KnowledgeType.INTERNAL;
                resultItem.image = await Promise.all(
                  chunkItem.image_paths.map(async path => {
                    try {
                      const res = await this.knowledgeRepoServ.previewImg(path);
                      return await KbUtils.readFileAsBase64(res);
                    } catch (imgError) {
                      return null;
                    }
                  })
                );
                resultItem.image = resultItem.image.filter(img => img !== null);
              } else {
                resultItem.kb_type = KnowledgeType.EXTERNAL;
                resultItem.image = chunkItem.image_paths;
              }
            }

            return resultItem;
          })
        );

        this.total = data.count;
      }
    } finally {
      this.isLoadingChunks = false;
    }
  }

  public addChunk() {
    this.modalTitle = this.i18n.transform('add_chunk');
    this.chunkForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(1000)]],
      content: ['', [Validators.required, Validators.maxLength(6000)]],
    });
    this.currentModalCb = async (formValue: KnowledgeChunk) => {
      await this.knowledgeRepoServ.createFaqChunk(this.knowledge_repo_id, this.fileId, formValue);
      await this.delay(1000);
      this.currentPage = 1;
      await this.getChunkList();
    };
    this.chunkModalRef = this.nzModal.create({
      nzTitle: this.modalTitle,
      nzContent: this.chunkModal,
      nzFooter: this.chunkModalFooter,
      nzWidth: 700,
      nzMaskClosable: false,
    });
  }

  private delay(time: number) {
    return new Promise(resolve => {
      const timer = setTimeout(() => {
        clearTimeout(timer);
        resolve(null);
      }, time);
    });
  }

  public editChunk(chunk: KnowledgeChunk) {
    const { title, content, id } = chunk;
    this.modalTitle = this.i18n.transform('edit_chunk');
    this.chunkForm = this.fb.group({
      title: [title, [Validators.required, Validators.maxLength(1000)]],
      content: [content, [Validators.required, Validators.maxLength(6000)]],
    });
    this.currentModalCb = async (formValue: KnowledgeChunk) => {
      await this.knowledgeRepoServ.modifyFaqChunk(this.knowledge_repo_id, this.fileId, id, formValue);
      await this.delay(1000);
      await this.getChunkList();
    };
    this.chunkModalRef = this.nzModal.create({
      nzTitle: this.modalTitle,
      nzContent: this.chunkModal,
      nzFooter: this.chunkModalFooter,
      nzWidth: 700,
      nzMaskClosable: false,
    });
  }

  openModal(pic) {
    this.isModalOpen = true;
    this.base64 = pic;
    // 可选：禁止背景滚动
    document.body.style.overflow = 'hidden';
  }

  public deleteChunk(chunk: KnowledgeChunk) {
    const { title, id } = chunk;
    this.nzModal.confirm({
      nzIconType: 'exclamation-circle',
      nzContent: this.i18n.transform('delete_chunk_warning_content', {
        title,
      }),
      nzTitle: this.i18n.transform('delete_chunk_warning_title'),
      nzOkText: this.i18n.transform('delete'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOkDanger: true,
      nzOnOk: async () => {
        try {
          await this.knowledgeRepoServ.deleteFaqChunk(this.knowledge_repo_id, this.fileId, id);
          this.nzMessage.success(this.i18n.transform('delete_success'));
          const currentPageCount = this.chunkList.length;
          if (currentPageCount === 1 && this.currentPage !== 1) {
            this.currentPage -= 1;
          }
          await this.delay(1000);
          this.getChunkList();
          return true;
        } catch (error) {
          // Add basic error boundary for delete failures.
          return false;
        }
      }
    });
  }

  closeModal(event: any) {
    if (event.target === event.currentTarget) {
      this.isModalOpen = false;
      document.body.style.overflow = ''; // 恢复滚动
    }
  }

  public async handleConfirm() {
    if (!this.chunkForm.valid) {
      Object.values(this.chunkForm.controls).forEach(control => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
      return;
    }
    try {
      this.confirmLoading = true;
      if (this.currentModalCb) {
        await this.currentModalCb(this.chunkForm.value);
      }
    } finally {
      this.confirmLoading = false;
      this.chunkModalRef?.close();
    }
  }

  viewImageWarn(url: string): void {
    this.nzModal.confirm({
      nzIconType: 'exclamation-circle',
      nzTitle: this.i18n.transform('warning'),
      nzContent: this.i18n.transform('redirect_to_third_party_system'),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOnOk: () => {
        window.open(url, '_blank');
      }
    });
  }

  protected readonly KnowledgeType = KnowledgeType;
}
