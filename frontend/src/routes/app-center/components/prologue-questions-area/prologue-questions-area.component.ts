import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  QueryList,
  SimpleChanges,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { Network } from '@model';
import { IsLongTextOverflowService } from '@shared/services/is-long-text-overflow.service';

@Component({
  selector: 'prologue-questions-area',
  templateUrl: './prologue-questions-area.component.html',
  styleUrls: ['./prologue-questions-area.component.less'],
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class PrologueQuestionsAreaComponent {
  @Input() knowledgeExampleInfo: any = {
    icon: '',
    title: '',
    prologue: '',
    cardList: [],
  };
  @Input() showPanel: boolean = true; //是否显示推荐问

  @Output() clickQuestionCard = new EventEmitter<string>();

  @ViewChildren('cardDivList') questionTipHost!: QueryList<ElementRef>;

  @ViewChild('prologueTipHost', { static: true }) prologueTipHost!: ElementRef;

  public questionIcon = `${Network.ASSETS_PATH}app-library/recommend-question-icon.svg`;

  public tipContent: string = '';

  constructor(private isLongTextOverflowServe: IsLongTextOverflowService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.knowledgeExampleInfo) {
      const { prologue } = changes.knowledgeExampleInfo.currentValue;
      this.tipContent = prologue;
    }
  }

  public onCardClick(cardInfo: string) {
    this.clickQuestionCard.emit(cardInfo);
  }
}
