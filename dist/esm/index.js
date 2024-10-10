import { registerPlugin } from "@capacitor/core";
const RestInformation = registerPlugin("RestInformation", {
    web: () => import("./web").then((m) => new m.RestInformationWeb()),
});
export * from "./definitions";
export { RestInformation };
//# sourceMappingURL=index.js.map