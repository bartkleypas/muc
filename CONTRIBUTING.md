# 🛠️ Contribution Guide for MUC (Multi-User Chat)

Welcome! Thank you for your interest in contributing to the MUC project. This project is built on a specific, lightweight architecture, so please adhere to the following mandates to ensure code consistency and maintainability.

## 🧑‍💻 Groovy Coding Mandates

The MUC project utilizes a unique **Dual-Style Coding Strategy** to balance rapid iteration with structured maintainability. Your coding style must adapt based on the purpose of the file:

### Style A: Scripting Mode (High-Level Execution)

This style is reserved *only* for **`main.groovy`** and files containing top-level execution logic.

| Feature | Goal | Requirements |
| :--- | :--- | :--- |
| **Goal** | **Fast, concise, idiomatic Groovy** for rapid iteration. | |
| **Typing** | Dynamic typing | Prioritize `def` for variables and return types. |
| **Syntax** | Concise Syntax | Omit semicolons. |
| **Features** | Idiomatic | Leverage the **Elvis operator `?:`**, **Safe Navigation `?.`**, and closures. |

### Style B: Helper/Module Mode (Reusable Logic)

This style is mandatory for all helper classes, modules, and complex business logic (e.g., `Cli.groovy`, `Item.groovy`, etc.).

| Feature | Goal | Requirements |
| :--- | :--- | :--- |
| **Goal** | **Structured, maintainable, and type-safe** reusable logic. | |
| **Typing** | Strong, Explicit Typing | Prefer **explicit variable types** (`String`, `List`, `int`, etc., over `def`). |
| **Methods** | Strict Definitions | Include **explicit return types** and **parameter types** for all methods. |
| **Resonance** | Scalar Logic | Personality traits must be handled as `Double` values, typically constrained between `0.0` and `2.0`. |
| **Safety** | Null-Awareness | Be cautious with the Elvis operator `?:` for scalar values where `0.0` is a valid input; use explicit `null` checks to preserve mathematical intent. |

## ⚙️ Development Constraints

1.  **"No-Build" Architecture:** We strictly avoid all formal build tools like **Gradle** or **Maven**. The application must be runnable directly via the `groovy` runtime.
2.  **Dependency Management:** Prefer built-in Groovy and Java functionality. If an external library is *mandatory*, it **must be handled using the Groovy `@Grab` annotation** to ensure dependencies are fetched without a formal build process.
3.  **Scalar Range Convention:** The standard for behavioral modulation faders is a `1.0` center, ranging from `0.0` (nullified/inverted) to `2.0` (maximized).

---

## 🤖 Interaction Protocol (For AI Assistance)

Please note these guidelines when using an AI assistant for development:

1.  **Code First, Explain Second:** Provide the complete, ready-to-use code in a single Groovy code block first.
2.  **Concise Explanation:** Follow the code block with a **brief explanation** of the logic, the rationale for the Groovy features used, and a note on potential **side-effects, security, or performance trade-offs**.
3.  **Security Focus:** Always flag and address potential security vulnerabilities (e.g., dynamic evaluation risks, insecure data handling).