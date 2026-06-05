import katex from 'katex';

const inlineRule =
  /^(\${1,2})(?!\$)((?:\\.|[^\\\n])*?(?:\\.|[^\\\n\$]))\1(?=[\s?!\.,:？！。，：]|$)/;
const blockRule = /^(\${1,2})\n((?:\\[^]|[^\\])+?)\n\1(?:\n|$)/;

export function katexExtends(options = {}) {
  const extensions = [
    inlineKatex(options, createRenderer(options, false)),
    blockKatex(options, createRenderer(options, true)),
  ];
  return {
    extensions,
  };
}

function createRenderer(options, newlineAfter) {
  return (token) => {
    token.text = token.text.replaceAll(/[\\]+/g, '\\');
    token.text = token.text.replaceAll('\\ ', '\\\\ ');
    let re;
    try {
      const fullHtml = katex.renderToString(token.text, {
        ...options,
        displayMode: token.displayMode,
        throwOnError: false,
      });

      // 只保留katex-mathml，不展示katex-html
      const match = fullHtml.match(/<span class="katex-mathml">.*?<\/span>/s);
      const mathmlOnly = match ? match[0] : token.text;

      re = mathmlOnly + (newlineAfter ? '\n' : '');
    } catch (err) {
      re = token.text;
    }
    return re;
  };
}

function inlineKatex(options, renderer) {
  return {
    name: 'inlineKatex',
    level: 'inline',
    start(src: string): void | number {
      let re: number;
      let index: number;
      let indexSrc = src;

      while (indexSrc) {
        index = indexSrc.indexOf('$');
        if (index === -1) {
          return re;
        }

        if (index === 0 || indexSrc.charAt(index - 1) === ' ') {
          const possibleKatex = indexSrc.substring(index);

          if (possibleKatex.match(inlineRule)) {
            re = index;
            return re;
          }
        }

        indexSrc = indexSrc.substring(index + 1).replace(/^\$+/, '');
      }
      return re;
    },
    tokenizer(src: string) {
      let re: any;
      const match = src.match(inlineRule);
      if (match) {
        re = {
          type: 'inlineKatex',
          raw: match[0],
          text: match[2].trim(),
          displayMode: match[1].length === 2,
        };
      }
      return re;
    },
    renderer,
  };
}

function blockKatex(options, renderer) {
  return {
    name: 'blockKatex',
    level: 'block',
    tokenizer(src: string) {
      let re: any;
      const match = src.match(blockRule);
      if (match) {
        re = {
          type: 'blockKatex',
          raw: match[0],
          text: match[2].trim(),
          displayMode: match[1].length === 2,
        };
      }
      return re;
    },
    renderer,
  };
}
