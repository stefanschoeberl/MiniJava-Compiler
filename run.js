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
        },
        imports: {
            "println#int": arg => console.log(arg),
            "println#float": arg => console.log(arg),
            "println#boolean": arg => console.log(arg === 0 ? 'false' : 'true'),
            "println#int[]": offset => {
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
            "malloc#int": numBytes => {
                const address = nextFreeAddress;
                nextFreeAddress += numBytes;
                return address;
            }
        }
    };
    const bytes = await readFile(file);
    const module = await WebAssembly.compile(bytes);
    const instance = new WebAssembly.Instance(module, imports);
    instance.exports.main();
}

runFile(process.argv[2]);
