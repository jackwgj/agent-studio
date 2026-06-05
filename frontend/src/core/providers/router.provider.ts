import { EnvironmentProviders } from '@angular/core';
import { provideRouter, withHashLocation } from '@angular/router';

import { ROOT_ROUTES } from '@routes/root.routes';

export const ROUTER_PROVIDER: EnvironmentProviders = provideRouter(
  ROOT_ROUTES,
  withHashLocation(),
);
