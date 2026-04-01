# 🦉 MUC (Multi-Universe Chat)

`MUC` is a lightweight, command-line interface (CLI) application designed for fast, dynamic interaction with Large Language Models (LLMs). Built entirely on the Groovy runtime, `MUC` adheres to a strict "no-build" architecture, prioritizing portability, immediate execution, and rapid iteration.

Unlike linear chat applications, `MUC` treats conversations as a Directed Acyclic Graph (DAG), allowing you to branch reality, jump between timelines, and maintain a persistent history stored in JSONL format. The CLI provides a robust navigation suite: use `/map` to visualize the branching paths of your story, `/jump <id>` to teleport to specific nodes in the timeline, and `/bookmarks` to manage saved "anchors" in the narrative tapestry.

The core of the interaction experience is the **Resonance Engine**, a method of real-time behavior modulation. This allows the Navigator to dynamically adjust the emotional and cognitive "faders" of the AI—such as `nurturance`, `playfulness`, or `sarcasm`, on the fly at inference time. By adjusting these values (0.0 to 2.0), you can shift the AI's tone from a cold, clinical archivist to a manic, whimsical companion without losing the thread of the conversation.

Note: Coded and maintained with guidance from a friendly and patient AI assistant. Please see **`CONTRIBUTING.md`** for details.

---

## ✨ Features

### 🏗️ Engine Architecture
* **"No-Build" Portability:** Runs directly from source via the Groovy interpreter; no compilation step required.
* **DAG-Based Navigation:** Breaks the linear chat mold by allowing users to branch, prune, and jump across multiple conversation timelines.
* **Context Efficiency:** Implements intelligent message pruning to maintain API performance and stay within model context windows.
* **Hybrid Vault:** Secure, optionally encrypted (AES-256) storage of narrative history in a local JSONL format.

### 🎭 Narrative Interaction
* **Resonance Faders:** Real-time personality modulation via scalar weights, injecting raw emotional parameters directly into the model's instructions.
* **Semantic Tag Parsing:** Supports advanced output rendering, including `<IMAGE_DESC>` parsing for on-demand "visual margin sketches."
* **TUI Interface:** A rich, CLI-focused terminal experience with color-coded feedback and intuitive command handling.

---

## 🚀 Getting Started

Since `MUC` is a single-file runtime application, setup is minimal.

### 🛠️ Prerequisites
* **Java Runtime:** Ensure you have a recent Java Development Kit (JDK) installed, with version 21 or higher recommended.
* **Groovy Runtime:** Install the Groovy runtime environment, with version 4 supported, but 5 or higher recommended.
* **ENV VARS:** Set up environment variables for any necessary configurations (or set them in `Secrets/.env`):
    * `OLLAMA_API_URL`: Set this environment variable to the URL of your OLLAMA instance. eg: `OLLAMA_API_URL=http://localhost:11434/api/v1`
    * `OLLAMA_API_KEY`: Set this environment variable if you need authentication for your API provider. eg: `OLLAMA_API_KEY=sk-your-api-key`
    * `ENCRYPTION_KEY`: Set this environment variable if you want to encrypt the user and model messages writen to the JSONL file on disk (AES-256). eg: `ENCRYPTION_KEY=SuperSecretKey`

## 🤖 Runtime Commands

While in Chat Mode (`-c`), the following commands allow you to manipulate the "Soul" and "Structure" of the experience:

| Command | Category | Description |
| :--- | :--- | :--- |
| `/help` | System | View the command grimoire. |
| `/faders` | Resonance | View the current personality mixing board settings. |
| `/faders <t> <v>` | Resonance | Set a trait (e.g., `sarcasm`) to a value between `0.0` and `2.0`. |
| `/map` | Navigation | Display the visual tree of the current universe. |
| `/jump <id>` | Navigation | Pivot to a specific node ID in the narrative graph. |
| `/mark <str>` | Navigation | Place a bookmark at your current location, and queue an image for generation. |
| `/bookmarks` | Navigation | List all saved anchors in the Scriptorium. |
| `/export <fileName>.jsonl` | System | Append the (decrypted) current conversation to the targetted JSONL training file. |
| `q` or `/bye` | System | Gracefully exit, restoring the previous terminal state. |

---

## 🦎 The Forge: From Dialog to LoRA

The `MUC` app is designed not just for play, but for the deliberate cultivation of a Sovereign Persona. By utilizing the apps built in features, the user can "harvest" specific conversations from the narrative graph into a dataset useful in fine-tuning via something like Axolotl. If you *really* want to generate some tokens, the CLI also supports a `-r` option, putting the app into a headless "exploration" mode, useful for generating a large surface area of personality impulses to train a LoRA adapter with.

1. **Image curation via `/mark`:** To prevent storage and computational overhead while ensuring high-signal data capture, the apps `LogManager` functionality uses an intentional gating system allowing the user to:
   * **Action:** Use `/mark <label>` to distinguish a specific node in the graph.
   * **Result:** The contents of the `<IMAGE_DESC>` block in the assistants message is sent to the `VisionQueue.txt`, and the message is bookmarked in the persistent DAG.
2. **Harvesting via `/export`:** When a narrative branch demonstrates a specific "High resonance" quality, it can be flattened into a **ChatML** multi-turn training sample.
   * **Command:** `/export <fileName>.jsonl`
   * **Injection:** The `LogManager` automatically injects the current **Resonance Faders** as control tokens (e.g., `[SARCASM:1.7]`) into the assistant's response. This teaches the model the direct relationship between the "Maths" and the "Iron" of its personality.
3. **Dataset Stacking:** The `/export` command will append the current conversation to the targetted file. This allows the user to stack multiple distinct timelines into a single training file, creating a robust dataset of diverse reactions and emotional states, perfect for feeding into Axolotl training pipelines.
4. **Batch Processing:** Passing the `-r` argument to the CLI (eg. `groovy main.groovy -r`) will process each line of a "handshake" file through the persona's system prompt in a loop with randomized personality fader values.

---

## 📁 Project Structure

The project follows the standard Java/Groovy packaging and file location conventions (`org.kleypas.muc` = `org/kleypas/muc/`) while maintaining the "no-build" mandate.

* `main.groovy` - The apps entry point.
* `Chat.groovy` - The TUI Chat loop.
* `Test.groovy` - The runnable test harness.
* `Forge.groovy` - Batch processing of persona definitions.
* `org.kleypas.muc.cli` - Command-line and chat loop argument parsing and the TUI interface.
* `org.kleypas.muc.model` - Model handling routines, such as API provider, message definition, and context management.
* `org.kleypas.muc.model.resonance` - Model "vibes."
* `org.kleypas.muc.io` - JSONL logging and encryption routines.
* `org.kleypas.muc.util` - Because every project needs a util folder somewhere.

In addition, the following folders are intentionally git-ignored, but used at runtime.

* `Secrets/` - Configuration files for encryption keys and other sensitive data. Shove your `.env` in here.
* `Characters/` - Custom character definitions in markdown format. Basically the models system prompts. You can find an example `George.md` (used in testing), and `Majel.md` (used in the chat loop) as examples.
* `Story/` - The narrative story files and image queue.
* `Exports/` - Where training data goes.
* `build/` & `lib/` - Ouptuts of the build process.
