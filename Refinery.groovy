
import org.kleypas.muc.model.*
import org.kleypas.muc.model.resonance.*
import org.kleypas.muc.io.*

class Refinery {
    private Model teacher
    private String persona
    private LogManager logManager
    private String trainingFile
    private List<String> seeds
    private int iterations

    Refinery(Map config = [:]) {
        this.teacher = config.teacher ?: new Model(ModelType.BIG)
        this.persona = config.persona ?: new File("Characters/George.md").text
        this.logManager = config.logManager ?: new LogManager("Story/fake.jsonl")
        this.trainingFile = config.trainingFile ?: "Exports/George.trainingSet.jsonl"
        this.iterations = config.iterations ?: 10

        def seedFile = config.seedFile ?: "Characters/George.handshakeSeeds.txt"
        this.seeds = new File(seedFile).readLines().findAll { it.trim() }
    }

    void run() {
        println "🌿 [SCRIPTORIUM REFINERY] Starting synthetic generation..."
        long totalStartTime = System.currentTimeMillis()

        int totalRuns = seeds.size() * iterations
        totalRuns.times { i ->
            processBranch(i)
        }

        printSummary(totalStartTime)
    }

    private void processBranch(int index) {
        long branchStartTime = System.currentTimeMillis()

        def resonance = new Resonance().randomize()
        print "Generating branch ${index + 1} with ${resonance.toPrefix()}... "

        def context = new Context().enableLogging(logManager)
        def systemMsg = context.addMessage(
            role: "system",
            content: persona,
            vibe: resonance
        )

        def seed = seeds[index % seeds.size()]
        def userMsg = context.addMessage(
            role: "user",
            parentId: systemMsg.messageId,
            content: seed,
            vibe: resonance
        )

        def teacherContent = teacher.generateResponse(context, resonance.toPrefix())
        def teacherMessage = context.addMessage(
            role: "assistant",
            parentId: userMsg.messageId,
            content: teacherContent,
            vibe: resonance
        )

        logManager.exportBranchToChatML(trainingFile, context.messages)

        long branchElapsed = (System.currentTimeMillis() - branchStartTime) / 1000
        println "Success (${branchElapsed}s)."
    }

    private void printSummary(long startTime) {
        long totalElapsed = (System.currentTimeMillis() - startTime) / 1000
        def minutes = (totalElapsed / 60).toInteger()
        def seconds = totalElapsed % 60
        println "---\n✅ Refinery run complete. Data added to ${trainingFile}"
        println "⏱️ Total Time: ${minutes}m ${seconds}s"
    }
}