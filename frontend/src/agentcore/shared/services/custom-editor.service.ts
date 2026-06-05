import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Unzipped, unzipSync } from 'fflate';
import { firstValueFrom, lastValueFrom } from 'rxjs';

import { CustomTreeNode } from '@agentcore/constants/skill.model';

@Injectable()
export class CustomEditorService {
  extensionToLanguageMap = {
    // Web 核心
    js: 'javascript',
    mjs: 'javascript',
    cjs: 'javascript',
    ts: 'typescript',
    jsx: 'javascript', // Monaco 默认用 javascript 处理 jsx
    tsx: 'typescript', // Monaco 默认用 typescript 处理 tsx
    html: 'html',
    htm: 'html',
    css: 'css',
    scss: 'scss',
    less: 'less',
    json: 'json',
    webmanifest: 'json',

    // 标记与文档
    md: 'markdown',
    markdown: 'markdown',
    xml: 'xml',
    svg: 'xml',
    log: 'plaintext',
    txt: 'plaintext',

    // 编程语言
    py: 'python',
    pyw: 'python',
    java: 'java',
    c: 'c',
    h: 'c',
    cpp: 'cpp',
    hpp: 'cpp',
    cc: 'cpp',
    cs: 'csharp',
    go: 'go',
    rs: 'rust',
    php: 'php',
    rb: 'ruby',
    sql: 'sql',

    // 脚本与配置文件
    sh: 'shell',
    bash: 'shell',
    bat: 'bat',
    ps1: 'powershell',
    yaml: 'yaml',
    yml: 'yaml',
    toml: 'toml',
    ini: 'ini',
    dockerfile: 'dockerfile',
    editorconfig: 'ini',
    gitignore: 'plaintext',
    env: 'properties', // .env 通常按 properties 高亮最合适

    // 现代框架 (注意：Monaco 核心不内置 vue/svelte，通常需插件或设为 html)
    vue: 'html',
    svelte: 'html',
  };
  supportIconMap = ['md', 'gitignore', 'txt'].reduce((s, c) => {
    s[c] = true;
    return s;
  }, {});

  constructor(private http: HttpClient) {}

  async downloadAndUnzip(url: string): Promise<Array<CustomTreeNode>> {
    try {
      // 1. 等待下载完成 (ArrayBuffer)
      const buffer = await lastValueFrom(this.http.get(url, { responseType: 'arraybuffer' }));
      // --- 强制检查数据真面目 ---
      const uint8 = new Uint8Array(buffer);
      const text = new TextDecoder().decode(uint8.slice(0, 100));
      // 2. 同步解压 (使用 unzipSync 替代 unzip)
      // 这行代码执行完后，data 立即就有值了，不需要回调函数
      const data: Unzipped = unzipSync(new Uint8Array(buffer));
      const tree = this.convertToTree(data);
      return tree;
    } catch (err) {
      // eslint-disable-next-line no-console
      console.warn(err);
      return [];
    }
  }

  /**
   * 主入口函数：将解压后的数据转换为树形结构
   */
  convertToTree(data: Unzipped): CustomTreeNode[] {
    const root: CustomTreeNode[] = [];

    Object.entries(data).forEach(([rawPath, content]: any) => {
      const { segments, isExplicitDir } = this.parsePath(rawPath);
      if (segments.length > 0) {
        this.buildNodeRecursive(root, segments, isExplicitDir, content);
      }
    });

    return this.sortTree(root);
  }

  /**
   * 1. 路径解析：处理 Windows/Linux 分隔符并提取片段
   */
  parsePath(rawPath: string) {
    // 统一替换反斜杠并去除前后空格
    const normalizedPath = rawPath.replace(/\\/g, '/').trim();
    return {
      segments: normalizedPath.split('/').filter(s => s.length > 0),
      isExplicitDir: normalizedPath.endsWith('/'),
    };
  }

  /**
   * 2. 递归/迭代构建节点：处理每一层级的逻辑
   */
  buildNodeRecursive(currentLevel: CustomTreeNode[], segments: string[], isExplicitDir: boolean, content: Uint8Array): void {
    let tempLevel = currentLevel;
    let currentAccumulatedPath = ''; // 用于记录累加的路径

    segments.forEach((segment, index) => {
      const isLast = index === segments.length - 1;
      const shouldBeDir = !isLast || isExplicitDir;

      // 构建当前节点的完整路径
      currentAccumulatedPath = currentAccumulatedPath ? `${currentAccumulatedPath}/${segment}` : segment;

      let node = tempLevel.find(n => n.label === segment);

      if (node) {
        this.ensureDirectoryType(node, shouldBeDir);
        // 即使节点已存在，也可以确保 path 正确（可选）
        node.path = currentAccumulatedPath;
      } else {
        node = this.createBaseNode(segment, shouldBeDir, content, currentAccumulatedPath);
        tempLevel.push(node);
      }

      // 移动到下一层级
      if (node.isDir && node.children) {
        tempLevel = node.children;
      }
    });
  }

  /**
   * 3. 节点创建：根据类型初始化属性
   */
  createBaseNode(label: string, isDir: boolean, content: Uint8Array, path: string): CustomTreeNode {
    if (isDir) {
      return {
        label,
        isDir: true,
        children: [],
        fileIcon: 'folder',
        path,
      };
    }

    const { language, fileType } = this.getMonacoLanguage(label);
    return {
      label,
      isDir: false,
      content,
      fileIcon: fileType,
      language,
      path,
    };
  }

  /**
   * 4. 类型兼容：确保中间路径节点具备文件夹属性
   */
  ensureDirectoryType(node: CustomTreeNode, shouldBeDir: boolean): void {
    if (shouldBeDir && !node.isDir) {
      node.isDir = true;
      node.children = node.children || [];
      node.fileIcon = 'folder';
      delete node.content; // 清除可能误存的文件内容
    }
  }

  getMonacoLanguage(fileName) {
    // 1. 获取后缀名并转为小写
    const ext = fileName.split('.').pop().toLowerCase();
    // 2. 从 Map 中查找，找不到则返回 plaintext
    return {
      language: this.extensionToLanguageMap[ext] || null,
      fileType: this.supportIconMap[ext] ? ext : 'unknown',
    };
  }

  /**
   * 排序：文件夹在前，文件在后，同类按字母序
   */
  sortTree(nodes: CustomTreeNode[]): CustomTreeNode[] {
    nodes.forEach(node => {
      if (node.children) {
        this.sortTree(node.children);
      }
    });

    return nodes.sort((a, b) => {
      const aIsDir = a.children ? 1 : 0;
      const bIsDir = b.children ? 1 : 0;
      if (aIsDir !== bIsDir) {
        return bIsDir - aIsDir;
      }
      return a.label.localeCompare(b.label);
    });
  }

  async downloadAndRenameZip(url: string, customName: string) {
    try {
      // 1. 以 Blob 形式下载整个 ZIP 文件
      // 使用 firstValueFrom 将 Observable 转为 Promise (同步风格写法)
      const blob = await firstValueFrom(this.http.get(url, { responseType: 'blob' }));

      // 2. 创建一个指向该 Blob 的 URL
      const objectUrl = window.URL.createObjectURL(blob);

      // 3. 动态创建 <a> 标签触发下载
      const link = document.createElement('a');
      link.href = objectUrl;

      // 4. 这里设置你自定义的外层文件名
      // 确保包含 .zip 后缀
      link.download = customName.endsWith('.zip') ? customName : `${customName}.zip`;

      // 5. 模拟点击并销毁
      link.click();
      window.URL.revokeObjectURL(objectUrl);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.warn(error);
    }
  }
}
