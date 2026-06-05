/**
 * 是否超过最大文件数量
 *
 * @param {number[]} filenames - 文件名称
 * @returns {boolean} 是否超过最大文件数量
 */
function isExceedMaxFileQuantity(filenames) {
  const MAX_FILE_QUANTITY = 20;

  return filenames.length > MAX_FILE_QUANTITY;
}

export default {
  '*.{js,mjs,cjs,ts,mts,cts}': (filenames) => {
    if (isExceedMaxFileQuantity(filenames)) {
      return 'pnpm exec eslint --fix --cache --cache-location ./.cache/eslint/ --cache-strategy content ./';
    }

    return `pnpm exec eslint --fix --cache --cache-location ./.cache/eslint/ --cache-strategy content ${filenames.join(
      ' ',
    )}`;
  },
  '*.{scss,html}': (filenames) => {
    if (isExceedMaxFileQuantity(filenames)) {
      return 'pnpm exec stylelint --fix --cache --cache-location ./.cache/stylelint/ --cache-strategy content "**/*.{scss,html}"';
    }

    return `pnpm exec stylelint --fix --cache --cache-location ./.cache/stylelint/ --cache-strategy content ${filenames.join(
      ' ',
    )}`;
  },
};
