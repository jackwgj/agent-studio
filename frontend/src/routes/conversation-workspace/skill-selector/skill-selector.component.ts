import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import { COMMON_MODULES, LIB_MODULES } from '@shared/modules';
import { ConversationSkillItem } from '../conversation-skill.model';

interface SlashTrigger {
  start: number;
  end: number;
  keyword: string;
}

@Component({
  selector: 'app-skill-selector',
  standalone: true,
  imports: [COMMON_MODULES, LIB_MODULES],
  templateUrl: './skill-selector.component.html',
  styleUrl: './skill-selector.component.less',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SkillSelectorComponent {
  readonly menuId = 'conversation-skill-selector-menu';
  private catalogSkills: ConversationSkillItem[] = [];
  private activeTrigger: SlashTrigger | null = null;

  @Input()
  set skills(items: ConversationSkillItem[]) {
    this.setSkills(items ?? []);
  }

  get skills(): ConversationSkillItem[] {
    return this.catalogSkills;
  }

  @Input() disabled = false;
  @Input() value = '';

  @Output() readonly valueChange = new EventEmitter<string>();
  @Output() readonly selectedSkillsChange = new EventEmitter<ConversationSkillItem[]>();
  @Output() readonly sendRequested = new EventEmitter<void>();

  selectedSkills: ConversationSkillItem[] = [];
  filteredSkills: ConversationSkillItem[] = [];
  menuOpen = false;
  activeSkillIndex = -1;

  /** 清空本轮菜单确认过的推荐，不修改输入正文。 */
  public clearRecommendations(): void {
    if (!this.selectedSkills.length) {
      return;
    }
    this.selectedSkills = [];
    this.selectedSkillsChange.emit([]);
  }

  /** 使用目录原有顺序更新候选项，并按 Skill ID 去重。 */
  public setSkills(items: ConversationSkillItem[]): void {
    const seenIds = new Set<string>();
    this.catalogSkills = items.filter((item) => {
      if (seenIds.has(item.skillId)) {
        return false;
      }
      seenIds.add(item.skillId);
      return true;
    });
    this.refreshMenu();
  }

  public onTextareaInput(event: Event): void {
    const textarea = event.target as HTMLTextAreaElement;
    this.onValueInput(textarea.value, textarea.selectionStart ?? textarea.value.length);
  }

  /**
   * 更新输入文字，并仅识别光标前最后一个由行首或空白引导的 /关键词 片段。
   */
  public onValueInput(value: string, cursorPosition = value.length): void {
    if (this.disabled) {
      return;
    }

    this.value = value;
    this.valueChange.emit(value);
    this.activeTrigger = this.findSlashTrigger(value, cursorPosition);
    this.refreshMenu();
  }

  public onKeydown(event: KeyboardEvent): void {
    if (this.disabled || (event.shiftKey && event.key === 'Enter')) {
      return;
    }

    if (event.key === 'Escape' && this.menuOpen) {
      event.preventDefault();
      this.closeMenu();
      return;
    }

    if ((event.key === 'ArrowDown' || event.key === 'ArrowUp') && this.menuOpen) {
      if (!this.filteredSkills.length) {
        return;
      }
      event.preventDefault();
      const offset = event.key === 'ArrowDown' ? 1 : -1;
      this.activeSkillIndex = (this.activeSkillIndex + offset + this.filteredSkills.length) % this.filteredSkills.length;
      return;
    }

    if (event.key !== 'Enter') {
      return;
    }

    event.preventDefault();
    if (this.menuOpen && this.filteredSkills.length) {
      this.selectSkill(this.filteredSkills[this.activeSkillIndex]);
      return;
    }
    this.sendRequested.emit();
  }

  /** 仅通过菜单调用；手写 /文本 不会调用此方法。 */
  public selectSkill(item: ConversationSkillItem): void {
    if (this.disabled) {
      return;
    }

    if (!this.selectedSkills.some((selected) => selected.skillId === item.skillId)) {
      this.selectedSkills = [...this.selectedSkills, item];
      this.selectedSkillsChange.emit(this.selectedSkills);
    }

    if (this.activeTrigger) {
      this.value = this.value.slice(0, this.activeTrigger.start) + this.value.slice(this.activeTrigger.end);
      this.valueChange.emit(this.value);
    }
    this.closeMenu();
  }

  public removeSkill(skillId: string): void {
    if (this.disabled) {
      return;
    }
    const nextSelectedSkills = this.selectedSkills.filter((item) => item.skillId !== skillId);
    if (nextSelectedSkills.length === this.selectedSkills.length) {
      return;
    }
    this.selectedSkills = nextSelectedSkills;
    this.selectedSkillsChange.emit(this.selectedSkills);
  }

  public closeMenu(): void {
    this.menuOpen = false;
    this.activeSkillIndex = -1;
    this.activeTrigger = null;
  }

  public optionId(index: number): string {
    return `${this.menuId}-option-${index}`;
  }

  private refreshMenu(): void {
    if (!this.activeTrigger || this.disabled) {
      this.menuOpen = false;
      this.filteredSkills = [];
      this.activeSkillIndex = -1;
      return;
    }

    const keyword = this.activeTrigger.keyword.toLocaleLowerCase();
    this.filteredSkills = this.catalogSkills.filter((item) =>
      item.name.toLocaleLowerCase().includes(keyword) ||
      item.description.toLocaleLowerCase().includes(keyword),
    );
    this.menuOpen = true;
    this.activeSkillIndex = this.filteredSkills.length ? 0 : -1;
  }

  private findSlashTrigger(value: string, cursorPosition: number): SlashTrigger | null {
    const safeCursorPosition = Math.min(Math.max(cursorPosition, 0), value.length);
    const beforeCursor = value.slice(0, safeCursorPosition);
    const match = /(^|\s)\/([^\s\/]*)$/.exec(beforeCursor);
    if (!match) {
      return null;
    }
    const start = match.index + match[1].length;
    return { start, end: safeCursorPosition, keyword: match[2] };
  }
}
