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
    public void appendEntry(Map<String, Object> entry, String encryptionKey = null) {
        final File logFile = new File(this.logPath)

        // If an encryption key is provided, wrap the contents in the CipherService
        if (encryptionKey && entry.content) {
            entry.content = CipherService.encrypt(entry.content as String, encryptionKey)
            entry.encrypted = true
        } else {
            entry.encrypted = false
        }

        final String jsonLine = JsonOutput.toJson(entry)
        logFile.append(jsonLine + "\n", "UTF-8")
    }

    /**
     * Reads the entire log file and returns a list of Maps.
     * @param encryptionKey The key used for decryption if present
     * @return List of message nodes.
     */
    public List<Map<String, Object>> readAllEntries(String encryptionKey = null) {
        final File logFile = new File(this.logPath)
        if (!logFile.exists()) {
            return [] as List
        }

        JsonSlurper slurper = new JsonSlurper()
        return logFile.readLines().collect { String line ->
            Map<String, Object> entry = (Map<String, Object>) slurper.parseText(line)
            if (entry.encrypted == true && encryptionKey) {
                try {
                    entry.content = CipherService.decrypt(entry.content as String, encryptionKey)
                } catch (Exception e) {
                    entry.content = "[DECRYPTION_FAILED: Ghost in the machine]"
                }
            }
            return entry
        }
    }
}