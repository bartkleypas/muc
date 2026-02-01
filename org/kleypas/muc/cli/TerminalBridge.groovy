package org.kleypas.muc.cli

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
\u001B[37m (O\u001B[33m,\u001B[37mO)  \u001B[36m<-- "The Bridge is established, Navigator. Trim Radiance with r/e, and q for quit."\u001B[0m
\u001B[37m (###)  
\u001B[37m  " "   \u001B[0m"""
        terminal.writer().println(owl)
        terminal.writer().println("\u001B[32m[SYSTEM]\u001B[0m: TerminalBridge online.\n")
        terminal.flush()
    }

    /**
     * Updates the pinned HUD with current world state.
     */
    void updateHUD(String location, String hero, int radiance) {
        if (!statusLine) return

        String text = " [ LOC: $location ] | [ HERO: $hero ] | [ MIXER: RAD ${radiance}% ] "
        AttributedString coloredStatus = new AttributedString(text, 
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
        terminal.writer()print(token)
        terminal.flush()
    }

    void flushBuffer() {
        terminal.writer().println()
        terminal.flush()
    }
}