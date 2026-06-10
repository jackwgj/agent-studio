import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { ObjectManageService } from '@routes/object-manage/object-manage.service';
import { ObjectField, ObjectParentType, ObjectType } from '@routes/object-manage/object.type';

@Component({
  selector: 'object-template',
  templateUrl: './object-template.component.html',
  styleUrls: ['./object-template.component.scss'],
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ObjectTemplateComponent {
  @Output() templatesSelected = new EventEmitter<any[]>();
  @Input() indent = 0;
  @Input() defaultExpandComplexType = true;

  selectedTemplates: any[] = [];
  selectedTemplate: any;
  selectedParams: ObjectType[] = [];
  currentPage: number = 1;
  pageSize: { options: Array<number>; size: number } = {
    options: [10, 20, 50, 100],
    size: 10,
  };

  totalNumber: number = 0;

  public show = false;

  loading = false;

  searchValue = '';

  objectTemplates: any[] = [];

  public isLoading = false;

  private tiModal: any;

  constructor(private objectManageService: ObjectManageService) {}

  ngOnInit() {
    this.getObjectList().then(() => {
      if (this.objectTemplates.length > 0) {
        this.selectTemplate(this.objectTemplates[0], {
          preventDefault: () => {},
          stopPropagation: () => {},
        });
      }
    });
  }

  selectTemplate(template: any, e) {
    e.preventDefault();
    e.stopPropagation();
    template.checked = !template.checked;
    this.selectedTemplate = template;
    this.selectedParams = this.objectTemplateFields2Views(JSON.parse(template.schemas));
    this.toggleSelection(template, template.checked);
  }

  searchList() {
    this.currentPage = 1;
    this.pageSize.size = 10;
    this.getObjectList();
  }

  async getObjectList(): Promise<void> {
    try {
      this.loading = true;
      const result = await this.objectManageService.getObjectsList({
        limit: this.pageSize.size,
        offset: (this.currentPage - 1) * this.pageSize.size,
        ...(this.searchValue && { name: this.searchValue }),
      });
      this.objectTemplates = (result.objects || []).map(o => {
        return {
          ...o,
          checked: false,
        };
      });
      this.totalNumber = result.count;
    } finally {
      this.loading = false;
    }
  }

  toggleSelection(template: any, checked: boolean) {
    const index = this.selectedTemplates.findIndex(t => template.id === t.id);

    if (index === -1 && checked) {
      this.selectedTemplates.push(template);
    } else {
      this.selectedTemplates.splice(index, 1);
    }
  }

  cancel() {
    this.templatesSelected.emit([]);
    this.tiModal.close();
  }

  importTemplates() {
    if (this.selectedTemplates) {
      const result: any[] = [];
      this.selectedTemplates.forEach(template => {
        try {
          const temp = JSON.parse(template.schemas);
          result.push(temp[0]);
        } catch (e) {
          //异常不进行数据添加
        }
      });
      this.templatesSelected.emit([...result]);
    }
  }

  //格式转换
  objectTemplateFields2Views(fields: ObjectField[], depth = 0, parentType: ObjectParentType = 'none'): ObjectType[] {
    const views: ObjectType[] = fields.map(field => {
      const { name, type, description, schema, value } = field;
      const view: ObjectType = {
        name,
        type: type.split('/'),
        description,
        depth,
        parentType,
        value,
      };

      if (type === 'array') {
        if ((schema as ObjectField).type === 'object') {
          view.type = ['array<object>'];
          view.children = this.objectTemplateFields2Views(
            ((schema as ObjectField)?.schema ?? []) as ObjectField[],
            depth + 1,
            view.type[0] as ObjectParentType
          );
          view.expanded = true;
        } else {
          const subType = (schema as ObjectField).type;
          if (subType.includes('file')) {
            const fileType = subType.split('/')[1];
            view.type = [`${type}<file>`, fileType];
          } else {
            view.type = [`array<${subType}>`];
          }
        }
      }

      if (type === 'object') {
        view.children = this.objectTemplateFields2Views((schema ?? []) as ObjectField[], depth + 1, view.type[0] as ObjectParentType);
        view.expanded = true;
      }

      return view;
    });

    return views;
  }

  formatType(type: string[]): string {
    const typeStr = Array.isArray(type) ? type.join('') : 'unknown';
    return typeStr.charAt(0).toUpperCase() + typeStr.slice(1);
  }

  getParamNameWidth(param) {
    let width = 180 - (param.depth || 0) * 24;
    if (['Array<object>', 'Object'].includes(this.formatType(param.type))) {
      width -= 20;
    }

    return width;
  }

  protected readonly JSON = JSON;
}
