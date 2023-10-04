import { WebPlugin } from '@capacitor/core';

import type { RestInformationPluginPlugin } from './definitions';

export class RestInformationPluginWeb
  extends WebPlugin
  implements RestInformationPluginPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
