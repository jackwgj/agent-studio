import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConversationSkillItem } from '../conversation-skill.model';
import { SkillSelectorComponent } from './skill-selector.component';

describe('SkillSelectorComponent', () => {
  let component: SkillSelectorComponent;
  let fixture: ComponentFixture<SkillSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SkillSelectorComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SkillSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('输入斜杠后按关键词过滤并由 Enter 选择', () => {
    component.skills = [skill('s1', 'meeting-minutes'), skill('s2', 'professional-rewriter')];
    component.value = '请处理 /meet';
    component.onValueInput(component.value);

    expect(component.menuOpen).toBeTrue();
    expect(component.filteredSkills.map((item) => item.skillId)).toEqual(['s1']);

    component.onKeydown(new KeyboardEvent('keydown', { key: 'Enter' }));

    expect(component.selectedSkills.map((item) => item.skillId)).toEqual(['s1']);
    expect(component.value).toBe('请处理 ');
  });

  it('支持多选去重并保留选择顺序', () => {
    component.selectSkill(skill('s2', 'b'));
    component.selectSkill(skill('s1', 'a'));
    component.selectSkill(skill('s2', 'b'));

    expect(component.selectedSkills.map((item) => item.skillId)).toEqual(['s2', 's1']);
  });

  it('手写未从菜单确认的斜杠文本不产生推荐', () => {
    component.value = '/meeting-minutes 直接发送';
    component.onValueInput(component.value);
    component.closeMenu();

    expect(component.selectedSkills).toEqual([]);
    expect(component.value).toBe('/meeting-minutes 直接发送');
  });

  it('使用上下键循环高亮菜单项', () => {
    component.skills = [skill('s1', 'a'), skill('s2', 'b')];
    component.onValueInput('/');

    component.onKeydown(new KeyboardEvent('keydown', { key: 'ArrowUp' }));
    expect(component.activeSkillIndex).toBe(1);

    component.onKeydown(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
    expect(component.activeSkillIndex).toBe(0);
  });

  it('Esc 关闭菜单且不改变输入内容', () => {
    component.skills = [skill('s1', 'a')];
    component.onValueInput('/a');

    component.onKeydown(new KeyboardEvent('keydown', { key: 'Escape' }));

    expect(component.menuOpen).toBeFalse();
    expect(component.value).toBe('/a');
  });

  it('删除标签只移除对应推荐项', () => {
    component.selectSkill(skill('s1', 'a'));
    component.selectSkill(skill('s2', 'b'));
    component.value = '保留正文';

    component.removeSkill('s1');

    expect(component.selectedSkills.map((item) => item.skillId)).toEqual(['s2']);
    expect(component.value).toBe('保留正文');
  });

  it('Shift+Enter 保留给 textarea 换行', () => {
    const event = new KeyboardEvent('keydown', { key: 'Enter', shiftKey: true, cancelable: true });
    spyOn(component.sendRequested, 'emit');

    component.onKeydown(event);

    expect(event.defaultPrevented).toBeFalse();
    expect(component.sendRequested.emit).not.toHaveBeenCalled();
  });

  it('普通 Enter 请求发送', () => {
    const event = new KeyboardEvent('keydown', { key: 'Enter', cancelable: true });
    spyOn(component.sendRequested, 'emit');

    component.onKeydown(event);

    expect(event.defaultPrevented).toBeTrue();
    expect(component.sendRequested.emit).toHaveBeenCalled();
  });

  it('禁用态不打开菜单或改变推荐项', () => {
    component.disabled = true;
    component.skills = [skill('s1', 'meeting-minutes')];
    component.onValueInput('/meet');
    component.selectSkill(skill('s1', 'meeting-minutes'));

    expect(component.menuOpen).toBeFalse();
    expect(component.selectedSkills).toEqual([]);
  });

  it('空目录和无匹配时保持稳定并允许发送', () => {
    component.onValueInput('/missing');
    const event = new KeyboardEvent('keydown', { key: 'Enter', cancelable: true });
    spyOn(component.sendRequested, 'emit');

    component.onKeydown(event);

    expect(component.menuOpen).toBeTrue();
    expect(component.filteredSkills).toEqual([]);
    expect(component.sendRequested.emit).toHaveBeenCalled();
  });

  it('清空推荐和设置目录均按 ID 去重', () => {
    component.setSkills([skill('s1', 'first'), skill('s1', 'duplicate'), skill('s2', 'second')]);
    component.selectSkill(skill('s1', 'first'));
    component.selectSkill(skill('s2', 'second'));
    component.clearRecommendations();

    expect(component.skills.map((item) => item.skillId)).toEqual(['s1', 's2']);
    expect(component.selectedSkills).toEqual([]);
  });
});

function skill(skillId: string, name: string): ConversationSkillItem {
  return { skillId, name, description: `description-${name}` };
}
