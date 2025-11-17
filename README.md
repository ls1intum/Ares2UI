## Ares UI

A JavaFX desktop application for generating test cases in precompiled mode from Ares 2. The application offers both a graphical user interface and a command line interface, allowing users to enter a YAML policy and a target project, which will automatically generate the corresponding JUnit tests in each project.

---

### Table of Contents
1. [Prerequisites](#prerequisites)
2. [Build & Verification](#build--verification)
3. [Running the JavaFX UI](#running-the-javafx-ui)
4. [Running the CLI Entry Point](#running-the-cli-entry-point)
5. [Testing & Coverage](#testing--coverage)
6. [Project Structure](#project-structure)
---

### Prerequisites
- JDK 25.
- Maven 3.9+.
- The maven `de.tum.cit.ase:ares` artifact.
- A maven or gradle project to generate tests for.

---

### Build & Verification
Compile and run all tests:
```bash
mvn clean verify
```

---

### Running the JavaFX UI
```bash
mvn javafx:run
```
1. Choose the YAML security policy file.
2. Choose the Maven project directory (root containing `src/`).
3. Click **Create Files** – tests are written to `<project>/src/test/java`.

---

### Running the CLI Entry Point
```bash
mvn -Dexec.mainClass=de.tum.cit.ase.aresUI.Main \
    -Dexec.args="/path/to/SecurityConfiguration.yaml /path/to/project"
```
- Argument 1 – YAML policy file (required)
- Argument 2 – Target Maven project directory (required)

Tests are written to `<project>/src/test/java`.

Exit codes:

| Code | Meaning                                  |
|------|------------------------------------------|
| 0    | Success, tests generated                 |
| 1    | Invalid/missing arguments                |
| 2    | Generation failed (error is printed)     |

---

### Testing & Coverage
- **Standard tests**: `mvn test`
- **Coverage report**: `mvn jacoco:report` (after running tests)
    - HTML report: `target/site/jacoco/index.html`
    - XML report: `target/site/jacoco/jacoco.xml`
- **Full suite incl. JavaFX tests** (requires GUI backend):
```bash
mvn -Denable.javafx.tests=true test jacoco:report
```

---

### Project Structure
```
src/
 ├─ main/java/de/tum/cit/ase/aresUI
 │    ├─ ViewModel.java   (JavaFX Application)
 │    ├─ View.java        (UI layout + Rx bindings)
 │    ├─ Model.java       (user selections)
 │    ├─ Main.java        (CLI entry point)
 │    └─ generation/...   (Ares generator glue code)
 └─ test/java/de/tum/cit/ase/aresUI
      ├─ ViewModelTest.java, MainTest.java, etc.
      └─ testing/FxTestSupport.java (utility for FX tests)
```
