export interface TreeNode {
  children?: TreeNode[];
  [key: string]: any;
}

export const TreeUtil = {
  traverse(nodes: TreeNode[], callback: (node: TreeNode) => void): void {
    if (!nodes || !Array.isArray(nodes)) {
      return;
    }
    
    const traverseRecursive = (nodeList: TreeNode[]): void => {
      for (const node of nodeList) {
        callback(node);
        if (node.children && Array.isArray(node.children)) {
          traverseRecursive(node.children);
        }
      }
    };
    
    traverseRecursive(nodes);
  },

  getParentNode(nodes: TreeNode[], targetNode: TreeNode): TreeNode | null {
    if (!nodes || !Array.isArray(nodes)) {
      return null;
    }

    const findParent = (
      nodeList: TreeNode[],
      parent: TreeNode | null
    ): TreeNode | null => {
      for (const node of nodeList) {
        if (node === targetNode) {
          return parent;
        }
        if (node.children && Array.isArray(node.children)) {
          const result = findParent(node.children, node);
          if (result !== null) {
            return result;
          }
        }
      }
      return null;
    };

    return findParent(nodes, null);
  },

  findNode(nodes: TreeNode[], predicate: (node: TreeNode) => boolean): TreeNode | null {
    if (!nodes || !Array.isArray(nodes)) {
      return null;
    }

    const findRecursive = (nodeList: TreeNode[]): TreeNode | null => {
      for (const node of nodeList) {
        if (predicate(node)) {
          return node;
        }
        if (node.children && Array.isArray(node.children)) {
          const result = findRecursive(node.children);
          if (result !== null) {
            return result;
          }
        }
      }
      return null;
    };

    return findRecursive(nodes);
  },

  filterNodes(nodes: TreeNode[], predicate: (node: TreeNode) => boolean): TreeNode[] {
    if (!nodes || !Array.isArray(nodes)) {
      return [];
    }

    const result: TreeNode[] = [];

    const filterRecursive = (nodeList: TreeNode[]): void => {
      for (const node of nodeList) {
        if (predicate(node)) {
          result.push(node);
        }
        if (node.children && Array.isArray(node.children)) {
          filterRecursive(node.children);
        }
      }
    };

    filterRecursive(nodes);
    return result;
  },

  mapNodes<T>(nodes: TreeNode[], callback: (node: TreeNode) => T): T[] {
    if (!nodes || !Array.isArray(nodes)) {
      return [];
    }

    const result: T[] = [];

    const mapRecursive = (nodeList: TreeNode[]): void => {
      for (const node of nodeList) {
        result.push(callback(node));
        if (node.children && Array.isArray(node.children)) {
          mapRecursive(node.children);
        }
      }
    };

    mapRecursive(nodes);
    return result;
  },

  some(nodes: TreeNode[], predicate: (node: TreeNode) => boolean): boolean {
    if (!nodes || !Array.isArray(nodes)) {
      return false;
    }

    const someRecursive = (nodeList: TreeNode[]): boolean => {
      for (const node of nodeList) {
        if (predicate(node)) {
          return true;
        }
        if (node.children && Array.isArray(node.children)) {
          if (someRecursive(node.children)) {
            return true;
          }
        }
      }
      return false;
    };

    return someRecursive(nodes);
  },

  every(nodes: TreeNode[], predicate: (node: TreeNode) => boolean): boolean {
    if (!nodes || !Array.isArray(nodes)) {
      return true;
    }

    const everyRecursive = (nodeList: TreeNode[]): boolean => {
      for (const node of nodeList) {
        if (!predicate(node)) {
          return false;
        }
        if (node.children && Array.isArray(node.children)) {
          if (!everyRecursive(node.children)) {
            return false;
          }
        }
      }
      return true;
    };

    return everyRecursive(nodes);
  }
};
