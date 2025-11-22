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

    Logger.info "# Sent a chat arg."
    Logger.setLevel(LogLevel.INFO)
    def context = new Context()
    context.addMessage("system", "You are a helpful and friendly chatbot.")
    def model = new Model(model: "narrator", body: context)

    while (true) {
        print "## You: "
        def input = System.in.newReader().readLine().trim()
        if (input.contains("/bye")) { return }
        context.addMessage("Phiglit", input)
        def output = model.generateResponse(context.swizzleSpeaker("George"))
        Logger.info "## Assistant:\r\n${output}"
        context.addMessage("George", output)
        context.exportContext("Story/Chat.json")
    }
}

// Let the machine "chat" with its self.
if (options.debate) {
    // Style A: Use implicit typing (def) and map literal for model configuration
    Logger.info "# Starting a debate"

    // --- 1. Setup Consolidation (Style A) ---
    def debateRounds = 2
    def debate = new Context()

    def moderatorName = "Moderator"
    def moderatorPrompt = "Your working memory has a hard limit of 32,768 tokens. Old messages will be truncated to prevent context overflow. You must prioritize summarizing the most recent 10 turns and key facts from the debate's start, while intentionally ignoring overly verbose historical context, as it will be forgotten. You are an impartial moderator of a debate between two chatbots. You must evaluate the chat history and summarize the points made by the participants. If they have come to a conclusion, please state it, concluding your response with either true or false between <CONSENSUS> tags on the last line of your output. (ex: '\\r\\n<CONSENSUS>true</CONSENSUS>', OR '\\r\\n<CONSENSUS>false</CONSENSUS>')"
    def moderatorContext = new Context().addMessage("system", moderatorPrompt)
    def moderator = new Model(model: "narrator")

    def proName = "Prodipto"
    def proSystemPrompt = "You are a chatbot, designed to interact and debate with other chatbots. Your position is pro-regulation of AI technology. Enjoy the debate!"
    def proContext = new Context().addMessage("system", proSystemPrompt)
    def yayRegs = new Model(model: "biggun")

    def negName = "Negorami"
    def negSystemPrompt = "You are a chatbot, designed to interact and debate with other chatbots. Your position is anti-regulation of AI technology. Enjoy the debate!"
    def negContext = new Context().addMessage("system", negSystemPrompt)
    def booRegs = new Model(model: "smallfry")

    // Groovy List Literal for Participant Ordering and Loop Control
    def participants = [
        [name: proName, model: yayRegs, context: proContext],
        [name: negName, model: booRegs, context: negContext]
    ]

    // --- 2. Opening Statements (Consolidated Logic) ---
    def openingStatement = "Good morning. Your position is . Please state your opening arguments."

    participants.each { p ->
        // Use map/dot notation and optional parentheses (Style A)
        def introMsg = openingStatement.replaceAll('Your position is [^\\.]+', "Your position is ${p.name == proName ? 'pro-regulation' : 'anti-regulation'}")

        p.context.addMessage(moderatorName, introMsg)
        debate.addMessage(moderatorName, introMsg)

        def resp = p.model.generateResponse(p.context.swizzleSpeaker(p.name))
        Logger.info "${p.name} says:\r\n${resp}"
        debate.addMessage(p.name, resp)
    }

    Logger.info "## Exporting our current context:\r\nStory/Debate_0.json"
    debate.exportContext("Story/Debate_0.json")

    // --- 3. Debate Rounds (Refactored using Groovy Range and Pruning) ---
    def prompt = "What is your response to the last speaker?"
    def consensus = false
    def safetyCounter = 0
    def MAX_ROUNDS = 25 // Safety limit to catch infinite loops

    // Style A: Use Range/Closure for concise iteration
    while (!consensus && safetyCounter < MAX_ROUNDS) {
        safetyCounter++
        Logger.info "--- Round ${safetyCounter} ---"

        // Use a loop to iterate through participants for each round
        participants.each { p ->
            // Rebuild context without creating new Context objects every time (or just create a fresh one)
            def subContext = new Context()

            // Add system instruction specific to the participant
            subContext.addMessage("system", p.context.messages[0].content)

            // Add full debate history
            subContext.messages.addAll debate.messages

            // Moderator interjection
            debate.addMessage(moderatorName, prompt)
            Logger.info "${moderatorName} says:\r\n${prompt}"

            // Generate response (optional parentheses)
            def resp = p.model.generateResponse(subContext.swizzleSpeaker(p.name))

            // Update main debate context
            debate.addMessage(p.name, resp)
            Logger.info "${p.name} says:\r\n${resp}"

            debate.pruneContext()
            Logger.info "Context pruned. Remaining messages: ${debate.messages.size()}"

            if (safetyCounter % 5 == 0) {
                Logger.info "## Mid-Round Moderation check"
                debate.addMessage("user", "What is your Evaluation?")
                moderatorContext.messages.addAll(debate.messages)
                def midRoundEval = moderator.generateResponse(moderatorContext.swizzleSpeaker(moderatorName))
                consensus = TagParser.extractBoolean(midRoundEval, "CONSENSUS") ?: false
            }
        }

        // CRITICAL: Call pruneContext to combat O(N^2) scaling
        debate.pruneContext()
        Logger.info "Context pruned. Remaining messages: ${debate.messages.size()}"
    }

    // --- 4. Final Moderation & Consensus Check ---
    Logger.info "## Final Moderation"

    // Ensure final context is not pruned before moderation, just use the final debate context.
    debate.addMessage("user", "What is your evaluation?")

    // Copy full debate history into the moderator's context
    moderatorContext.messages.addAll(debate.messages)

    def finalResp = moderator.generateResponse(moderatorContext.swizzleSpeaker(moderatorName))
    Logger.info "${moderatorName} says:\r\n${finalResp}"

    // Use safe navigation in case TagParser returns null (Style A)
    consensus = TagParser.extractBoolean(finalResp, "CONSENSUS") ?: false
    Logger.info "Consensus reached?:\r\n${consensus}"

    debate.addMessage(moderatorName, finalResp)
    debate.exportContext("Story/Debate.json")
    Logger.info "How about a nice game of chess?"
}
