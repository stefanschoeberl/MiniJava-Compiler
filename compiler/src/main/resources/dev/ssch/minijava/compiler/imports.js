const internals = require('./internal');
const scripts = require('./module.js');

module.exports = runtime => {
    const nativeMethods = {};
    for (let script of scripts) {
        const imports = script(runtime);
        for (let name in imports) {
            if (imports.hasOwnProperty(name)) {
                nativeMethods[name] = imports[name];
            }
        }
    }

    return {
        internal: internals(runtime),
        imports: nativeMethods
    }
};