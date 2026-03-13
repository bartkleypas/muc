#!/bin/bash
//usr/bin/env groovy -cp lib/:lib/main.jar "$0" $@; exit $?

import org.kleypas.muc.io.LogManager

import org.kleypas.muc.model.Context
import org.kleypas.muc.model.Message
import org.kleypas.muc.model.Model
import org.kleypas.muc.model.Resonance
import org.kleypas.muc.model.ResonanceEngine
import org.kleypas.muc.model.TagParser

import org.kleypas.muc.illustrator.Illustrator
import org.kleypas.muc.illustrator.ImageType

import org.kleypas.muc.cli.Cli
import org.kleypas.muc.cli.CommandProcessor
import org.kleypas.muc.cli.Logger
import org.kleypas.muc.cli.LogLevel
import org.kleypas.muc.cli.TerminalBridge

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
    // test.narrator()
    // test.illustrator() <-- currently disabled.
    // test.story()
    // test.tui() // <-- NOTE: Will block test completion needing input if enabled.
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
    Logger.setLevel(LogLevel.INFO)
    Logger.info "# Sent a chat arg."

    def historyFile = "Story/Majel.jsonl"
    if (options.chat instanceof String) {
        historyFile = options.chat
    }

    def logManager = new LogManager(historyFile, cli.envVars.ENCRYPTION_KEY.getBytes())
    def context = new Context().enableLogging(logManager)
    def model = new Model(model: "biggun")
    def resonanceEngine = new ResonanceEngine()

    // Create a new chat, or load up the message stream from an existing one if it exists.
    if (!new File(historyFile).exists()) {
        Logger.info "## Building a new Chronicle to ${historyFile}..."
        def promptText = new File("Characters/Majel.md").text

        def systemMsg = context.addMessage(
            role: "system",
            author: "Majel",
            content: promptText
        )

        logManager.appendEntry(systemMsg)
    } else {
        logManager.readAllEntries().each { entry ->
            context.messages.add(entry)
        }
    }

    new TerminalBridge().withCloseable { bridge ->
        def stats = logManager.getChronicleStats()
        bridge.drawSignature(stats)

        def processor = new CommandProcessor(bridge, logManager, context)

        def lastMessage = context.messages.last()

        // Don't echo out the system prompt.
        // Instead, give a nice lil' hint to the user for a first prompt
        if (lastMessage.role == "system") {
            bridge.printToken("Welcome to the Starship Majel. I am its AI assistant, but you can call me Majal too. Welcome to the crew. What can I help you with today?")
            bridge.flushBuffer()
        } else {
            bridge.replayLastTurn(context)
            bridge.flushBuffer()
        }

        def reader = org.jline.reader.LineReaderBuilder.builder()
                        .terminal(bridge.terminal)
                        .build()

        // The main loop of our chat TUI.
        while (true) {
            def last = context.messages.last()
            bridge.updateHUD("The Starship Majel", "Navigator", last.getStats())
            bridge.flushBuffer()

            String prompt = "\u001B[1;32mNavigator\u001B[0m: "

            // Handle inputs and commands
            def input = reader.readLine(prompt)?.trim()
            if (!input || input == "/bye" || input == "q") { break }
            if (processor.process(input)) {
                context = processor.context
                continue
            }

            bridge.terminal.writer().print("\033[1A\033[2K")

            def userResponse = context.addMessage(
                role: "user",
                author: "Navigator",
                content: input,
                parentId: last.messageId,
                stats: processor.getStats()
            )

            logManager.appendEntry(userResponse)

            // Shallow clone of the context object, so we can add the resonance
            // Ensures our live context remains clean of the adjustments.
            def virtualContext = new Context(context.messages)

            // echo's the "last" (ie the models) author.
            bridge.printSpeaker(last)

            // Take the resonance marks, and print a lil' debugger line with them
            // into the bridge output stream.
            def currentStats = userResponse.getStats()
            String faderPrefix = currentStats.collect { k, v -> "[${k.toUpperCase()}:${v}]" }.join(" ")
            bridge.terminal.writer().print("\u001B[30;1m${faderPrefix} \u001B[0m\n") 

            // The Model speaks! Uses the virtualContext shallow clone from above
            // and prefix the models output with the faders.
            StringBuilder fullOutput = new StringBuilder()
            model.streamResponseWithPrefix(virtualContext, faderPrefix) { token ->
                bridge.printToken(token)
                fullOutput.append(token)
            }

            // Clean content from the models stream
            def fullContent = fullOutput.toString().trim()

            def assistantResponse = context.addMessage(
                role: "assistant",
                author: last.author,
                content: fullContent,
                parentId: userResponse.messageId,
                stats: currentStats
            )

            // Commenting out, but not getting rid of this. We might want to pick
            // up some feedback from what George just said, but at the moment,
            // I don't think it is adding much value to the conversation train.
            /*
            def deltas = resonanceEngine.calculate(fullContent)
            resonanceEngine.applyResonance(assistantResponse, deltas)
            */

            // Put that stuff on paper (jsonl log)
            logManager.appendEntry(assistantResponse)
        }
    }
}

if (options.debate) {
    Logger.info "# Starting a debate: Let it cook."

    // --- 1. The Registry of Souls (Aligned with Model.groovy) ---
    def debate = new Context().enableLogging("Story/DebateLive.jsonl")

    def moderator = new Model(model: "narrator")
    debate.addMessage("system", new File("Characters/Moderator.md").text)

    def lastMessage
    // We map participant names to their specific Model configurations
    def participants = ["ParticipantA", "ParticipantB"].collect { role ->
        def identityText = new File("Characters/${role}.md").text
        return [role: role, identity: identityText, model: new Model(model: "biggun")]
    }

    // --- 2. The Recursive Closure (Fixes the nesting error) ---
    def conductRound
    conductRound = { Context mainContext, List debateTeam, Model mod, int round, int max ->
        if (round > max) return mainContext

        Logger.info "--- Round ${round} ---"

        debateTeam.each { p ->
            Logger.info("PRE_GEN: ${p.role} context isolated via getThreadForModel.")

            // 1. The Moderator's prompt is added to the GLOBAL history (The "Iron")
            lastMessage = mainContext.messages.last()
            // We keep the Moderator's "Nagging" brief to reduce noise
            mainContext.addMessage("Moderator", "Round ${round}: ${p.role}, your response?", lastMessage.messageId)

            // 2. THE SUTURE: Prepare the "Radiance" (The isolated view)
            // We create a temporary identity message for the model
            def identity = new Message("system", "You are ${p.role}. Identity: ${p.identity}")

            // We swizzle the BANTER, then thread it with the IDENTITY
            def modelContext = mainContext.swizzleSpeaker(p.role).getThreadForModel(identity)

            // 3. Generate response using the isolated view
            def resp = p.model.generateResponse(modelContext)

            if (resp == null || resp.isEmpty()) {
                Logger.error("GHOST DETECTED: ${p.role} produced an empty string!")
            }

            // 4. Commit the new response to the GLOBAL history
            lastMessage = mainContext.messages.last()
            mainContext.addMessage(p.role, resp, lastMessage.messageId)
            Logger.info "[${p.role}]: ${resp}"
        }

        // Evaluation: The Moderator checks for CONSENSUS
        def evalCtx = mainContext.swizzleSpeaker("Moderator")
        lastMessage = mainContext.getLastMessage()
        evalCtx.addMessage("Moderator", "What is your evaluation? (Search for <CONSENSUS>true</CONSENSUS>)", lastMessage.messageId)
        def eval = mod.generateResponse(evalCtx)
        
        if (TagParser.extractBoolean(eval, "CONSENSUS")) {
            Logger.info "## Consensus reached: ${eval}"
            return mainContext
        }

        return conductRound.call(mainContext, debateTeam, mod, round + 1, max)
    }

    // --- 3. Fire the Engine ---
    conductRound(debate, participants, moderator, 1, 5)

    debate.exportContext("Story/Debate_Final.json")
    Logger.info "Debate concluded. Final context saved to Story/Debate_Final.json"
}
