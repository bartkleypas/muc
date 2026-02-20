package org.kleypas.muc.io

import org.kleypas.muc.model.Message

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

    private final byte[] persistentKey

    public LogManager(String logPath, byte[] key = null) {
        this.logPath = logPath
        this.persistentKey = key
    }

    /**
     * Appends a single node to the JSONL log.
     * @param entry The Map containing messageId, parentId, role, and content.
     */
    public void appendEntry(Message entry, byte[] key = null) {
        final File logFile = new File(this.logPath)

        byte[] encryptionKey = key ?: this.persistentKey

        Map logMap = [
            timestamp: entry.timestamp.toString(),
            messageId: entry.messageId,
            parentId: entry.parentId,
            role: entry.role,
            nurturance: entry.nurturance,
            playfulness: entry.playfulness,
            steadfastness: entry.steadfastness,
            attunement: entry.attunement,
            encrypted: false,
            content: entry.content
        ]

        // If an encryption key is provided, wrap the contents in the CipherService
        if (encryptionKey && entry.content) {
            logMap.content = CipherService.encrypt(entry.content as String, encryptionKey)
            logMap.encrypted = true
        } else {
            logMap.encrypted = false
        }

        final String jsonLine = JsonOutput.toJson(logMap)
        logFile.append(jsonLine + "\n", "UTF-8")
    }

    /**
     * Reads the entire log file and returns a list of Maps.
     * @param key (Optional) The key used for decryption if present (or re-use the persistent key)
     * @return List of message nodes.
     */
    public List<Map<String, Object>> readAllEntries(byte[] key = null) {
        byte[] activeKey = key ?: this.persistentKey
        final File logFile = new File(this.logPath)
        if (!logFile.exists()) {
            return [] as List
        }

        JsonSlurper slurper = new JsonSlurper()
        return logFile.readLines().collect { String line ->
            Map<String, Object> entry = (Map<String, Object>) slurper.parseText(line)
            if (entry.encrypted == true && activeKey) {
                try {
                    entry.content = CipherService.decrypt(entry.content as String, activeKey)
                } catch (Exception e) {
                    entry.content = "[DECRYPTION_FAILED: Ghost in the machine]"
                }
            }
            return entry
        }
    }

    Map<String, List<Map>> buildHistoryTree() {
        List<Map> allEntries = readAllEntries()

        return allEntries.groupBy { it.parentId as String }
    }

    /**
     * Finds a specific message entry py a partial ID match
     * Useful for jumping to branches identified in the TUI map.
     */
    Map findEntryByPartialId(String partialId) {
        List<Map> allEntries = readAllEntries()
        return allEntries.find { it.messageId.startsWith(partialId) }
    }

    /**
     * Get some stats about our conversation
     */
    Map getChronicleStats() {
        def allEntries = readAllEntries()
        def branches = allEntries.groupBy { it.parentId }.size()
        return [
            totalMessage: allEntries.size(),
            branchCount: branches,
            lastJumpId: allEntries.last()?.messageId?.take(8) ?: "NONE"
        ]
    }
}