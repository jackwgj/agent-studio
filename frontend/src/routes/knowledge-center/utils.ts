import { type I18NextEagerPipe } from 'angular-i18next';
import { IDENTIFIER_OPTIONS } from './knowledge.const';
import { SplitMode } from './knowledge.types';

export function buildSplitMode(knowledge: any) {
  const { split_mode } = knowledge?.split_conf || {};
  if (SplitMode.AUTO === split_mode) {
    return 'split_mode_auto';
  } else if (SplitMode.LENGTH === split_mode) {
    return 'split_mode_length';
  }
  return 'split_mode_level';
}

export function buildLevelMode(knowledge: any) {
  const { split_mode } = knowledge?.split_conf || {};
  if (SplitMode.CATALOG === split_mode) {
    return 'level_mode_catalog';
  } else if (SplitMode.RULE === split_mode) {
    return 'level_mode_rule';
  }
  return '';
}

export function buildCombineTitle(knowledge: any) {
  const { combine_title = '' } = knowledge?.split_conf || {};
  if (combine_title === '') {
    return '';
  }
  return combine_title ? 'combine_title_true' : 'combine_title_false';
}

export function buildMergeTitle(knowledge: any) {
  const { merge_titles = '' } = knowledge?.split_conf || {};
  if ('' === merge_titles) {
    return '';
  }
  return merge_titles ? 'open_merge_title' : 'close_merge_title';
}

export function buildTitleLevel(knowledge: any) {
  const { title_level = '' } = knowledge?.split_conf || {};
  if ('' === title_level) {
    return '';
  }
  return title_level;
}

export function buildSeparators(knowledge: any, i18n: I18NextEagerPipe) {
  const { separator_ids = '' } = knowledge?.split_conf || {};
  if ('' === separator_ids) {
    return '';
  }
  return separator_ids
    .reduce((res, separator) => {
      const { value } = IDENTIFIER_OPTIONS.find(
        (item) => item.value === separator,
      );
      res.push(i18n.transform(value));
      return res;
    }, [])
    .join(' ');
}

export function buildChunkSize(knowledge: any) {
  const { chunk_size = '' } = knowledge?.split_conf || {};
  if ('' === chunk_size) {
    return '';
  }
  return chunk_size;
}
