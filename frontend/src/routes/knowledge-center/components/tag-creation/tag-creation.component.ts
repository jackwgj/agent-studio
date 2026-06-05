import { Component, EventEmitter, inject, input, OnInit, Output, signal, TemplateRef, viewChild } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { I18nNamespace } from '@i18n';
import { MAX_KB_TAG_LIMIT } from '@routes/knowledge-center/knowledge.const';
import { KbTagColor } from '@routes/knowledge-center/knowledge.types';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { v4 as uuidV4 } from 'uuid';
import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzButtonModule } from 'ng-zorro-antd/button';

@Component({
  selector: 'tag-creation',
  templateUrl: './tag-creation.component.html',
  styleUrls: ['./tag-creation.component.less'],
  standalone: true,
  imports: [
    MODULES,
    NzInputModule,
    NzFormModule,
    NzIconModule,
    NzButtonModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.KNOWLEDGE, I18nNamespace.COMMON],
    },
  ],
})
export class TagCreationComponent implements OnInit {
  @Output() goToTagManage = new EventEmitter();
  tagCreationModal = viewChild<TemplateRef<any>>('tagCreationModal');

  isShowTagJumper = input(false);

  existedTagsQuantities = input<number>(undefined);

  kbId = input<string>();

  modelRef = signal<NzModalRef>(null);

  kbManagementUrl = '';
  isFromDevelopSpace = false;
  protected readonly MAX_KB_TAG_LIMIT = MAX_KB_TAG_LIMIT;

  private readonly nzModal = inject(NzModalService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly kbApiService = inject(KnowledgeRepoService);
  private readonly i18n = inject(I18NextEagerPipe);

  tagForm = this.fb.group({
    tags: this.fb.array([
      this.createTagGroup(),
    ]),
  });

  get tagsFormArray(): FormArray {
    return this.tagForm.get('tags') as FormArray;
  }

  goToTag() {
    this.goToTagManage.emit();
  }

  ngOnInit() {
    if (window.location && window.location.href && window.location.href.includes('develop-space')) {
      this.isFromDevelopSpace = true;
    }
    const prefixUrl = window.location.href?.split('/home')[0];
    const tagUrl = this.router.createUrlTree([
      '/home/knowledge-center/knowledge',
      this.kbId(),
    ], {
      queryParams: {
        active: 'kb_tag_management',
        previousUrl: '/home/agent-center/library-home?tabId=knowledge',
      },
    }).toString();
    this.kbManagementUrl = `${prefixUrl}${tagUrl}`;
  }

  showCreationModal(successCallBack?: (tags: any[]) => void) {
    this.tagForm = this.fb.group({
      tags: this.fb.array([
        this.createTagGroup(),
      ]),
    });

    const modalRef = this.nzModal.create({
      nzTitle: this.i18n.transform('new_kb_tag'),
      nzContent: this.tagCreationModal(),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnCancel: () => {
        this.modelRef.set(null);
        return true;
      },
      nzOnOk: () => {
        const tagsArray = this.tagForm.get('tags') as FormArray;
        tagsArray.controls.forEach((control: AbstractControl) => {
          const group = control as FormGroup;
          Object.values(group.controls).forEach(innerControl => {
            innerControl.markAsDirty();
            innerControl.updateValueAndValidity({ onlySelf: true });
          });
        });

        if (this.tagForm.invalid) {
          return false;
        }

        const tags = this.tagForm.getRawValue().tags;
        if (tags[0].name) {
          const id = this.kbId();
          return this.kbApiService.createKbTag(id, {
            name: tags[0].name,
            color: KbTagColor.grey,
          }).then(() => {
            successCallBack?.(this.tagForm.getRawValue().tags);
            this.modelRef.set(null);
            return true; // Modal close
          }).catch(() => {
            return false; // Modal keeps open, loading stops
          });
        }
        return true;
      }
    });

    this.modelRef.set(modalRef);
  }

  addTag() {
    this.tagsFormArray.push(this.createTagGroup());
  }

  createTagGroup() {
    return this.fb.group({
      id: uuidV4(),
      name: ['', Validators.required],
    });
  }

  removeTag(index: number) {
    this.tagsFormArray.removeAt(index);
  }

  close() {
    this.modelRef()?.destroy();
    this.modelRef.set(null);
  }
}
