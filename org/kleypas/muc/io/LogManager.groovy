package org.kleypas.muc.io

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File
import java.util.List
import java.util.Map

/**
 * Handles all File I/O for the Narrator.jsonl persistence layer.
 * Adheres to Style B mandates: Explicit typing and strict access modifiers.
 */
public class LogManager {

    private final String logPath

    public LogManager(String logPath) {
        this.logPath = logPath
    }

    /**
     * Appends a single node to the JSONL log.
     * @param entry The Map containing messageId, parentId, role, and content.
     */
    public void appendEntry(Map<String, Object> entry) {
        final File logFile = new File(this.logPath)
        final String jsonLine = JsonOutput.toJson(entry)
        logFile.append(jsonLine + "\n", "UTF-8")
    }

    /**
     * Reads the entire log file and returns a list of Maps.
     * @return List of message nodes.
     */
    public List<Map<String, Object>> readAllEntries() {
        final File logFile = new File(this.logPath)
        if (!logFile.exists()) {
            return [] as List
        }

        JsonSlurper slurper = new JsonSlurper()
        return logFile.readLines().collect { String line ->
            (Map<String, Object>) slurper.parseText(line)
        }
    }
}