const fs = require('fs');
const { promisify } = require("util");

// https://webassembly.org/getting-started/js-api/

const readFile = promisify(fs.readFile);

async function runFile(file) {
    const memory = new WebAssembly.Memory({initial:1});
    let nextFreeAddress = 1;

    const imports = {
        internal: {
            "memory": memory,
        },
        imports: {
            "println#int": arg => console.log(arg),
            "println#float": arg => console.log(arg),
            "println#boolean": arg => console.log(arg === 0 ? 'false' : 'true'),
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
