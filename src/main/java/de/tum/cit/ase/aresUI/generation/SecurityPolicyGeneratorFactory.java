package de.tum.cit.ase.aresUI.generation;

import java.nio.file.Path;

/**
 * Factory abstraction for creating Ares-specific generators.
 *
 * <p>Description: Hides the concrete {@code SecurityPolicyReaderAndDirector} dependency so the UI code can
 * inject stubs or mocks in tests.
 *
 * <p>Design Rationale: Decoupling instantiation simplifies testing and isolates proprietary dependencies.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public interface SecurityPolicyGeneratorFactory {

    /**
     * Creates a new executor configured for the provided paths.
     *
     * @param policyFile YAML policy file
     * @param projectDirectory project root directory
     * @return configured executor
     * @since 0.0.1
     * @author Markus Paulsen
     */
    SecurityPolicyExecutor create(Path policyFile, Path projectDirectory);

    /**
     * Thin wrapper that mimics the behavior of {@code SecurityPolicyReaderAndDirector}.
     *
     * <p>Description: Provides the fluent API used by the generator pipeline.
     *
     * <p>Design Rationale: Enables testing UI logic without depending on the real Ares library.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     * @version 0.0.1
     */
    interface SecurityPolicyExecutor {
        /**
         * Builds the test cases in memory.
         *
         * @return same executor to allow fluent chaining
         * @since 0.0.1
         * @author Markus Paulsen
         */
        SecurityPolicyExecutor createTestCases();

        /**
         * Writes generated test cases to disk.
         *
         * @param outputDirectory destination directory
         * @since 0.0.1
         * @author Markus Paulsen
         */
        void writeTestCases(Path outputDirectory);
    }
}
