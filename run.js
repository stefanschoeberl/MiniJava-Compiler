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

    let nextFreeReference = 1;
    const objects = new Map();
    const references = new Map();

    const runtime = {
        memoryView: memoryView,
        wasmRef: (obj) => {
            if (obj == null) {
                return 0;
            } if (objects.has(obj)) {
                return objects.get(obj);
            } else {
                const address = nextFreeReference++;
                objects.set(obj, address);
                references.set(address, obj);
                return address;
            }
        },
        wasmDeref: (address) => {
            return references.get(address);
        }
    };

    const nativeMethods = {};
    for (let script of scripts) {
        const imports = script(runtime);
        for (let name in imports) {
            if (imports.hasOwnProperty(name)) {
                nativeMethods[name] = imports[name];
            }
        }
    }

    const imports = {
        internal: {
            "memory": memory,
            "malloc": numBytes => {
                const address = nextFreeAddress;
                nextFreeAddress += numBytes;
                return address;
            },
            "new_array_numeric": size => {
                return runtime.wasmRef(Array(size).fill(0))
            },
            "new_array_boolean": size => {
                return runtime.wasmRef(Array(size).fill(false))
            },
            "new_array_reference": size => {
                return runtime.wasmRef(Array(size).fill(null))
            },
            "get_array_numeric": (arrayAddress, index) => {
                const a = runtime.wasmDeref(arrayAddress);
                return a[index];
            },
            "get_array_boolean": (arrayAddress, index) => {
                const a = runtime.wasmDeref(arrayAddress);
                return a[index] ? 1 : 0;
            },
            "get_array_reference": (arrayAddress, index) => {
                const a = runtime.wasmDeref(arrayAddress);
                return runtime.wasmRef(a[index]);
            },
            "set_array_numeric": (arrayAddress, index, value) => {
                const a = runtime.wasmDeref(arrayAddress);
                a[index] = value;
            },
            "set_array_boolean": (arrayAddress, index, value) => {
                const a = runtime.wasmDeref(arrayAddress);
                a[index] = value > 0;
            },
            "set_array_reference": (arrayAddress, index, valueAddress) => {
                const a = runtime.wasmDeref(arrayAddress);
                a[index] = runtime.wasmDeref(valueAddress);
            },
        },
        imports: nativeMethods
    };
    const bytes = await readFile(file);
    const module = await WebAssembly.compile(bytes);
    const instance = new WebAssembly.Instance(module, imports);
    instance.exports["Main.main"]();
}

runModule(process.argv[2]);
