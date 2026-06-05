enum PromptCategory {
  GENERAL = 'general',
  CREATIVE = 'creative',
  TECHNICAL = 'technical',
  BUSINESS = 'business',
  EDUCATION = 'education',
  ENTERTAINMENT = 'entertainment',
}

enum PromptDifficulty {
  BEGINNER = 'beginner',
  INTERMEDIATE = 'intermediate',
  ADVANCED = 'advanced',
  EXPERT = 'expert',
}

enum PromptStatus {
  DRAFT = 'draft',
  REVIEW = 'review',
  APPROVED = 'approved',
  PUBLISHED = 'published',
  ARCHIVED = 'archived',
}

interface Prompt {
  id: string;
  title: string;
  content: string;
  description: string;
  category: PromptCategory;
  difficulty: PromptDifficulty;
  tags: string[];
  status: PromptStatus;
  version: number;
  authorId: string;
  createdAt: Date;
  updatedAt: Date;
  lastUsedAt?: Date;
  usageCount: number;
  variables?: PromptVariable[];
  examples?: string[];
  metadata: Record<string, any>;
}

interface PromptVariable {
  name: string;
  description: string;
  type: VariableType;
  required: boolean;
  defaultValue?: string;
  validationRules?: ValidationRule[];
}

enum VariableType {
  TEXT = 'text',
  NUMBER = 'number',
  BOOLEAN = 'boolean',
  SELECT = 'select',
  LIST = 'list',
  JSON = 'json',
}

interface ValidationRule {
  type: ValidationType;
  value?: any;
  message: string;
}

enum ValidationType {
  MIN_LENGTH = 'min_length',
  MAX_LENGTH = 'max_length',
  REGEX = 'regex',
  MIN_VALUE = 'min_value',
  MAX_VALUE = 'max_value',
  REQUIRED = 'required',
}

interface PromptSearchCriteria {
  query?: string;
  categories?: PromptCategory[];
  difficulty?: PromptDifficulty[];
  tags?: string[];
  status?: PromptStatus[];
  authorId?: string;
  minUsageCount?: number;
  createdAfter?: Date;
  createdBefore?: Date;
  limit?: number;
  offset?: number;
  sortBy?: 'createdAt' | 'updatedAt' | 'usageCount' | 'title';
  sortOrder?: 'asc' | 'desc';
}

interface PromptCreateData {
  title: string;
  content: string;
  description: string;
  category: PromptCategory;
  difficulty: PromptDifficulty;
  tags: string[];
  authorId: string;
  variables?: Omit<PromptVariable, 'name'>[];
  examples?: string[];
  metadata?: Record<string, any>;
}

interface PromptUpdateData {
  title?: string;
  content?: string;
  description?: string;
  category?: PromptCategory;
  difficulty?: PromptDifficulty;
  tags?: string[];
  status?: PromptStatus;
  variables?: PromptVariable[];
  examples?: string[];
  metadata?: Record<string, any>;
} // ==================== 工具函数 ====================
class TestUtils {
  static generateId(prefix: string = 'prompt'): string {
    const timestamp = Date.now();
    const random = Math.random().toString(36).substring(2, 8);
    return `${prefix}_${timestamp}_${random}`;
  }

  static generateRandomString(length: number): string {
    const chars =
      'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let result = '';
    for (let i = 0; i < length; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
  }

  static generateRandomPromptContent(): string {
    const templates = [
      'Write a {tone} {type} about {topic} in {language}',
      'Create a {format} for {audience} about {subject}',
      'Generate {count} {style} ideas for {purpose}',
      'Explain {concept} to a {level} audience in {words} words',
      'Compare and contrast {item1} and {item2} in terms of {aspect}',
    ];
    return templates[Math.floor(Math.random() * templates.length)];
  }

  static generateRandomTags(count: number = 3): string[] {
    const allTags = [
      'ai',
      'writing',
      'coding',
      'marketing',
      'productivity',
      'creative',
      'technical',
      'business',
      'education',
      'entertainment',
      'research',
      'analysis',
      'summarization',
      'translation',
      'brainstorming',
    ];
    const shuffled = [...allTags].sort(() => 0.5 - Math.random());
    return shuffled.slice(0, Math.min(count, allTags.length));
  }

  static createMockPrompt(overrides?: Partial<Prompt>): Prompt {
    const id = TestUtils.generateId();
    const now = new Date();
    return {
      id,
      title: `Test Prompt ${id.substring(0, 8)}`,
      content: TestUtils.generateRandomPromptContent(),
      description: `This is a test prompt for testing purposes. ID: ${id}`,
      category:
        Object.values(PromptCategory)[
          Math.floor(Math.random() * Object.values(PromptCategory).length)
        ],
      difficulty:
        Object.values(PromptDifficulty)[
          Math.floor(Math.random() * Object.values(PromptDifficulty).length)
        ],
      tags: TestUtils.generateRandomTags(3),
      status: PromptStatus.PUBLISHED,
      version: 1,
      authorId: `user_${TestUtils.generateRandomString(8)}`,
      createdAt: now,
      updatedAt: now,
      lastUsedAt: Math.random() > 0.5 ? now : undefined,
      usageCount: Math.floor(Math.random() * 100),
      variables: [
        {
          name: 'tone',
          description: 'The tone of the response',
          type: VariableType.SELECT,
          required: true,
          defaultValue: 'professional',
          validationRules: [
            { type: ValidationType.REQUIRED, message: 'Tone is required' },
          ],
        },
      ],
      examples: [
        'Example 1: Professional tone for business context',
        'Example 2: Casual tone for informal context',
      ],
      metadata: { test: true, generatedAt: now.toISOString() },
      ...overrides,
    };
  }

  static sleep(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
}

// ==================== 提示词服务类 ====================
class PromptService {
  private prompts: Map<string, Prompt> = new Map();
  private searchIndex: Map<string, Set<string>> = new Map();

  constructor() {
    this.initializeSearchIndex();
  }

  private initializeSearchIndex(): void {
    // 初始化搜索索引
    Object.values(PromptCategory).forEach((category) => {
      this.searchIndex.set(`category:${category}`, new Set());
    });
    Object.values(PromptDifficulty).forEach((difficulty) => {
      this.searchIndex.set(`difficulty:${difficulty}`, new Set());
    });
    Object.values(PromptStatus).forEach((status) => {
      this.searchIndex.set(`status:${status}`, new Set());
    });
  }

  private updateSearchIndex(prompt: Prompt, oldPrompt?: Prompt): void {
    // 更新分类索引
    if (oldPrompt) {
      this.searchIndex.get(`category:${oldPrompt.category}`)?.delete(prompt.id);
      this.searchIndex
        .get(`difficulty:${oldPrompt.difficulty}`)
        ?.delete(prompt.id);
      this.searchIndex.get(`status:${oldPrompt.status}`)?.delete(prompt.id);
      oldPrompt.tags.forEach((tag) => {
        const key = `tag:${tag}`;
        if (this.searchIndex.has(key)) {
          this.searchIndex.get(key)?.delete(prompt.id);
        }
      });
    }
    // 添加新索引
    this.searchIndex.get(`category:${prompt.category}`)?.add(prompt.id);
    this.searchIndex.get(`difficulty:${prompt.difficulty}`)?.add(prompt.id);
    this.searchIndex.get(`status:${prompt.status}`)?.add(prompt.id);
    prompt.tags.forEach((tag) => {
      const key = `tag:${tag}`;
      if (!this.searchIndex.has(key)) {
        this.searchIndex.set(key, new Set());
      }
      this.searchIndex.get(key)?.add(prompt.id);
    });
  } // 创建提示词

  createPrompt(data: PromptCreateData): Prompt {
    // 验证必填字段
    if (!data.title || data.title.trim().length === 0) {
      throw new Error('Prompt title is required');
    }
    if (!data.content || data.content.trim().length === 0) {
      throw new Error('Prompt content is required');
    }
    if (data.title.length > 100) {
      throw new Error('Prompt title cannot exceed 100 characters');
    }
    if (data.content.length > 5000) {
      throw new Error('Prompt content cannot exceed 5000 characters');
    }
    const id = TestUtils.generateId();
    const now = new Date();
    const prompt: Prompt = {
      id,
      title: data.title.trim(),
      content: data.content.trim(),
      description: data.description?.trim() || '',
      category: data.category,
      difficulty: data.difficulty,
      tags: [...new Set(data.tags || [])], // 去重
      status: PromptStatus.DRAFT,
      version: 1,
      authorId: data.authorId,
      createdAt: now,
      updatedAt: now,
      usageCount: 0,
      variables: data.variables?.map((v, index) => ({
        name: `var_${index + 1}`,
        ...v,
      })),
      examples: data.examples || [],
      metadata: data.metadata || {},
    };
    this.prompts.set(id, prompt);
    this.updateSearchIndex(prompt);
    return prompt;
  }

  // 获取提示词
  getPrompt(id: string): Prompt | undefined {
    return this.prompts.get(id);
  }

  // 获取多个提示词
  getPrompts(ids: string[]): Prompt[] {
    return ids
      .map((id) => this.prompts.get(id))
      .filter((prompt): prompt is Prompt => prompt !== undefined);
  } // 更新提示词
  updatePrompt(id: string, data: PromptUpdateData): Prompt | undefined {
    const existingPrompt = this.prompts.get(id);
    if (!existingPrompt) {
      return undefined;
    } // 验证更新数据
    if (data.title && data.title.trim().length === 0) {
      throw new Error('Prompt title cannot be empty');
    }
    if (data.content && data.content.trim().length === 0) {
      throw new Error('Prompt content cannot be empty');
    }
    const updatedPrompt: Prompt = {
      ...existingPrompt,
      ...data,
      title: data.title ? data.title.trim() : existingPrompt.title,
      content: data.content ? data.content.trim() : existingPrompt.content,
      description: data.description
        ? data.description.trim()
        : existingPrompt.description,
      tags: data.tags ? [...new Set(data.tags)] : existingPrompt.tags,
      version: existingPrompt.version + 1,
      updatedAt: new Date(),
    };
    this.prompts.set(id, updatedPrompt);
    this.updateSearchIndex(updatedPrompt, existingPrompt);
    return updatedPrompt;
  } // 删除提示词
  deletePrompt(id: string): boolean {
    const prompt = this.prompts.get(id);
    if (!prompt) {
      return false;
    } // 从搜索索引中移除
    this.searchIndex.get(`category:${prompt.category}`)?.delete(id);
    this.searchIndex.get(`difficulty:${prompt.difficulty}`)?.delete(id);
    this.searchIndex.get(`status:${prompt.status}`)?.delete(id);
    prompt.tags.forEach((tag) => {
      this.searchIndex.get(`tag:${tag}`)?.delete(id);
    });
    return this.prompts.delete(id);
  } // 批量删除
  deletePrompts(ids: string[]): { deleted: number; failed: number } {
    let deleted = 0;
    let failed = 0;
    ids.forEach((id) => {
      if (this.deletePrompt(id)) {
        deleted++;
      } else {
        failed++;
      }
    });
    return { deleted, failed };
  } // 搜索提示词
  searchPrompts(criteria: PromptSearchCriteria): {
    prompts: Prompt[];
    total: number;
  } {
    let promptIds: Set<string> | undefined; // 按分类过滤
    if (criteria.categories && criteria.categories.length > 0) {
      const categoryIds = criteria.categories
        .map((category) => this.searchIndex.get(`category:${category}`))
        .filter((set): set is Set<string> => set !== undefined)
        .reduce((acc, set) => {
          if (!acc) return new Set(set);
          return new Set([...acc].filter((x) => set.has(x)));
        }, undefined as Set<string> | undefined);
      promptIds = categoryIds;
    } // 按难度过滤
    if (criteria.difficulty && criteria.difficulty.length > 0) {
      const difficultyIds = criteria.difficulty
        .map((difficulty) => this.searchIndex.get(`difficulty:${difficulty}`))
        .filter((set): set is Set<string> => set !== undefined)
        .reduce((acc, set) => {
          if (!acc) return new Set(set);
          return new Set([...acc].filter((x) => set.has(x)));
        }, promptIds);
      promptIds = difficultyIds;
    } // 按标签过滤
    if (criteria.tags && criteria.tags.length > 0) {
      const tagIds = criteria.tags
        .map((tag) => this.searchIndex.get(`tag:${tag}`))
        .filter((set): set is Set<string> => set !== undefined)
        .reduce((acc, set) => {
          if (!acc) return new Set(set);
          return new Set([...acc].filter((x) => set.has(x)));
        }, promptIds);
      promptIds = tagIds;
    } // 按状态过滤
    if (criteria.status && criteria.status.length > 0) {
      const statusIds = criteria.status
        .map((status) => this.searchIndex.get(`status:${status}`))
        .filter((set): set is Set<string> => set !== undefined)
        .reduce((acc, set) => {
          if (!acc) return new Set(set);
          return new Set([...acc].filter((x) => set.has(x)));
        }, promptIds);
      promptIds = statusIds;
    } // 如果没有过滤条件，使用所有提示词
    if (!promptIds) {
      promptIds = new Set(this.prompts.keys());
    } // 转换为提示词数组
    let prompts = Array.from(promptIds)
      .map((id) => this.prompts.get(id))
      .filter((prompt): prompt is Prompt => prompt !== undefined); // 关键词搜索
    if (criteria.query) {
      const query = criteria.query.toLowerCase();
      prompts = prompts.filter(
        (prompt) =>
          prompt.title.toLowerCase().includes(query) ||
          prompt.content.toLowerCase().includes(query) ||
          prompt.description.toLowerCase().includes(query) ||
          prompt.tags.some((tag) => tag.toLowerCase().includes(query)),
      );
    } // 作者过滤
    if (criteria.authorId) {
      prompts = prompts.filter(
        (prompt) => prompt.authorId === criteria.authorId,
      );
    } // 使用次数过滤
    if (criteria.minUsageCount !== undefined) {
      prompts = prompts.filter(
        (prompt) => prompt.usageCount >= criteria.minUsageCount!,
      );
    } // 创建时间过滤
    if (criteria.createdAfter) {
      prompts = prompts.filter(
        (prompt) => prompt.createdAt >= criteria.createdAfter!,
      );
    }
    if (criteria.createdBefore) {
      prompts = prompts.filter(
        (prompt) => prompt.createdAt <= criteria.createdBefore!,
      );
    } // 排序
    if (criteria.sortBy) {
      prompts.sort((a, b) => {
        const order = criteria.sortOrder === 'desc' ? -1 : 1;
        switch (criteria.sortBy) {
          case 'createdAt':
            return (a.createdAt.getTime() - b.createdAt.getTime()) * order;
          case 'updatedAt':
            return (a.updatedAt.getTime() - b.updatedAt.getTime()) * order;
          case 'usageCount':
            return (a.usageCount - b.usageCount) * order;
          case 'title':
            return a.title.localeCompare(b.title) * order;
          default:
            return 0;
        }
      });
    }
    const total = prompts.length; // 分页
    if (criteria.offset !== undefined || criteria.limit !== undefined) {
      const offset = criteria.offset || 0;
      const limit = criteria.limit || prompts.length;
      prompts = prompts.slice(offset, offset + limit);
    }
    return { prompts, total };
  } // 获取统计信息
  getStats(): {
    total: number;
    byCategory: Record<PromptCategory, number>;
    byDifficulty: Record<PromptDifficulty, number>;
    byStatus: Record<PromptStatus, number>;
    topTags: Array<{ tag: string; count: number }>;
  } {
    const byCategory = {} as Record<PromptCategory, number>;
    const byDifficulty = {} as Record<PromptDifficulty, number>;
    const byStatus = {} as Record<PromptStatus, number>; // 初始化统计对象
    Object.values(PromptCategory).forEach((category) => {
      byCategory[category] = 0;
    });
    Object.values(PromptDifficulty).forEach((difficulty) => {
      byDifficulty[difficulty] = 0;
    });
    Object.values(PromptStatus).forEach((status) => {
      byStatus[status] = 0;
    }); // 标签统计
    const tagCounts: Record<string, number> = {}; // 遍历所有提示词进行统计
    this.prompts.forEach((prompt) => {
      byCategory[prompt.category]++;
      byDifficulty[prompt.difficulty]++;
      byStatus[prompt.status]++;
      prompt.tags.forEach((tag) => {
        tagCounts[tag] = (tagCounts[tag] || 0) + 1;
      });
    }); // 获取热门标签
    const topTags = Object.entries(tagCounts)
      .map(([tag, count]) => ({
        tag,
        count,
      }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 10);
    return {
      total: this.prompts.size,
      byCategory,
      byDifficulty,
      byStatus,
      topTags,
    };
  } // 记录使用次数
  recordUsage(id: string): boolean {
    const prompt = this.prompts.get(id);
    if (!prompt) {
      return false;
    }
    prompt.usageCount++;
    prompt.lastUsedAt = new Date();
    return true;
  } // 批量更新状态
  bulkUpdateStatus(
    ids: string[],
    status: PromptStatus,
  ): { updated: number; failed: number } {
    let updated = 0;
    let failed = 0;
    ids.forEach((id) => {
      try {
        const result = this.updatePrompt(id, { status });
        if (result) {
          updated++;
        } else {
          failed++;
        }
      } catch (error) {
        failed++;
      }
    });
    return { updated, failed };
  } // 导出提示词
  exportPrompts(format: 'json' | 'csv' | 'text'): string {
    const prompts = Array.from(this.prompts.values());
    switch (format) {
      case 'json':
        return JSON.stringify(prompts, null, 2);
      case 'csv':
        const headers = [
          'ID',
          'Title',
          'Category',
          'Difficulty',
          'Status',
          'Usage Count',
        ];
        const rows = prompts.map((p) => [
          p.id,
          `"${p.title.replace(/"/g, '""')}"`,
          p.category,
          p.difficulty,
          p.status,
          p.usageCount,
        ]);
        return [headers.join(','), ...rows.map((row) => row.join(','))].join(
          '\n',
        );
      case 'text':
        return prompts
          .map(
            (p) =>
              `# ${p.title}\n` +
              `ID: ${p.id}\n` +
              `Category: ${p.category}\n` +
              `Difficulty: ${p.difficulty}\n` +
              `Status: ${p.status}\n` +
              `Usage: ${p.usageCount}\n` +
              `---\n` +
              `${p.content}\n` +
              `---\n`,
          )
          .join('\n');
      default:
        throw new Error(`Unsupported export format: ${format}`);
    }
  }
} // ==================== 测试类 ====================
class PromptServiceTests {
  private promptService: PromptService;

  constructor() {
    this.promptService = new PromptService();
  }

  async runAllTests(): Promise<boolean> {
    console.log('🚀 Running Prompt Service Tests...\n');
    try {
      await this.testCreatePrompt();
      await this.testGetPrompt();
      await this.testUpdatePrompt();
      await this.testDeletePrompt();
      console.log('\n✅ All tests passed successfully!');
      return true;
    } catch (error) {
      console.error('\n❌ Test failed:', error);
      return false;
    }
  }

  private async testCreatePrompt(): Promise<void> {
    console.log('📝 Testing createPrompt...'); // 测试正常创建
    const createData: PromptCreateData = {
      title: '如何写一篇优秀的博客文章',
      content:
        '请帮我写一篇关于{topic}的博客文章，要求{tone}风格，长度大约{words}字',
      description: '用于生成博客文章的提示词模板',
      category: PromptCategory.CREATIVE,
      difficulty: PromptDifficulty.INTERMEDIATE,
      tags: ['writing', 'blog', 'content'],
      authorId: 'user_123',
      variables: [
        {
          description: '文章主题',
          type: VariableType.TEXT,
          required: true,
          validationRules: [
            {
              type: ValidationType.MIN_LENGTH,
              value: 3,
              message: '主题至少需要3个字符',
            },
          ],
        },
        {
          description: '写作风格',
          type: VariableType.SELECT,
          required: false,
          defaultValue: 'professional',
        },
        {
          description: '文章字数',
          type: VariableType.NUMBER,
          required: true,
          validationRules: [
            {
              type: ValidationType.MIN_VALUE,
              value: 300,
              message: '字数至少300字',
            },
          ],
        },
      ],
      examples: [
        'topic=人工智能，tone=专业，words=800',
        'topic=旅行，tone=轻松，words=500',
      ],
    };
    const prompt = this.promptService.createPrompt(createData); // 验证创建结果
    if (!prompt.id) throw new Error('Prompt should have an ID');
    if (prompt.title !== createData.title) throw new Error('Title mismatch');
    if (prompt.status !== PromptStatus.DRAFT)
      throw new Error('Should be in DRAFT status');
    if (prompt.version !== 1) throw new Error('Initial version should be 1');
    if (prompt.authorId !== 'user_123') throw new Error('Author ID mismatch');
    if (prompt.tags.length !== 3) throw new Error('Tags count mismatch');
    if (!prompt.variables || prompt.variables.length !== 3)
      throw new Error('Variables count mismatch');
    console.log('  ✅ createPrompt - Basic creation passed'); // 测试验证逻辑
    try {
      this.promptService.createPrompt({
        ...createData,
        title: '', // 空标题
      });
      throw new Error('Should throw error for empty title');
    } catch (error: any) {
      if (!error.message.includes('Prompt title is required')) {
        throw new Error('Wrong error message for empty title');
      }
    }
    try {
      this.promptService.createPrompt({
        ...createData,
        content: '', // 空内容
      });
      throw new Error('Should throw error for empty content');
    } catch (error: any) {
      if (!error.message.includes('Prompt content is required')) {
        throw new Error('Wrong error message for empty content');
      }
    }
    console.log('  ✅ createPrompt - Validation passed'); // 创建更多测试数据
    for (let i = 0; i < 20; i++) {
      this.promptService.createPrompt({
        title: `测试提示词 ${i + 1}`,
        content: `这是第 ${i + 1} 个测试提示词的内容`,
        description: `描述 ${i + 1}`,
        category:
          i % 2 === 0 ? PromptCategory.TECHNICAL : PromptCategory.BUSINESS,
        difficulty:
          i % 3 === 0
            ? PromptDifficulty.BEGINNER
            : i % 3 === 1
            ? PromptDifficulty.INTERMEDIATE
            : PromptDifficulty.ADVANCED,
        tags: i % 2 === 0 ? ['coding', 'ai'] : ['marketing', 'sales'],
        authorId: `user_${i % 5}`,
      });
    }
    console.log('  ✅ createPrompt - Bulk creation passed');
  }

  private async testGetPrompt(): Promise<void> {
    console.log('\n🔍 Testing getPrompt...'); // 创建测试提示词
    const createData: PromptCreateData = {
      title: '测试获取提示词',
      content: '这是一个用于测试获取功能的提示词',
      description: '测试描述',
      category: PromptCategory.GENERAL,
      difficulty: PromptDifficulty.BEGINNER,
      tags: ['test', 'get'],
      authorId: 'test_user',
    };
    const createdPrompt = this.promptService.createPrompt(createData); // 测试获取存在的提示词
    const retrievedPrompt = this.promptService.getPrompt(createdPrompt.id);
    if (!retrievedPrompt) throw new Error('Should retrieve existing prompt');
    if (retrievedPrompt.id !== createdPrompt.id)
      throw new Error('Retrieved prompt ID mismatch');
    if (retrievedPrompt.title !== createdPrompt.title)
      throw new Error('Retrieved prompt title mismatch');
    console.log('  ✅ getPrompt - Existing prompt retrieval passed'); // 测试获取不存在的提示词
    const nonExistentPrompt = this.promptService.getPrompt('non_existent_id');
    if (nonExistentPrompt !== undefined)
      throw new Error('Should return undefined for non-existent prompt');
    console.log('  ✅ getPrompt - Non-existent prompt handling passed'); // 测试批量获取
    const prompts = Array.from({ length: 5 }, (_, i) =>
      this.promptService.createPrompt({
        title: `批量测试 ${i}`,
        content: `内容 ${i}`,
        description: `描述 ${i}`,
        category: PromptCategory.EDUCATION,
        difficulty: PromptDifficulty.INTERMEDIATE,
        tags: [`tag${i}`],
        authorId: 'batch_user',
      }),
    );
    const ids = prompts.map((p) => p.id);
    const retrievedPrompts = this.promptService.getPrompts(ids);
    if (retrievedPrompts.length !== 5)
      throw new Error('Should retrieve all 5 prompts'); // 测试混合存在和不存在的ID
    const mixedIds = [...ids, 'non_existent_1', 'non_existent_2'];
    const mixedResults = this.promptService.getPrompts(mixedIds);
    if (mixedResults.length !== 5)
      throw new Error('Should only retrieve existing prompts');
    console.log('  ✅ getPrompt - Batch retrieval passed');
  }

  private async testUpdatePrompt(): Promise<void> {
    console.log('\n✏️ Testing updatePrompt...'); // 创建测试提示词
    const createData: PromptCreateData = {
      title: '原始标题',
      content: '原始内容',
      description: '原始描述',
      category: PromptCategory.GENERAL,
      difficulty: PromptDifficulty.BEGINNER,
      tags: ['original'],
      authorId: 'update_user',
    };
    const createdPrompt = this.promptService.createPrompt(createData); // 测试正常更新
    const updateData: PromptUpdateData = {
      title: '更新后的标题',
      content: '更新后的内容',
      description: '更新后的描述',
      category: PromptCategory.TECHNICAL,
      difficulty: PromptDifficulty.INTERMEDIATE,
      tags: ['updated', 'technical'],
      status: PromptStatus.APPROVED,
    };
    const updatedPrompt = this.promptService.updatePrompt(
      createdPrompt.id,
      updateData,
    );
    if (!updatedPrompt) throw new Error('Should update existing prompt');
    if (updatedPrompt.title !== updateData.title)
      throw new Error('Title not updated');
    if (updatedPrompt.content !== updateData.content)
      throw new Error('Content not updated');
    if (updatedPrompt.category !== updateData.category)
      throw new Error('Category not updated');
    if (updatedPrompt.version !== 2)
      throw new Error('Version should increment to 2');
    if (updatedPrompt.status !== PromptStatus.APPROVED)
      throw new Error('Status not updated');
    console.log('  ✅ updatePrompt - Basic update passed'); // 测试部分更新
    const partialUpdate: PromptUpdateData = { title: '再次更新的标题' };
    const partiallyUpdated = this.promptService.updatePrompt(
      createdPrompt.id,
      partialUpdate,
    );
    if (!partiallyUpdated) throw new Error('Should partially update prompt');
    if (partiallyUpdated.title !== partialUpdate.title)
      throw new Error('Partial title update failed');
    if (partiallyUpdated.content !== updateData.content)
      throw new Error('Content should remain unchanged');
    if (partiallyUpdated.version !== 3)
      throw new Error('Version should increment to 3');
    console.log('  ✅ updatePrompt - Partial update passed'); // 测试验证逻辑
    try {
      this.promptService.updatePrompt(createdPrompt.id, { title: '' });
      throw new Error('Should throw error for empty title');
    } catch (error: any) {
      if (!error.message.includes('Prompt title cannot be empty')) {
        throw new Error('Wrong error message for empty title in update');
      }
    }
    try {
      this.promptService.updatePrompt(createdPrompt.id, { content: '' });
      throw new Error('Should throw error for empty content');
    } catch (error: any) {
      if (!error.message.includes('Prompt content cannot be empty')) {
        throw new Error('Wrong error message for empty content in update');
      }
    }
    console.log('  ✅ updatePrompt - Validation passed'); // 测试更新不存在的提示词
    const nonExistentUpdate = this.promptService.updatePrompt(
      'non_existent_id',
      { title: 'test' },
    );
    if (nonExistentUpdate !== undefined)
      throw new Error('Should return undefined for non-existent prompt');
    console.log('  ✅ updatePrompt - Non-existent prompt handling passed');
  }

  private async testDeletePrompt(): Promise<void> {
    console.log('\n🗑️ Testing deletePrompt...'); // 创建测试提示词
    const prompts = Array.from({ length: 5 }, (_, i) =>
      this.promptService.createPrompt({
        title: `删除测试 ${i}`,
        content: `内容 ${i}`,
        description: `描述 ${i}`,
        category: PromptCategory.ENTERTAINMENT,
        difficulty: PromptDifficulty.BEGINNER,
        tags: [`delete${i}`],
        authorId: 'delete_user',
      }),
    );
    const promptToDelete = prompts[0]; // 测试删除存在的提示词
    const deleteResult = this.promptService.deletePrompt(promptToDelete.id);
    if (!deleteResult) throw new Error('Should delete existing prompt');
    const afterDelete = this.promptService.getPrompt(promptToDelete.id);
    if (afterDelete !== undefined) throw new Error('Prompt should be deleted');
    console.log('  ✅ deletePrompt - Existing prompt deletion passed'); // 测试删除不存在的提示词
    const deleteNonExistent =
      this.promptService.deletePrompt('non_existent_id');
    if (deleteNonExistent)
      throw new Error('Should return false for non-existent prompt');
    console.log('  ✅ deletePrompt - Non-existent prompt handling passed'); // 测试批量删除
    const idsToDelete = prompts.slice(1, 4).map((p) => p.id);
    const batchResult = this.promptService.deletePrompts([
      ...idsToDelete,
      'non_existent_id',
    ]);
    if (batchResult.deleted !== 3)
      throw new Error(
        `Should delete 3 prompts, deleted: ${batchResult.deleted}`,
      );
    if (batchResult.failed !== 1)
      throw new Error(
        `Should have 1 failed deletion, failed: ${batchResult.failed}`,
      );
    console.log('  ✅ deletePrompt - Batch deletion passed');
  }
}
