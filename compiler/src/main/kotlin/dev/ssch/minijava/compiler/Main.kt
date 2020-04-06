package dev.ssch.minijava.compiler

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.io.File

fun main(args: Array<String>) {
    val options = Options()
    options.addRequiredOption("o", "output", true,"output folder")
    val parser = DefaultParser()
    val cli = parser.parse(options, args)

    val inputFiles = cli.argList
    val outputFolder = cli.getOptionValue("o")

    val context = CompilationContext()

    val sourceDefinitions = inputFiles.map(::File)
    val files = context.compiler.expandSourceDefinitions(sourceDefinitions)
    val bundle = context.compiler.compile(files)

    context.bundleGenerator.generateBundle(bundle, files, File(outputFolder))
}