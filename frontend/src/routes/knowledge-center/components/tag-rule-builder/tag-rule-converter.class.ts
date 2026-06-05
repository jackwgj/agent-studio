import { FormControl, FormGroup } from '@angular/forms';
import { AdvancedRuleNode } from '@routes/knowledge-center/knowledge.types';
import { RuleNodeClass } from '@shared/components/rule-tree/classes/rule-node.class';
import {
  ILogicalOperator,
  IRuleNode,
  IRuleNodeType,
  IRuleTreeData,
} from '@shared/components/rule-tree/rule-tree.interface';
import { I18NextEagerPipe } from 'angular-i18next';

interface ReplaceOptions {
	baseClass?: string;
	leftSuffix?: string;
	rightSuffix?: string;
	style?: string;
}

export class TagRuleConverter {
	static convertToRuleTreeData(tagRule: any, maxDepth: number): IRuleTreeData {
		const convertNode = (node: any, depth: number = 0): IRuleNode => {
			if (node.type === 'RULE') {
				// 处理 RULE 类型
				return {
					type: IRuleNodeType.RULE,
					businessData: {
						formGroup: new FormGroup({
							singleRule: new FormControl({
								operator: 'EQUAL',
								tag: node.value,
							}),
						}),
					},
				};
			} else if (node.type === 'GROUP') {
				// 处理 GROUP 类型
				if (node.operator === 'NOT') {
					// 如果是 NOT 类型，视为一个 RULE
					return {
						type: IRuleNodeType.RULE,
						businessData: {
							formGroup: new FormGroup({
								singleRule: new FormControl({
									operator: 'NOT_EQUAL',
									tag: node.children[0].value,
								}),
							}),
						},
					};
				} else {
					// 否则处理为 LOGIC_OPERATOR 类型
					const children = node.children.map((child: any) => convertNode(child, depth + 1));
					const logicOperator = node.operator === 'AND' ? ILogicalOperator.AND : ILogicalOperator.OR;

					// 如果 children 的最大深度未超过 maxDepth，则添加一个 RULE_ADD 节点
					const hasExceededMaxDepth = children.some((child: any) => child.depth >= maxDepth);
					if (!hasExceededMaxDepth) {
						children.push({
							type: IRuleNodeType.RULE_ADD,
						});
					}

					return {
						type: IRuleNodeType.LOGIC_OPERATOR,
						logicOperator,
						children,
						depth: depth,
					};
				}
			} else {
				// 处理其他类型（如 RULE_ADD）
				return {
					type: IRuleNodeType.RULE_ADD,
				};
			}
		};

		return {
			type: IRuleNodeType.LOGIC_OPERATOR,
			logicOperator: tagRule.operator,
			children: [
				...(tagRule.children.map((child: any) => convertNode(child, 1))),
				{
					type: IRuleNodeType.RULE_ADD,
				},
			],
		};
	}

	static convertToRuleStruct(treeData: RuleNodeClass): AdvancedRuleNode {
		const convertNode = (node: RuleNodeClass): AdvancedRuleNode => {
			if (node.type === IRuleNodeType.RULE) {
				const singleRule = node.businessData?.formGroup?.getRawValue()?.singleRule;
				if (singleRule?.operator === 'EQUAL') {
					return {
						type: 'RULE',
						value: singleRule?.tag,
					};
				} else if (singleRule?.operator === 'NOT_EQUAL') {
					return {
						type: 'GROUP',
						operator: 'NOT',
						children: [
							{
								type: 'RULE',
								value: singleRule?.tag,
							},
						],
					};
				}
			} else if (node.type === IRuleNodeType.LOGIC_OPERATOR) {
				const operator = node.logicOperator === ILogicalOperator.AND ? 'AND' : 'OR';
				const children = node.children
					.filter((child: RuleNodeClass) => child.type !== IRuleNodeType.RULE_ADD)
					.map((child: RuleNodeClass) => convertNode(child));

				return {
					type: 'GROUP',
					operator,
					children,
				};
			} else {
				return {
          type: 'GROUP',
          children: [],
        };
			}
      return {
        type: 'GROUP',
        children: [],
      };
		};

		return convertNode(treeData);
	}

	static stringifyNode(data: IRuleNode, i18nPipe: I18NextEagerPipe): string {
		const dataTransform = (nodeData: IRuleNode): string => {
			if (nodeData.type === IRuleNodeType.RULE) {
				const singleRule = nodeData.businessData?.formGroup?.getRawValue()?.singleRule;
				if (!singleRule) {
					return '';
				}
				const { operator, tag } = singleRule;
				if (!tag) {
					return '';
				}
				if (operator === 'EQUAL') {
					return `<span class="tag-content">${tag}</span>`;
				} else if (operator === 'NOT_EQUAL') {
					return `(<span class="tag-content">NOT ${tag}</span>)`;
				}
			} else if (nodeData.type === IRuleNodeType.LOGIC_OPERATOR) {
				const operator = nodeData.logicOperator;
				const children = nodeData.children
					.filter((child: IRuleNode) => child.type !== IRuleNodeType.RULE_ADD)
					.map((child: IRuleNode) => dataTransform(child))
					.filter(Boolean);

				if (!children?.length) {
					return '';
				}

				if (children.length === 1) {
					return children[0];
				}

				return `(${children.join(` ${operator} `)})`;
			}

			return '';
		};

		const toHtml = (tagRuleText: string): string => {
			let result = tagRuleText;
			result = result.replace(/(?<operator>OR|AND)/g, (match) => {
				return `<span class="tag-operator-${match.toLowerCase()}">${match === 'OR' ?
					i18nPipe.transform('common_or') :
					i18nPipe.transform('common_and')}</span>`;
			});

			result = TagRuleConverter.replaceBrackets(result, {
				leftSuffix: '-open',
				rightSuffix: '-close',
			});

			return result;
		};
		return toHtml(dataTransform(data));
	}

	static replaceBrackets(
		input: string,
		options: ReplaceOptions = {},
	): string {
		const {
			baseClass = 'bracket-pair',
			leftSuffix = '-left',
			rightSuffix = '-right',
			style = '',
		} = options;

		const styleAttr = style ? ` style="${style}"` : '';

		const pairStack: number[] = [];
		const replacements: Map<number, string> = new Map();
		let pairId = 0;

		for (let i = 0; i < input.length; i++) {
			const charRes = input[i];

			if (charRes === '(') {
				const currentPairId = pairId++;
				pairStack.push(currentPairId);
				const className = `${baseClass}-${currentPairId}${leftSuffix} ${baseClass}-${currentPairId} tag-rule-brackets`;
				replacements.set(i, `<span class="${className}"${styleAttr}>(</span>`);
			} else if (charRes === ')') {
				if (pairStack.length > 0) {
					const currentPairId = pairStack.pop();
					const className = `${baseClass}-${currentPairId}${rightSuffix} ${baseClass}-${currentPairId} tag-rule-brackets`;
					replacements.set(i, `<span class="${className}"${styleAttr}>)</span>`);
				} else {
					replacements.set(i, `<span class="bracket-unmatched-right"${styleAttr}>)</span>`);
				}
			}
		}


		let result = '';
		for (let i = 0; i < input.length; i++) {
			const replacement = replacements.get(i);
			if (replacement) {
				result += replacement;
			} else {
				result += input[i];
			}
		}

		return result;
	}

  static convertBasicToConstruct(basicTags: string[]): AdvancedRuleNode{
    if (!basicTags?.length) {
      return null;
    }
    return {
      type: 'GROUP',
      operator: 'OR',
      children: basicTags.map(tagName => {
        return {
          value: tagName,
          type: 'RULE',
        }
      })
    }
  }

  static convertConstructToBasic(advancedRule: AdvancedRuleNode): string[] {
    if (!advancedRule) {
      return [];
    }

    if (advancedRule.type === 'RULE') {
      return [advancedRule.value];
    }

    if (advancedRule.type === 'GROUP') {
      return advancedRule.children
        .map(child => this.convertConstructToBasic(child))
        .flat();
    }

    return [];
  }
}
