# MiniJava-Compiler for WebAssembly - Master's Thesis Project

A compiler for a subset of Java (MiniJava) implemented in Kotlin with the support of ANTLR.

## Requirements

* JDK 8 or above
* ```wat2wasm```
  * tested version: 1.0.12
  * part of [WABT](https://github.com/WebAssembly/wabt)
  * should be locatable in your $PATH
  * [Download prebuilt WABT here](https://github.com/WebAssembly/wabt/releases)
* Docker
  * tested version: 19.03.8
  * [Docker Installation Guide](https://docs.docker.com/get-docker/)
* Docker Compose
  * tested version: 1.25.5
  * [Docker Compose Installation Guide](https://docs.docker.com/compose/install/)
* Node.js
  * tested version: 12.16.3
  * [Download Node.js here](https://nodejs.org/en/)

## Quick Start (Hello World)
Step 1: Build the compiler
```
$ ./gradlew compiler:allInOneJar
```

Step 2: Compile the ```demo.minijava``` file
```
$ java -jar compiler/build/libs/compiler-all-in-one-1.0.jar stdlib/core demo.minijava -o wasm-output
```

Step 3: Run it 
```
$ ./run ./wasm-output
```

Step 4: Experiment with MiniJava in the ```demo.minijava``` file!

## Node.js demo application
Step 1: Build the application

```
$ ./gradlew demo-nodejs:build
```

Step 2: Run it
```
$ cd demo-nodejs
$ npm run start
```

## Browser demo application
Step 1: Build the application
```
$ ./gradlew demo-browser:build
```

Step 2: Run it
```
$ cd demo-browser
$ docker-compose up
```
Navigate to [http://localhost:8080](http://localhost:8080)
