export interface ITask {
  name: string;
  id: string;
  tags?: {
    id: string;
    name: string;
  }[];
  desc?: string;
}
