import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { CommonValidation } from '@shared/validation/commonValidation';
import { NzModalRef } from 'ng-zorro-antd/modal';

@Component({
  selector: 'update-node-name-modal',
  templateUrl: './update-node-name-modal.component.html',
  styleUrls: ['./update-node-name-modal.component.less'],
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class UpdateNodeNameModalComponent implements OnInit {
  @Input() name: string;

  @Input() names: string[];

  @Output() nameChange = new EventEmitter<string>();

  constructor(
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    public i18n: I18NextEagerPipe,
    private modalRef: NzModalRef,
  ) {}

  public modelFormGroup = this.fb.group({
    nodeName: new FormControl('', [Validators.required]),
  });

  public confirmValue(e: Event) {
    e.stopPropagation();
    this.modelFormGroup.markAllAsTouched();
    if (this.modelFormGroup.invalid) {
      return;
    }

    this.name = this.modelFormGroup.controls.nodeName.value;
    this.nameChange.emit(this.name);
    this.modalRef.close(this.name);
  }

  close(): void {
    this.modalRef.close();
  }

  dismiss() {
    this.modalRef.close();
  }

  ngOnInit() {
    this.modelFormGroup.controls.nodeName.addValidators(
      CommonValidation.nameUniquenessVerify(
        this.names,
        this.i18n.transform('name_uniqueness'),
        this.name,
      ),
    );
    this.modelFormGroup.controls.nodeName.setValue(this.name);
    this.modelFormGroup.controls.nodeName.valueChanges.subscribe(() => {
      this.cdr.detectChanges();
    });
  }
}
