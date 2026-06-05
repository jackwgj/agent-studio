export function parseXMLToJson(xmlStr: string): EleJosn | null {
  try {
    const dom: Document | null = loadXML(xmlStr) as Document | null;
    if (dom) {
      return domToJson(dom);
    }
  } catch (e) {}
  return null;
}

//  xml字符串 转 xml
export function loadXML(xmlString) {
  let xmlDoc = null;
  // 判断浏览器的类型
  // 支持IE浏览器
  // @ts-expect-error: Unreachable code error
  if (!window.DOMParser && window.ActiveXObject) {
    // window.DOMParser 判断是否是非ie浏览器
    // eslint-disable-next-line no-inner-declarations
    let xmlDomVersions = [
      'MSXML.2.DOMDocument.6.0',
      'MSXML.2.DOMDocument.3.0',
      'Microsoft.XMLDOM',
    ];
    // eslint-disable-next-line no-inner-declarations
    for (let i = 0; i < xmlDomVersions.length; i++) {
      try {
        /* eslint-disable no-undef */
        // @ts-expect-error: Unreachable code error
        xmlDoc = new ActiveXObject(xmlDomVersions[i]);
        xmlDoc.async = false;
        xmlDoc.loadXML(xmlString); // loadXML方法载入xml字符串
        break;
      } catch (e) {}
    }
  }
  // 支持Mozilla浏览器
  else if (
    window.DOMParser &&
    document.implementation &&
    document.implementation.createDocument
  ) {
    try {
      /* DOMParser 对象解析 XML 文本并返回一个 XML Document 对象。
       * 要使用 DOMParser，使用不带参数的构造函数来实例化它，然后调用其 parseFromString() 方法
       * parseFromString(text, contentType) 参数text:要解析的 XML 标记 参数contentType文本的内容类型
       * 可能是 "text/xml" 、"application/xml" 或 "application/xhtml+xml" 中的一个。注意，不支持 "text/html"。
       */
      let domParser = new DOMParser();
      xmlDoc = domParser.parseFromString(xmlString, 'text/xml');
    } catch (e) {}
  } else {
    return null;
  }

  return xmlDoc;
}

export function domToJson(dom: Document) {
  const ret = {
    children: [],
  };
  for (let i = 0; i < dom.children.length; i++) {
    const node = dom.children[i];
    ret.children.push(eleToJson(node));
  }

  return ret;
}

interface EleJosn {
  nodeName?: string;
  children?: EleJosn[];
  attributes?: { [key in string]: string };
}

function eleToJson(ele: Element) {
  const ret: EleJosn = {
    nodeName: ele.nodeName,
  };
  for (let i = 0; i < ele.children.length; i++) {
    const node = ele.children[i];
    if (!ret.children) {
      ret.children = [];
    }
    ret.children.push(eleToJson(node));
  }
  ret.attributes = getAttribute(ele);
  return ret;
}

function getAttribute(ele: Element) {
  const ret: any = {};

  if (ele.tagName === 'enum') {
    ret.enum_value = ele.innerHTML;
  }
  if (ele.hasAttributes()) {
    const attrs = ele.attributes;
    for (let i = 0; i < attrs.length; i++) {
      const attr = attrs[i];
      ret[attr.name] = attr.value;
    }
    return ret;
  }

  return undefined;
}
