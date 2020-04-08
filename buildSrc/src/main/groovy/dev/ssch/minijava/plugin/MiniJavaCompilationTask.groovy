package dev.ssch.minijava.plugin

import org.gradle.api.tasks.JavaExec

// https://docs.gradle.org/current/userguide/custom_plugins.html
// https://stackoverflow.com/questions/7111362/pulling-a-gradle-dependency-jar-from-maven-and-then-running-it-directly

class MiniJavaCompilationTask extends JavaExec {
    private List<Object> inputParameters = []
    private Object outputDir

    void inputFiles(List<Object> inputFiles) {
        inputs.files(inputFiles)
        this.inputParameters.addAll(inputFiles)
        updateArgs()
    }

    void inputDirs(Object... inputDirs) {
        Arrays.stream(inputDirs).forEach { inputs.dir(it) }
        this.inputParameters.addAll(inputDirs)
        updateArgs()
    }

    void outputDir(Object outputDir) {
        outputs.dir(outputDir)
        this.outputDir = outputDir
        updateArgs()
    }

    MiniJavaCompilationTask() {
        group = 'MiniJava'
        description = 'Compiles MiniJava source files and generates a WebAssembly module'
        main = 'dev.ssch.minijava.compiler.MainKt'
        classpath project.configurations.minijava
        project.assemble.dependsOn this
    }

    private void updateArgs() {
        def arguments = ['-o']
        arguments.add(outputDir.toString())
        arguments.addAll(inputParameters.collect { it.toString() })
        args = arguments
    }
}