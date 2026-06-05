import { Component, effect, inject, input, output, signal, TemplateRef, untracked, viewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { NzDrawerService, NzDrawerModule, NzDrawerRef } from 'ng-zorro-antd/drawer';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { I18nNamespace } from '@i18n';
import { WORKFLOW_SVGS } from '@routes/agent-center/app-flow/flow.const';
import { TagRuleConverter } from '@routes/knowledge-center/components/tag-rule-builder/tag-rule-converter.class';
import { TagRuleSelectComponent } from '@routes/knowledge-center/components/tag-rule-builder/tag-rule-select/tag-rule-select.component';
import { KbItem, TagRuleMode } from '@routes/knowledge-center/knowledge-base.interface';
import { MAX_KB_TAG_LIMIT } from '@routes/knowledge-center/knowledge.const';
import { TagRule } from '@routes/knowledge-center/knowledge.types';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { KbTagService } from '@services/knowledge-center/kb-tag.service';
import { RuleNodeClass } from '@shared/components/rule-tree/classes/rule-node.class';
import { RuleTreeClass } from '@shared/components/rule-tree/classes/rule-tree.class';
import { RuleTreeComponent } from '@shared/components/rule-tree/rule-tree.component';
import { IRuleTreeConfig, IRuleTreeData } from '@shared/components/rule-tree/rule-tree.interface';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';

@Component({
  selector: 'tag-rule-builder',
  templateUrl: './tag-rule-builder.component.html',
  styleUrls: ['./tag-rule-builder.component.less'],
  standalone: true,
  imports: [
    MODULES,
    RuleTreeComponent,
    TagRuleSelectComponent,
    NzDrawerModule,
    NzButtonModule,
    NzSelectModule,
    NzSwitchModule,
    NzRadioModule,
    NzAlertModule,
    NzIconModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.KNOWLEDGE],
    },
  ],
})
export class TagRuleBuilderComponent {

  nzDrawerService = inject(NzDrawerService);

  kbAbilitiesService = inject(KbAbilitiesService);

  i18n = inject(I18NextEagerPipe);

  tagRuleModal = viewChild<TemplateRef<any>>('tagRuleModal');

  tagRuleModalFooter = viewChild<TemplateRef<any>>('tagRuleModalFooter');

  tagRule = input<TagRule>({
    basic: null,
    advancedRules: null,
  });

  kbs = input<KbItem[]>();

  ruleComplete = output<TagRule>();

  enableTagRule = signal(false);

  basicTagFilter = signal<string[]>([]);

  tagRuleTreeData = signal<IRuleTreeData>(null);

  ruleTree = signal<RuleTreeClass>(null);

  ruleMode = signal(TagRuleMode.RULE);

  previewRule = signal('');

  basicTagOptions = signal([]);

  fold = signal(false);

  isTagSelectLoading = signal(false);

  items = [
    {
      text: this.i18n.transform('basic_retrieve'),
      value: TagRuleMode.BASIC,
    },
    {
      text: this.i18n.transform('advanced_retrieve'),
      value: TagRuleMode.RULE,
    },
  ];

  defaultTagRule = {
    type: 'GROUP',
    operator: 'OR',
    children: [],
  };

  ruleTreeConfig: IRuleTreeConfig = {
    maxDepth: 4,
    nodeAddBusinessDataGenerate: node => {
      return {
        formGroup: new FormGroup({
          singleRule: new FormControl({
            operator: 'EQUAL',
            tag: null,
          }),
        }),
      };
    },
  };

  drawerRef: NzDrawerRef | null = null;

  protected readonly TagRuleMode = TagRuleMode;
  protected readonly MAX_KB_TAG_LIMIT = MAX_KB_TAG_LIMIT;
  private readonly kbTagService = inject(KbTagService);

  constructor() {
    effect(() => {
      const currentRule = this.tagRule();
      const { basic, advancedRules } = currentRule;
      untracked(() => {
        if (basic || advancedRules) {
          this.enableTagRule.set(true);
        }
        this.ruleMode.set(Boolean(advancedRules) ? TagRuleMode.RULE : TagRuleMode.BASIC);
        if (this.ruleMode() === TagRuleMode.RULE) {
          this.tagRuleTreeData.set(TagRuleConverter.convertToRuleTreeData(advancedRules, 3));
          this.previewRule.set(TagRuleConverter.stringifyNode(this.tagRuleTreeData(), this.i18n));
        } else {
          this.getTagOptions();
          this.basicTagFilter.set(TagRuleConverter.convertConstructToBasic(basic));
        }
      });
    });

    effect(() => {
      const ruleMode = this.ruleMode();

      untracked(() => {
        const currentRule = this.tagRule();
        const { advancedRules } = currentRule;
        if (ruleMode === TagRuleMode.RULE) {
          if (!advancedRules && !this.tagRuleTreeData()) {
            this.tagRuleTreeData.set(TagRuleConverter.convertToRuleTreeData(this.defaultTagRule, 3));
            this.previewRule.set(TagRuleConverter.stringifyNode(this.tagRuleTreeData(), this.i18n));
          }
        }
      });
    });
  }

  onTagSelectOpen(isOpen: boolean) {
    if (isOpen) {
      this.getTagOptions();
    }
  }

  getTagOptions() {
    const kbs = this.kbs();
    if (kbs.length) {
      this.isTagSelectLoading.set(true);
      this.kbTagService.batchGetTags(kbs, {
        offset: 0,
        limit: MAX_KB_TAG_LIMIT,
      }).subscribe({
        next: res => {
          this.basicTagOptions.set(res.filter(tagGroup => Boolean(tagGroup.children.length)));
          this.validateValueValid();
          this.isTagSelectLoading.set(false);
        },
        error: () => {
          this.isTagSelectLoading.set(false);
        }
      });
    }
  }

  validateValueValid() {
    const currentTags = this.basicTagFilter();
    if (currentTags?.length) {
      const validTags = currentTags.filter(currentTag => {
        let isValid = false;
        this.basicTagOptions().forEach(tagGroup => {
          const target = tagGroup.children.find(tag => tag.name === currentTag);
          if (!isValid) {
            isValid = Boolean(target);
          }
        });
        return isValid;
      })
      if (validTags.length !== currentTags.length) {
        this.basicTagFilter.set(validTags);
      }
    }
  }

  showRuleModal() {
    this.initRule();
    this.drawerRef = this.nzDrawerService.create({
      nzTitle: this.i18n.transform('appoint_tag_retrieve'),
      nzContent: this.tagRuleModal(),
      nzFooter: this.tagRuleModalFooter(),
      nzWidth: 700,
      nzMask: false,
      nzClosable: true
    });

    this.drawerRef.afterClose.subscribe((action) => {
      if (action === 'close') {
        if (this.enableTagRule()) {
          this.ruleComplete.emit({
            basic: this.ruleMode() === TagRuleMode.BASIC ?
              TagRuleConverter.convertBasicToConstruct(this.basicTagFilter()) :
              null,
            advancedRules: this.ruleMode() === TagRuleMode.RULE ?
              TagRuleConverter.convertToRuleStruct(this.ruleTree()?.rootNode) :
              null,
          });
        } else {
          this.ruleComplete.emit({
            basic: null,
            advancedRules: null,
          });
        }
      }
      this.kbTagService.clearCache();
    });
  }

  closeModal(action: 'close' | 'dismiss') {
    this.drawerRef?.close(action);
  }

  initRule() {
    this.tagRuleTreeData.set(null);
    this.basicTagFilter.set([]);

    const currentRule = this.tagRule();
    const { basic, advancedRules } = currentRule;
    if (this.ruleMode() === TagRuleMode.RULE) {
      if (advancedRules) {
        this.tagRuleTreeData.set(TagRuleConverter.convertToRuleTreeData(advancedRules, 3));
      } else {
        this.tagRuleTreeData.set(TagRuleConverter.convertToRuleTreeData(this.defaultTagRule, 3));
      }
      this.previewRule.set(TagRuleConverter.stringifyNode(this.tagRuleTreeData(), this.i18n));
    } else {
      this.basicTagFilter.set(TagRuleConverter.convertConstructToBasic(basic));
    }
  }

  delCurrentRule(ruleNode: RuleNodeClass) {
    ruleNode.remove();
  }

  nodeChange(rootNode: RuleNodeClass) {
    this.previewRule.set(TagRuleConverter.stringifyNode(rootNode, this.i18n));
    this.tagRuleTreeData.set(TagRuleConverter.convertToRuleTreeData(TagRuleConverter.convertToRuleStruct(this.ruleTree()?.rootNode), 3));
  }

  afterTreeInit(ruleTree: RuleTreeClass) {
    this.ruleTree.set(ruleTree);
  }

  previewMouseOver(event) {
    const target = event.target as HTMLElement;
    if (target.classList.contains('tag-rule-brackets')) {
      const className = target.className;
      const pairMatch = className.match(/bracket-pair-(?<index>\d+)/);

      if (pairMatch) {
        const pairId = pairMatch[1];
        const pairs = document.querySelectorAll(`.bracket-pair-${pairId}`);
        pairs.forEach(bracket => {
          (bracket as HTMLElement).classList.add('tag-highlight');
        });
      }
    }
  }

  previewMouseOut(event) {
    const target = event.target as HTMLElement;
    if (target.classList.contains('tag-rule-brackets')) {
      const highlighted = document.querySelectorAll('.tag-highlight');
      highlighted.forEach(bracket => {
        (bracket as HTMLElement).classList.remove('tag-highlight');
      });
    }
  }

  protected readonly WORKFLOW_SVGS = WORKFLOW_SVGS;
}
