# 🏗️ MUC Architectural Design and Rationale

The MUC project is designed around a lightweight, portable, and rapid-deployment architecture using the Groovy runtime. This document details the key constraints and design philosophies.

## 🧱 Core Architectural Constraints

### 1. The "No-Build" Mandate
* **Constraint:** The project strictly operates under a "no-build" architecture, meaning we do not use formal build tools like Gradle or Maven.
* **Rationale:** To maximize portability and minimize setup time, allowing the application to be invoked directly via the `groovy` runtime.
* **Implication:** All external dependencies must be resolved dynamically using the **`@Grab` annotation** if built-in Groovy/Java functionality is insufficient.

### 2. Execution Entry Point
* **Constraint:** The application entry point must be the **`main.groovy`** script.
* **Execution:** The app is invoked via the Groovy runtime: `groovy main.groovy [OPTIONS]`.
* **Examples of Invocation:**
    * `groovy main.groovy -t` (Integration tests)
    * `groovy main.groovy -c` (Direct chat interface)
    * `groovy main.groovy -i` (Image generation prompt)
    * `groovy main.groovy -v` (Verbose output)

### 3. Modularity and Structure
* **Design:** Modularity is achieved via local imports (`import path.to.Class`) of files located in the root directory.
* **Modules:** Helper classes like `Cli.groovy`, `Rng.groovy`, `Location.groovy`, and `Item.groovy` contain reusable, structured logic.
* **CLI Implementation:** Command-line argument and environment variable definitions **must be handled within a dedicated helper class, typically `Cli.groovy`**, which is then consumed by `main.groovy`.

---

## 💡 Groovy Style Philosophy

The design utilizes a **Dual-Style Coding Strategy** to optimize different parts of the application:

* **Style A (Scripting):** Used in `main.groovy` for immediate execution and flow control, prioritizing concise, idiomatic Groovy for **rapid iteration**.
* **Style B (Modules):** Used in all helper classes (e.g., `Item.groovy`) to ensure **maintainability, type safety, and reusability** by adopting more rigorous, Java-like explicit typing and class definitions.