import { Injectable, signal, inject } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzDrawerRef, NzDrawerService } from 'ng-zorro-antd/drawer';
import { MemoryLibDeleteModalComponent } from '@routes/memory-lib/components/memory-lib-delete-modal/memory-lib-delete-modal.component';
import { MemoryLibReferenceHalfmodalComponent } from '@routes/memory-lib/components/memory-lib-reference-halfmodal/memory-lib-reference-halfmodal.component';
import { MemoryLibSelectorHalfmodalComponent } from '@routes/memory-lib/components/memory-lib-selector-halfmodal/memory-lib-selector-halfmodal.component';
import { MemoryLibApiService } from '@routes/memory-lib/memory-lib-api.service';
import { MemoryLibCreationHalfmodalComponent } from '@routes/memory-lib/memory-lib-creation-halfmodal/memory-lib-creation-halfmodal.component';
import { IMemoryLibCreationData, IMemoryLibItem, IMemoryLibSelectData } from '@routes/memory-lib/memory-lib-interfaces';
import { CommonService } from '@services/common.service';
import { HttpService } from '@services/http.service';
import { I18NextEagerPipe } from 'angular-i18next';

@Injectable({
  providedIn: 'any',
})
export class MemoryLibService {
  readonly nzDrawerService = inject(NzDrawerService);
  readonly nzModalService = inject(NzModalService);
  readonly nzMessageService = inject(NzMessageService);
  readonly commonService = inject(CommonService);
  readonly memoryLibApiService = inject(MemoryLibApiService);
  readonly i18n = inject(I18NextEagerPipe);

  subscribeBtnDisabled = signal(false);

  constructor(private readonly http: HttpService) {
    this.subscription();
  }

  subscription() {}

  /**
   * 创建或者编辑记忆库
   * @param callBack
   * @param memoryLibId
   */
  createOrEditMemLib(callBack: (data: IMemoryLibCreationData) => void, memoryLibId = '') {
    const drawerRef = this.nzDrawerService.create({
      nzContent: MemoryLibCreationHalfmodalComponent,
      nzWidth: '700px',
      nzMask: true,
      nzContentParams: {
        libId: memoryLibId,
      },
      nzData: {
        libId: memoryLibId,
        beforeHide: ({ reason }) => {
          const creatioComp: MemoryLibCreationHalfmodalComponent = drawerRef.getContentComponent();
          const { basicInfoFormGroup, ltmRetrievalStrategyFormGroup, setLoading } = creatioComp;
          if (reason) {
            const errors = [basicInfoFormGroup?.invalid, ltmRetrievalStrategyFormGroup?.invalid].filter(Boolean);
            if (!errors.length) {
              callBack({
                reason,
                halfModalRef: drawerRef as any,
                data: {
                  ...basicInfoFormGroup.getRawValue(),
                  long_term_memory_strategies: ltmRetrievalStrategyFormGroup.getRawValue().ltmRetrievalStrategy ?? [],
                },
                setLoading,
              });
            }
          } else {
            callBack({
              reason,
              halfModalRef: drawerRef as any,
              data: null,
              setLoading,
            });
          }
        },
      },
    });
  }

  /**
   * 记忆库选择弹窗
   * @param existedLibs
   * @param callBack
   */
  showMemoryLibSelector(existedLibs: IMemoryLibItem[], callBack: (data: IMemoryLibSelectData) => void): void {
    this.nzDrawerService.create({
      nzContent: MemoryLibSelectorHalfmodalComponent,
      nzWidth: '700px',
      nzMask: true,
      nzData: {
        existedLibs,
        beforeHide: (drawerRef: NzDrawerRef, reason: boolean) => {
          if (reason) {
            const creatioComp: MemoryLibSelectorHalfmodalComponent = drawerRef.getContentComponent();
            const { existedMemoryLibs } = creatioComp;
            callBack({
              reason,
              halfModalRef: drawerRef as any,
              data: existedMemoryLibs(),
            });
          } else {
            callBack({
              reason,
              halfModalRef: drawerRef as any,
              data: [],
            });
          }
        },
      },
    });
  }

  /**
   * 记忆库引用弹窗
   * @param memLibId
   */
  showReference(memLibId: string): void {
    this.nzDrawerService.create({
      nzContent: MemoryLibReferenceHalfmodalComponent,
      nzWidth: '700px',
      nzMask: true,
      nzData: {
        memLibId,
      },
    });
  }

  /**
   * 单记忆库删除（自带引用判断封装）
   * @param memoryLib
   * @return 是否删除成功（取消列为删除失败）
   */
  safeDeleteMemoryLib(memoryLib: IMemoryLibItem): Promise<boolean> {
    return new Promise(resolve => {
      const myModal = this.nzModalService.create({
        nzContent: MemoryLibDeleteModalComponent,
        nzData: {
          deletedMemoryLib: memoryLib,
        },
        nzOnOk: (instance: MemoryLibDeleteModalComponent) => {
          return new Promise<boolean>(resolveP => {
            const { setLoading, checkTemplateForm } = instance;
            if (!checkTemplateForm()) {
              setLoading(true);
              this.memoryLibApiService
                .deleteMemory(memoryLib.memory_repo_id)
                .then(() => {
                  this.nzMessageService.success(this.i18n.transform('memory.management.modal.deleteTip'));
                  setLoading(false);
                  resolve(true);
                  resolveP(true);
                })
                .catch(() => {
                  setLoading(false);
                  resolveP(false);
                });
            } else {
              resolveP(false);
            }
          });
        },
        nzOnCancel: () => {
          resolve(false);
          return true;
        },
      });
      const content = myModal.getContentComponent();
      content.deletedMemoryLib = memoryLib;
    });
  }
}
