// --- Refinery.groovy ---
package org.kleypas.muc

import org.kleypas.muc.model.*
import org.kleypas.muc.io.*

// Setup the heavy-duty persona teacher.
def teacher = new Model(model: "gemma3:27b", temperature: 0.8)
def persona = new File("Characters/George.md").text
def logManager = new LogManager("Story/fake.jsonl") // We don't actually write to this
def trainingFile = "Exports/George.trainingSet.jsonl" // We DO write to this one though

// The conversation handshake seeds, and the number of times we iterate on each.
def seeds = new File("Characters/George.handshakeSeeds.txt").readLines().findAll { it.trim() }
int iterations = 10

println "🌿 [SCRIPTORIUM REFINERY] Starting synthetic generation..."
long totalStartTime = System.currentTimeMillis()

// The inference loop. We iterate over each conversation seed and generate a synthetic
// response, in tune with a random resonance setting.
int loopSize = seeds.size() * iterations
loopSize.times { i ->

    long branchStartTime = System.currentTimeMillis()

    // Craft a short lived context, and add the models persona to the system prompt
    def context = new Context().enableLogging(logManager)
    context.addMessage(role: "system", content: persona)

    // Pick one of the conversation seeds to finish off our handshake with the model
    def seed = seeds[i % seeds.size()]

    // Generate a random "Impulse"
    def resonance = new Resonance().randomize()

    // Add the user turn to the context
    context.addMessage(role: "user", content: seed, resonance: resonance)

    // Fabricate a prefix of our resonance to lead the models response
    def faderPrefix = resonance.asMap().collect { k, v -> "[${k.toUpperCase()}:${v}]" }.join(" ")
    def responseBuffer = new StringBuilder()

    print "Generating branch ${i+1} with [${faderPrefix}]... "

    // Now we generate the response from the model.
    teacher.streamResponseWithPrefix(context, faderPrefix) { token ->
        responseBuffer.append(token)
    }

    // Add the models turn to the loops context.
    context.addMessage(
        role: "assistant",
        content: responseBuffer.toString(),
        resonance: resonance
    )

    // Finally write the context out in a format that can be used for training.
    logManager.exportBranchToChatML(trainingFile, context.messages)

    long branchElapsed = (System.currentTimeMillis() - branchStartTime) / 1000
    println "Success (${branchElapsed}s)."
}

long totalElapsed = (System.currentTimeMillis() - totalStartTime) / 1000
def minutes = (totalElapsed / 60).toInteger()
def seconds = totalElapsed % 60

println "---"
println "✅ Refinery run complete. Data added to ${trainingFile}"
println "⏱️ Total Time: ${minutes}m ${seconds}s"