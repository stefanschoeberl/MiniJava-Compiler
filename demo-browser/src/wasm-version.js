const Runtime = require('../build/wasm-module/runtime');
const imports = require('../build/wasm-module/imports');

async function runModule() {
    const runtime = new Runtime();
    let instance;
    if (typeof WebAssembly.instantiateStreaming === 'function') {
        const result = await WebAssembly.instantiateStreaming(fetch('module.wasm'), imports(runtime));
        instance = result.instance;

    } else {
        const response = await fetch('module.wasm');
        const arrayBuffer = await response.arrayBuffer();
        const module = await WebAssembly.compile(arrayBuffer);
        instance = await WebAssembly.instantiate(module, imports(runtime));
    }

    runtime.setWasmModuleInstance(instance);
    runtime.staticMethod('Main', 'main')();
}

module.exports = runModule;