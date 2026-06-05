import { FlowParsedTask } from "../../flow-async-test.interface";

export interface TaskGroup {
  id: string;
  groupLabel: string;
  taskList: FlowParsedTask[];
}