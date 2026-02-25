package org.kleypas.muc.cli

import org.kleypas.muc.model.Context
import org.kleypas.muc.io.LogManager

class CommandProcessor {
    TerminalBridge bridge
    LogManager logManager
    Context context

    CommandProcessor(TerminalBridge bridge, LogManager logManager, Context context) {
        this.bridge = bridge
        this.logManager = logManager
        this.context = context
    }

    /**
     * @return true if a command was handled and the loop should continue, 
     * false if the input is a normal chat message.
     */
    boolean process(String input) {
        if (!input.startsWith("/")) return false

        switch (input.split(" ")[0]) {
            case "/help":
                handleHelp()
                return true
            case "/h":
                handleHelp()
                return true
            case "/map":
                handleMap()
                return true
            case "/jump":
                handleJump(input)
                return true
            case "/mark":
                handleMark(input)
                return true
            case "/bookmarks":
                handleBookmarks()
                return true
            default:
                bridge.terminal.writer().println("\u001B[31m[UNKNOWN COMMAND]\u001B[0m")
                return true
        }
    }

    // Display a nice lil' help message
    private void handleHelp() {
        def w = bridge.terminal.writer()
        w.println("\n\u001B[1;33m--- SCRIPTORIUM COMMAND MANUAL ---\u001B[0m")
        w.println("\u001B[32m/map\u001B[0m          : View the Chronicle Tapestry (DAG tree).")
        w.println("\u001B[32m/jump <id>\u001B[0m    : Leap to a specific (assistant) message ID in the timeline.")
        w.println("\u001B[32m/mark <text>\u001B[0m  : Create a bookmark at the current (assistant) turn.")
        w.println("\u001B[32m/bookmarks\u001B[0m    : List all saved points of interest.")
        w.println("\u001B[32m/bye\u001B[0m or \u001B[32mq\u001B[0m     : Save and exit the chronicle.")
        w.println("\u001B[1;33m----------------------------------\u001B[0m\n")
        bridge.terminal.flush()
    }

    // Display the narrative graph
    private void handleMap() {
        def currentTip = context.messages.reverse().find { it.role == "assistant" }
        String currentId = currentTip?.messageId ?: ""
        Map tree = logManager.buildHistoryTree()
        bridge.terminal.writer().println("\n\u001B[33m## THE CHRONICLE TAPESTRY ##\u001B[0m")
        bridge.drawChronicleMap(null, tree, currentId)
    }

    // Jump somewhere else in the graph
    private void handleJump(String input) {
        String targetId = input.split(" ")[1]
        Map targetEntry = logManager.findEntryByPartialId(targetId)
        if (targetEntry && targetEntry.role == "assistant") {
            context.loadBranch(targetEntry.messageId)

            bridge.replayLastTurn(context)

            bridge.updateHUD("The Library", "Navigator", [
                nurturance: targetEntry.nurturance,
                playfulness: targetEntry.playfulness,
                steadfastness: targetEntry.steadfastness,
                attunement: targetEntry.attunement
            ])
        } else {
            bridge.terminal.writer().println("\u001B[31m[ERROR]\u001B[0m: Cannot pivot to a user node or invalid ID.")
        }
    }

    // Bookmark an entry in the graph with the given input string
    private void handleMark(String input) {
        String label = input.substring(6).trim()
        def target = context.messages.reverse().find { it.role == "assistant"}
        if (target) {
            target.bookmark = label
            logManager.updateEntry(target)
            bridge.terminal.writer().println("\u001B[35m## George marks the page: \"${label}\"\u001B[0m")
        }
    }

    // Print the current bookmarks from the graph
    private void handleBookmarks() {
        bridge.terminal.writer().println("\n\u001B[35m## The navigator's current bookmarks ##\u001b[0m")
        context.messages.findAll { it.bookmark }.each { bm ->
            bridge.terminal.writer().println("\u001B[1;35m[${bm.messageId.take(8)}]\u001B[0m ${bm.bookmark.padRight(20)} | ${bm.content.take(30)}...")
        }
    }
}