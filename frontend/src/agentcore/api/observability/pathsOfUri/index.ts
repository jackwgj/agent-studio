import agentsPaths, { ITracesApis } from './traces.api';

export type IApisProxyAll = ITracesApis;

export default {
  ...agentsPaths,
};
