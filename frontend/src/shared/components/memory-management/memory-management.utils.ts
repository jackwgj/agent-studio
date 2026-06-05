import { v4 as uuidV4 } from 'uuid';

import { IMemoryInfo, IMemoryRes, MemUpdatedOperationType } from './memory-management.interface';

export class MemoryManagementUtils {
  /**
   * 标准化展示记忆内容，不放在标准化中的对象字段是减少更新的心智负担，性能消耗可以忽略
   * @param mem 单条标准化后的记忆
   * @returns 展示的记忆内容
   */
  static getMemDisplayContent(mem: IMemoryInfo): string {
    return mem.content;
  }

  /**
   * 将IMemoryRes标准化为IMemoryInfo数组
   * @param memoryRes 接口返回的记忆信息
   * @returns 标准化后的记忆信息
   */
  static standardizedMemoryInfo(memoryRes: IMemoryRes): IMemoryInfo[] {
    if (!memoryRes) {
      return [];
    }

    const result: IMemoryInfo[] = [];

    this.processNaturalLanguageMemories(memoryRes, result);

    return result;
  }

  /**
   * 将IMemoryInfo数组还原为IMemoryRes
   * @param memoryInfos 标准化后的记忆信息
   * @returns 用于调接口的数据结构
   */
  static reverseStandardizedMemoryInfo(memoryInfos: IMemoryInfo[]): IMemoryRes {
    if (!memoryInfos?.length) {
      return {
        memories: [],
      };
    }

    return {
      memories: memoryInfos.map(item => {
        return {
          memory_id: item.id,
          content: item.content,
        };
      }),
    };
  }

  /**
   * 自然语言记忆
   * @private
   */
  private static processNaturalLanguageMemories(memoryRes: IMemoryRes, result: IMemoryInfo[]): void {
    if (!memoryRes.memories?.length) {
      return;
    }

    memoryRes.memories.forEach(mem => {
      if (!mem.memory_id || !mem.content) {
        return;
      }

      result.push({
        content: mem.content,
        updatedType: MemUpdatedOperationType.UPDATE,
        id: mem.memory_id,
        fakeId: uuidV4(),
      });
    });
  }

  /**
   * 添加自然语言记忆
   * @private
   */
  private static addNaturalLanguageMemory(result: IMemoryRes, info: IMemoryInfo): void {
    if (!info.id || !info.content) {
      return;
    }

    result.memories.push({
      memory_id: info.id,
      content: info.content,
    });
  }
}
