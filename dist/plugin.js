var capacitorRestInformationPlugin = (function (exports, core) {
    'use strict';

    const RestInformation = core.registerPlugin("RestInformation", {
        web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.RestInformationWeb()),
    });

    class RestInformationWeb extends core.WebPlugin {
        async scan(_scanCall) {
            return Promise.reject(new Error("PLATFORM_NOT_SUPPORTED"));
        }
    }

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        RestInformationWeb: RestInformationWeb
    });

    exports.RestInformation = RestInformation;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
