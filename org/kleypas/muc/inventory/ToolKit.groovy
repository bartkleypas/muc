package org.kleypas.muc.inventory

/**
 * Tool defines the interface for all tools that can be used by the model.
 */
interface Tool {
    /**
     * Generates a JSON-like map definition for the tool to be used by the model.
     * @return a map containing the tool's function definition
     */
    Map getDefinition()

    /**
     * Executes the tool's logic based on the action defined in the item's metadata.
     * @return the result of the execution as a string
     */
    String execute()
}

/**
 * ShellTool is a tool that executes shell commands.
 */
class ShellTool implements Tool {
    private Item item
    private static final List ALLOWED_BINARIES = ['ls', 'cat', 'grep', 'find', 'groovy', 'git', 'date']
    private static final List FORBIDDEN_STRINGS = [';', '&', '|', '>', '<', '`', '$', '..']

    ShellTool(Item item) {
        this.item = item
    }

    @Override
    Map getDefinition() {
        return [
            type: "function",
            function: [
                name: item.name.toLowerCase().replaceAll(/[^a-z0-9_]/, "_"),
                description: item.description,
                parameters: [
                    type: "object",
                    properties: [
                        action: [
                            type: "string",
                            description: "The specific action/instructions for ${item.name}, sent as a shell command."
                        ]
                    ],
                    required: ["action"]
                ]
            ]
        ]
    }

    @Override
    String execute() {
        if (!item.metadata.get("action")?.trim()) {
            throw new RuntimeException("This tool is broken; no action in the metadata, boss.")
        }

        String action = item.metadata.get("action")?.trim()
        println "Character is activating \"${item.name}\", trying to do \"${action}\" action..."

        if (FORBIDDEN_STRINGS.any { action.contains(it) }) {
            throw new RuntimeException("FATAL: Potential breach of trust detected.")
        }

        def parts = action.trim().split(/\s+/)
        def baseCommand = parts[0]

        if (!ALLOWED_BINARIES.contains(baseCommand)) {
            return "Error: The Scriptorium forbids the use of '${baseCommand}'."
        }

        def stdout = new StringBuilder()
        try {
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", action)
            pb.redirectErrorStream(true)

            Process proc = pb.start()

            proc.inputStream.eachLine { line ->
                stdout.append(line).append("\n")
            }

            proc.waitFor()
            def finalOutput = stdout.toString().trim()

            item.metadata.result = finalOutput.isEmpty() ? "[No Output]" : finalOutput

            return finalOutput.isEmpty() ? "[Success - No Output]" : finalOutput
        } catch (Exception e) {
            def errorMsg = "System Error during tool execution: ${e.message}"
            item.metadata.result = errorMsg
            return errorMsg
        }
    }
}

/**
 * ToolKit is a factory for creating tool handlers based on items.
 */
class ToolKit {
    /**
     * Creates a Tool handler for the given item if it is of type TOOL.
     * @param item the item to create a tool for
     * @return a Tool instance, or null if the item is not a tool
     */
    static Tool createTool(Item item) {
        if (item.type != ItemType.TOOL) {
            return null
        }
        // For now, all tools are ShellTools, but this factory can be extended
        // to return different Tool implementations based on item metadata or name.
        return new ShellTool(item)
    }
}
