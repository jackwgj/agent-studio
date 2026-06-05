import { Injectable } from '@angular/core';
import { cloneDeep } from 'lodash';
import { ReferenceItemData } from '@routes/knowledge-center/knowledge-base.interface';

@Injectable({
  providedIn: 'root',  // 使服务成为单例全局可用
})
export class AiAnswerListService {
  aiAnswerList = [];

  constructor() {
  }

  /**
   * kb source resolve begin
   */

  public setFileSourceWithNewData(sourceContent) {
    this.replaceSourceContent(sourceContent);
    let retrievePlugin;
    sourceContent?.forEach(item => {
      const target = item?.thought?.find(thoughtItem => thoughtItem.pluginName === 'retrieval');
      if (target) {
        retrievePlugin = target;
      }
    });
    if (!retrievePlugin) {
      return;
    }
    retrievePlugin?.response?.result?.output_list?.forEach(item => {
      const target = this.aiAnswerList.find(exist => exist.retrieval_id ===
        item.retrieval_id &&
        exist.serial_number ===
        item.serial_number);
      if (!target) {
        this.aiAnswerList.push(item);
      }
    });
  }

  public setFileSource(sourceContent) {
    this.replaceSourceContent(sourceContent);
    const pluginContent = sourceContent?.filter(content => {
      return content.toolType === 'plugin';
    });
    pluginContent?.forEach(plugin => {
      plugin?.response?.result?.output_list?.forEach(item => {
        const target = this.aiAnswerList.find(exist => exist.retrieval_id ===
          item.retrieval_id &&
          exist.serial_number ===
          item.serial_number);
        if (!target) {
          this.aiAnswerList.push(item);
        }
      });
    });
  }

  getReferenceHTML(serial, retirevalId) {
    return `<span id="reference_${ serial }" class="agent-base-show-source retrieval-id-${ retirevalId }">”</span>`;
  }

  public filterSourceFile(sourceContent) {
    let sourceFiles = [];
    const copyList = cloneDeep(this.aiAnswerList);
    const retrievalIds: string[] = [];
    sourceContent.forEach(content => {
      let currentIds = this.getRetrievalIds(content.content);
      const serNums = this.getSerNumber(content.content);
      currentIds.forEach(id => {
        if (id && !retrievalIds.includes(id)) {
          retrievalIds.push(id);
          sourceFiles =
            [...sourceFiles, ...copyList.filter(ans => ans.retrieval_id === id && serNums.includes(ans.serial_number))];
        }
      });
    });
    return this.filesAggregation(sourceFiles);
  }

  public filesAggregation(sourceFiles: any) {
    const showList: ReferenceItemData[] = [];

    sourceFiles.forEach(item => {
      const target = showList.find(file => file.file_id === item.file_id);
      if (target) {
        target.slicingFiles.push(item);
      } else {
        const { document_name, file_id, knowledge_base_id, retrieval_id, type, knowledge_base_type } = item;
        showList.push({
          document_name,
          file_id,
          knowledge_base_id,
          knowledge_base_type,
          retrieval_id,
          type,
          slicingFiles: [item],
        });
      }
    });
    return showList;
  }

  public getSerNumber(str) {
    if (!str) {
      return [];
    }
    const matches = str.match(/reference_(\d+)/g);
    return [...new Set(matches?.map(item => Number(item.split('_')[1])) ?? [])];
  }

  private replaceSourceContent(answerContent) {
    answerContent?.forEach(ans => {
      if (ans?.content) {
        let currentContent = ans.content;
        if (typeof currentContent === 'string') {
          ans.content = currentContent.replace(/\{([0-9]+)\|([a-zA-Z0-9-]+)}/gm, string => {
            const regex = /{([^|]+)\|([^}]+)}/;
            const match = regex.exec(string);
            const serial = match[1];
            const retirevalId = match[2];
            if (serial && retirevalId) {
              return this.getReferenceHTML(serial, retirevalId);
            } else {
              return '';
            }
          });
        }
      }
    });
  }

  private getRetrievalIds(str: string): string[] {
    if (!str) {
      return [];
    }
    const re = /retrieval-id-([^"]*)"/g;
    const ids = [];
    let m;
    while ((m = re.exec(str)) !== null) {
      ids.push(m[1]);
    }
    return [...new Set(ids)];
  }

  /**
   * kb source resolve end
   */
}
