import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { Clipboard } from '@angular/cdk/clipboard';
import { HelpDocKey } from '@constants/support-topic.const';
import { I18nNamespace } from '@i18n';
import {
  MonacoEditorConstructionOptions,
  MonacoEditorModule,
} from '@materia-ui/ngx-monaco-editor';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { ContextService } from '@services/context.service';
import { MODULES } from '@shared/modules';
import { HelpLinksService } from '@shared/services/help-links.service';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { environment } from 'src/environment/environment';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import * as uuid from 'uuid';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'API-call-page',
  templateUrl: './API-call-page.component.html',
  styleUrls: ['./API-call-page.component.scss'],
  imports: [MODULES, MonacoEditorModule, NzTabsModule, NzTableModule],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class APICallPageComponent implements OnInit {
  @Input() versionId = '';
  @Input() id = '';
  @Input() pageType = '';
  @Input() isMultiAgent = false;

  public isHcsOrPoc = false;
  public changeUrl = cdnAssetUrl;
  public tabs: any = [
    { title: this.i18n.transform('technical_documentation'), active: true },
    { title: this.i18n.transform('sample_code'), active: false },
  ];

  get selectedTabIndex(): number {
    const index = this.tabs.findIndex((tab: any) => tab.active);
    return index >= 0 ? index : 0;
  }

  onTabChange(index: number) {
    this.tabs.forEach((tab: any, i: number) => {
      tab.active = i === index;
    });
  }
  host = `https://mas.{regionId}.${window.location.host}`;
  path = `/v1/{projectId}/${this.pageType}-assets/{id}/conversations/{conversation_id}?version={versionId}`;
  token = '';
  uuid: string = '';
  urlTip: string = '';
  pathTip: string = '';

  requestInfo = [
    {
      part: 'Header',
      field: 'Content-Type',
      value: 'application/json',
    },
    {
      part: this.i18n.transform('call_path'),
      field: this.pageType === 'agents' ? 'Agent_id' : 'Workflow_id',
      tip: '',
      value: this.id,
    },
    {
      part: '',
      field: 'url',
      tip: '',
      value: `${this.host}${this.path}`,
    },
  ];
  apiDoc = [
    {
      field: this.i18n.transform('interface_description'),
      value: this.i18n.transform('interface_value'),
    },
    {
      field: this.i18n.transform('version_id'),
      type: 'slot',
      slot: 'version',
    },
    {
      field: this.i18n.transform('permission_description'),
      value: `X-Auth-Token${this.i18n.transform(
        'need_to_fill_in_user_credentials',
      )}`,
      type: 'slot',
      slot: 'token',
    },
    {
      field: this.i18n.transform('interface_definition'),
      type: 'slot',
      slot: 'apiDefine',
    },
    {
      field: this.i18n.transform('request_structure'),
      type: 'slot',
      slot: 'image',
    },
    {
      field: this.i18n.transform('request_header_domain'),
      value: this.i18n.transform('request_header_domain_value'),
    },
    {
      field: this.i18n.transform('request_params'),
      type: 'slot',
      slot: 'request',
    },
    {
      field: this.i18n.transform('response_header'),
      value: this.i18n.transform('response_header_value'),
    },
    {
      field: this.i18n.transform('response_parameters_streaming'),
      type: 'slot',
      slot: 'response',
    },
  ];
  fieldColDef = {
    headerName: this.i18n.transform('field'),
    field: 'field',
    width: 200,
    sortable: false,
  };
  typeColDef = {
    headerName: this.i18n.transform('type'),
    field: 'type',
    width: 200,
    sortable: false,
  };
  mandatoryColDef = {
    headerName: this.i18n.transform('mandatory'),
    field: 'mandatory',
    width: 200,
    sortable: false,
  };
  descColDef = {
    headerName: this.i18n.transform('describe'),
    field: 'desc',
    sortable: false,
  };

  public parameters = [
    {
      key: 'Path',
      value: this.path,
    },
    {
      key: 'Method',
      value: 'POST',
    },
    {
      key: 'Content-Type',
      value: 'application/json',
    },
    {
      key: 'X-Auth-Token',
      value:
        this.i18n.transform('user_credentials') +
        this.i18n.transform('auth_token'),
    },
  ];
  public requestTableData = [
    {
      field: 'query',
      type: 'string',
      mandatory: this.i18n.transform('yes'),
      desc: this.i18n.transform('user_issues'),
    },
  ];
  public responseTableData = [
    {
      field: 'event',
      type: 'string',
      desc: this.i18n.transform('data_unit_type'),
    },
    {
      field: 'content',
      type: 'Object',
      desc: this.i18n.transform('content_of_message'),
    },
    {
      field: 'createdTime',
      type: 'long',
      desc: `${this.i18n.transform('timestamp_message')}1733817348963。`,
    },
    {
      field: 'latency',
      type: 'Object',
      desc: this.i18n.transform('latency_desc'),
    },
    {
      field: 'plugin',
      type: 'Object',
      desc: this.i18n.transform('plugin_desc'),
    },
  ];
  editorOptions: MonacoEditorConstructionOptions = {
    theme: 'vs',
    language: 'python',
    lineNumbers: 'off',
    folding: false,
    readOnly: true,
    minimap: {
      enabled: false,
    },
  };
  requestCode = '';
  requestCodeDeepResearch = '';
  code = '';

  runtime_api_endpoint_prefix = '';
  isDeepResearch: boolean = false;
  iamUrl: string = '';
  constructor(
    private ctxServ: ContextService,
    private i18n: I18NextEagerPipe,
    private configServ: AgentConfigService,
    private appAgentRepoService: AppAgentRepoService,
    private helpLinksService: HelpLinksService,
    private clipboard: Clipboard,
    private nzMessageService: NzMessageService,
    private cdr: ChangeDetectorRef,
  ) {
    this.uuid = this.getUUID();
  }

  async ngOnInit() {
    if (this.id && this.pageType === 'agents') {
      const res: any = await this.appAgentRepoService.fetchAgentDetail(this.id);
      if (res.sub_type === 'deepresearch') {
        this.isDeepResearch = true;
      }
    }
    this.urlTip = this.i18n.transform('tuningpanelcomponent_89', {
      uuid: this.uuid,
    });
    this.pathTip = this.i18n.transform('tuningpanelcomponent_90', {
      uuid: this.uuid,
    });
    this.iamUrl = this.helpLinksService.getHelpCenterLink(
      HelpDocKey.IAM_GET_TOKEN_GUIDE,
    );
  }

  ngOnChanges(): void {
    if (this.versionId || this.id) {
      this.requestInfo[1].field =
        this.pageType === 'agents' ? 'Agent_id' : 'Workflow_id';
      let projectId = this.ctxServ.projectId;
      this.runtime_api_endpoint_prefix =
        this.configServ.getConfigs()?.runtime_api_endpoint_prefix ?? '';
      this.host = `https://${this.runtime_api_endpoint_prefix}`;
      this.path = `/v1/${projectId}/${this.pageType}/${this.id}/conversations/${
        this.uuid
      }?${this.isDeepResearch ? 'agent_type=deepresearch&' : ''}version=${
        this.versionId
      }`;
      if (this.isMultiAgent) {
        this.path = `${this.path}&type=controller`;
      }
      this.requestInfo[1].value = this.id;
      this.requestInfo[2].value = `${this.host}${this.path}`;
      this.parameters[0].value = `${this.path}`;
      this.parameters = [...this.parameters];
      if (!this.runtime_api_endpoint_prefix) {
        this.requestInfo = this.requestInfo.slice(0, 2);
      }
      this.generateCode();
    }
  }

  generateCode() {
    let host = `<span class="hljs-symbol">HOST</span>: ${this.runtime_api_endpoint_prefix}`;
    if (!this.runtime_api_endpoint_prefix) {
      host = '';
    }
    this.requestCode = `
    <span class="hljs-symbol">POST</span> ${
      this.path
    } <span class="hljs-symbol">HTTP/1.1</span>
    ${host}
    <span class="hljs-section">X-Auth-Token</span>: ${this.token}
    Content-Type: application/json

   {
     "inputs": {
        <span class="hljs-section">"query": </span><span class="hljs-name">"${this.i18n.transform(
          'hello',
        )}"</span>
      }
   }`;

    this.code = `
    import requests
    import json
    def main ():
       url = "${this.host}${this.path}"

       payload = json.dumps ({"inputs":{
            "query": "${this.i18n.transform('hello')}"
       }}, ensure_ascii=False)
       headers = {
            'Content-Type': 'application/json',
            'X-Auth-Token': ''
       }

       response = requests.request ("POST", url, headers=headers, data=payload.encode ("utf-8"))
       print (response.text)

    if __name__ == '__main__':
       main ()`;

    if (this.isDeepResearch) {
      this.setRequestCodeDeepResearch(host);
    }
  }

  private setRequestCodeDeepResearch(host: string) {
    this.requestCode = `
    <span class="hljs-symbol">POST</span> ${
      this.path
    } <span class="hljs-symbol">HTTP/1.1</span>
    ${host}
    <span class="hljs-section">X-Auth-Token</span>: ${this.token}
    Content-Type: application/json

   {
     "inputs": {
        <span class="hljs-section">"message": </span><span class="hljs-name">"${this.i18n.transform(
          'hello',
        )}"</span>
      }
   }`;

    this.requestCodeDeepResearch = `
    <span class="hljs-symbol">POST</span> ${this.path} <span class="hljs-symbol">HTTP/1.1</span>
    ${host}
    <span class="hljs-section">X-Auth-Token</span>: ${this.token}
    Content-Type: application/json

   {
     "inputs": {
        <span class="hljs-section">"message": </span><span class="hljs-name">"ok",</span>
        <span class="hljs-section">"interrupt_feedback": </span><span class="hljs-name">"accepted"</span>
      }
   }`;

    this.code = `
    import requests
    import json
    def main ():
       url = "${this.host}${this.path}"
       payload = json.dumps ({"inputs":{
            "message": "${this.i18n.transform('hello')}"
       }}, ensure_ascii=False)
       headers = {
            'Content-Type': 'application/json',
            'X-Auth-Token': ''
       }

       response = requests.request ("POST", url, headers=headers, data=payload.encode ("utf-8"))
       print (response.text)

    if __name__ == '__main__':
       main ()`;
  }

  getUUID() {
    return uuid.v4();
  }

  copyToClipboard(content: string) {
    this.clipboard.copy(content);
    this.nzMessageService.success(this.i18n.transform('copy_success'));
  }
}
