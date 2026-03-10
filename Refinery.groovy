// --- Refinery.groovy ---
package org.kleypas.muc

import org.kleypas.muc.model.*
import org.kleypas.muc.io.*

// 1. Setup the heavy-duty Teacher
def teacher = new Model(model: "gemma3:27b", temperature: 0.8)
def trainingFile = "Exports/george.v2_goldilocks.jsonl"
def persona = new File("Characters/George.v2.md").text
def logManager = new LogManager("Exports/fake.jsonl") // We don't actually write to this

// 1.5 Set how many total branches we want to create.
// Target ~100 for a "quick" verification pass to verify that we're generating good
// response vibes for our faders, and 500-1000 for a goldilocks run.
int loopSize = 1000

// 2. The Seed Bank. Currently has around 30 user turns to start the ball rolling.
def seeds = new File("handshakeSeeds.txt").readLines().findAll { it.trim() }

println "🌿 [SCRIPTORIUM REFINERY] Starting synthetic generation..."
long totalStartTime = System.currentTimeMillis()

// 3. The Extraction Loop
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