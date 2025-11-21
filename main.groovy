#!/bin/bash
//usr/bin/env groovy -cp lib/:lib/main.jar "$0" $@; exit $?

import java.nio.file.Files
import java.nio.file.Paths

import org.kleypas.muc.model.Context
import org.kleypas.muc.model.Message
import org.kleypas.muc.model.Model
import org.kleypas.muc.model.TagParser

import org.kleypas.muc.illustrator.Illustrator
import org.kleypas.muc.illustrator.ImageType

import org.kleypas.muc.cli.Cli
import org.kleypas.muc.cli.Logger

import Test

Cli cli = new Cli()
def options = cli.parse(args)

Logger.info "Welcome to the Muc. Starting execution loop."

// Build the project if the `--build` flag is provided.
if (options.build) {
    Logger.info "Sent a build arg."

    Files.createDirectories(Paths.get("./lib"))
    cli.runCommand "groovyc -d=./lib main.groovy"
}

// Ok, first step was hashtag simplify by at least
// moving it into a helper class in Test.groovy. We can
// refine it from here at least.
if (options.test) {
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
    def illustrator = new Illustrator()
    illustrator.style = ImageType.PORTRAIT
    illustrator.title = "ComfyUI"
    def input = cli.waitForInput()
    def prompt = illustrator.getPrompt(input)

    illustrator.generateImage(prompt)
}

// Get directly to a chat.
if (options.chat) {
    if (options.test) { return }
    def context = new Context()
    context.addMessage("system", "You are a helpful and friendly chatbot.")
    def model = new Model(model: "narrator", body: context)

    while (true) {
        input = cli.waitForInput()
        if (input.contains("/bye")) { return }
        context.addMessage("Phiglit", input)
        def output = model.generateResponse(context.swizzleSpeaker("George"))
        cli.log("### Assistant:\r\n${output}")
        context.addMessage("George", output)
        context.exportContext("Story/Chat.json")
    }
}

// Let the machine "chat" with its self.
if (options.debate) {
    cli.log("Inside a debate")
    // a "global state" context? Maybe?
    def debate = new Context()

    cli.log("Introducing our Moderator:")
    def moderatorName = "Moderator"
    def moderatorContext = new Context()
    def moderatorPrompt = "You are an impartial moderator of a debate between two chatbots. You must evaluate the chat history and summarize the points made by the participants. If they have come to a conclusion, please state it, concluding your response with either true or false between <COSENSUS> tags on the last line of your output. (ex: '\r\n<CONSENSUS>true</CONSENSUS>', OR '\r\n<CONSENSUS>false</CONSENSUS>')"
    moderatorContext.addMessage("system", moderatorPrompt)
    def moderator = new Model(model: "narrator")

    def proName = "Prodipto"
    def proContext = new Context()
    def proSystemPrompt = "You are a chatbot, designed to interact and debate with other chatbots. Your position is pro-regulation of AI technology. Enjoy the debate!"
    proContext.addMessage("system", proSystemPrompt)
    def yayRegs = new Model(model: "biggun")

    def resp = ""

    proContext.addMessage(moderatorName, "Good morning. Your position is pro-regulation of AI technology. Please state your opening arguments.")
    debate.addMessage(moderatorName, "Good morning. Your position is pro-regulation of AI technology. Please state your opening arguments.")
    resp = yayRegs.generateResponse(proContext.swizzleSpeaker(proName))
    cli.log("${proName} says:\r\n${resp}")
    debate.addMessage(proName, resp)

    def negName = "Negorami"
    def negContext = new Context()
    def negSystemPrompt = "You are a chatbot, designed to interact and debate with other chatbots. Your position is anti-regulation of AI technology. Enjoy the debate!"
    negContext.addMessage("system", negSystemPrompt)
    def booRegs = new Model(model: "smallfry")

    negContext.addMessage(moderatorName, "Good morning. Your position is anti-regulation of AI technology. Please state your opening arguments.")
    debate.addMessage(moderatorName, "Good morning. Your position is anti-regulation of AI technology. Please state your opening arguments.")
    resp = booRegs.generateResponse(negContext.swizzleSpeaker(negName))
    cli.log("${negName} says:\r\n${resp}")
    debate.addMessage(negName, resp)

    debate.exportContext("Story/Debate_0.json")

    def debateRounds = 2
    def consensus = false
    for (int i = 0; i < debateRounds; i++) {
        def count = i+1
        def subContext = new Context()

        debate.addMessage(moderatorName, "What is your response?")
        cli.log("${moderatorName} says:\r\nWhat is your response?")
        subContext.addMessage("system", proSystemPrompt)
        subContext.messages.addAll(debate.messages)
        resp = yayRegs.generateResponse(subContext.swizzleSpeaker(proName))
        cli.log("${proName} says:\r\n${resp}")
        debate.addMessage(proName, resp)

        subContext = new Context()
        debate.addMessage(moderatorName, "What is your response?")
        cli.log("${moderatorName} says:\r\nWhat is your response?")
        subContext.addMessage("system", negSystemPrompt)
        subContext.messages.addAll(debate.messages)

        resp = booRegs.generateResponse(subContext.swizzleSpeaker(negName))
        debate.addMessage(negName, resp)

        cli.log("${negName} says:\r\n${resp}")
    }

    debate.addMessage("user", "What is your evaluation?")
    moderatorContext.messages.addAll(debate.messages)
    resp = moderator.generateResponse(moderatorContext.swizzleSpeaker(moderatorName))
    cli.log("${moderatorName} says:\r\n${resp}")

    consensus = TagParser.extractBoolean(resp, "CONSENSUS")
    cli.log "Consensus reached?:\r\n${consensus}"

    debate.addMessage(moderatorName, resp)
    debate.exportContext("Story/Debate.json")
    cli.log("How about a nice game of chess?")
}
