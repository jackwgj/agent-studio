import { FormGroup } from '@angular/forms';
import { RuleNodeClass } from '@shared/components/rule-tree/classes/rule-node.class';

export interface IRuleTreeData extends IRuleNode {
	logicOperator: ILogicalOperator;
	type: IRuleNodeType.LOGIC_OPERATOR;
}

export interface IRuleTree {
	rootNode: RuleNodeClass;
}

export interface IRuleTreeConfig {
	maxDepth?: number;
	nodeAddBusinessDataGenerate?: (node: RuleNodeClass) => Record<string, any>;
}

export interface IRuleNode {
	type: IRuleNodeType;
	logicOperator?: ILogicalOperator | null;
	children?: IRuleNode[];
	businessData?: {
		formGroup?: FormGroup;

		[k: string]: any;
	};

	// inner properties
	parent?: IRuleNode | null;
	depth?: number;
	ruleTree?: IRuleTree;
}

export enum IRuleNodeType {
	RULE_ADD = 'ruleAdd',
	LOGIC_OPERATOR = 'logic_operator',
	RULE = 'rule'
}

export enum ILogicalOperator {
	AND = 'AND',
	OR = 'OR'
}

export enum IConditionType {
	EQUALS = 'EQUALS',
	NOT_EQUALS = 'NOT_EQUALS',
}
