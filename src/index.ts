import { registerPlugin } from '@capacitor/core';

import type { RestInformationPlugin } from './definitions';

const RestInformation = registerPlugin<RestInformationPlugin>('RestInformation', {
  web: () => import('./web').then((m) => new m.RestInformationWeb()),
});

export * from './definitions';
export { RestInformation };
