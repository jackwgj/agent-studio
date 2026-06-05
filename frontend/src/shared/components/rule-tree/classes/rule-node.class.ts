import { RuleTreeClass } from '@shared/components/rule-tree/classes/rule-tree.class';
import { ILogicalOperator, IRuleNode, IRuleNodeType } from '@shared/components/rule-tree/rule-tree.interface';
import { v4 as uuidV4 } from 'uuid';

export class RuleNodeClass implements IRuleNode {
	id = uuidV4();
	type = IRuleNodeType.LOGIC_OPERATOR;
	logicOperator = null;
	children: RuleNodeClass[] = [];
	parent: RuleNodeClass = null;
	ruleTree: RuleTreeClass = null;
	depth = -1;
	businessData: Record<string, any> = null;

	constructor(
		initData: IRuleNode,
	) {
		Object.keys(initData).forEach(key => {
			this[key] = initData[key];
		});
	}

	get maxDepth() {
		return this.ruleTree?.maxDepth;
	}

	addRule() {
		this.type = IRuleNodeType.RULE;
		if (this.ruleTree.ruleTreeConfig.nodeAddBusinessDataGenerate) {
			this.businessData = this.ruleTree.ruleTreeConfig.nodeAddBusinessDataGenerate(this);
		}
		this.parent?.addRuleAdd();
		this.trigger('nodeChange');
	}

	addRuleAdd() {
		this.children?.push(
			new RuleNodeClass({
				type: IRuleNodeType.RULE_ADD,
				parent: this,
				depth: this.depth + 1,
				ruleTree: this.ruleTree,
			}),
		);
	}

	addRuleGroup() {
		this.type = IRuleNodeType.LOGIC_OPERATOR;
		this.logicOperator = this.depth % 2 === 0 ? ILogicalOperator.OR : ILogicalOperator.AND;
		const childRuleAddNode = new RuleNodeClass({
			type: IRuleNodeType.RULE_ADD,
			parent: this,
			depth: this.depth + 1,
			ruleTree: this.ruleTree,
		});
		this.children = [childRuleAddNode];
		childRuleAddNode.addRule();
		this.parent?.addRuleAdd();
		this.trigger('nodeChange');
	}

	on(eventName: string, handler: (...args) => void) {
		this.ruleTree.on(eventName, handler);
	}

	trigger(eventName: string, params?: any) {
		this.ruleTree.trigger(eventName, params);
	}


	remove() {
		this.ruleTree.removeNode(this);
	}
}
