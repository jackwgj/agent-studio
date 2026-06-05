export enum FeedbackType {
  UPVOTE = 1,
  DOWNVOTE = -1,
}

export const messageSourceFileMap = new Map([
  ['doc','assets/knowledge-center/file/doc.svg'],
  ['docx','assets/knowledge-center/file/docx.svg'],
  ['pdf','assets/knowledge-center/file/pdf.svg'],
  ['pptx','assets/knowledge-center/file/pptx.svg'],
  ['ppt','assets/knowledge-center/file/ppt.svg'],
  ['xlsx','assets/knowledge-center/file/xlsx.svg'],
  ['xls','assets/knowledge-center/file/xls.svg'],
  ['csv','assets/knowledge-center/file/csv.svg'],
  ['wps','assets/knowledge-center/file/wps.svg'],
  ['png','assets/knowledge-center/file/png.svg'],
  ['jpg','assets/knowledge-center/file/jpg.svg'],
  ['jpeg','assets/knowledge-center/file/jpeg.svg'],
  ['bmp','assets/knowledge-center/file/bmp.svg'],
  ['gif','assets/knowledge-center/file/gif.svg'],
  ['tiff','assets/knowledge-center/file/tiff.svg'],
  ['tif','assets/knowledge-center/file/tif.svg'],
  ['webp','assets/knowledge-center/file/webp.svg'],
  ['pcx','assets/knowledge-center/file/pcx.svg'],
  ['ico','assets/knowledge-center/file/ico.svg'],
  ['psd','assets/knowledge-center/file/psd.svg'],
  ['dps','assets/knowledge-center/file/dps.svg'],
  ['et','assets/knowledge-center/file/et.svg'],
  ['txt','assets/knowledge-center/file/txt.svg'],
  ['ofd','assets/knowledge-center/file/ofd.svg'],
  ['md','assets/knowledge-center/file/md.svg'],
  ['html','assets/knowledge-center/file/html.svg'],
  ['mhtml','assets/knowledge-center/file/html.svg'],
  ['','assets/agent-center/images/mode-faq.svg'],
]);

// 获取文件名后缀
export function getFileExtension(filename) {
  const lastDotIndex = filename?.lastIndexOf('.');
  return filename?.slice((lastDotIndex - 1 >>> 0) + 2)?.toLowerCase() ?? '';
}
