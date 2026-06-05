import { RuleNodeClass } from '@shared/components/rule-tree/classes/rule-node.class';
import {
	IRuleNode,
	IRuleNodeType,
	IRuleTreeConfig,
	IRuleTreeData,
} from '@shared/components/rule-tree/rule-tree.interface';
import { removeNode } from "../../../../utils/utils";

export class RuleTreeClass {
	eventListener: { [k: string]: (...args) => void } = {};

	constructor(
		private data: IRuleTreeData,
		ruleTreeConfig?: IRuleTreeConfig,
	) {
		this._rootNode = this.#generateTree(data);
		this._ruleTreeConfig = ruleTreeConfig ?? this.ruleTreeConfig;
	}

	private _rootNode: RuleNodeClass;

	get rootNode() {
		return this._rootNode;
	}

	private _ruleTreeConfig: IRuleTreeConfig = {
		maxDepth: 4,
	};

	get ruleTreeConfig() {
		return this._ruleTreeConfig;
	}

	get maxDepth() {
		return this.ruleTreeConfig.maxDepth;
	}

	on(eventName: string, handler: (...args) => void) {
		this.eventListener[eventName] = handler;
	}

	trigger(eventName: string, params?: any) {
		this.eventListener[eventName]?.(params);
	}

	removeNode(node: RuleNodeClass) {
		const nodeParent = node.parent;
		removeNode([this.rootNode], node);
		if (nodeParent) {
			const remainChildren = nodeParent.children?.filter(remainNode => remainNode.type !== IRuleNodeType.RULE_ADD);
			if (!remainChildren?.length) {
				this.removeNode(nodeParent);
			}
		}
		this.trigger('nodeChange');
	}

	#generateTree(data: IRuleTreeData | IRuleNode, depth = 0, parentNode?: RuleNodeClass): RuleNodeClass {
		const node = new RuleNodeClass({
			...data,
			parent: parentNode ?? null,
			depth,
			ruleTree: this,
		});
		node.children = data.children?.map(nodeData => {
			return this.#generateTree(nodeData, depth + 1, node);
		});
		return node;
	}

}
