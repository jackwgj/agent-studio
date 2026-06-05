import { HttpErrorResponse } from '@angular/common/http';

/**
 * 各个服务对错误结构体的定义存在不一致，以通用为主，陆续兼容其他格式
 */

declare interface CustomErrorResponse extends HttpErrorResponse {
  error: {
    code?: string;
    message?: string;
    error_code?: string;
    error_msg?: string;
    error?: any; // 适配IAM 报错
  };
}

interface MessageConfig {
  success?: string | ((res: any) => string);
  fail?: string | ((res: CustomErrorResponse) => string);
  showFailMessage?: boolean; // 默认true
  customError?: boolean; // 默认false
}
