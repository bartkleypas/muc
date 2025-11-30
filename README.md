# MUC (Multi-User Chat) 🤖

MUC is a lightweight, command-line interface (CLI) application designed for fast, dynamic interaction with Large Language Models (LLMs). Built entirely on the Groovy runtime, MUC adheres to a strict "no-build" architecture, prioritizing portability, immediate execution, and rapid iteration.

Note: Coded and maintained with patient guidance from a friendly AI assistant.

---

## ✨ Features

* **No-Build Architecture:** Runs directly using the Groovy runtime—no Gradle, Maven, or complex build setups required.
* **CLI Focused:** All functionality, from chatting to testing, is driven by command-line arguments.
* **Context Management:** Implements robust message pruning logic to manage conversation history, prevent context window bloat, and maintain API cost efficiency.
* **Dual-Style Groovy:** Code is cleanly separated into high-speed scripting logic and structured, type-safe modules for maximum maintainability.

---

## 🚀 Getting Started

Since MUC is a single-file runtime application, setup is minimal.

### Prerequisites

1.  **Java Runtime:** Ensure you have a Java Development Kit (JDK) installed (version 8 or newer is recommended).
2.  **Groovy Runtime:** Install the Groovy runtime environment.
3.  **API Key:** An environment variable must be set for the LLM API key (e.g., `export OLLAMA_API_KEY="sk-..."`).

### Running the Application

All application modes are invoked directly via the `groovy` command pointing to the entry point, `main.groovy`.

| Command | Description |
| :--- | :--- |
| `groovy main.groovy -c` | **Chat Mode:** Starts a direct, interactive, multi-turn chat session. |
| `groovy main.groovy -i` | **Image Mode:** Prompts the model to generate an image based on your text input (requires relevant API configuration). |
| `groovy main.groovy -t` | **Test Mode:** Runs the integrated test harness defined in `Test.groovy`. |
| `groovy main.groovy -v` | **Verbose Mode:** Adds verbose output to the console for debugging (can be combined with other commands, e.g., `groovy main.groovy -c -v`). |
| `groovy main.groovy --debate` | **Debate:** Starts a debate. Put in a `-v` to get the output on the command line. |

---

## 🛠️ Project Structure

The project follows the standard Java/Groovy packaging convention (`org.kleypas.muc`) while maintaining the "no-build" mandate.

* **`main.groovy`:** The single application entry point (uses Groovy Scripting Style).
* **`Test.groovy`:** The runnable test harness (uses Groovy Scripting Style).
* **`org/kleypas/muc/cli/`:** Contains structured, type-safe logic for command-line argument parsing (`Cli.groovy`).
* **`org/kleypas/muc/model/`:** Contains structured data models and business logic, such as context management (`Context.groovy`).

For details on the coding style and architectural mandates, please see **`CONTRIBUTING.md`** and **`DESIGN.md`**.