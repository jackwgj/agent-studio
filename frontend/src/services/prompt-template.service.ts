import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class PromptTmplService {
  public getCheckedCardStyle(
    cardCheckedArr: string[],
    id: string,
  ): Record<string, string> {
    if (cardCheckedArr.find((tmplId) => tmplId === id)) {
      return {
        border: '1px solid #80b4ff',
        outline: '1px solid #80b4ff',
        'box-shadow': '0 2px 12px 0 rgba(0,0,0,0.08)',
      };
    }

    return {};
  }

  /**
   * @param checkedTmplIds 被选中的提示词模板ids
   * @param deletedId 被删除的提示词模板id
   * @returns 如果 checkedTmplIds 中包含 deletedId
   * 返回从 checkedTmplIds 中删除 deletedId 后的结果
   * 否则返回 checkedTmplIds
   */
  public deleteCheckedTmplId(
    checkedTmplIds: string[],
    deletedId: string,
  ): string[] {
    const copiedTmplIds = checkedTmplIds.slice(0);
    const index = copiedTmplIds.findIndex((id) => id === deletedId);

    if (index !== -1) {
      copiedTmplIds.splice(index, 1);
    }

    return copiedTmplIds;
  }
}
