package org.kleypas.muc.io

import org.kleypas.muc.model.Message
import org.kleypas.muc.model.Resonance

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
            timestamp: entry.timestamp,
            messageId: entry.messageId,
            parentId: entry.parentId,
            role: entry.role,
            bookmark: entry.bookmark,
            encrypted: false,
            content: entry.content
        ] + entry.getStats()

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
     * Finds an existing entry by ID and updates its metadata (such as bookmarking)
     * while preserving the rest of the message integrity
     */
    void updateEntry(Message updatedMsg, byte[] key = null) {
        byte[] encryptionKey = key ?: this.persistentKey
        File file = new File(this.logPath)
        if (!file.exists()) return

        // Read all lines and parse them
        List<String> lines = file.readLines()
        List<String> updatedLines = lines.collect { line ->
            def entry = new JsonSlurper().parseText(line)

            // Update our targetted message
            if (entry.messageId == updatedMsg.messageId) {
                if (encryptionKey && updatedMsg.bookmark) {
                    entry.bookmark = CipherService.encrypt(updatedMsg.bookmark, encryptionKey)
                    entry.encrypted = true
                } else {
                    entry.bookmark = updatedMsg.bookmark
                }
                return JsonOutput.toJson(entry)
            }
            return line
        }

        file.withWriter { writer ->
            updatedLines.each { writer.println(it) }
        }
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
            if (entry.encrypted == true && activeKey && entry.bookmark) {
                try {
                    entry.bookmark = CipherService.decrypt(entry.bookmark as String, activeKey)
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
     * Finds a specific message entry by a partial ID match
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
        def allEntries = readAllEntries() ?: []
        int total = allEntries.size()
        def uniqueParents = allEntries.findAll { it.parentId != null }.collect { it.parentId }.unique()
        int branches = uniqueParents.size()
        String lastId = allEntries.isEmpty() ? "NONE" : allEntries.last().messageId?.take(8)
        return [
            totalMessages: total,
            branchCount: branches,
            lastJumpId: lastId
        ]
    }
}