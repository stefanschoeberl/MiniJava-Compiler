package dev.ssch.minijava.plugin

import org.gradle.api.tasks.JavaExec

// https://docs.gradle.org/current/userguide/custom_plugins.html
// https://stackoverflow.com/questions/7111362/pulling-a-gradle-dependency-jar-from-maven-and-then-running-it-directly

class MiniJavaCompilationTask extends JavaExec {
    private List<Object> inputFiles = []
    private List<Object> inputDirs = []
    private Object outputDir

    List<Object> getInputFiles() {
        return inputFiles
    }

    void setInputFiles(List<Object> inputFiles) {
        inputs.files(inputFiles)
        this.inputFiles = inputFiles
        updateArgs()
    }

    List<Object> getInputDirs() {
        return inputDirs
    }

    void setInputDirs(List<Object> inputDirs) {
        inputDirs.forEach { inputs.dir(it) }
        this.inputDirs = inputDirs
    }

    Object getOutputDir() {
        return outputDir
    }

    void setOutputDir(Object outputDir) {
        outputs.files(outputDir)
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
        arguments.addAll(inputFiles.collect { it.toString() })
        arguments.addAll(inputDirs.collect { it.toString() })
        args = arguments
    }
}