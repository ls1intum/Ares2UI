package de.tum.cit.ase.aresUI;

import de.tum.cit.ase.aresUI.generation.AresTestGenerator;
import de.tum.cit.ase.aresUI.generation.DefaultSecurityPolicyGeneratorFactory;

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Command-line entry point for invoking the generator without the UI.
 *
 * <p>Description: Offers a minimal interface for running the Ares generator via shell scripts or CI jobs,
 * bypassing JavaFX entirely.
 *
 * <p>Design Rationale: Keeping a separate CLI facilitates debugging and batch processing scenarios.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public final class Main {

    /**
     * Hidden constructor to prevent instantiation.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private Main() {
    }

    /**
     * JVM entry point.
     *
     * @param args CLI arguments
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public static void main(String[] args) {
        runCli(args, new AresTestGenerator(new DefaultSecurityPolicyGeneratorFactory()), System.out, System.err);
    }

    /**
     * Parses CLI parameters and triggers test generation.
     *
     * @param args CLI arguments provided by the user
     * @param generator generator instance used for execution
     * @param out output stream used for informational messages
     * @param err error stream used for failures
     * @return exit code (0 on success, non-zero otherwise)
     * @since 0.0.1
     * @author Markus Paulsen
     */
    static int runCli(String[] args, AresTestGenerator generator, PrintStream out, PrintStream err) {
        if (args.length < 2) {
            err.println("Usage: Main <policyFile> <projectDirectory> [outputDirectory]");
            return 1;
        }

        Path policyFile = Path.of(args[0]).toAbsolutePath().normalize();
        Path projectDirectory = Path.of(args[1]).toAbsolutePath().normalize();
        Path outputDirectory = args.length >= 3
                ? Path.of(args[2]).toAbsolutePath().normalize()
                : projectDirectory.resolve("src").resolve("test").resolve("java");

        try {
            generator.generateTests(policyFile, projectDirectory, outputDirectory);
            out.printf("Tests generated in %s%n", outputDirectory);
            return 0;
        } catch (Exception e) {
            err.printf("Failed to generate tests: %s%n", e.getMessage());
            e.printStackTrace(err);
            return 2;
        }
    }
}
