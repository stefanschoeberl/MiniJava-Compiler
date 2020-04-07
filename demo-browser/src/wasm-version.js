const Runtime = require('../build/wasm-module/runtime');
const imports = require('../build/wasm-module/imports');

async function runModule() {
    const runtime = new Runtime();
    if (typeof WebAssembly.instantiateStreaming === 'function') {
        const instance = await WebAssembly.instantiateStreaming(fetch('module.wasm'), imports(runtime));
        instance.instance.exports["Main.main"]();
    } else {
        const response = await fetch('module.wasm');
        const arrayBuffer = await response.arrayBuffer();
        const module = await WebAssembly.compile(arrayBuffer);
        const instance = await WebAssembly.instantiate(module, imports(runtime));
        instance.exports["Main.main"]();
    }
}

module.exports = runModule;