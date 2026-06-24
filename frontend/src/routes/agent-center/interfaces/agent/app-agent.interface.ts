export interface MemoryVariableType {
  id: string;
  name: string;
  active: boolean;
}

export interface MemoryVariableUserParamType {
  key: string;
  value: string;
  description: string;
}

export interface MemoryVariableInputParamType {
  key: string;
  value: string | number | boolean;
  type: string;
  description: string;
  isRight: boolean;
  errorMsg?: string;
}

export interface CommonSelectType {
  label: string;
  value: string;
}
