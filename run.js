const fs = require('fs');
const { promisify } = require("util");

// https://webassembly.org/getting-started/js-api/

const readFile = promisify(fs.readFile);

async function runFile(file) {
    const imports = {
        imports: {
            println: arg => console.log(arg)
        }
    };
    const bytes = await readFile(file);
    const module = await WebAssembly.compile(bytes);
    const instance = new WebAssembly.Instance(module, imports);
    instance.exports.main();
}

runFile(process.argv[2]);
