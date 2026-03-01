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
 */
public class LogManager {

    private final String logPath

    private final byte[] persistentKey

    public LogManager(String logPath, byte[] key = null) {
        this.logPath = logPath
        this.persistentKey = key
    }

    /**
     * Reads the entire log file and returns a list of Maps.
     * @param key (Optional) The key used for decryption if present (or re-use the persistent key)
     * @return List of formal Message objects.
     */
    public List<Message> readAllEntries(byte[] key = null) {
        byte[] activeKey = key ?: this.persistentKey
        final File logFile = new File(this.logPath)
        if (!logFile.exists()) return [] as List<Message>

        JsonSlurper slurper = new JsonSlurper()

        return logFile.readLines().collect { String line ->
            Map<String, Object> raw = (Map<String, Object>) slurper.parseText(line)

            // Hard gate our decription logics.
            if (raw.encrypted == true) {
                if (!activeKey) {
                    // Hard-gate: If encrypted but no key, we provide the 'Ghost' content
                    raw.content = "[DECRYPTION_FAILED: Missing Key]"
                } else {
                    try {
                        raw.content = CipherService.decrypt(raw.content as String, activeKey)
                        if (raw.bookmark) {
                            raw.bookmark = CipherService.decrypt(raw.bookmark as String, activeKey)
                        }
                    } catch (Exception e) {
                        raw.content = "[DECRYPTION_FAILED: Invalid Key]"
                    }
                }
            }

            // Re-hydrate a message object to return.
            Message msg = new Message(
                messageId: raw.messageId as String,
                parentId: raw.parentId as String,
                role: raw.role as String,
                content: raw.content as String,
                timestamp: raw.timestamp as String
            )
            msg.bookmark = raw.bookmark as String

            // Reconstructs the resonance state from the raw map.
            msg.resonance = new Resonance(raw)

            return msg
        } as List<Message> // Give back a list of (decrypted) messages.
    }

    public Map<String, List<Message>> buildHistoryTree() {
        return readAllEntries().groupBy { it.parentId as String }
    }

    public Message findEntryByPartialId(String partialId) {
        return readAllEntries().find { it.messageId.startsWith(partialId) }
    }

    public Map getChronicleStats() {
        List<Message> allEntries = readAllEntries() ?: []
        return [
            totalMessages: allEntries.size(),
            branchCount: allEntries.findAll { it.parentId != null}.collect { it.parentId }.unique().size(),
            lastJumpId: allEntries.isEmpty() ? "NONE" : allEntries.last().messageId?.take(8)
        ]
    }

    /**
     * Appends a single node to the JSONL log.
     * @param entry The Map containing messageId, parentId, role, content, etc.
     * @param key (Optional) The byte array representing the encryption key.
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
     * while preserving the rest of the message integrity.
     * @param updatedMsg The new message to update.
     * @param key (Optional) The byte array representing the encryption key.
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
}