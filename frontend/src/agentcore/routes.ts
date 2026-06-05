import { Routes } from '@angular/router';
import { I18NEXT_NAMESPACE_RESOLVER } from 'angular-i18next';
import { environment } from 'src/environment/environment';
import { AGENT_CORE, I18N_NAMESPACE } from './constants/common';
import { CfService } from './core/cf.service';

const routes: Routes = [
  {
    path: 'skill',
    loadChildren: () => import('./library/skill/skill.router').then((m) => m.skillRoutes),
    data: {
      i18nextNamespaces: [I18N_NAMESPACE.Common, I18N_NAMESPACE.Skill],
    },
  },
];

export const cfServiceGuard = async () => {
  if (CfService.isInit()) {
    return true;
  }
  await CfService.init();
  return true;
};

export const agentCoreRoutes = routes.map((v) => {
  // 处理空间前缀
  if (v.data?.i18nextNamespaces) {
    v.data.i18nextNamespaces = v.data.i18nextNamespaces.map((ns) =>
      ns.startsWith(AGENT_CORE) ? ns : `${AGENT_CORE}.${ns}`,
    );

    // 自动加上common空间词条
    v.data.i18nextNamespaces.push(`${AGENT_CORE}.${I18N_NAMESPACE.Common}`);

    // 自动加上namesapce resolve
    v.resolve = {
      ...v.resolve,
      i18next: I18NEXT_NAMESPACE_RESOLVER,
    };
  }

  if (v.canActivate) {
    v.canActivate.push(cfServiceGuard);
  } else {
    v.canActivate = [cfServiceGuard];
  }
  return v;
});
