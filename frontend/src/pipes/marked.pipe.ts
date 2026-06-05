import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { I18NextEagerPipe } from "angular-i18next";
import hljs from "highlight.js";
import * as markedObjs from 'marked';
import { katexExtends } from 'src/utils/render-LaTeX-Markdown';
const marked = markedObjs.marked;

@Pipe({
  name: 'markedPipe',
  standalone: true,
})

export class MarkedPipe implements PipeTransform {
  constructor(
    private sanitizer: DomSanitizer,
    private i18n: I18NextEagerPipe,
  ) { }

  transform(content: string): Promise<string> {
    const { extensions } = katexExtends();
    // 自定义 marked 解析规则，禁止单 `~` 被解析为删除线
    const delRule: any = {
      name: 'no-single-tilde',
      level: 'inline',
      start(src) {
        return src.match(/~[^~]/)?.index; // 查找单个 `~`
      },
      tokenizer(src, tokens) {
        const match = src.match(/^~([^~]+)~/);
        if (match) {
          return {
            type: 'text',
            raw: match[0],
            text: match[0], // 让 `~xxx~` 保持原样
          };
        }
        return undefined;
      },
    };
    extensions.push(delRule);
  const initRenderer = () => {
      let renderer = new marked.Renderer();
      let codeBlockCounter = 0;
      renderer.code = (code: any, language: any) => {
        // 使用highlight.js进行代码高亮
        const validLanguage = hljs.getLanguage(language) ? language : 'plaintext';
        const highlightedCode = hljs.highlight(validLanguage, code).value;

        // 分割代码为多行
        let lines = highlightedCode.split('\n');

        // 若language存在，为每一行添加行号
        let numberedCode = language
          ? lines
            .map((line: any, index: any) => {
              return `<span class="line-numbers">${index + 1}</span> ${line}`;
            })
            .join('\n')
          : highlightedCode;

        // <code>标签可能表示代码，也可能表示表格。通过language的值是否存在进行区分
        let customCodeStyle = language
          ? `${language} custom-code-content`
          : `table-code`;

        const copiedText = this.i18n.transform('replicated_tip');
        const copyText = this.i18n.transform('base_copy');
        // 如果language存在，返回带有语言类型、复制图标和行号的代码块；反之，不展示语言类型、复制图标和行号
        let result = language
          ? `<pre class="code-wrapper">
           <div class="code-header">
           <span class="language-type">${language}</span>
           <span class="copy-container-markdown"><span class="copy-success-tooltip">${copiedText}</span><span class="copy-icon copy-${language}-${codeBlockCounter}" data-code="${encodeURIComponent(
            code
          )}">${copyText}</span></span>
           </div>
           <code class="${customCodeStyle}">${numberedCode}</code>
           </pre>`
          : `<pre class="not-code-wrapper">
           <code class="${customCodeStyle}">${numberedCode}</code>
           </pre>`;

        codeBlockCounter++;
        return result;
      };

      renderer.table = (header: string, body: string) => {
        return `<div class="custom-table-container">
        <table class="custom-table-style">
          <thead>${header}</thead>
          <tbody>${body}</tbody>
        </table>
      </div>`;
      };

      renderer.link = (href, title, text) => {
        return `<a href="${href}" target="_blank" class="custom-link" rel="noopener noreferrer">${text}</a>`;
      };
      return renderer;
    }
    marked.use({ extensions });
    marked.use({ renderer: initRenderer(), breaks: true });
    const markedRe = marked(content.replaceAll(/\\/g, "\\\\"));
    const parser = new DOMParser();
    const doms = parser.parseFromString(markedRe, 'text/html');
    return new Promise((resolve) => {
      resolve(doms.body.innerHTML);
    })
  }
}
