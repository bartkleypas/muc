package org.kleypas.muc.cli

import org.kleypas.muc.model.Message

import org.jline.terminal.*
import org.jline.utils.*

/**
 * TerminalBridge: The Sovereign Interface for the MUC Scriptorium.
 * Handles the persistent HUD, ASCII signatures, and Raw Mode interactions.
 */
class TerminalBridge implements AutoCloseable {
    private Terminal terminal
    private Status statusLine
    private Attributes originalAttributes
    private int currentLinePos = 0
    private StringBuilder wordBuffer = new StringBuilder()

    TerminalBridge() {
        this.terminal = TerminalBuilder.builder().system(true).build()
        this.originalAttributes = terminal.enterRawMode()
        this.statusLine = Status.getStatus(terminal)
    }

    /**
     * Draws the "Radiant Strix" signature and initial header.
     */
    void drawSignature() {
        String owl = """\u001B[37m  ,_,   
\u001B[37m (O\u001B[33m,\u001B[37mO)  \u001B[36m<-- "The Bridge is established, Navigator. Type /bye or /q to quit, and /map to show the conversation tree."\u001B[0m
\u001B[37m (###)  
\u001B[37m  " "   \u001B[0m"""
        terminal.writer().println(owl)
        terminal.writer().println("\u001B[32m[SYSTEM]\u001B[0m: TerminalBridge online.\n")
        terminal.flush()
    }

    /**
     * Draws an ASCII representation of the conversation branches.
     */
    void drawChronicleMap(String parentId, Map<String, List<Message>> tree, String prefix = "", boolean isLast = true) {
        List<Message> children = tree[parentId] ?: []

        children.eachWithIndex { msg, idx ->
            boolean lastChild = (idx == children.size() - 1)

            // Format the line: [ID] Role: Snippet
            String color = (msg.role == "assistant") ? "\u001B[36m" : "\u001B[32m"
            String snippet = msg.content.take(40).replaceAll("\n", " ")
            String line = "${prefix}${lastChild ? '└── ' : '├── '}${color}[${msg.messageId.take(8)}] ${msg.role.toUpperCase()}: ${snippet}...\u001B[0m"
            terminal.writer().println(line)

            // Recurse into children of this message
            String newPrefix = prefix + (lastChild ? "    " : "|   ")
            drawChronicleMap(msg.messageId, tree, newPrefix, lastChild)
        }
        terminal.flush()
    }
    /**
     * Updates the pinned HUD with current world state.
     */
    void updateHUD(String location, String hero, Map<String, Double> resonance) {
        if (!statusLine) return

        String left = " [ LOC:${location.toUpperCase()} ] | [ HERO:${hero.toUpperCase()} ]"
        String right = String.format(
            "[ N:%.1f P:%.1f S:%.1f A:%.1f ]",
            resonance.nurturance ?: 1.0,
            resonance.playfulness ?: 1.0,
            resonance.steadfastness ?: 1.0,
            resonance.attunement ?: 1.0
        )

        int width = terminal.getWidth()
        int paddingSize = Math.max(1, width - (left.length() + right.length()))
        String padding = " ".multiply(paddingSize)

        String fullText = left + padding + right
        AttributedString coloredStatus = new AttributedString(fullText,
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).background(AttributedStyle.BLUE))
        
        statusLine.update([coloredStatus])
        terminal.flush()
    }

    /**
     * Captures a single character of input.
     */
    int readKey() {
        return terminal.reader().read()
    }

    /**
     * Restores the terminal to its original state.
     * Implements AutoCloseable for use in try-with-resources.
     */
    @Override
    void close() {
        if (statusLine) statusLine.update([])
        if (originalAttributes) terminal.setAttributes(originalAttributes)
        terminal.writer().println("\n\u001B[32m[SYSTEM]\u001B[0m: Bridge offline. Terminal restored.")
        terminal.flush()
    }

    void printSpeaker(String role) {
        String color = (role.equalsIgnoreCase("assistant")) ? "\u001B[1;36m" : "\u001B[1;32m"
        String name = (role.equalsIgnoreCase("assistant")) ? "[George]" : "[You]"
        terminal.writer().print("\n${color}${name}\u001b[0m: ")
        terminal.flush()
    }

    void printToken(String token) {
        if (!token) return

        for (char c in token.toCharArray()) {
            terminal.writer().print(c)
            terminal.flush()

            // Slight random delay to feel more organic (15-40ms)
            long delay = 10 + new Random().nextInt(20)

            // Punctuation gets a lil' longer delay for dramatic impact
            if (c == '.' || c == '?' || c == '!') delay += 250
            if (c == ',') delay += 40

            try {
                Thread.sleep(delay)
            } catch (InterruptedException e) {
                Thread.currentThread().intuerrupt()
            }
        }
    }

    void flushBuffer() {
        terminal.writer().println()
        terminal.flush()
    }
}