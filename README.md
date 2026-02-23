# MUC (Multi-Universe Chat) 🤖

`MUC` is a lightweight, command-line interface (CLI) application designed for fast, dynamic interaction with Large Language Models (LLMs). Built entirely on the Groovy runtime, `MUC` adheres to a strict "no-build" architecture, prioritizing portability, immediate execution, and rapid iteration.


Unlike linear chat applications, `MUC` treats conversations as a Directed Acyclic Graph (DAG), allowing you to branch reality, jump between timelines, and maintain a persistent and optionally encrypted history, stored in a JSONL file in the `Story/` directory.

Note: Coded and maintained with guidance from a friendly and patient AI assistant.

---

## ✨ Features

* **CLI Focused:** All functionality, from chatting to testing, is driven by command-line arguments.
* **Context Management:** Implements robust message pruning logic to manage conversation history, preventing context window bloat, and maintaining API cost efficiency.
* **API Integration:** Supports attaching to a local or hosted LLM API for seamless interaction.
* **Tag Parsing:** Supports parsing tags produced by the LLM, such as `<IMAGE_DESC>`.
* **Hybrid Vault:** Writes messages in an optionally encrypted JSONL file.
* **DAG based navigation:** Supports a DAG-based navigation system for easy exploration of alternative paths through the narrative.

---

## 🚀 Getting Started

Since MUC is a single-file runtime application, setup is minimal.

### Prerequisites

1.  **Java Runtime:** Ensure you have a recent Java Development Kit (JDK) installed, with version 21 or higher recommended.
2.  **Groovy Runtime:** Install the Groovy runtime environment, with version 4 supported, but 5 or higher recommended.
3.  **ENV VARS:** Set up environment variables for any necessary configurations:
    1. `OLLAMA_API_URL`: Set this environment variable to the URL of your OLLAMA instance. eg: `http://localhost:11434/api/v1`
    2. `OLLAMA_API_KEY`: Set this environment variable if you need authentication for your API provider. eg: `OLLAMA_API_KEY=sk-your-api-key`
    3. `ENCRYPTION_KEY`: Set this environment variable if you want to encrypt the user and model messages to the JSONL file on disk. (AES-256)

### Running the Application

All application modes are invoked directly via the `groovy` command pointing to the entry point, `main.groovy`.

| Command | Description |
| :--- | :--- |
| `groovy main.groovy -c` | **Chat Mode:** Starts a direct, interactive, multi-turn chat session in a TUI, leveraging tags to flavor the output rendering. (see details below for runtime usage) |
| `groovy main.groovy -i` | **Image Mode:** Prompts the model to generate an image based on your text input (requires relevant API configuration). |
| `groovy main.groovy -t` | **Test Mode:** Runs the integrated test harness defined in `Test.groovy`. |
| `groovy main.groovy -v` | **Verbose Mode:** Adds verbose output to the console for debugging (can be combined with other commands, e.g., `groovy main.groovy -c -v`). |
| `groovy main.groovy --debate` | **Debate:** Starts a debate. Put in a `-v` to get the output on the command line. |

The runtime commands available in the chat mode are:
* `/map` - View the current conversation map.
* `/jump <id>` - Jump to a specific message in the conversation map (must target assistant entry)
* `/bye` `/q` - Gracefully close the app and restore your terminal.

---

## 🛠️ Project Structure

The project follows the standard Java/Groovy packaging and file location conventions (`org.kleypas.muc`) while maintaining the "no-build" mandate.

* **`main.groovy`:** The single application entry point (uses Groovy Scripting Style).
* **`Test.groovy`:** The runnable test harness (uses Groovy Scripting Style).
* **`org.kleypas.muc.cli`:** Command-line argument parsing and the TUI interface.
* **`org.kleypas.muc.model`:** Model handling routines, such as API provider, message definition, context management, and output tagging.
* **`org.kleypas.muc.io`:** JSONL logging and encryption routines.

In addition, the following folders are intentionally git-ignored, but used at runtime:

* **`Secrets/`:** Configuration files for encryption keys and other sensitive data. Shove your `.env` in here.
* **`Characters/`:** Custom character definitions in markdown. Basically the models system prompts. You can find an example `George.md` in here to test out.
* **`Story/`:** The narrative story files.
* **`build/` & `lib/`:** Ouptuts of the build process.

For details on code style and architectural mandates, including assistant instructions, please see **`CONTRIBUTING.md`**.