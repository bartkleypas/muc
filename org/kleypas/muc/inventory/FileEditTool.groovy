package org.kleypas.muc.inventory

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * FileEditTool provides structured text editing capabilities,
 * allowing for safe and precise manipulation of files within the project.
 */
class FileEditTool implements Tool {
    private Item item
    private static final List ALLOWED_EXTENSIONS = ['.groovy', '.md', '.json', '.jsonl', '.txt', '.env']
    private static final List FORBIDDEN_DIR_STRINGS = ['..', '/etc', '/var', '/usr', '/bin', '/sbin', '/lib', '/root', '/boot']

    FileEditTool(Item item) {
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
                        path: [
                            type: "string",
                            description: "The relative path to the file to be edited."
                        ],
                        mode: [
                            type: "string",
                            enum: ["read", "replace", "insert", "append", "overwrite"],
                            description: "The editing mode to use."
                        ],
                        search: [
                            type: "string",
                            description: "The string or regex pattern to search for (required for replace, insert, append)."
                        ],
                        replacement: [
                            type: "string",
                            description: "The text to insert or replace with (required for replace, insert, append, overwrite)."
                        ],
                        line_number: [
                            type: "integer",
                            description: "The specific line number to target (required for insert)."
                        ]
                    ],
                    required: ["path", "mode"]
                ]
            ]
        ]
    }

    @Override
    String execute() {
        Map<String, Object> params = item.metadata.get("params") as Map
        if (params == null) {
            return "Error: No parameters provided for FileEditTool."
        }

        String pathStr = params.get("path")
        String mode = params.get("mode")
        
        if (!pathStr || !mode) {
            return "Error: 'path' and 'mode' are required parameters."
        }

        if (isForbiddenPath(pathStr)) {
            return "Error: Access to path '${pathStr}' is forbidden."
        }

        try {
            java.nio.file.Path filePath = Paths.get(pathStr)
            if (!Files.exists(filePath)) {
                if (mode == "read") return "Error: File does not exist."
                // For overwrite/append, we can create it if it doesn't exist, 
                // but let's be safe and only allow editing existing files for now.
                return "Error: File does not exist."
            }

            String content = new String(Files.readAllBytes(filePath))
            String result = ""

            switch (mode) {
                case "read":
                    result = content
                    break

                case "replace":
                    String search = params.get("search")
                    String replacement = params.get("replacement")
                    if (!search || replacement == null) return "Error: 'search' and 'replacement' are required for replace mode."
                    
                    // Check if it's a regex or literal
                    if (search.startsWith("/") && search.endsWith("/")) {
                        String regex = search.substring(1, search.length() - 1)
                        content = content.replaceAll(regex, replacement)
                    } else {
                        content = content.replace(search, replacement)
                    }
                    Files.write(filePath, content.getBytes())
                    result = "Successfully replaced occurrences of '${search}' in ${pathStr}."
                    break

                case "insert":
                    Integer lineNum = params.get("line_number") as Integer
                    String textToInsert = params.get("replacement")
                    if (lineNum == null || textToInsert == null) return "Error: 'line_number' and 'replacement' are required for insert mode."
                    
                    List<String> lines = content.split(/\r?\n\n?|(?<=\n)/).collect { it.trim() }.findAll { !it.isEmpty() }
                    // This is a simplistic line splitting. A better way would be to use a proper line iterator.
                    // For now, let's use a more robust approach.
                    List<String> allLines = Files.readAllLines(filePath)
                    if (lineNum > allLines.size() + 1 || lineNum < 1) return "Error: Line number out of bounds."
                    
                    allLines.add(lineNum - 1, textToInsert)
                    Files.write(filePath, allLines)
                    result = "Successfully inserted text at line ${lineNum} in ${pathStr}."
                    break

                case "append":
                    String textToAppend = params.get("replacement")
                    if (textToAppend == null) return "Error: 'replacement' is required for append mode."
                    Files.write(filePath, (textToAppend + "\n").getBytes(), StandardOpenOption.APPEND)
                    result = "Successfully appended text to ${pathStr}."
                    break

                case "overwrite":
                    String newContent = params.get("replacement")
                    if (newContent == null) return "Error: 'replacement' is required for overwrite mode."
                    Files.write(filePath, newContent.getBytes())
                    result = "Successfully overwrote ${pathStr}."
                    break

                default:
                    return "Error: Unknown mode '${mode}'."
            }

            item.metadata.result = result
            return result

        } catch (Exception e) {
            String errorMsg = "FileEditTool Error: ${e.message}"
            item.metadata.result = errorMsg
            return errorMsg
        }
    }

    private boolean isForbiddenPath(String pathStr) {
        if (pathStr.contains("..")) return true
        if (FORBIDDEN_DIR_STRINGS.any { pathStr.contains(it) }) return true
        return !ALLOWED_EXTENSIONS.any { pathStr.endsWith(it) }
    }
}
