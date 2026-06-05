export function cdnAssetUrl(url:string): string {
  let prefix = window.AppWebPath;

  if (!prefix.startsWith('/')) {
    prefix = `/${prefix}`;
  }

  if (!prefix.endsWith('/')) {
    prefix = `${prefix}/`;
  }

  prefix = `${prefix}${url}`;

  return prefix;
}
