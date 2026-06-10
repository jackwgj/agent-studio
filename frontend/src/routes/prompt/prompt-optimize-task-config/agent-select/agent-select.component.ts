import {
  ChangeDetectionStrategy,
  Component,
  Input,
  SimpleChanges,
} from '@angular/core';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'meta-agent-select',
  standalone: true,
  imports: [NzSelectModule, MODULES],
  templateUrl: './agent-select.component.html',
  styleUrl: './agent-select.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.PROMPT_PLATFORM
      ],
    },
  ],
})
export class AgentSelectComponent {
  @Input() selected?: string;
  optionsTotalNumber: number;
  size: number = 20;
  isSearchable: boolean = true; // select是否可搜索
  options: Array<any> = [];
  value: any;
  placeholderStr: string = this.i18n.transform('agent-select-1');

  constructor(private appAgentRepoService: AppAgentRepoService, private i18n: I18NextEagerPipe) {
    const params = {
      id: this.selected ? this.selected : undefined,
    };
    this.appAgentRepoService
      .getAgentList(params, this.options.length, this.size)
      .then((res) => {
        this.options = this.options.concat(res.agent_list);
        this.optionsTotalNumber = res.count;
      });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.selected) {
      if (this.selected.trim() !== '') {
        const result = this.options.find((item) => {
          return item.id === this.selected;
        });
        if (result) {
          this.value = result;
        } else {
          // 如果尚未加载该条数据，则单独请求
          const params = {
            id: this.selected ? this.selected : undefined,
          };
          this.appAgentRepoService.getAgentList(params, 0, 1).then((res) => {
            this.options = this.options.concat(res.agent_list);
            this.value = res.agent_list[0];
          });
        }
      }
    }
  }

  beforeSearch(searchWord: string): void {
    // 获取搜索内容
    const params = { name: searchWord === '' ? undefined : searchWord };
    this.appAgentRepoService
      .getAgentList(params, this.options.length, this.size)
      .then((result) => {
        this.optionsTotalNumber = result.count;
        this.options = result.agent_list;
      });
  }

  loadMore(): void {
    if (this.options.length >= this.optionsTotalNumber) {
      return;
    }
    this.appAgentRepoService
      .getAgentList({}, this.options.length, this.size)
      .then((result: any) => {
        this.options = [...this.options, ...result.agent_list];
        this.optionsTotalNumber = result.count;
      });
  }
}
