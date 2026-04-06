#!/bin/bash
//usr/bin/env groovy -cp lib/:lib/main.jar "$0" $@; exit $?

import Chat
import Forge
import Test

import org.kleypas.muc.cli.Cli
import org.kleypas.muc.cli.Logger
import org.kleypas.muc.cli.LogLevel

import org.kleypas.muc.illustrator.Illustrator
import org.kleypas.muc.illustrator.ImageType

Cli cli = new Cli()
def options = cli.parse(args)
def version = "1.3-majel"

Logger.info "Welcome to the Muc. Starting execution loop."

/*
 * Start up a chat. Takes the args value as a string location of an existing
 * or new journal to work on
 */
if (options.chat) {
    Logger.info "# Sent a chat arg."
    new Chat(options).run()
}

/*
 * Burn some tokens with this one. Runs inference in a loop over handshakes.
 */
if (options.refinery) {
    Logger.info "# Sent a refinery arg. Starting the Forge."
    new Forge(iterations: 5).run()
}

/*
 * Dive directly into an image generation prompt
 */
if (options.image) {
    Logger.info "# Sent an image arg."
    def illustrator = new Illustrator()
    illustrator.style = ImageType.PORTRAIT
    illustrator.title = "ComfyUI"

    print "## You: "
    def input = System.in.newReader().readLine().trim()
    def prompt = illustrator.getPrompt(input)

    def recipt = illustrator.generateImage(prompt)
    Logger.info "## Recipt:\r\n${recipt}"
}

/*
 * I know I said "no-build", but sometimes it is nice to have a compiled
 * jar that we can shove into a class path.
 */
if (options.build) {
    Logger.info "Starting project build (Style B: Compile and Package)..."

    // --- 1. Define Build Paths ---
    def buildDir = "build"
    def classesDir = "${buildDir}/classes"
    def libDir = "lib"
    def jarName = "${libDir}/muc.jar"

    // --- 2. Clean previous build and create new directories ---
    Logger.debug "Cleaning up previous build artifacts..."
    cli.runCommand("rm -rf ${buildDir} ${libDir}")
    cli.runCommand("mkdir -p ${classesDir} ${libDir}")

    // --- 3. Compile all Style B Groovy Modules (.groovy files in org/kleypas/muc/...) ---
    Logger.info "Compiling Groovy source files..."

    def compileCommand = "groovyc -d ${classesDir} main.groovy"
    def compileCode = cli.runCommand(compileCommand)

    if (compileCode != 0) {
        Logger.fatal "Compilation FAILED. Exiting."
        System.exit(1)
    }

    // --- 4. Package Compiled Classes into a JAR ---
    Logger.info "Creating application JAR: ${jarName}..."

    // To create a runnable JAR, we must include a Manifest file defining the Main-Class.
    // Since main.groovy is a script, Groovyc compiles it to a class typically named 'main.class'.
    // We create a temporary Manifest file for the 'jar' utility.

    def manifestContent = "Main-Class: main" // 'main' is the assumed compiled script name
    def manifestPath = "${buildDir}/MANIFEST.MF"
    new File(manifestPath).text = manifestContent

    // Command Breakdown:
    // c: create a new archive
    // v: verbose output
    // f: specify the JAR file name
    // m: include the manifest file
    // -C: change directory (ensures the JAR path starts at the root of the class files)
    def jarCommand = "jar cvfm ${jarName} ${manifestPath} -C ${classesDir} ."
    def jarCode = cli.runCommand(jarCommand)

    if (jarCode != 0) {
        Logger.fatal "JAR creation FAILED. Exiting."
        System.exit(1)
    }

    Logger.info "Build SUCCESSFUL. Application JAR created at: ${jarName}"
    System.exit(0)
}

/*
 * Finally, a test routine. You get to meet George here.
 */
if (options.test) {
    Logger.info "# Sent a test arg."
    new Test().run()
}
