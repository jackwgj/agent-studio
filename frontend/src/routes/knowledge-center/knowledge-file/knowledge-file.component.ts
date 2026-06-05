import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { KbUtils } from '@routes/knowledge-center/kb.utils';
import { MODULES } from '@shared/modules';
import { BytesPipe } from 'src/pipes/bytes.pipe';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import {
  IFile,
  IFileChunkReq,
} from '@routes/agent-center/app-knowledge/knowledge.types';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { KnowledgeChunk, SplitMode } from '../knowledge.types';
import { IDENTIFIER_OPTIONS } from '../knowledge.const';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { SetSidebarVisibilityService } from "@shared/services/set-sidebar-visibility.service";
import { KnowledgeType } from "@routes/knowledge-center/knowledge-base-list/knowledge-base-list.map";
import { DomSanitizer } from '@angular/platform-browser';
import { MarkedPipe } from "../../../pipes/marked.pipe";
import { AsyncPipe } from "@angular/common";
import { SafeHtmlPipe } from "../../../pipes/safehtml.pipe";
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { PipesModule } from "../../../pipes/pipes.module";

import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzModalModule, NzModalService, NzModalRef } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';

@Component({
  selector: 'meta-knowledge-file',
  templateUrl: './knowledge-file.component.html',
  styleUrls: ['./knowledge-file.component.less'],
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
    NzToolTipModule,
    NzSpinModule,
    NzDescriptionsModule,
    NzTabsModule,
    NzPaginationModule,
    NzEmptyModule,
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
export class KnowledgeFileComponent implements OnInit, OnDestroy {
  @ViewChild('chunkModalContent', { static: true }) chunkModalContent!: TemplateRef<any>;
  @ViewChild('chunkModalFooter', { static: true }) chunkModalFooter!: TemplateRef<any>;

  public knowledge_repo_id = '';

  public fileId = '';

  public isLoadingInfo = false;

  public isLoadingChunks = false;

  public tabs: any[] = [
    {
      id: 'all',
      title: 'tab_all',
      active: true,
    },
  ];

  public knowledge: any;

  public file: IFile = {
    file_size: 0,
    file_id: '',
    file_status: '',
    file_name: '',
    file_type: '',
    update_time: '',
    create_time: '',
  };

  public currentPage = 1;

  public chunkList: any = [];

  public total = 0;

  public noDataDesc = '';

  public chunkPageSize = {
    options: [12, 24, 48, 64],
    size: 12,
  };

  public modalTitle = '';

  public chunkForm: FormGroup;

  public confirmLoading = false;

  public chunkModalRef?: NzModalRef;

  private currentModalCb?: (val: KnowledgeChunk) => Promise<void>;

  base64 = '';
  isModalOpen = false;

  public get splitMode() {
    const { split_mode } = this.knowledge?.split_conf || {};
    if (SplitMode.AUTO === split_mode) {
      return 'split_mode_auto';
    } else if (SplitMode.LENGTH === split_mode) {
      return 'split_mode_length';
    }
    return 'split_mode_level';
  }

  public get levelMode() {
    const { split_mode } = this.knowledge?.split_conf || {};
    if (SplitMode.CATALOG === split_mode) {
      return 'level_mode_catalog';
    } else if (SplitMode.RULE === split_mode) {
      return 'level_mode_rule';
    }
    return '';
  }

  public get combineTitle() {
    const { combine_title = '' } = this.knowledge?.split_conf || {};
    if ('' === combine_title) {
      return '';
    }
    return combine_title ? 'combine_title_true' : 'combine_title_false';
  }

  public get mergeTitle() {
    const { merge_titles = '' } = this.knowledge?.split_conf || {};
    if ('' === merge_titles) {
      return '';
    }
    return merge_titles ? 'open_merge_title' : 'close_merge_title';
  }

  public get titleLevel() {
    const { title_level = '' } = this.knowledge?.split_conf || {};
    if ('' === title_level) {
      return '';
    }
    return title_level;
  }

  public get separators() {
    const { separator_ids = '' } = this.knowledge?.split_conf || {};
    if ('' === separator_ids) {
      return '';
    }
    return separator_ids
      .reduce((res, separator) => {
        const { value } = IDENTIFIER_OPTIONS.find(
          (item) => item.value === separator,
        );
        res.push(this.i18n.transform(value));
        return res;
      }, [])
      .join(' ');
  }

  public get chunkSize() {
    const { chunk_size = '' } = this.knowledge?.split_conf || {};
    if ('' === chunk_size) {
      return '';
    }
    return chunk_size;
  }

  public get delimiter() {
    const { delimiter = '' } = this.knowledge?.rag_chunk_parser_conf || {};
    if ('' === delimiter || !delimiter?.length || !delimiter[0]) {
      return '';
    }
    return delimiter.map(item => this.i18n.transform(item)).join('; ');
  }

  public get chunkTokenNum() {
    const { chunk_token_num = 0 } = this.knowledge?.rag_chunk_parser_conf || {};
    if (0 === chunk_token_num) {
      return 0;
    }
    return chunk_token_num;
  }

  public get chunkMethod() {
    const { chunk_method = '' } = this.knowledge?.rag_chunk_parser_conf || {};
    if ('' === chunk_method) {
      return '';
    }
    return this.i18n.transform(`rag_${chunk_method}`);
  }

  isRagFlow = false;
  constructor(
    private route: ActivatedRoute,
    private knowledgeRepoServ: KnowledgeRepoService,
    private fb: FormBuilder,
    private nzModal: NzModalService,
    private nzMessage: NzMessageService,
    private i18n: I18NextEagerPipe,
    private setSidebarVisibilityService: SetSidebarVisibilityService,
    private sanitizer: DomSanitizer,
    private appAgentRepoServ: AppAgentRepoService,
    public kbAbilitiesService: KbAbilitiesService,
  ) {}

  async ngOnInit() {
    this.noDataDesc = this.i18n.transform('knowledge_chunk_no_data');
    const { knowledge_source } = await this.appAgentRepoServ.getConfigs();
    this.isRagFlow = knowledge_source === 'AgentBaseRag';
    this.setSidebarVisibilityService.setSidebarsVisibilityByState('init');
    this.route.params.subscribe((params) => {
      this.fileId = params?.fileId ?? '';
      this.knowledge_repo_id = params?.id ?? '';
      this.getInfo();
      this.getChunkList();
    });
  }

  public back() {
    window.history.back();
  }

  public async getInfo() {
    try {
      this.isLoadingInfo = true;
      const [knowledge, file] = await Promise.all([
        this.knowledgeRepoServ.retrieveKb(this.knowledge_repo_id),
        this.knowledgeRepoServ.retrieveFile(
          this.knowledge_repo_id,
          this.fileId,
        ),
      ]);
      this.knowledge = knowledge;
      this.file = file;
    } finally {
      this.isLoadingInfo = false;
    }
  }

  public async getChunkList() {
    try {
      this.isLoadingChunks = true;
      const query: IFileChunkReq = {
        file_id: this.fileId,
        knowledge_repo_id: this.knowledge_repo_id,
        offset: ((this.currentPage || 1) - 1) * this.chunkPageSize.size,
        limit: this.chunkPageSize.size,
      };

      const data = await this.knowledgeRepoServ.listFileChunks(query);
      if (data?.file_chunk_list) {
        this.chunkList = await Promise.all(
          data.file_chunk_list.map(async (chunk) => {
            const resultItem = { ...chunk };
            if (chunk.image_paths?.length > 0) {
              if (!chunk.image_paths[0]?.startsWith('http')) {
                resultItem.kb_type = KnowledgeType.INTERNAL;
                resultItem.image = await Promise.all(
                  chunk.image_paths.map(async (path) => {
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
                resultItem.image = chunk.image_paths;
                resultItem.kb_type = KnowledgeType.EXTERNAL;
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

  private delay(time: number) {
    return new Promise((resolve) => {
      const timer = setTimeout(() => {
        clearTimeout(timer);
        resolve(null);
      }, time);
    });
  }

  openModal(pic) {
    this.isModalOpen = true;
    this.base64 = pic;
    // 可选：禁止背景滚动
    document.body.style.overflow = 'hidden';
  }

  public addChunk() {
    this.modalTitle = 'add_chunk';
    this.chunkForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(1000)]],
      content: ['', [Validators.required, Validators.maxLength(6000)]],
    });

    this.currentModalCb = async (formValue: KnowledgeChunk) => {
      await this.knowledgeRepoServ.createFileChunk(
        this.knowledge_repo_id,
        this.fileId,
        formValue,
      );
      await this.delay(1000);
      this.currentPage = 1;
      await this.getChunkList();
    };

    this.chunkModalRef = this.nzModal.create({
      nzTitle: this.i18n.transform(this.modalTitle),
      nzContent: this.chunkModalContent,
      nzFooter: this.chunkModalFooter,
      nzWidth: 700,
      nzMaskClosable: false,
    });
  }

  viewImageWarn(url): void {
    this.nzModal.confirm({
      nzIconType: 'exclamation-circle',
      nzTitle: this.i18n.transform('warning'),
      nzContent: this.i18n.transform('redirect_to_third_party_system'),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOnOk: () => { window.open(url, '_blank'); }
    });
  }

  public editChunk(chunk: KnowledgeChunk) {
    const { title, content, id } = chunk;
    this.modalTitle = 'edit_chunk';
    this.chunkForm = this.fb.group({
      title: [title, [Validators.required, Validators.maxLength(1000)]],
      content: [content, [Validators.required, Validators.maxLength(6000)]],
    });

    this.currentModalCb = async (formValue: KnowledgeChunk) => {
      await this.knowledgeRepoServ.modifyFileChunk(
        this.knowledge_repo_id,
        this.fileId,
        id,
        formValue,
      );
      await this.delay(1000);
      await this.getChunkList();
    };

    this.chunkModalRef = this.nzModal.create({
      nzTitle: this.i18n.transform(this.modalTitle),
      nzContent: this.chunkModalContent,
      nzFooter: this.chunkModalFooter,
      nzWidth: 700,
      nzMaskClosable: false,
    });
  }

  public deleteChunk(chunk: KnowledgeChunk) {
    const { title, id } = chunk;
    this.nzModal.confirm({
      nzIconType: 'exclamation-circle',
      nzTitle: this.i18n.transform('delete_chunk_warning_title'),
      nzContent: this.i18n.transform('delete_chunk_warning_content', { title }),
      nzOkText: this.i18n.transform('delete'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOkDanger: true,
      nzOnOk: async () => {
        try {
          await this.knowledgeRepoServ.deleteFileChunk(
            this.knowledge_repo_id,
            this.fileId,
            id,
          );
          this.nzMessage.success(this.i18n.transform('delete_success'));
          if (this.chunkList.length === 1 && this.currentPage !== 1) {
            this.currentPage -= 1;
          }
          await this.delay(1000);
          this.getChunkList();
          return true;
        } catch (error) {
          return false;
        }
      },
    });
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
      this.chunkModalRef?.destroy();
    }
  }

  closeModal(event: any) {
    if (event.target === event.currentTarget) {
      this.isModalOpen = false;
      document.body.style.overflow = ''; // 恢复滚动
    }
  }

  ngOnDestroy(): void {
    this.setSidebarVisibilityService.setSidebarsVisibilityByState('destroy');
  }

  protected readonly KnowledgeType = KnowledgeType;
}
