import { registerPlugin } from '@capacitor/core';

import type { RestInformationPluginPlugin } from './definitions';

const RestInformationPlugin = registerPlugin<RestInformationPluginPlugin>(
  'RestInformationPlugin',
  {
    web: () => import('./web').then(m => new m.RestInformationPluginWeb()),
  },
);

export * from './definitions';
export { RestInformationPlugin };
