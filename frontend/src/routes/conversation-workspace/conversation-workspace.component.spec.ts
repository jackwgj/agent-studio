import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { provideNzIcons } from 'ng-zorro-antd/icon';
import { AudioOutline, NumberOutline, SendOutline, UploadOutline } from '@ant-design/icons-angular/icons';
import { BehaviorSubject } from 'rxjs';
import { ModelManagementService } from '@services/repositories/model-management-new';
import { HttpService } from '@services/http.service';
import { ConversationSkillItem } from './conversation-skill.model';
import { ConversationWorkspaceComponent } from './conversation-workspace.component';
import { ConversationWorkspaceService, SessionItem } from './conversation-workspace.service';
import { SSE } from '@shared/services/sse';

describe('ConversationWorkspaceComponent', () => {
  let fixture: ComponentFixture<ConversationWorkspaceComponent>;
  let component: ConversationWorkspaceComponent;
  let service: any;
  let http: { workspaceId: string; getWorkspaceId: () => string };

  beforeEach(async () => {
    service = {
      sessions$: new BehaviorSubject<SessionItem[]>([]),
      activeSession$: new BehaviorSubject<SessionItem | null>(null),
      createSession: jasmine.createSpy('createSession').and.resolveTo(session('new-session')),
      detailSession: jasmine.createSpy('detailSession').and.resolveTo({ messages: [] }),
      refreshSessions: jasmine.createSpy('refreshSessions').and.resolveTo(),
      setActiveSession: jasmine.createSpy('setActiveSession').and.callFake((item: SessionItem | null) => service.activeSession$.next(item)),
      newDraftSession: jasmine.createSpy('newDraftSession'),
      listSkills: jasmine.createSpy('listSkills').and.resolveTo([skill('s1')]),
      chatSSE: jasmine.createSpy('chatSSE').and.returnValue({ close: jasmine.createSpy('close') }),
    };
    http = { workspaceId: 'workspace-1', getWorkspaceId: () => http.workspaceId };

    await TestBed.configureTestingModule({
      imports: [ConversationWorkspaceComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNzIcons([AudioOutline, NumberOutline, SendOutline, UploadOutline]),
        { provide: ConversationWorkspaceService, useValue: service },
        {
          provide: ModelManagementService,
          useValue: { getAvailableModelList: jasmine.createSpy('getAvailableModelList').and.returnValue(new Promise(() => void 0)) },
        },
        { provide: HttpService, useValue: http },
        { provide: ActivatedRoute, useValue: { queryParams: new BehaviorSubject({}) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ConversationWorkspaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.selectedModel = 'model-1';
  });

  it('初始化独立加载工作空间 Skill，并把选择器保留在工具栏同级', async () => {
    await Promise.resolve();
    fixture.detectChanges();

    expect(service.listSkills).toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('app-skill-selector')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.chat-composer > .composer-toolbar')).not.toBeNull();
  });

  it('发送时只提交有序推荐 ID，并且只在 SSE open 后清空输入和选择', () => {
    (component as any).recommendedSkills = [skill('s2'), skill('s1')];
    component.currentSession = session('c1');
    component.inputText = '整理会议';

    component.send();

    expect(service.chatSSE).toHaveBeenCalledWith('c1', jasmine.objectContaining({
      query: '整理会议',
      model_deployment_id: 'model-1',
      recommended_skill_ids: ['s2', 's1'],
    }), jasmine.any(Object));
    expect(component.inputText).toBe('整理会议');
    const callbacks = service.chatSSE.calls.mostRecent().args[2];
    callbacks.onOpen();
    expect(component.inputText).toBe('');
    expect((component as any).recommendedSkills).toEqual([]);
  });

  it('连接前失败保留输入和推荐以便重试，并刷新目录', () => {
    (component as any).recommendedSkills = [skill('s1')];
    component.currentSession = session('c1');
    component.inputText = '整理会议';

    component.send();
    const callbacks = service.chatSSE.calls.mostRecent().args[2];
    callbacks.onError();

    expect(component.inputText).toBe('整理会议');
    expect((component as any).recommendedSkills.map((item: ConversationSkillItem) => item.skillId)).toEqual(['s1']);
    expect(service.listSkills.calls.count()).toBeGreaterThan(1);
  });

  it('连接前 timeout 与错误相同地保留草稿并刷新目录', () => {
    (component as any).recommendedSkills = [skill('s1')];
    component.currentSession = session('c1');
    component.inputText = '整理会议';

    component.send();
    service.chatSSE.calls.mostRecent().args[2].onTimeout();

    expect(component.inputText).toBe('整理会议');
    expect((component as any).recommendedSkills.map((item: ConversationSkillItem) => item.skillId)).toEqual(['s1']);
    expect(service.listSkills.calls.count()).toBeGreaterThan(1);
  });

  it('流已经打开后的失败不恢复已清除的草稿', () => {
    (component as any).recommendedSkills = [skill('s1')];
    component.currentSession = session('c1');
    component.inputText = '整理会议';

    component.send();
    const callbacks = service.chatSSE.calls.mostRecent().args[2];
    callbacks.onOpen();
    callbacks.onError();

    expect(component.inputText).toBe('');
    expect((component as any).recommendedSkills).toEqual([]);
    expect(service.listSkills.calls.count()).toBe(1);
  });

  it('SSE open 会通过子组件公开命令清除已渲染的推荐 chips', async () => {
    await Promise.resolve();
    fixture.detectChanges();
    const selector = (component as any).skillSelector;
    selector.onValueInput('/name-s1');
    selector.selectSkill(skill('s1'));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('.skill-chip').length).toBe(1);

    component.currentSession = session('c1');
    component.inputText = '整理会议';
    component.send();
    service.chatSSE.calls.mostRecent().args[2].onOpen();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('.skill-chip').length).toBe(0);
  });

  it('草稿创建异步完成后仍发送调用时的推荐快照', async () => {
    (component as any).recommendedSkills = [skill('s1')];
    component.currentSession = { conversation_id: '', title: '草稿', status: 'ACTIVE' };
    component.inputText = '整理会议';

    component.send();
    (component as any).recommendedSkills = [skill('s2')];
    await Promise.resolve();

    expect(service.createSession).toHaveBeenCalledWith({ title: '草稿' });
    expect(service.chatSSE).toHaveBeenCalledWith('new-session', jasmine.objectContaining({
      recommended_skill_ids: ['s1'],
    }), jasmine.any(Object));
    expect(service.setActiveSession).toHaveBeenCalledWith(session('new-session'));
    expect(service.refreshSessions).toHaveBeenCalled();
  });

  it('接收技能激活事件后只在当前页面显示按版本去重的标签', () => {
    const assistant = { role: 'assistant' as const, content: '', loading: true };

    dispatchSse(component, assistant, { event: 'skill_activated', data: { skillId: 's1', name: '会议纪要', versionId: 'v1' } });
    dispatchSse(component, assistant, { event: 'skill_activated', data: { skillId: 's1', name: '会议纪要', versionId: 'v1' } });
    dispatchSse(component, assistant, { event: 'skill_activated', data: { skillId: 's1', name: '会议纪要 v2', versionId: 'v2' } });

    expect((component as any).activatedSkills).toEqual([
      { skillId: 's1', name: '会议纪要', versionId: 'v1' },
      { skillId: 's1', name: '会议纪要 v2', versionId: 'v2' },
    ]);
  });

  it('切换会话时清除推荐与激活标签，且不从历史恢复', async () => {
    (component as any).recommendedSkills = [skill('s1')];
    (component as any).activatedSkills = [{ skillId: 's1', name: '会议纪要', versionId: 'v1' }];

    service.activeSession$.next(session('c2'));
    await Promise.resolve();

    expect((component as any).recommendedSkills).toEqual([]);
    expect((component as any).activatedSkills).toEqual([]);
  });

  it('收到实际工作空间切换事件时立即清除状态并重载目录', () => {
    (component as any).recommendedSkills = [skill('s1')];
    (component as any).activatedSkills = [{ skillId: 's1', name: '会议纪要', versionId: 'v1' }];
    http.workspaceId = 'workspace-2';

    window.dispatchEvent(new Event('WorkspaceChange'));

    expect((component as any).recommendedSkills).toEqual([]);
    expect((component as any).activatedSkills).toEqual([]);
    expect(service.listSkills.calls.count()).toBeGreaterThan(1);
  });
});

describe('ConversationWorkspaceService', () => {
  it('加载目录时只携带工作空间并映射最小浏览器字段', async () => {
    const http = {
      getWorkspaceId: jasmine.createSpy('getWorkspaceId').and.returnValue('workspace-1'),
      getAsync: jasmine.createSpy('getAsync').and.resolveTo([
        { skill_id: 's1', name: '会议纪要', description: '整理会议内容' },
      ]),
    };
    const service = createWorkspaceService(http);

    await expectAsync(service.listSkills()).toBeResolvedTo([
      { skillId: 's1', name: '会议纪要', description: '整理会议内容' },
    ]);
    expect(http.getAsync).toHaveBeenCalledWith({
      url: '/v1/project/conversation/sessions/skills',
      query: { workspace_id: 'workspace-1' },
    });
  });

  it('完整转发推荐 ID，并保留既有 SSE 地址、头、超时和九类回调注册', () => {
    sessionStorage.setItem('cfCurrentRegion', JSON.stringify('region-1'));
    spyOn(XMLHttpRequest.prototype, 'open').and.stub();
    spyOn(XMLHttpRequest.prototype, 'send').and.stub();
    spyOn(XMLHttpRequest.prototype, 'setRequestHeader').and.stub();
    const service = createWorkspaceService({ prefixPath: '/api', getWorkspaceId: () => 'workspace-1' });
    const source = service.chatSSE('conversation-1', {
      query: '整理会议',
      model_deployment_id: 'model-1',
      recommended_skill_ids: ['s2', 's1'],
    }, {});

    expect((source as any).url).toBe('/api/v1/project/conversation/sessions/conversation-1/messages?workspace_id=workspace-1');
    expect(JSON.parse((source as any).payload)).toEqual({
      query: '整理会议',
      model_deployment_id: 'model-1',
      recommended_skill_ids: ['s2', 's1'],
    });
    expect((source as any).headers).toEqual(jasmine.objectContaining({
      'Content-Type': 'application/json',
      stream: 'true',
      'X-Language': 'zh-cn',
      'X-Invoke-Mode': 'PUBLISHED',
    }));
    expect((source as any).timeout).toBe(3600000);
    expect((source as any).streamFirstChunkTimeout).toBe(180000);
    expect((source as any).streamTimeout).toBe(180000);
    expect(Object.keys((source as any).listeners).sort()).toEqual([
      'abort', 'done', 'error', 'message', 'moderation', 'open', 'readystatechange', 'status', 'timeout',
    ]);
  });
});

function skill(skillId: string): ConversationSkillItem {
  return { skillId, name: `name-${skillId}`, description: `description-${skillId}` };
}

function session(conversationId: string): SessionItem {
  return { conversation_id: conversationId, title: '会话', status: 'ACTIVE' };
}

function dispatchSse(component: ConversationWorkspaceComponent, assistant: any, payload: object): void {
  (component as any).handleMessage({ data: JSON.stringify(payload) }, assistant);
}

function createWorkspaceService(http: any): ConversationWorkspaceService {
  return new ConversationWorkspaceService(
    http,
    { baseUrl: '/v1/project', projectId: 'project' } as any,
    { getConfigs: () => ({}) } as any,
  );
}
