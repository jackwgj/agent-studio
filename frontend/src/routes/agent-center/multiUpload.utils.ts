import { v4 as uuidV4 } from 'uuid';

export interface FileItem {
  fileId: string;
  name: string;
  progress: 'loading' | 'succeeded' | 'failed';
  type: string;
  url: string;
  controller: AbortController;
}

export function validateFileSize(
  file: File,
  isImage: boolean,
  maxImageSize: number = 5 * 1024 * 1024,
  maxFileSize: number = 60 * 1024 * 1024,
): string | null {
  const extension = file.name.split('.').pop()?.toLowerCase() || '';
  const validImageExtensions = ['png', 'jpeg', 'gif', 'webp', 'jpg', 'svg'];

  // 检查文件类型是否为图片
  if (isImage && !validImageExtensions.includes(extension)) {
    return 'Invalid image format';
  }

  // 检查文件大小
  const maxSize = isImage ? maxImageSize : maxFileSize;
  if (file.size > maxSize) {
    return isImage
      ? `image_size_cannot_exceed_5mb`
      : `file_size_cannot_exceed_128mb`;
  }

  return null;
}

export function createFileItem(file: File): FileItem {
  return {
    fileId: uuidV4(),
    name: file.name,
    progress: 'loading',
    type: file.type,
    url: file.name,
    controller: new AbortController(),
  };
}

export async function uploadFile(
  repoServ: any, // 替换为实际服务类型
  file: File,
  isImage: boolean,
  fileItem: FileItem,
): Promise<void> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('is_image', JSON.stringify(isImage));

  try {
    const res = await repoServ.uploadFile(formData, fileItem.controller.signal);
    fileItem.progress = 'succeeded';
    fileItem.url = res.url;
  } catch (error) {
    fileItem.progress = 'failed';
  }
}
