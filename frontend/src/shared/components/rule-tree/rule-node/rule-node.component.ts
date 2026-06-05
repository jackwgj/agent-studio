import { CommonModule } from '@angular/common';
import { Component, inject, input, TemplateRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { RuleNodeClass } from '@shared/components/rule-tree/classes/rule-node.class';
import { ILogicalOperator, IRuleNodeType } from '@shared/components/rule-tree/rule-tree.interface';
import { I18NextEagerPipe, I18NextModule } from 'angular-i18next';
import { MODULES } from "@shared/modules";

@Component({
	selector: 'rule-node',
	templateUrl: './rule-node.component.html',
	styleUrls: ['./rule-node.component.less'],
	standalone: true,
	imports: [CommonModule, NzSelectModule, FormsModule, I18NextModule, MODULES],
})
export class RuleNodeComponent {
	i18n = inject(I18NextEagerPipe);

	isFirst = input(false);

	isLast = input(false);

	ruleNode = input<RuleNodeClass>();

	ruleTemplate = input<TemplateRef<any>>();

	logicOperatorOptions = [
		{
			label: this.i18n.transform('common_and'),
			value: ILogicalOperator.AND,
		},
		{
			label: this.i18n.transform('common_or'),
			value: ILogicalOperator.OR,
		},
	];
	protected readonly ILogicalOperator = ILogicalOperator;
	protected readonly IRuleNodeType = IRuleNodeType;

	get isPermitAddRule() {
		return this.ruleNode()?.depth < this.ruleNode().maxDepth;
	}

	get isPermitAddRuleGroup() {
		return this.ruleNode()?.depth < this.ruleNode().maxDepth - 1;
	}
}
