import { FormGroup } from "@angular/forms";

export interface SimpleModalConfig {
    className?: string;
    type?: 'success' | 'error' | 'warn' | 'prompt' | 'simple';
    title?: string;
    warn?: string;
    simpleText?: string[];
    confirmText?: string;
    basicInfoConfig?: {
      basicInfo: any;
      formfieldConfig: any;
    };
    dynamicConfig?: {
      formGroup: FormGroup;
      formModel: any;
      groupOption?;
    };
    callBackFn?: (context) => void;
  }
