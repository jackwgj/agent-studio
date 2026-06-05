import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';

export interface CrumbItem {
  label: string;
  clickFn?: () => void;
}

export interface CustomHeaderParam {
  title: string;
  img?: string;
  tags?: { label: string; color?: string }[];
  operators?: any[];
  crumb: Array<CrumbItem>;
  isCreate?: boolean;
  status?: {
    icon: string;
    color: string;
    labelKey: string;
  };
}
