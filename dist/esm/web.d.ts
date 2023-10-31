import { WebPlugin } from '@capacitor/core';
import type { RestInformationPlugin } from './definitions';
import { IScanCall } from "./definitions";
export declare class RestInformationWeb extends WebPlugin implements RestInformationPlugin {
    scan(_scanCall: IScanCall): Promise<{}>;
}
