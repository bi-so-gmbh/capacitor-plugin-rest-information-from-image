import { WebPlugin } from "@capacitor/core";

import type { RestInformationPlugin, IScanCall } from "./definitions";

export class RestInformationWeb
  extends WebPlugin
  implements RestInformationPlugin
{
  async scan(_scanCall: IScanCall): Promise<object> {
    return Promise.reject(new Error("PLATFORM_NOT_SUPPORTED"));
  }
}
