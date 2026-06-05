import { TreeFormItem } from "../custom-connectors-models/connectors-model";
import {
  SWAGGER_TREE_NODE_MAX_LEVEL,
  TreeInputParamsType,
} from "../custom-connectors-models/connectors-const";
import i18next from 'i18next';
import { Injectable } from "@angular/core";
import { I18nNamespace } from '@i18n';

 enum TreeFormOperatorEnum {
  addSiblingNode = 'addSiblingNode',
  addChildrenNode = 'addChildrenNode',
  deleteNode = 'deleteNode',
  advancedNode = 'advancedNode',
}

@Injectable({
  providedIn: "root",
})
export class ConnectorParamTreeService {
  // 请分析注释constructor(
  // 请分析注释 // 请分析注释private tiMessage: TiMessageService,
  // 请分析注释  private i18n: I18nNamespace,
  // 请分析注释) {}
  constructor() {}

  public addSiblingNode(node: TreeFormItem, treeData, paramsType, isEditFromFlow?: boolean) {
    let item = new TreeFormItem({
      _treeId: this.getMaxTreeId(treeData) + 1,
      pId: node.pId,
      level: node.level,
    });
    if (isEditFromFlow) {
      item.isExtend = true;
    }
    if (paramsType === "query") {
      item.defaultCodeUrl = true;
    }
    item.hasChildren = !this.isBasicType(item.type.label);
    if (paramsType === "body") {
      const childrenIndex = this.getAllChildrenIndex(node, treeData);
      childrenIndex.unshift(this.getNodeInListIndex(node, treeData));
      treeData.splice(Math.max(...childrenIndex) + 1, 0, item);
      this.onNgModelCheckChange(true, item, treeData);
    } else {
      treeData.splice(this.getNodeInListIndex(node, treeData) + 1, 0, item);
    }
    return item;
  }

  public addChildrenNode(
    node: TreeFormItem,
    treeData,
    paramsType,
    isArrayChildNode: boolean,
    isEditFromFlow?: boolean
  ) {
    if (this.getChildrenNodeMaxLevel(node, treeData) >= SWAGGER_TREE_NODE_MAX_LEVEL) {
      // 请分析注释this.tiMessage.open({
      // 请分析注释  type: "warn",
      // 请分析注释  content: "嵌套等级过高",
      // 请分析注释  cancelButton: {
      // 请分析注释    show: false,
      // 请分析注释  },
      // 请分析注释});
      return '';
    }
    node.hasChildren = true;

    let item = new TreeFormItem({
      _treeId: this.getMaxTreeId(treeData) + 1,
      pId: node._treeId,
      level: node.level + 1,
      isArrayChildNode,
      name: isArrayChildNode ? "items" : "",
    });
    if (paramsType === "query") {
      item.defaultCodeUrl = true;
    }
    if (isEditFromFlow) {
      item.isExtend = true;
    }
    item.hasChildren = !this.isBasicType(item.type.label);

    if (paramsType === "body") {
      const childrenIndex = this.getAllChildrenIndex(node, treeData);
      childrenIndex.unshift(this.getNodeInListIndex(node, treeData));
      treeData.splice(Math.max(...childrenIndex) + 1, 0, item);
      this.onNgModelCheckChange(true, item, treeData);
    } else {
      treeData.splice(this.getNodeInListIndex(node, treeData) + 1, 0, item);
    }
    return item;
  }

  getChildrenNodeMaxLevel(node: TreeFormItem, treeData): number {
    const childrenNodes = this.getAllChildrenNodes(node, treeData);
    if (childrenNodes.length === 0) {
      childrenNodes.push(node);
    }
    return Math.max(...childrenNodes.map(item => item.level));
  }

  public deleteChildren(node: TreeFormItem, treeData, paramsType) {
    if (!this.isBasicType(node.type.label)) {
      this.deleteChildrenNode(node, treeData, paramsType);
    } else {
      // 请分析注释 简单类型 删除 当前节点
      treeData.splice(this.getNodeInListIndex(node, treeData), 1);
    }
  }

  public deleteChildrenNode(node: TreeFormItem, treeData, paramsType) {
    if (paramsType === "body") {
      const childrenNodes = this.getAllChildrenNodes(node, treeData);
      childrenNodes.unshift(node);

      childrenNodes.forEach(item => {
        treeData.splice(this.getNodeInListIndex(item, treeData), 1);
      });
    } else {
      const index = this.getNodeInListIndex(node, treeData);
      treeData.splice(index, 1);
    }
  }

  public getAllChildrenNodes(node: TreeFormItem, treeData) {
    const resultData = [];
    const childLists = this.getFirstChildrenNodes(node, treeData);
    resultData.push(...childLists);
    if (childLists.length > 0) {
      for (let i = 0; i < childLists.length; i++) {
        resultData.push(...this.getAllChildrenNodes(childLists[i], treeData));
      }
    }
    return resultData;
  }

  private getAllChildrenIndex(node: TreeFormItem, treeData) {
    const resultIndex = [];
    const childLists = this.getFirstChildrenNodes(node, treeData);
    if (childLists.length > 0) {
      for (let i = 0; i < childLists.length; i++) {
        resultIndex.push(
          this.getNodeInListIndex(childLists[i], treeData),
          ...this.getAllChildrenIndex(childLists[i], treeData)
        );
      }
    }
    return resultIndex;
  }

  private getFirstChildrenNodes(node: TreeFormItem, treeData) {
    return treeData.filter(item => item.pId === node._treeId);
  }

  getMaxTreeId(treeData) {
    return Math.max(...treeData.map(node => node._treeId));
  }

  getNodeInListIndex(node: TreeFormItem, treeData) {
    return treeData.findIndex(item => item._treeId === node._treeId);
  }

  getParentNode(node: TreeFormItem, treeData) {
    return treeData.find(item => item._treeId === node.pId);
  }

  getSiblingNodes(node: TreeFormItem, treeData) {
    return treeData.filter(item => item.pId === node.pId);
  }
  isBasicType(type: TreeInputParamsType): boolean {
    return !["array", "object"].includes(type);
  }

  onNgModelCheckChange(isNecessary: boolean, node: TreeFormItem, treeData): void {
    if (isNecessary) {
      this.setParentNodeLinkRequiredTrue(node, treeData);
    } else {
      this.setParentNodeLinkRequiredFalse(node, treeData);
    }
  }
  setParentNodeLinkRequiredTrue(currentNode: TreeFormItem, treeData) {
    let tempParentNode = this.getParentNode(currentNode, treeData);
    while (tempParentNode.pId !== -1) {
      if (tempParentNode.name === "items") {
        tempParentNode = this.getParentNode(tempParentNode, treeData);
      } else {
        tempParentNode.isNecessary = true;
        tempParentNode = this.getParentNode(tempParentNode, treeData);
      }
      if (tempParentNode.name !== "items" && tempParentNode.isNecessary) {
        break;
      }
    }
  }

  setParentNodeLinkRequiredFalse(currentNode: TreeFormItem, treeData) {
    const setNodeRequiredToUpFn = nodeItem => {
      let tempParentNode = this.getParentNode(nodeItem, treeData);
      let tempNode = { ...nodeItem };
      while (tempParentNode.pId !== -1) {
        if (tempParentNode.name === "items") {
          tempParentNode = this.getParentNode(tempParentNode, treeData);
        } else {
          const siblingNodes = this.getSiblingNodes(tempNode, treeData);
          if (siblingNodes.every(it => !it.isNecessary)) {
            tempParentNode.isNecessary = false;
          }
          tempNode = { ...tempParentNode };
          tempParentNode = this.getParentNode(tempParentNode, treeData);
        }
        if (tempParentNode.name !== "items" && !tempParentNode.isNecessary) {
          break;
        }
      }
    };

    const setNodeRequiredToDownFn = nodeItem => {
      const allChildrenNodes = this.getAllChildrenNodes(nodeItem, treeData);
      allChildrenNodes.forEach(it => {
        if (it.name !== "items") {
          it.isNecessary = false;
        }
      });
    };

    if (this.isBasicType(currentNode.type.label)) {
      setNodeRequiredToUpFn(currentNode);
    } else {
      // 请分析注释to up
      setNodeRequiredToUpFn(currentNode);
      // 请分析注释to down
      setNodeRequiredToDownFn(currentNode);
    }
  }

  getOperationItems(
    node: TreeFormItem,
    paramsType,
    isFunctionArg,
    treeData,
    isFromConnect,
    isResponse,
    isXml?: any,
    isFormData?: boolean
  ): { [key: string]: any }[] {
    let defaultConfigList = [
      { type: TreeFormOperatorEnum.advancedNode, tips: i18next.t('flowutils_653', {ns: I18nNamespace.AGENT_CENTER}) },
      { type: TreeFormOperatorEnum.addSiblingNode, tips: i18next.t('connectorparamtreeservice_970', {ns: I18nNamespace.AGENT_CENTER}) },
      { type: TreeFormOperatorEnum.addChildrenNode, tips: i18next.t('connectorparamtreeservice_971', {ns: I18nNamespace.AGENT_CENTER}) },
      { type: TreeFormOperatorEnum.deleteNode, tips: i18next.t('delete')},
    ].map(item => ({
      ...item,
      defaultUrl: `assets/mssi/images/custom-connector/${item.type}-default.svg`,
      disabledUrl: `assets/mssi/images/custom-connector/${item.type}-disabled.svg`,
      disabled: false,
      show: true,
    }));
    defaultConfigList[0].show = !isFunctionArg;
    defaultConfigList[3].disabled = node.cdmName ? true : false;
    if (this.getNodeInListIndex(node, treeData) === 0) {
      if (node.name === "body") {
        if (isXml) {
          defaultConfigList = defaultConfigList.filter(
            item =>
              item.type === TreeFormOperatorEnum.addChildrenNode || item.type === TreeFormOperatorEnum.advancedNode
          );
        } else {
          if (isResponse) {
            defaultConfigList = defaultConfigList.filter(item => item.type === TreeFormOperatorEnum.addChildrenNode);
          } else
            defaultConfigList = defaultConfigList.filter(
              item =>
                item.type === TreeFormOperatorEnum.addChildrenNode || item.type === TreeFormOperatorEnum.advancedNode
            );
        }
      } else {
        defaultConfigList = defaultConfigList.filter(item => item.type === TreeFormOperatorEnum.addChildrenNode);
      }

      if (node.pId === -1 && node.type.label === "array") {
        if (!isResponse) {
          defaultConfigList[1].disabled = true;
        } else {
          defaultConfigList[0].disabled = true;
        }
      }
    } else {
      if (paramsType !== "body" || this.isBasicType(node.type.label)) {
        defaultConfigList[2].disabled = true;
      }
      if (paramsType === "body") {
        const parentNode = this.getParentNode(node, treeData);
        //当参数类型为array 时，控制操作中的按钮是否可点击
        if (parentNode?.type?.label === "array" && node.pId !== -1) {
          defaultConfigList[1].disabled = true;
          defaultConfigList[3].disabled = true;
        }
        if (node.type.label === "array") {
          defaultConfigList[2].disabled = true;
        }
      }
    }
    if (isFromConnect) {
      for (let index = defaultConfigList.length - 1; index >= 0; index--) {
        if (defaultConfigList[index].type !== "advancedNode") {
          defaultConfigList.splice(index, 1);
        }
      }
    }

    this.handlePath(paramsType, defaultConfigList);
    // 请分析注释需要处理去除数组 defaultConfigList 对象属性里面type 为advancedNode 的对象
    // 请分析注释使用 filter 方法
    const filteredList = defaultConfigList.filter(item => item.type !== 'advancedNode');
    return filteredList;
  }
  handlePath(paramsType: any, defaultConfigList:any) {
    if (paramsType === "path") {
      for (let index = defaultConfigList.length - 1; index >= 0; index--) {
        if (defaultConfigList[index].type !== "advancedNode") {
          defaultConfigList.splice(index, 1);
        }
      }
    }
  }
}
