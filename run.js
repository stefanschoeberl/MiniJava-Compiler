const fs = require('fs');
const path = require('path');
const { promisify } = require("util");

// https://webassembly.org/getting-started/js-api/

const readFile = promisify(fs.readFile);

async function runModule(folder) {
    const folderAbsolute = path.resolve(folder);
    const file = path.join(folderAbsolute, "module.wasm");
    const scripts = require(path.join(folderAbsolute, "module"));

    const memory = new WebAssembly.Memory({initial:1});
    const memoryView = new DataView(memory.buffer);
    let nextFreeAddress = 1;

    const runtime = {
        memoryView: memoryView
    };

    const nativeMethods = {};
    for (let script of scripts) {
        const imports = script(runtime);
        for (let name in imports) {
            nativeMethods[name] = imports[name];
        }
    }

    const imports = {
        internal: {
            "memory": memory,
            "malloc": numBytes => {
                const address = nextFreeAddress;
                nextFreeAddress += numBytes;
                return address;
            }
        },
        imports: nativeMethods
    };
    const bytes = await readFile(file);
    const module = await WebAssembly.compile(bytes);
    const instance = new WebAssembly.Instance(module, imports);
    instance.exports["Main.main"]();
}

runModule(process.argv[2]);
