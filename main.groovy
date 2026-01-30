#!/bin/bash
//usr/bin/env groovy -cp lib/:lib/main.jar "$0" $@; exit $?

import org.kleypas.muc.model.Context
import org.kleypas.muc.model.Message
import org.kleypas.muc.model.Model
import org.kleypas.muc.model.TagParser

import org.kleypas.muc.illustrator.Illustrator
import org.kleypas.muc.illustrator.ImageType

import org.kleypas.muc.cli.Cli
import org.kleypas.muc.cli.Logger
import org.kleypas.muc.cli.LogLevel

import Test

Cli cli = new Cli()
def options = cli.parse(args)

Logger.info "Welcome to the Muc. Starting execution loop."

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

// Ok, first step was hashtag simplify by at least
// moving it into a helper class in Test.groovy. We can
// refine it from here at least.
if (options.test) {
    Logger.info "# Sent a test arg."
    def test = new Test()

    test.rng()
    test.location()
    test.character()
    test.inventory()
    test.narrator()
    test.illustrator()
    test.story()
}

// Prompt for input to generate an image.
if (options.image) {
    Logger.info "# Sent an image arg."
    Logger.setLevel(LogLevel.INFO)
    def illustrator = new Illustrator()
    illustrator.style = ImageType.PORTRAIT
    illustrator.title = "ComfyUI"

    print "## You: "
    def input = System.in.newReader().readLine().trim()
    def prompt = illustrator.getPrompt(input)

    def recipt = illustrator.generateImage(prompt)
    Logger.info "## Recipt:\r\n${recipt}"
}

// Get directly to a chat.
if (options.chat) {
    if (options.test) { return }

    def promptText = new File("Characters/George.md").text
    def characterSheet = new File("Characters/George.json").text

    promptText = promptText + "\r### This is your json formatted character sheet:\r${characterSheet}"

    def locationSheet = new File("Locations/Library.json").text
    promptText = promptText + "\r### This is your json formatted location details:\r${locationSheet}"

    Logger.info "# Sent a chat arg."
    Logger.setLevel(LogLevel.INFO)
    def context = new Context().enableLogging("Story/Story_live.jsonl")
    def model = new Model(model: "biggun", body: context)
    model.body.addMessage("system", promptText)

    while (true) {
        print "## You: "
        def input = System.in.newReader().readLine().trim()
        if (input.contains("/bye")) { return }

        def lastMessage = model.body.getLastMessage()
        def userMessage = model.body.addMessage("user", input, lastMessage.messageId).getLastMessage()

        def output = model.generateResponse(context.swizzleSpeaker("assistant"))
        Logger.info "## Assistant:\r\n${output}"
        def modelMessage = model.body.addMessage("assistant", output, userMessage.messageId).getLastMessage()
        model.body.exportContext("Story/Chat.json")
    }
}

if (options.debate) {
    Logger.info "# Starting a debate: Let it cook."

    // --- 1. The Registry of Souls (Aligned with Model.groovy) ---
    def debate = new Context().enableLogging("Story/Debate_live.jsonl")

    def moderator = new Model(model: "narrator")
    debate.addMessage("system", new File("Characters/Moderator.md").text)

    // We map participant names to their specific Model configurations
    def participants = ["ParticipantA", "ParticipantB"].collect { role ->
        debate.addMessage(role, "You are ${role}. Identity: " + new File("Characters/${role}.md").text)
        return [role: role, model: new Model(model: "biggun")]
    }


    // --- 2. The Recursive Closure (Fixes the nesting error) ---
    def conductRound 
    conductRound = { Context mainContext, List debateTeam, Model mod, int round, int max ->
        if (round > max) return mainContext

        Logger.info "--- Round ${round} ---"

        debateTeam.each { p ->
            Logger.info("PRE_GEN: ${p.role} context has ${mainContext.messages} messages.")
            // Moderator interjection added to the shared chain
            mainContext.addMessage("Moderator", "Round ${round}: What is your response to the last speaker?")
            
            // Generate response using the existing Model.generateResponse(Context)
            // Note: swizzleSpeaker provides the context the model needs to see
            def resp = p.model.generateResponse(mainContext.swizzleSpeaker(p.role))

            if (resp == null || resp.isEmpty()) {
                Logger.error("GHOST DETECTED: ${p.role} produced an empty string!")
            }
            mainContext.addMessage(p.role, resp)
            Logger.info "[${p.role}]: ${resp}"
        }

        // Evaluation: The Moderator checks for CONSENSUS
        def evalCtx = mainContext.swizzleSpeaker("Moderator")
        evalCtx.addMessage("user", "What is your evaluation? (Search for <CONSENSUS>true</CONSENSUS>)")
        def eval = mod.generateResponse(evalCtx)
        
        if (TagParser.extractBoolean(eval, "CONSENSUS")) {
            Logger.info "## Consensus reached: ${eval}"
            return mainContext
        }

        return conductRound.call(mainContext, debateTeam, mod, round + 1, max)
    }

    // --- 3. Fire the Engine ---
    conductRound(debate, participants, moderator, 1, 15)
    
    debate.exportContext("Story/Debate_Final.json")
    Logger.info "Debate concluded. Final context saved to Story/Debate_Final.json"
}
