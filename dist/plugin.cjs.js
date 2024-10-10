'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var core = require('@capacitor/core');

const RestInformation = core.registerPlugin('RestInformation', {
    web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.RestInformationWeb()),
});

class RestInformationWeb extends core.WebPlugin {
    async scan(_scanCall) {
        return Promise.reject(new Error('PLATFORM_NOT_SUPPORTED'));
    }
}

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    RestInformationWeb: RestInformationWeb
});

exports.RestInformation = RestInformation;
//# sourceMappingURL=plugin.cjs.js.map
