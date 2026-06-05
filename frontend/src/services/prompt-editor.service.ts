import { EventEmitter, Injectable, Output } from '@angular/core';
import {
  CHECKVARIABLENAME_REG,
  SNIPPET_REG,
  VARIANT_BG_COLORS,
  VARIANT_BG_COLOR_UNDEFINED,
  VARIANT_MARK_COLORS,
  VARIANT_MARK_COLOR_UNREF,
  VARIANT_NAME_ERROR_COLOR,
  VARIANT_NAME_ERROR_POS,
  VARIANT_REG,
  VARIANT_UNDEFINED_POS,
  VARIANT_UNREF_POS,
} from '@constants/exp-tmpl-config.const';
import {
  EChunkType,
  EConfirmBtnStatus,
  EVarSyncSrc,
} from '@enums/prompt-editor.enum';
import {
  IPromptChunk,
  ITmplPrompt,
  IVarSyncInfo,
  IVariant,
  IWritingHistory,
} from '@interfaces/prompt/prompt-editor.interface';
import { cloneDeep, unionWith } from 'lodash';
import { BehaviorSubject, Observable } from 'rxjs';
import { CommonUtils } from 'src/utils/common.util';

@Injectable({
  providedIn: 'root',
})
export class PromptEditorService {
  @Output() isExistErrorTips = new EventEmitter<boolean>();

  // 用于从外部设置确认按钮的显隐
  private readonly confirmBtnState$ = new EventEmitter<EConfirmBtnStatus>();

  private readonly prompt$ = new BehaviorSubject<string>('');

  private readonly fileId$ = new BehaviorSubject<string>('');

  private readonly textareaValue$ = new BehaviorSubject<string>('');

  private readonly prePrompt$ = new BehaviorSubject<string>('');

  /** 右侧变量输入区的变量 */
  private readonly variants$ = new BehaviorSubject<IVariant[]>([]);

  /** 用于同步在变量定义和变量输入中修改的变量 */
  private readonly modifiedVar$ = new BehaviorSubject<IVarSyncInfo>({
    src: EVarSyncSrc.VAR_DEF,
    variant: {
      key: '',
      value: '',
      name: '',
    },
  });

  /** 预览展示所需的变量和 prompt */
  private readonly tmplPrompt$ = new BehaviorSubject<ITmplPrompt>({
    prompt: '',
    variants: [],
    answer: '',
  });

  /** 全局变量 */
  private readonly globalVars$ = new BehaviorSubject<IVariant[]>([]);

  /** 变量池 由全局变量与从撰写区中定义的变量组成 */
  private readonly varPool$ = new BehaviorSubject<IVariant[]>([]);

  /** 历史记录列表 */
  private readonly histories$ = new BehaviorSubject<IWritingHistory[]>([]);

  public confirmBtnStateUpdate$(): Observable<EConfirmBtnStatus> {
    return this.confirmBtnState$.asObservable();
  }

  public setConfirmBtnState(state: EConfirmBtnStatus): void {
    this.confirmBtnState$.next(state);
  }

  /** 当前光标的位置 */
  private readonly selectionStart$ = new BehaviorSubject<number>(0);

  public getSelectionStart(): number {
    return this.selectionStart$.getValue();
  }

  public selectionStartUpdate$(): Observable<number> {
    return this.selectionStart$.asObservable();
  }

  public setSelectionStart(index: number): void {
    this.selectionStart$.next(index);
  }

  public getPrePrompt(): string {
    return this.prePrompt$.getValue();
  }

  public setPrePrompt(prompt: string): void {
    this.prePrompt$.next(prompt);
  }

  public prePromptUpdate$(): Observable<string> {
    return this.prePrompt$.asObservable();
  }

  public getTextareaValue(): string {
    return this.textareaValue$.getValue();
  }

  public setTextareaValue(value: string): void {
    this.textareaValue$.next(value);
  }

  public getPrompt(): string {
    return this.prompt$.getValue();
  }

  public setPrompt(prompt: string): void {
    this.prompt$.next(prompt);
  }

  public getFileId(): string {
    return this.fileId$.getValue();
  }

  public setFileId(id: string): void {
    this.fileId$.next(id);
  }

  public promptUpdate$(): Observable<string> {
    return this.prompt$.asObservable();
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

  public setTmplPrompt(tmplPrompt: ITmplPrompt): void {
    this.tmplPrompt$.next(tmplPrompt);
    this.prePrompt$.next(tmplPrompt.prompt);
    this.variants$.next(tmplPrompt.variants);
  }

  public tmplPromptUpdate$(): Observable<ITmplPrompt> {
    return this.tmplPrompt$.asObservable();
  }

  public getGlobalVars(): IVariant[] {
    return this.globalVars$.getValue();
  }

  public setGlobalVars(variants: IVariant[]): void {
    this.globalVars$.next(variants);
  }

  public globalVarsUpdate$(): Observable<IVariant[]> {
    return this.globalVars$.asObservable();
  }

  public getVarPool(): IVariant[] {
    return this.varPool$.getValue();
  }

  public setVarPool(variants: IVariant[]): void {
    this.varPool$.next(variants);
  }

  public varPoolUpdate$(): Observable<IVariant[]> {
    return this.varPool$.asObservable();
  }

  public getModifiedVar(): IVarSyncInfo {
    return this.modifiedVar$.getValue();
  }

  public setModifiedVar(varSyncInfo: IVarSyncInfo): void {
    this.modifiedVar$.next(varSyncInfo);
  }

  public modifiedVarUpdate$(): Observable<IVarSyncInfo> {
    return this.modifiedVar$.asObservable();
  }

  /** 将新定义的变量添加到变量池 */
  public addVars2VarPool(vars: IVariant[]): void {
    const variant = cloneDeep(this.getVarPool());
    variant.push(...vars);

    this.varPool$.next(variant);
  }

  /** 更新 varpool 中与 vars 所包含元素 key 相同元素的值 */
  public updateVarPool(vars: IVariant[]): void {
    const varPool = this.varPool$.getValue();

    vars.forEach((newVar) => {
      const targetIndex = varPool.findIndex((v) => v.key === newVar.key);

      if (targetIndex !== -1) {
        varPool[targetIndex].value = newVar.value;
      }
    });

    this.setVarPool(varPool);
  }

  public initVarPool(): void {
    const globalVars = cloneDeep(this.getGlobalVars());
    this.varPool$.next([...globalVars]);
  }

  public getHistories(): IWritingHistory[] {
    return this.histories$.getValue();
  }

  public setHistories(histories: IWritingHistory[]): void {
    this.histories$.next(histories);
  }

  public deleteHistory(index: number): void {
    const histories = cloneDeep(this.histories$.getValue());

    histories.splice(index, 1);
    this.histories$.next(histories);
  }

  public unshiftHistory(history: IWritingHistory): void {
    const histories = cloneDeep(this.histories$.getValue());

    histories.unshift(history);
    this.histories$.next(histories);
  }

  public useHistory(history: IWritingHistory): void {
    /** 以 variants 的值为基准去重 */
    const variants = cloneDeep(history.variants);
    const globalVars = cloneDeep(this.globalVars$.getValue());

    this.varPool$.next(
      unionWith(variants.concat(globalVars), (a, b) => a.key === b.key),
    );
    this.prompt$.next(history.prompt);
    this.prePrompt$.next(history.prompt);
    this.tmplPrompt$.next({
      prompt: history.prompt,
      variants: cloneDeep(history.variants),
      answer: history.answer,
      file: history.file,
    });
    this.variants$.next(history.variants);
  }

  /** 给历史记录中的 variants 加上大括号 */
  public buildSafeHistories(histories: IWritingHistory[]): IWritingHistory[] {
    const historiesCopy = cloneDeep(histories);

    return historiesCopy.map((history) => {
      const historyCopy = cloneDeep(history);

      return {
        ...historyCopy,
        variants: historyCopy.variants.map(CommonUtils.addCurlyBrackets),
      };
    });
  }

  public historiesUpdate$(): Observable<IWritingHistory[]> {
    return this.histories$.asObservable();
  }

  public processPrompt(prompt: string): IPromptChunk[] {
    const match = prompt.match(SNIPPET_REG);

    if (match) {
      const content = this.processVar(
        prompt.split(SNIPPET_REG)[0],
        true,
        false,
      );
      content.push({
        type: EChunkType.SNIPPET,
        value: match[0],
      });

      return content;
    }

    return this.processVar(prompt, true, false);
  }

  public processVar(promptWithVar: string): IPromptChunk[];
  /**
   *
   * @param promptWithVar 包含模板变量的字符串
   * @param shouldRenderUndefined 是否需要渲染未定义变量，仅在有变量定义的编辑器场景下使用
   */
  public processVar(
    promptWithVar: string,
    shouldRenderUndefined: boolean,
    shouldRegCheckName: boolean,
  ): IPromptChunk[];
  public processVar(
    promptWithVar: string,
    shouldRenderUndefined?: boolean,
    shouldRegCheckName?: boolean,
  ): IPromptChunk[] {
    let varChunkIndex = 0;
    const varChunkMap = new Map<string, number>();
    const isVarPool = this.getVarPool().length;

    return promptWithVar
      .split(VARIANT_REG)
      .filter((prompt) => prompt !== '')
      .map((chunk) => {
        if (chunk.match(VARIANT_REG)) {
          /** 点击确定后，检查变量命名 */
          const regex = new RegExp(CHECKVARIABLENAME_REG);
          const chunkName = chunk.replace(/{{|}}/g, '');

          if (shouldRenderUndefined && !this.checkIfVarDefined(chunk)) {
            /** 若需要渲染未定义变量，并且该变量为未定义变量 */
            return {
              type: EChunkType.VARIANT,
              value: chunk,
              pos: VARIANT_UNDEFINED_POS,
            };
          }

          /** 无需渲染未定义变量，或变量已定义 */
          let pos = 0;

          if (varChunkMap.has(chunk)) {
            /** 渲染时相同 key 背景色保持一致 */
            pos = varChunkMap.get(chunk);
          } else if (shouldRegCheckName && !regex.test(chunkName)) {
            pos = VARIANT_NAME_ERROR_POS;
          } else if (isVarPool) {
            // 如果有定义变量上下文，则使用已定义变量的位置信息
            pos = this.getVarPool().findIndex((v) => v.key === chunk);
            varChunkMap.set(chunk, pos);
          } else {
            // 没有则默认枚举
            pos = varChunkIndex++;
            varChunkMap.set(chunk, pos);
          }

          return {
            type: EChunkType.VARIANT,
            value: chunk,
            pos,
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
    if (index === VARIANT_NAME_ERROR_POS) {
      return VARIANT_NAME_ERROR_COLOR;
    }
    if (index === VARIANT_UNREF_POS) {
      return VARIANT_MARK_COLOR_UNREF;
    }

    return VARIANT_MARK_COLORS[index % VARIANT_MARK_COLORS.length];
  }

  public getVarBgColor(index: number): string {
    if (index === VARIANT_UNDEFINED_POS) {
      return VARIANT_BG_COLOR_UNDEFINED;
    }

    return VARIANT_BG_COLORS[index % VARIANT_BG_COLORS.length];
  }

  /** 同步修改后的变量 */
  public syncModifiedVar(variants: IVariant[], variant: IVariant): IVariant[] {
    const tmpVars = cloneDeep(variants);
    const targetIndex = tmpVars.findIndex((v) => v.key === variant.key);

    if (targetIndex !== -1) {
      tmpVars[targetIndex] = variant;
    }

    return tmpVars;
  }

  public initWriting(): void {
    this.selectionStart$.next(0);
    this.prompt$.next('');
    this.variants$.next([]);
    this.tmplPrompt$.next({
      prompt: '',
      variants: [],
      answer: '',
    });
    this.prePrompt$.next('');
    this.initVarPool();
  }

  public getCandidates(chunkContent: string): IVariant[] {
    return this.getVarPool().filter((v) => v.key.includes(chunkContent));
  }

  /** 检查变量是否在 varpool 中已定义 */
  private checkIfVarDefined(key: string) {
    return this.varPool$
      .getValue()
      .map((v) => v.key)
      .includes(key);
  }
}
