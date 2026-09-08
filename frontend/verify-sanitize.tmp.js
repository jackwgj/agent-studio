const { JSDOM } = require('jsdom');
const createDOMPurify = require('dompurify');
const w = new JSDOM('').window;
const DOMPurify = createDOMPurify(w);
const dirty =
  '<style>.loading{position:fixed;top:0;width:100%;height:100%;background:#f4f7f9;z-index:9999}</style>' +
  '<div class="loading"><div class="title">供应链AI管理系统</div></div>' +
  '<p>正常段落</p><form action="/x"><input type="text"></form>';

const def = DOMPurify.sanitize(dirty);
const fixed = DOMPurify.sanitize(dirty, { FORBID_TAGS: ['style', 'form', 'input'] });

console.log('=== 默认配置（修复前行为） ===');
console.log('保留<style>:', def.includes('<style'), '| 保留 form:', def.includes('<form'));
console.log('=== 修复后配置 ===');
console.log('保留<style>:', fixed.includes('<style'), '| 保留 form:', fixed.includes('<form'), '| 保留.loading div(无样式无害):', fixed.includes('class="loading"'), '| 保留正常段落:', fixed.includes('正常段落'));
