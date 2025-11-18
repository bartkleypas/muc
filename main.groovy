#!/usr/bin/env groovy
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.json.JsonParserType

import Cli
import Character
import Illustrator
import Inventory
import Item
import Location
import Model
import Rng
import Test

Cli cli = new Cli()
def options = cli.parse(args)

if (options.json) {
    cli.logLevel = LogLevel.JSON
}

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
    def debate = new Context()

    def proName = "Prodipto"
    def proContext = new Context()
    def proSystemPrompt = "You are a chatbot, designed to interact and debate with other chatbots. Your position is pro-regulation of AI technology. Enjoy the debate!"
    proContext.addMessage("system", proSystemPrompt)
    def yayRegs = new Model(model: "biggun")

    def resp = ""

    proContext.addMessage("Phiglit", "Good morning. Your position is pro-regulation of AI technology. Please state your opening arguments.")
    debate.addMessage("Phiglit", "Good morning. Your position is pro-regulation of AI technology. Please state your opening arguments.")
    resp = yayRegs.generateResponse(proContext.swizzleSpeaker(proName))
    cli.log("${proName} says:\r\n${resp}")
    debate.addMessage(proName, resp)

    def negName = "Negorami"
    def negContext = new Context()
    def negSystemPrompt = "You are a chatbot, designed to interact and debate with other chatbots. Your position is anti-regulation of AI technology. Enjoy the debate!"
    negContext.addMessage("system", negSystemPrompt)
    def booRegs = new Model(model: "smallfry")

    negContext.addMessage("Phiglit", "Good morning. Your position is anti-regulation of AI technology. Please state your opening arguments.")
    debate.addMessage("Phiglit", "Good morning. Your position is anti-regulation of AI technology. Please state your opening arguments.")
    resp = booRegs.generateResponse(negContext.swizzleSpeaker(negName))
    cli.log("${negName} says:\r\n${resp}")
    debate.addMessage(negName, resp)

    debate.exportContext("Story/Debate.json")

    def debateRounds = 3
    for (int i = 0; i < debateRounds; i++) {
        def subContext = new Context()

        debate.addMessage("Phiglit", "What is your response?")
        cli.log("Phiglit says:\r\nWhat is your response?")
        subContext.addMessage("system", proSystemPrompt)
        subContext.messages.addAll(debate.messages)
        resp = yayRegs.generateResponse(subContext.swizzleSpeaker(proName))
        cli.log("${proName} says:\r\n${resp}")
        debate.addMessage(proName, resp)

        subContext = new Context()
        debate.addMessage("Phiglit", "What is your response?")
        cli.log("Phiglit says:\r\nWhat is your response?")
        subContext.addMessage("system", negSystemPrompt)
        subContext.messages.addAll(debate.messages)

        resp = booRegs.generateResponse(subContext.swizzleSpeaker(negName))
        debate.addMessage(negName, resp)
        cli.log("${negName} says:\r\n${resp}")
    }

    def closer = new Model(model: "narrator")
    def closerSystemPrompt = "You are an impartial moderator of a debate between two chatbots. You must evaluate the chat history and summarize the points made by the participants. If they have come to a conclusion, please state it."
    def closerContext = new Context()
    closerContext.addMessage("system", closerSystemPrompt)
    closerContext.messages.addAll(debate.messages)
    closerContext.addMessage("user", "What is your evaluation?")
    debate.addMessage("user", "What is your evaluation?")
    resp = closer.generateResponse(closerContext.swizzleSpeaker("Phiglit"))
    cli.log("Closer:\r\n${resp}")
    debate.addMessage("Phiglit", resp)
    debate.exportContext("Story/Debate.json")
    cli.log("How about a nice game of chess?")
}
