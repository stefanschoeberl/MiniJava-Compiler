const fs = require('fs');
const path = require('path');
const { promisify } = require("util");

// https://webassembly.org/getting-started/js-api/

const readFile = promisify(fs.readFile);

async function runModule(folder) {
    const folderAbsolute = path.resolve(folder);
    const file = path.join(folderAbsolute, "module.wasm");

    const Runtime = require(path.join(folderAbsolute, 'runtime'));

    const runtime = new Runtime();
    const imports = require(path.join(folderAbsolute, 'imports'))(runtime);

    const bytes = await readFile(file);
    const module = await WebAssembly.compile(bytes);
    const instance = new WebAssembly.Instance(module, imports);
    instance.exports["Main.main"]();
}

runModule(process.argv[2]);
