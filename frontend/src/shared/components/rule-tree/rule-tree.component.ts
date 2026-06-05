import { CommonModule } from '@angular/common';
import { Component, contentChild, effect, input, output, signal, TemplateRef, untracked } from '@angular/core';
import { RuleNodeClass } from '@shared/components/rule-tree/classes/rule-node.class';
import { RuleTreeClass } from '@shared/components/rule-tree/classes/rule-tree.class';
import { RuleNodeComponent } from '@shared/components/rule-tree/rule-node/rule-node.component';
import { IRuleTreeConfig, IRuleTreeData } from '@shared/components/rule-tree/rule-tree.interface';

@Component({
	selector: 'rule-tree',
	templateUrl: './rule-tree.component.html',
	styleUrls: ['./rule-tree.component.less'],
	standalone: true,
	imports: [CommonModule, RuleNodeComponent],
})
export class RuleTreeComponent {
	ruleTemplate = contentChild<TemplateRef<any>>('singleRule');

	ruleTree = signal<RuleTreeClass>(null);

	ruleTreeConfig = input<IRuleTreeConfig>(null);

	treeData = input<IRuleTreeData>();

	nodeChange = output<RuleNodeClass>();

	afterTreeInit = output<RuleTreeClass>();

	constructor() {
		effect(() => {
			const data = this.treeData();
			const ruleTreeConfig = this.ruleTreeConfig();
			untracked(() => {
				if (data) {
					this.ruleTree.set(new RuleTreeClass(data, ruleTreeConfig));
					this.ruleTree().on('nodeChange', () => {
						this.nodeChange.emit(this.ruleTree().rootNode);
					});
					this.afterTreeInit.emit(this.ruleTree());
				}
			});
		});
	}
}
