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
            case "/faders":
                handleFaders(input)
                return true
            default:
                bridge.terminal.writer().println("\u001B[31m[UNKNOWN COMMAND]\u001B[0m")
                return true
        }
    }

    // Display a nice lil' help message
    private void handleHelp() {
        PrintWriter w = bridge.terminal.writer()
        w.println("\n\u001B[1;33m--- SCRIPTORIUM COMMAND MANUAL ---\u001B[0m")
        w.println("\u001B[32m/map\u001B[0m          : View the Chronicle Tapestry (DAG tree).")
        w.println("\u001B[32m/jump <id>\u001B[0m    : Leap to a specific (assistant) message ID in the timeline.")
        w.println("\u001B[32m/mark <text>\u001B[0m  : Create a bookmark at the current (assistant) turn.")
        w.println("\u001B[32m/bookmarks\u001B[0m    : List all saved points of interest.")
        w.println("\u001B[32m/faders\u001B[0m     : Show current resonance values.")
        w.println("\u001B[32m/faders <trait> <value>\u001B[0m : Set a new resonance value for one of the traits, in range (0.0..2.0).")
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
        // Basic safety check for the input
        String[] parts = input.split(" ")
        if (parts.size() < 2) {
            bridge.terminal.writer().println("\u001B[31m[ERROR]\u001B[0m: Usage: /jump <partial_id>")
            return
        }

        String targetId = parts[1]
        Map targetEntry = logManager.findEntryByPartialId(targetId)

        if (targetEntry && targetEntry.role == "assistant") {
            // The core "Iron" of the jump
            context.loadBranch(targetEntry.messageId)
            bridge.replayLastTurn(context)
            bridge.updateHUD("The Library", "Navigator", targetEntry)
            bridge.terminal.writer().println("\u001B[35m## Timeline shifted to [${targetEntry.messageId.take(8)}]\u001B[0m")
        } else {
            bridge.terminal.writer().println("\u001B[31m[ERROR]\u001B[0m: Cannot pivot to a user node or invalid ID.")
        }
    }

    // Bookmark an entry in the graph with the given input string
    private void handleMark(String input) {
        String label = input.substring(6).trim()
        Message target = context.messages.reverse().find { it.role == "assistant"}
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

    // Handle some faders, ya dig?
    private void handleFaders(String input) {
        PrintWriter w = bridge.terminal.writer()
        Message lastMessage = context.messages.last()
        String[] parts = input.split(" ")

        // If we just get "/faders" -> List the current values
        if (parts.size() == 1) {
            w.println("\n\u001b[1;36m-- GEORGE'S INTERNAL MIXING BOARD --\u001B[0m")
            lastMessage.getStats().each { trait, value ->
                String bar = "█" * (int)(value * 10)
                w.println("\u001B[32m${trait.padRight(15)}\u001B[0m: [${value.toString().padRight(4)}] ${bar}")
            }
            w.println("\u001B[1;36m--------------------------------------\u001B[0m")
            w.println("Usage: /faders <trait> <value> (e.g., /faders sarcasm 0.8)\n")
        }
        else if (parts.size() == 3) {
            String trait = parts[1]
            try {
                double newValue = Double.parseDouble(parts[2])
                newValue = Math.max(0.0, Math.min(2.0, newValue))
                if (lastMessage.resonance.hasProperty(trait)) {
                    lastMessage.resonance."${trait}" = newValue
                    w.println("\u001B[36m[George]\u001B[0m: I feel a sudden shift in my ${trait}... (Set to ${newValue})\u001B[0m")
                    bridge.updateHUD("The Library", "Navigator", lastMessage.getStats())
                } else {
                    w.println("\u001B[31m[ERROR]\u001B[0m:George doesn't have a '${trait}' fader.")
                }
            } catch (NumberFormatException e) {
                w.println("\u001B[31m[ERROR]\u001B[0m: Value must be a number (0.0 to 2.0).")
            }
        }
        bridge.terminal.flush()
    }

    Map getStats() {
        return this.context.messages.last().getStats()
    }
}