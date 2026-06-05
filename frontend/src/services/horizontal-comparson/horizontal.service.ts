import { Injectable } from '@angular/core';
import {
  SNIPPET_REG,
  VARIANT_BG_COLORS,
  VARIANT_MARK_COLORS,
  VARIANT_REG,
} from '@constants/exp-tmpl-config.const';
import { EChunkType } from '@enums/prompt-editor.enum';
import { IPromptChunk } from '@interfaces/prompt/prompt-editor.interface';
import {
  IPromptList,
  IVariant,
  ITmplPrompt,
  IUpdateScore,
} from '@interfaces/prompt/horizontal.interface';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class HorizontalService {
  // 变量集
  private readonly variants$ = new BehaviorSubject<IVariant[]>([]);
  // 模板+变量
  private readonly tmplPrompt$ = new BehaviorSubject<ITmplPrompt>({
    prompt: '',
    variants: [],
  });
  // 候选模板
  private readonly candidate$ = new BehaviorSubject<IPromptList[]>([]);
  // 左右选中模板
  private readonly discrepancyLeft$ = new BehaviorSubject<IPromptList>({
    name: '',
    id: '',
    content: '',
    model: '',
    answer: '',
    model_config: {
      n: 0,
      top_p: 0,
      max_tokens: 0,
      temperature: 0,
      presence_penalty: 0,
      endpoint: '',
      project_id: '',
      deployment_id: '',
      model_id: '',
      metadata: {
        max_tokens: 0,
      },
      model_type: '',
    },
    variable_vo: [],
    manual_score: 0,
    creator: '',
    updater: '',
    created_on: '',
    updated_on: '',
  });

  private readonly discrepancyRight$ = new BehaviorSubject<IPromptList>({
    name: '',
    id: '',
    content: '',
    model: '',
    answer: '',
    model_config: {
      n: 0,
      top_p: 0,
      max_tokens: 0,
      temperature: 0,
      presence_penalty: 0,
      endpoint: '',
      project_id: '',
      deployment_id: '',
      model_id: '',
      metadata: {
        max_tokens: 0,
      },
      model_type: '',
    },
    variable_vo: [],
    manual_score: 0,
    creator: '',
    updater: '',
    created_on: '',
    updated_on: '',
  });
  // 当前更新分数
  private readonly currentUpdateScore$ = new BehaviorSubject<IUpdateScore>({
    manual_score: 0,
    id: '',
    name: '',
  });

  // 当前在运行中的任务列表
  public readonly promptRunningList$ = new BehaviorSubject<string[]>([]);

  // 选择下拉框loading效果
  public readonly isViewLoading$ = new BehaviorSubject<boolean>(false);

  // 左右答案
  public readonly answerLeft$: BehaviorSubject<string>;
  public readonly answerRight$: BehaviorSubject<string>;

  public readonly tokenLeft$: BehaviorSubject<string>;
  public readonly tokenRight$: BehaviorSubject<string>;

  constructor() {
    // 存入 localStorage
    const savedAnswerLeft = localStorage.getItem('answerLeft');
    this.answerLeft$ = new BehaviorSubject<string>(savedAnswerLeft || ``);
    const savedAnswerRight = localStorage.getItem('answerRight');
    this.answerRight$ = new BehaviorSubject<string>(savedAnswerRight || ``);
    const savedTokenLeft = localStorage.getItem('tokenLeft');
    this.tokenLeft$ = new BehaviorSubject<string>(savedTokenLeft || '');
    const savedTokenRight = localStorage.getItem('tokenRight');
    this.tokenRight$ = new BehaviorSubject<string>(savedTokenRight || '');
  }

  public getVariants(): IVariant[] {
    return this.variants$.getValue();
  }

  public setVariants(variants: IVariant[]): void {
    this.variants$.next(variants);
  }

  public variantsUpdate$(): Observable<IVariant[]> {
    return this.variants$.asObservable();
  }

  public getTmplPrompt(): ITmplPrompt {
    return this.tmplPrompt$.getValue();
  }

  public setTmplPromptLeft(tmplPrompt: ITmplPrompt): void {
    this.tmplPrompt$.next(tmplPrompt);
    this.variants$.next(tmplPrompt.variants);
  }

  public setTmplPromptRight(tmplPrompt: ITmplPrompt): void {
    this.tmplPrompt$.next(tmplPrompt);
    this.variants$.next(tmplPrompt.variants);
  }

  public tmplPromptUpdate$(): Observable<ITmplPrompt> {
    return this.tmplPrompt$.asObservable();
  }

  public processPrompt(prompt: string): IPromptChunk[] {
    const match = prompt.match(SNIPPET_REG);

    if (match) {
      const content = this.processVar(prompt.split(SNIPPET_REG)[0]);
      content.push({
        type: EChunkType.SNIPPET,
        value: match[0],
      });

      return content;
    }

    return this.processVar(prompt);
  }

  public processVar(promptWithVar: string): IPromptChunk[] {
    let varChunkIndex = 0;

    return promptWithVar.split(VARIANT_REG).map((chunk) => {
      if (chunk.match(VARIANT_REG)) {
        return {
          type: EChunkType.VARIANT,
          value: chunk,
          pos: varChunkIndex++,
        };
      }

      return {
        type: EChunkType.COMMON,
        value: chunk,
      };
    });
  }

  /**
   * 在字符串 a 的 index 处插入字符串 b
   * 返回插入后的字符串
   */
  public insertString(a: string, b: string, index: number): string {
    return a.slice(0, index).concat(b, a.slice(index));
  }

  public getVarBarColor(index: number): string {
    return VARIANT_MARK_COLORS[index % VARIANT_MARK_COLORS.length];
  }

  public getVarBgColor(index: number): string {
    return VARIANT_BG_COLORS[index % VARIANT_BG_COLORS.length];
  }

  public getCandidate(): IPromptList[] {
    return this.candidate$.getValue();
  }

  public setCandidate(candidate: IPromptList[]): void {
    this.candidate$.next(candidate);
  }

  public candidateUpdate$(): Observable<IPromptList[]> {
    return this.candidate$.asObservable();
  }

  public getDiscrepancyLeft(): IPromptList {
    return this.discrepancyLeft$.getValue();
  }

  public setDiscrepancyLeft(discrepancyLeft: IPromptList): void {
    this.discrepancyLeft$.next(discrepancyLeft);
  }

  public discrepancyLeftUpdate$(): Observable<IPromptList> {
    return this.discrepancyLeft$.asObservable();
  }

  public getDiscrepancyRight(): IPromptList {
    return this.discrepancyRight$.getValue();
  }

  public setDiscrepancyRight(discrepancyRight: IPromptList): void {
    this.discrepancyRight$.next(discrepancyRight);
  }

  public discrepancyRightUpdate$(): Observable<IPromptList> {
    return this.discrepancyRight$.asObservable();
  }

  public getCurrentUpdateScore(): IUpdateScore {
    return this.currentUpdateScore$.getValue();
  }

  public setCurrentUpdateScore(currentUpdateScore: IUpdateScore): void {
    this.currentUpdateScore$.next(currentUpdateScore);
  }

  public currentUpdateScoreUpdate$(): Observable<IUpdateScore> {
    return this.currentUpdateScore$.asObservable();
  }

  public getPromptRunningList(): string[] {
    return this.promptRunningList$.getValue();
  }

  public setPromptRunningList(promptRunningList: string[]): void {
    this.promptRunningList$.next(promptRunningList);
  }

  public promptRunningListUpdate$(): Observable<string[]> {
    return this.promptRunningList$.asObservable();
  }

  public getViewLoading(): boolean {
    return this.isViewLoading$.getValue();
  }

  public setViewLoading(isViewLoading: boolean): void {
    this.isViewLoading$.next(isViewLoading);
  }

  public viewLoadingUpdate$(): Observable<boolean> {
    return this.isViewLoading$.asObservable();
  }

  public getAnswerLeft(): string {
    return this.answerLeft$.getValue();
  }

  public setAnswerLeft(answer: string): void {
    this.answerLeft$.next(answer);
    localStorage.setItem('answerLeft', answer);
  }

  public answerLeftUpdate$(): Observable<string> {
    return this.answerLeft$.asObservable();
  }

  public clearAnswerLeft(): void {
    this.answerLeft$.next(``);
    localStorage.removeItem('answerLeft');
  }

  public getAnswerRight(): string {
    return this.answerRight$.getValue();
  }

  public setAnswerRight(answer: string): void {
    this.answerRight$.next(answer);
    localStorage.setItem('answerRight', answer);
  }

  public answerRightUpdate$(): Observable<string> {
    return this.answerRight$.asObservable();
  }

  public clearAnswerRight(): void {
    this.answerRight$.next(``);
    localStorage.removeItem('answerRight');
  }

  public getTokenLeft(): string {
    return this.answerLeft$.getValue();
  }

  public setTokenLeft(answer: string): void {
    this.tokenLeft$.next(answer);
    localStorage.setItem('tokenLeft', answer);
  }

  public tokenLeftUpdate$(): Observable<string> {
    return this.tokenLeft$.asObservable();
  }

  public clearTokenLeft(): void {
    this.tokenLeft$.next('');
    localStorage.removeItem('tokenLeft');
  }

  public getTokenRight(): string {
    return this.tokenRight$.getValue();
  }

  public setTokenRight(answer: string): void {
    this.tokenRight$.next(answer);
    localStorage.setItem('tokenRight', answer);
  }

  public tokenRightUpdate$(): Observable<string> {
    return this.tokenRight$.asObservable();
  }

  public clearTokenRight(): void {
    this.tokenRight$.next('');
    localStorage.removeItem('tokenRight');
  }
}
