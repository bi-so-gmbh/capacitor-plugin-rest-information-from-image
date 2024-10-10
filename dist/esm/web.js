import { WebPlugin } from "@capacitor/core";
export class RestInformationWeb extends WebPlugin {
    async scan(_scanCall) {
        return Promise.reject(new Error("PLATFORM_NOT_SUPPORTED"));
    }
}
//# sourceMappingURL=web.js.map