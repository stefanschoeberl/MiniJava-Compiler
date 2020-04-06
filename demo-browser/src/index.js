const Runtime = require('../build/wasm-module/runtime');
const imports = require('../build/wasm-module/imports');

async function runModule() {
    const runtime = new Runtime();
    const instance = await WebAssembly.instantiateStreaming(fetch('module.wasm'), imports(runtime));
    instance.instance.exports["Main.main"]();
}

runModule();