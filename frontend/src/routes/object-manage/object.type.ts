import { FieldValueContent } from '@routes/agent-center/app-flow/node.type';

export type ObjectParentType = ObjectFieldType | 'none';

export type ObjectType = {
  name?: string;
  description?: string;
  parentType?: ObjectParentType;
  children?: ObjectType[];
  depth?: number;
  expanded?: boolean;
  type: string[];
} & Omit<ObjectField, 'type'>;

export type ObjectFieldSchema = ObjectField[] | ObjectField;

export interface ObjectField {
  name?: string;
  type?: ObjectFieldType;
  description?: string;
  field_type?: string;
  schema?: ObjectFieldSchema;
  value?: ObjectFieldValue;
  reflection?: boolean;
  isDisabled?: boolean;
  aging_level?: 'session' | 'permanent';
  storage_method?: 'auto' | 'assignment';
}

export interface ObjectFieldValue {
  type: ObjectValueType;
  default?: string;
  hint: string;
  content: FieldValueContent;
}
export type ObjectFieldType =
  | 'string'
  | 'number'
  | 'integer'
  | 'boolean'
  | 'array'
  | 'object'
  | 'array<string>'
  | 'array<number>'
  | 'array<integer>'
  | 'array<boolean>'
  | 'array<object>'
  | 'file/default'
  | 'file/doc'
  | 'file/image'
  | 'file/video'
  | 'file/txt'
  | 'file/ppt'
  | 'file/excel'
  | 'array<file/default>'
  | 'array<file/doc>'
  | 'array<file/image>'
  | 'array<file/video>'
  | 'array<file/txt>'
  | 'array<file/ppt>'
  | 'array<file/excel>';

export type ObjectValueType = 'generated' | 'ref' | 'literal' | 'nested' | 'operator';
