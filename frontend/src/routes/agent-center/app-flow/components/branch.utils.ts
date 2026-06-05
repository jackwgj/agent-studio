import { I18NextEagerPipe } from 'angular-i18next';
import { cloneDeep } from 'lodash';
import {
  IBranchCondition,
  IBranchConfigs,
  IBranchOp,
  IParamRef,
  IWorkflowField,
  IWorkflowFieldType,
} from '../node.type';
import { NodeUtils } from './utils';

function getCommonConditions(i18n: I18NextEagerPipe) {
  return [
    {
      label: i18n.transform('acf_contain'),
      value: 'contain',
    },
    {
      label: i18n.transform('acf_not_contain'),
      value: 'not_contain',
    },
    {
      label: i18n.transform('acf_is_empty'),
      value: 'is_empty',
    },
    {
      label: i18n.transform('acf_is_not_empty'),
      value: 'is_not_empty',
    },
  ];
}

export const BranchUtils = {
  getStrConditionOptions(i18n: I18NextEagerPipe) {
    return [
      {
        label: i18n.transform('acf_longer_than'),
        value: 'longer_than',
      },
      {
        label: i18n.transform('acf_longer_than_or_eq'),
        value: 'longer_than_or_eq',
      },
      {
        label: i18n.transform('acf_shorter_than'),
        value: 'shorter_than',
      },
      {
        label: i18n.transform('acf_shorter_than_or_eq'),
        value: 'shorter_than_or_eq',
      },
      { label: i18n.transform('acf_eq'), value: 'eq', notation: '=' },
      {
        label: i18n.transform('acf_not_eq'),
        value: 'not_eq',
      },
      ...getCommonConditions(i18n),
    ];
  },

  getArrConditionOptions(i18n: I18NextEagerPipe) {
    return [
      {
        label: i18n.transform('acf_longer_than'),
        value: 'longer_than',
      },
      {
        label: i18n.transform('acf_longer_than_or_eq'),
        value: 'longer_than_or_eq',
      },
      {
        label: i18n.transform('acf_shorter_than'),
        value: 'shorter_than',
      },
      {
        label: i18n.transform('acf_shorter_than_or_eq'),
        value: 'shorter_than_or_eq',
      },
      ...getCommonConditions(i18n),
    ];
  },

  getNumConditionOptions(i18n: I18NextEagerPipe) {
    return [
      {
        label: i18n.transform('acf_larger_than'),
        value: 'longer_than',
      },
      {
        label: i18n.transform('acf_larger_than_or_eq'),
        value: 'longer_than_or_eq',
      },
      {
        label: i18n.transform('acf_smaller_than'),
        value: 'shorter_than',
      },
      {
        label: i18n.transform('acf_smaller_than_or_eq'),
        value: 'shorter_than_or_eq',
      },
      { label: i18n.transform('acf_eq'), value: 'eq', notation: '=' },
      {
        label: i18n.transform('acf_not_eq'),
        value: 'not_eq',
      },
    ];
  },

  getBoolConditionOptions(i18n: I18NextEagerPipe) {
    return [
      {
        label: i18n.transform('acf_eq_true'),
        value: 'eq_true',
      },
      {
        label: i18n.transform('acf_eq_false'),
        value: 'eq_false',
      },
    ];
  },

  getObjConditionOptions(i18n: I18NextEagerPipe) {
    return [
      {
        label: i18n.transform('acf_is_empty'),
        value: 'is_empty',
      },
      {
        label: i18n.transform('acf_is_not_empty'),
        value: 'is_not_empty',
      },
    ];
  },

  getConditionOpIconClassName(op: string) {
    const icons = {
      longer_than: 'n-longer-than',
      longer_than_or_eq: 'n-longer-than-or-eq',
      shorter_than: 'n-shorter-than',
      shorter_than_or_eq: 'n-shorter-than-or-eq',
      contain: 'n-contain',
      not_contain: 'n-not-contain',
      is_empty: 'n-eq',
      is_not_empty: 'n-not-eq',
      eq: 'n-eq',
      not_eq: 'n-not-eq',
    };

    return icons[op] ?? 'n-eq';
  },

  getBooleanOps() {
    return [
      {
        label: 'true',
        value: true,
      },
      {
        label: 'false',
        value: false,
      },
    ];
  },

  getLeftVarObj(): IWorkflowField {
    return {
      name: '',
      description: '',
      required: false,
      value: {
        type: 'ref',
        content: {
          ref_node_id: '',
          ref_var_name: '',
          source: 'user',
        },
        hint: '',
      },
      source: 'user',
    };
  },

  getRightVarObj(): IWorkflowField {
    return {
      name: '',
      description: '',
      required: false,
      value: {
        type: 'literal',
        content: '',
        hint: '',
      },
      source: 'user',
    };
  },

  getNewBranchConfigs(ops: IParamRef[]): IBranchConfigs {
    return {
      logic: 'and',
      conditions: [
        {
          operator: 'eq',
          left: NodeUtils.initRefField(BranchUtils.getLeftVarObj(), ops),
          right: NodeUtils.initRefField(BranchUtils.getLeftVarObj(), ops),
        },
      ],
    };
  },

  getNewBranchBlankConfigs(): IBranchConfigs {
    return {
      logic: 'and',
      conditions: [],
    };
  },

  onExpChange(configs: IBranchConfigs) {
    const op = configs.logic;
    configs.logic = op === 'and' ? 'or' : 'and';
  },

  opChange(op: IBranchOp, con: IBranchCondition) {
    if (['is_empty', 'is_not_empty', 'eq_true', 'eq_false'].includes(op)) {
      con.right.value.type = 'literal';

      const valMap = {
        is_empty: '',
        is_not_empty: '',
        eq_true: 'true',
        eq_false: 'false',
      };

      con.right.value.content = valMap[op];
      return;
    }

    BranchUtils.initRightValue(con);
  },

  initRightValue(con: IBranchCondition) {
    if (con.right.value.type === 'literal') {
      const typeValMap = {
        string: '',
        number: 0,
        integer: 0,
        boolean: true,
      };

      const leftInfo = (con.left.value.content as IParamRef[])[0];

      if (leftInfo.type === 'string') {
        if (BranchUtils.isCompareLen(con.operator)) {
          con.right.type = 'integer';
        } else {
          con.right.type = 'string';
        }
      }

      if (leftInfo.type.startsWith('array')) {
        if (BranchUtils.isCompareLen(con.operator)) {
          con.right.type = 'integer';
        } else {
          con.right.type = (leftInfo.schema as IWorkflowField).type;
        }
      }

      if (['integer', 'number'].includes(leftInfo.type)) {
        con.right.type = 'number';
      }

      con.right.value.content = typeValMap[con.right.type];
    }
  },

  isCompareLen(op: IBranchOp) {
    return [
      'longer_than',
      'longer_than_or_eq',
      'shorter_than',
      'shorter_than_or_eq',
    ].includes(op);
  },

  leftTypeChange(param: IParamRef[], con: IBranchCondition) {
    // array<object>
    const type = param[0]?.type as IWorkflowFieldType;

    if (type === 'object') {
      con.operator = 'is_empty';
      return;
    }

    if (type === 'boolean') {
      con.operator = 'eq_true';
      con.right.value.type = 'literal';
      con.right.value.content = 'true';
      con.right.type = 'boolean';
      return;
    }

    con.operator = 'longer_than';

    if (con.right.value.type === 'literal') {
      con.right.value.content = 0;
      con.right.type = 'integer';
    }
  },

  onTypeChange(con: IBranchCondition) {
    con.right.value.content = NodeUtils.getChangeContent(con.right.value.type);
    BranchUtils.initRightValue(con);
  },

  getBranchConditionObj(ops: IParamRef[]): IBranchCondition {
    return {
      operator: 'eq',
      left: NodeUtils.initRefField(BranchUtils.getLeftVarObj(), ops),
      right: NodeUtils.initRefField(BranchUtils.getRightVarObj(), ops),
    };
  },

  buildViewCondition(condition: IBranchCondition, ops: IParamRef[]) {
    let operator = condition.operator;
    const left = NodeUtils.initRefField(condition.left, ops);

    let right: IWorkflowField = null;
    if (!condition?.right) {
      right = {
        name: '',
        description: '',
        required: false,
        value: {
          type: 'literal',
          content: '',
          hint: '',
        },
        source: 'user',
      };
    } else {
      right = condition.right;
    }
    right = NodeUtils.initRefField(right, ops);

    if (left.type === 'boolean') {
      if (right.value.content === true) {
        operator = 'eq_true';
      }

      if (right.value.content === false) {
        operator = 'eq_false';
      }
      right.value.content = String(right.value.content);
    }

    const con = {
      operator,
      left,
      right,
    };

    return con;
  },

  branchConfigsMapper(configs: IBranchConfigs): IBranchConfigs {
    const branchConfigs = cloneDeep(configs);
    branchConfigs.conditions = branchConfigs.conditions.map((condition) => {
      if (['eq_true', 'eq_false'].includes(condition.operator)) {
        condition.right.value.content = condition.right.value.content === 'true';
      }
      const con = {
        ...condition,
        left: NodeUtils.getDtoInput(condition.left),
        right: NodeUtils.getDtoInput(condition.right),
      };

      if (
        ['is_empty', 'is_not_empty', 'eq_true', 'eq_false'].includes(
          con.operator,
        )
      ) {
        if (['is_empty', 'is_not_empty'].includes(con.operator)) {
          con.right = null;
        }

        if (['eq_true', 'eq_false'].includes(con.operator)) {
          con.operator = 'eq';
        }

        return con;
      }

      if (con.right.value.type === 'literal') {
        if (
          con.left.type === 'string' &&
          BranchUtils.isCompareLen(con.operator)
        ) {
          con.right.value.content = Number(con.right.value.content);
        }

        if (con.left.type === 'array') {
          if (BranchUtils.isCompareLen(con.operator)) {
            con.right.value.content = Number(con.right.value.content);
          }

          if (['number', 'integer'].includes(con.right.type)) {
            con.right.value.content = Number(con.right.value.content);
          }
        }

        if (['number', 'integer'].includes(con.left.type)) {
          con.right.type = 'number';
        }
      }

      return con;
    });

    return branchConfigs;
  },
};
