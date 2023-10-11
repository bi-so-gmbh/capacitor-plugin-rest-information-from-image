import { WebPlugin } from '@capacitor/core';

import type { RestInformationPlugin } from './definitions';
import {IScanCall} from "./definitions";

export class RestInformationWeb
  extends WebPlugin
  implements RestInformationPlugin
{
  async scan(_scanCall: IScanCall): Promise<{}> {
    return Promise.reject(new Error("PLATFORM_NOT_SUPPORTED"));
  }
}
