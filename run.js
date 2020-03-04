const fs = require('fs');
const { promisify } = require("util");

// https://webassembly.org/getting-started/js-api/

const readFile = promisify(fs.readFile);

async function runFile(file) {
    const memory = new WebAssembly.Memory({initial:1});
    const memoryView = new DataView(memory.buffer);
    let nextFreeAddress = 1;

    const imports = {
        internal: {
            "memory": memory,
            "malloc": numBytes => {
                const address = nextFreeAddress;
                nextFreeAddress += numBytes;
                return address;
            }
        },
        imports: {
            "Main.println#int": arg => console.log(arg),
            "Main.println#float": arg => console.log(arg),
            "Main.println#boolean": arg => console.log(arg === 0 ? 'false' : 'true'),
            "Main.println#int[]": offset => {
                const size = memoryView.getInt32(offset, true);
                const firstElement = offset + 4;
                const elementSize = 4;
                const lastElement = firstElement + size * elementSize;
                let result = [];
                for (let i = firstElement; i < lastElement; i += elementSize) {
                    result.push(memoryView.getInt32(i, true));
                }
                console.log('[' + result.join(', ') + ']');
            },
            "Main.println#boolean[]": offset => {
                const size = memoryView.getInt32(offset, true);
                const firstElement = offset + 4;
                const lastElement = firstElement + size;
                let result = [];
                for (let i = firstElement; i < lastElement; i++) {
                    result.push(memoryView.getInt8(i) === 0 ? 'false' : 'true');
                }
                console.log('[' + result.join(', ') + ']');
            },
            "Main.println#float[]": offset => {
                const size = memoryView.getInt32(offset, true);
                const firstElement = offset + 4;
                const elementSize = 4;
                const lastElement = firstElement + size * elementSize;
                let result = [];
                for (let i = firstElement; i < lastElement; i += elementSize) {
                    result.push(memoryView.getFloat32(i, true));
                }
                console.log('[' + result.join(', ') + ']');
            }
        }
    };
    const bytes = await readFile(file);
    const module = await WebAssembly.compile(bytes);
    const instance = new WebAssembly.Instance(module, imports);
    instance.exports["Main.main"]();
}

runFile(process.argv[2]);
