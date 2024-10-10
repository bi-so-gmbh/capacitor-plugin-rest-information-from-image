import { WebPlugin } from "@capacitor/core";
import type { RestInformationPlugin, IScanCall } from "./definitions";
export declare class RestInformationWeb extends WebPlugin implements RestInformationPlugin {
    scan(_scanCall: IScanCall): Promise<object>;
}
