/** 预览内容的渲染类型。 */
export type PreviewType = 'url' | 'md' | 'txt' | 'unsupported';

/** 打开一次预览所需的全部信息。 */
export interface PreviewSource {
  type: PreviewType;
  /** 面板标题：文件用文件名，网址用域名或完整地址 */
  title: string;
  /** 网址预览的目标地址 */
  url?: string;
  /** 文件预览：所属会话 id（走后端代理取内容，无跨域与鉴权问题） */
  conversationId?: string;
  /** 文件预览：对象 key */
  objectKey?: string;
  /** 文件预览：原始文件名 */
  fileName?: string;
  size?: number;
  mediaType?: string;
}

/** 面板内部使用的状态，key 用于强制重建 iframe / 重新取数。 */
export interface PreviewState extends PreviewSource {
  key: string;
}

export const PREVIEW_TEXT_EXTENSIONS = ['md', 'markdown', 'txt', 'log'] as const;

/** 判断文件名是否属于可预览的纯文本类型，返回对应的渲染类型。 */
export function resolvePreviewType(fileNameOrUrl: string): PreviewType {
  const raw = (fileNameOrUrl || '').split(/[?#]/)[0];
  const ext = raw.split('.').pop()?.toLowerCase() ?? '';
  if (ext === 'md' || ext === 'markdown') {
    return 'md';
  }
  if (ext === 'txt' || ext === 'log') {
    return 'txt';
  }
  return 'url';
}

/** 从网址中提取展示用的标题（host + 端口）。 */
export function urlToTitle(url: string): string {
  try {
    const parsed = new URL(url);
    return parsed.host || url;
  } catch {
    return url;
  }
}

/**
 * 由「附件 / 产物」构造预览源。
 * 文本类走 md/txt 渲染，其他类型暂不支持预览（面板内提供下载入口）。
 */
export function buildFilePreviewSource(params: {
  fileName: string;
  conversationId?: string;
  objectKey?: string;
  size?: number;
  mediaType?: string;
}): PreviewSource {
  const type = resolvePreviewType(params.fileName);
  const previewType: PreviewType = type === 'url' ? 'unsupported' : type;
  return {
    type: previewType,
    title: params.fileName,
    url: undefined,
    conversationId: params.conversationId,
    objectKey: params.objectKey,
    fileName: params.fileName,
    size: params.size,
    mediaType: params.mediaType,
  };
}
