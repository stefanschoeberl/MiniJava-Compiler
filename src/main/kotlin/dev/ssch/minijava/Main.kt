package dev.ssch.minijava

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

    val files = inputFiles.map(::File)
    val compiler = Compiler()
    val module = compiler.compile(files)

    val bundleGenerator = BundleGenerator()
    bundleGenerator.generateBundle(module, files, File(outputFolder))
}