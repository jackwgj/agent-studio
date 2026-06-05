import { EnvironmentProviders, Provider } from '@angular/core';
import HideLeftmenuGuard from '@shared/guard/hideLeftmenu.guard';
import ShowLeftmenuGuard from '@shared/guard/showLeftmenu.guard';
import DevelopGuard from "@shared/guard/develop.guard";
import SpaceTeamGuard from "@shared/guard/spaceTeam.guard"

export const ROUTER_GUARD_PROVIDER: (Provider | EnvironmentProviders)[] = [
  HideLeftmenuGuard,
  ShowLeftmenuGuard,
  DevelopGuard,
  SpaceTeamGuard
];
