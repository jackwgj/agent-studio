import { ElementRef } from "@angular/core";
import { FormGroup, ValidationErrors } from "@angular/forms";
import { StorageService } from '@shared/services/cfdata.service';
import {
  DEFAULT_FONT_STYLE,
  VARIANT_REG,
  XLSX_FILE_TYPE
} from "@constants/exp-tmpl-config.const";
import { IVariant } from "@interfaces/prompt";
import {
  ICustomError,
  IErrorMessage,
  IInternalError
} from "@interfaces/prompt/common.interface";
import dayjs from "dayjs";
import { unionWith } from "lodash";
import i18next from "i18next";
import { FormateTimePipe } from "src/pipes/formate-time.pipe";
import { ConsoleFrameworkService } from "@core/services";

export class CommonUtils {
  private static formateTimePipe: FormateTimePipe = new FormateTimePipe();
  private static consoleFrameworkService: ConsoleFrameworkService = new ConsoleFrameworkService();

  static dateFormatter(date: Date): string {
    return dayjs(date).format("YYYY/MM/DD HH:mm");
  }

  static buildErrorMessage(error: IErrorMessage): string {
    if (error.status === 500) {
      const { data } = error as IInternalError;
      return data.error;
    }

    const { message } = error as ICustomError;
    return message;
  }

  /** 移除变量 key 两边的花括号 */
  static removeCurlyBrackets(v: IVariant): IVariant {
    const isMatch = v.key.match(VARIANT_REG);

    if (isMatch) {
      return {
        ...v,
        key: v.key.replace(/{{|}}/g, "")
      };
    }

    return v;
  }

  static addCurlyBrackets(v: IVariant): IVariant {
    const isMatch = v.key.match(VARIANT_REG);

    if (!isMatch) {
      return {
        ...v,
        key: `{{${v.key}}}`
      };
    }

    return v;
  }

  /** 下载 response type 为 blob 的请求获取的文件 */
  static downloadFile(
    blob: Blob,
    contentDisposition: string,
    useDisposition = false
  ) {
    let fileName = "";

    if (useDisposition) {
      fileName = contentDisposition;
    } else {
      const pattern = /filename\*?=['"]?(?:UTF-\d['"]*)?([^;\r\n"']*)['"]?;?/;
      const result = pattern.exec(contentDisposition);
      fileName = decodeURI((result && result[1]) || "");
    }

    const downloadElement = document.createElement("a");
    const href = window.URL.createObjectURL(blob);

    downloadElement.style.display = "none";
    downloadElement.href = href;
    downloadElement.download = fileName;
    document.body.appendChild(downloadElement);
    downloadElement.click();
    document.body.removeChild(downloadElement);

    /** 释放掉blob对象 */
    window.URL.revokeObjectURL(href);
  }

  /**
   * @param canvas canvas 对象
   * @param text 待计算宽度的字符串
   * @param font 该字符串的样式 (e.g. "bold 14px verdana")
   * @returns 该字符串的宽度, 向上取整
   */
  static getTextWidth(
    canvas: HTMLCanvasElement,
    text: string,
    font = DEFAULT_FONT_STYLE
  ): number {
    const context = canvas.getContext("2d");
    context.font = font;
    const metrics = context.measureText(text);

    return Math.ceil(metrics.width);
  }

  /**
   * 判断给定的值是否是 true(boolean) 或者 'true'(string)
   */
  static isTrueSet(val: any): boolean {
    if (typeof val === "boolean") {
      return val;
    }

    if (typeof val === "string") {
      return val?.toLowerCase() === "true";
    }

    return false;
  }

  /**
   * 将变量 IVariant 按照 key 值是否相等进行去重
   */
  static getUniqueVars(vars: IVariant[]): IVariant[] {
    return unionWith(vars, (a, b) => a.key === b.key);
  }

  /**
   * 判断文件是否为 xlsx 格式
   */
  static checkXLSXFileFormat(file: File): boolean {
    const fileExtension = file.name.split(".").pop().toLowerCase();

    if (fileExtension === "xlsx" && file.type === XLSX_FILE_TYPE) {
      return true;
    }

    return false;
  }

  /**
   * 在 str 后追加 appendstr，使得
   * 1. 若追加后的字符串长度小于等于 limit，则返回
   * 2. 若大于 limit，则将 str 超出部分截断舍弃，再与 appendstr 相加
   * @param str
   * @param appendstr
   * @param limit
   */
  static appendStrWithLenLimit(
    str: string,
    appendstr: string,
    limit: number
  ): string {
    const output = str + appendstr;

    if (output.length <= limit) {
      return output;
    }

    const excessLength = output.length - limit;

    return str.slice(0, str.length - excessLength) + appendstr;
  }

  // 文本表单值的转换
  static setInfoFormValue(infoItems, detail): void {
    infoItems.forEach((item) => {
      let value = detail[item.key];

      if (item.transfrom) {
        value = item.transfrom(item.key ? detail[item.key] : detail);
      }

      item.value = value ?? "--";
    });
  }

  static getLanguage = () => {
    const curLang =
      (this.consoleFrameworkService.dataService?.getLocale() as string) ||
      "zh-cn";
    return curLang;
  };

  // 清除对象中值为空的属性
  static clearEmpty = (obj: object) => {
    const dist = new Set();
    const _clearEmpty = (obj: object) => {
      if (dist.has(obj)) {
        return;
      }
      dist.add(obj);
      Object.keys(obj).forEach((key) => {
        if (obj[key] === undefined || obj[key] === null) {
          delete obj[key];
        } else if (typeof obj[key] === "object") {
          _clearEmpty(obj);
        }
      });
    };
    _clearEmpty(obj);
  };

  // 将对象属性转换为children的数组,用于树表
  static objToChildrenArr = (obj) => {
    const toChildrenArr = (obj, fatherKey) => {
      const childrenArr = [];
      Object.keys(obj).forEach((key) => {
        const value = obj[key];
        const isPureObject = value?.constructor?.name === "Object";
        const item: {
          id: string;
          name: string;
          value: string;
          children?: any[];
        } = {
          id: `${fatherKey}-${key}`,
          name: key,
          value: isPureObject ? "" : value
        };
        if (isPureObject) {
          item.children = toChildrenArr(value, key);
        }

        childrenArr.push(item);
      });

      return childrenArr;
    };

    return toChildrenArr(obj, "");
  };

  // 防抖
  static debounceS = (fn, timeout = 300) => {
    let timer = null;
    return (...args) => {
      clearTimeout(timer);
      timer = setTimeout(() => {
        fn(...args);
      }, timeout);
    };
  };

  /** 字符串转对象 */
  static stringToObject = (s: string) => {
    try {
      if (typeof s === "string") {
        return JSON.parse(s);
      }
      return s;
    } catch (error) {
      return {};
    }
  };

  /** 对象转字符串 */
  static objectToString = (o: unknown, n?: number) => {
    try {
      return JSON.stringify(o, null, n);
    } catch (error) {
      return "";
    }
  };

  /**
   * @param elementRef
   * @param formGroup
   * @param config
   * @returns 如果有error，返回true；没有返回 false
   */
  static checkAndFocusFirstError(
    elementRef: ElementRef<HTMLDivElement>,
    formGroup: FormGroup,
    config?: any
  ) {
    const formErrors: ValidationErrors | null = null;

    if (formErrors) {
      const firstError = Object.keys(formErrors)[0];

      (
        elementRef.nativeElement.querySelector(
          `[formControlName=${firstError}]`
        ) as HTMLInputElement | HTMLTextAreaElement
      )?.focus();

      return true;
    }

    return false;
  }

  static isNumber(value: any): value is number {
    return typeof value === "number";
  }

  static isNumberFinite(value: any): value is number {
    return this.isNumber(value) && Number.isFinite(value);
  }

  static isPositive(value: number): boolean {
    return value >= 0;
  }

  static isInteger(value: number): boolean {
    return value % 1 === 0;
  }

  static toDecimal(value: number, decimal: number): number {
    return Math.round(value * 10 ** decimal) / 10 ** decimal;
  }

  /**
   * 检查 `value` 是否是 `null` 或 `undefined`.
   *
   * isNil(null)
   * // => true
   *
   * isNil(void 0)
   * // => true
   *
   * isNil(NaN)
   * // => false
   */
  static isNil(value: any): boolean {
    return value == null;
  }

  static buildUrl(
    domain: string,
    config: {
      https: boolean;
      with3w: boolean;
    }
  ): string {
    const unSafeProtocal = "http";
    const safeProtocal = "https";

    return `${config.https ? safeProtocal : unSafeProtocal}://${
      config.with3w ? "www." : ""
    }${domain}.com`;
  }

  static buildSafeUrl(domain: string, with3w: boolean) {
    return this.buildUrl(domain, {
      https: true,
      with3w
    });
  }

  static buildUnSafeUrl(domain: string, with3w: boolean) {
    return this.buildUrl(domain, {
      https: false,
      with3w
    });
  }

  static random(max: number): number {
    return Math.floor(
      (crypto.getRandomValues(new Uint32Array(1))[0] / (0xffffffff + 1)) * max
    );
  }

  /** 将运行时间转化为合适单位 */
  static formatLatency(latency: number): string {
    if (latency < 1000) {
      return `${latency}ms`;
    } else if (latency < 60_000) {
      return `${(latency / 1000).toFixed(2)}s`;
    } else {
      return `${(latency / 60_000).toFixed(2)}min`;
    }
  }

  static getFormattedTime(time: any) {
    const year = time.getFullYear();
    const month = ("0" + (time.getMonth() + 1)).slice(-2);
    const day = ("0" + time.getDate()).slice(-2);
    const hours = ("0" + time.getHours()).slice(-2);
    const minutes = ("0" + time.getMinutes()).slice(-2);
    const seconds = ("0" + time.getSeconds()).slice(-2);
    return `${year}${month}${day}${hours}${minutes}${seconds}`;
  }

  public static getDateToString(dateStr: string): string {
    if (!dateStr) {
      return "-";
    }
    return this.formateTimePipe.transform(dateStr);
  }

  public static daysBetweenDates(date1, date2) {
    if (date1.getTime() - date2.getTime() > 0) {
      // 将日期转换为毫秒
      const millisecondsPerDay = 1000 * 60 * 60 * 24;
      const timeDiff = Math.abs(date1.getTime() - date2.getTime());
      // 计算天数差
      const diffDays = Math.ceil(timeDiff / millisecondsPerDay);
      return diffDays;
    }
    return null;
  }

  public static user_set_effTime = 7;

  /** 若插件支持流式输出，使用自定义变量覆盖历史输出变量列表 */
  static updateStreamingPluginOutputs(nodes: any[]) {
    return nodes.map((node) => {
      if (node.type === "Plugin" && node.configs?.streaming) {
        return {
          ...node,
          outputs: [
            {
              name: "raw_output",
              description: i18next.t("plugin_stream_output"),
              required: false,
              type: "string",
              value: {
                type: "literal",
                content: "",
                hint: ""
              },
              source: "user",
              isDisabled: false
            }
          ]
        };
      }
      return node;
    });
  }

  /**
   * @function isValidExt 校验文件后缀合法
   * @param { string } accept 形如 ‘.jpg,.png'
   * @param { File } file
   * @return { boolean } 是否合法
   */
  static isValidExt(accept: string, file: File): boolean {
    const allowSet = new Set(
      accept.split(",").map((s) => s.trim().toLowerCase().replace(/^\./, ""))
    );
    if (!file?.name) {
      return false;
    }
    const ext = file.name
      .slice(((file.name.lastIndexOf(".") - 1) >>> 0) + 2)
      .toLowerCase();
    return allowSet.has(ext);
  }

  static judeg_develop_space() {
    return (
      window.location &&
      window.location.href &&
      (window.location.href.endsWith("/home/develop-space") || window.location.href.indexOf("from=develop-space") > -1 || window.location.href.indexOf("/home/chat/") > -1 || window.location.href.indexOf("/mobile-index") > -1)
    );
  }

  static isPC() {
    let userAgentInfo = navigator.userAgent;
    let Agents = ["Android", "iPhone", "SymbianOS", "Windows Phone", "iPad", "iPod"];
    let flag = true;
    for (let v = 0; v < Agents.length; v++) {
      if (userAgentInfo.indexOf(Agents[v]) > 0) {
        flag = false;
        break;
      }
    }
    return flag;
  }

  static titleCase3(s: string) {
    return s.toLowerCase().split(/\s+/).map((item, index) => {
      return item.slice(0, 1).toUpperCase() + item.slice(1);
    }).join(" ");
  }

  // 为当前用户设置一个key值，主要功能时不在显示
  static setCurUserLocalStorage(key_name: string, status: Boolean) {
    const userInfo = StorageService.getLocalStorage("PROMPT_ENGINEERING_ME");
    const initModalSession =
      StorageService.getLocalStorage(key_name);
    let initModalSessionList = [];
    try {
      initModalSessionList = JSON.parse(initModalSession);
    } catch {
      initModalSessionList = [];
    }
    const initHomeModalStateList = initModalSessionList ?? [];
    const visited_list =
      initHomeModalStateList.filter((item) =>
        [false, true].includes(item[userInfo.userId])
      ) ?? [];
    if (visited_list.length === 0) {
      const nj = {};
      nj[userInfo.userId] = status;
      initHomeModalStateList.push(nj);
      StorageService.setLocalStorage(key_name, initHomeModalStateList);
    } else {
      visited_list.forEach((item) => {
        item[userInfo.userId] = status;
      });
      StorageService.setLocalStorage(key_name, visited_list);
    }
  }
}
