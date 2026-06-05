import {ChangeDetectorRef, Component, Input, signal, TemplateRef, ViewChild, ViewEncapsulation, EventEmitter, Output} from '@angular/core';
import { Router } from '@angular/router';
import {NzModalService} from 'ng-zorro-antd/modal';
import {COMMON_MODULES, MODULES} from "@shared/modules";
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from "angular-i18next";
import {isNull} from "lodash";
import {I18nNamespace} from "@i18n";
import {cdnAssetUrl} from "../../../../single-spa/assets-url";
import {ModelModalComponent} from "@routes/model-square/components/model-modal/model-modal.component";
import { HttpService } from '@services/http.service';
import { CommonService } from '@services/common.service';
import {ModelSquareService} from "@routes/model-square/model-square.service";

@Component({
  selector: 'card-render',
  templateUrl: './card-renderer.component.html',
  styleUrl: './card-renderer.component.less',
  encapsulation: ViewEncapsulation.None,
  standalone: true,
  imports: [MODULES, COMMON_MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.JIUWEN_MODEL],
    },
  ],
})
export class ModelServiceCardsRender {

  @Input() cardInfo: any;
  @Input() authStatus:boolean;
  @ViewChild('customTemplate', { static: true }) customTemplateRef: TemplateRef<any>;

  @Output() notifySubscribeStatus = new EventEmitter();

  public item: any;
  public description: string;
  public type: string;
  public hasAvailableButton = signal(true);
  public allowTest = false;
  public iconNoneEmpty = cdnAssetUrl('assets/model/default_model_detail.svg');

  subscribeBtnStatus = this.commonService.getSubscribeStatus();

  constructor(
    private cd: ChangeDetectorRef,
    private router: Router,
    private nzModalService: NzModalService,
    public i18n: I18NextEagerPipe,
    private readonly http: HttpService,
    private commonService: CommonService,
  ) {
  }

  ngOnInit() {
    this.allowTest = this.cardInfo?.model_type && !ModelSquareService.NOT_ALLOW_TEST_TYPE.includes(this.cardInfo?.model_type);
  }

  // 创建模型调优作业
  public handleCard(item: any, $event: Event, type:string): void {
    $event.stopPropagation();
    switch (type) {
      case "open":
        this.openModel(item);
        return;
      case 'view':
        this.goModelSquareDetail(item?.id)
        return;
      case 'tuning':
        this.router.navigate(['/home/model-square/model-test'], {
          queryParams: {
            id: item?.id,
            modelType: item?.model_type,
          }
        });
        return;
      default:
        return
    }
  }

  public goModelSquareDetail(id: string): void {
    //maas 使用的是 templateId
    this.router.navigate(['/home/model/detail'], {
      queryParams: {
        id: id,
      },
    });

  }

  openModel(item){
    const modal = this.nzModalService.create({
      nzTitle: null,
      nzFooter: null,
      nzContent: ModelModalComponent,
      nzWidth: '736px',
      nzData: {
        title: this.i18n.transform('resource_insufficient'),
        defaultSelect: [item?.resident_model_id]
      },
      nzOnOk: () => {
        modal.destroy();
      }
    });
    modal.afterClose.subscribe((result) => {
      if (result) {
        this.notifySubscribeStatus.emit(result);
      }
    });
  }

  protected readonly isNull = isNull;

  get isDisable():boolean{
    if (!this.subscribeBtnStatus) {
      return true
    }
    if (this.cardInfo?.is_in_free_trial) {
      return false
    }
    if(this.authStatus){
      return !this.cardInfo?.is_subscribed
    }

    //无鉴权& 无免费token -- true
    return !this.authStatus && !this.cardInfo?.is_in_free_trial;
  }
}
