package de.tum.cit.ase.aresUI.generation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AresTestGenerator}.
 *
 * <p>Description: Verifies validation and delegation logic for test generation.
 *
 * <p>Design Rationale: Ensures errors are surfaced correctly before invoking Ares components.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
class AresTestGeneratorTest {

    /**
     * Temporary directory used for test files.
     */
    @TempDir
    Path tempDir;

    /**
     * Ensures missing policies trigger validation errors.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void failsWhenPolicyFileMissing() {
        AresTestGenerator generator = new AresTestGenerator((policy, project) -> null);
        Path missingFile = tempDir.resolve("missing.yaml");
        Path projectDir = createDirectory("project");
        Path outputDir = projectDir.resolve("src/test/java");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.generateTests(missingFile, projectDir, outputDir));
        assertTrue(exception.getMessage().contains("Policy file"), "Expected message to mention policy file");
    }

    /**
     * Ensures missing project directories trigger validation errors.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void failsWhenProjectDirectoryMissing() throws IOException {
        AresTestGenerator generator = new AresTestGenerator((policy, project) -> null);
        Path policyFile = Files.writeString(tempDir.resolve("SecurityConfiguration.yaml"), "policy: value");
        Path missingProject = tempDir.resolve("missing-project");
        Path outputDir = tempDir.resolve("generated");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.generateTests(policyFile, missingProject, outputDir));
        assertTrue(exception.getMessage().contains("Project directory"));
    }

    /**
     * Verifies directory creation and factory invocation success path.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void runsFactoryAndCreatesOutputDirectory() throws IOException {
        RecordingFactory factory = new RecordingFactory();
        AresTestGenerator generator = new AresTestGenerator(factory);

        Path policyFile = Files.writeString(tempDir.resolve("SecurityConfiguration.yaml"), "policy: value");
        Path projectDir = createDirectory("project");
        Path outputDir = projectDir.resolve("generated-tests");

        generator.generateTests(policyFile, projectDir, outputDir);

        assertTrue(Files.isDirectory(outputDir), "Output directory should have been created");
        assertTrue(factory.writeCalled.get(), "Factory should have written tests");
        assertEquals(outputDir.toAbsolutePath(), factory.writtenPath.get());
        assertEquals(policyFile.toAbsolutePath(), factory.policyPath.get());
        assertEquals(projectDir.toAbsolutePath(), factory.projectPath.get());
    }

    /**
     * Ensures null output directories raise errors.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void throwsWhenOutputDirectoryNull() throws IOException {
        RecordingFactory factory = new RecordingFactory();
        AresTestGenerator generator = new AresTestGenerator(factory);
        Path policyFile = Files.writeString(tempDir.resolve("policy.yaml"), "policy: value");
        Path projectDir = createDirectory("project");

        assertThrows(NullPointerException.class,
                () -> generator.generateTests(policyFile, projectDir, null));
    }

    /**
     * Utility for creating directories in the temp workspace.
     *
     * @param name directory name
     * @return created path
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private Path createDirectory(String name) {
        try {
            return Files.createDirectory(tempDir.resolve(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

        /**
         * Recording factory stub used for assertions.
     *
     * <p>Description: Captures inputs and indicates when writes occurred.
     *
     * <p>Design Rationale: Lightweight stand-in instead of the real Ares factory.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     * @version 0.0.1
     */
    private static class RecordingFactory implements SecurityPolicyGeneratorFactory {
        /**
         * Tracks whether writeTestCases was invoked.
         */
        private final AtomicBoolean writeCalled = new AtomicBoolean(false);
        /**
         * Captures the output directory path.
         */
        private final AtomicReference<Path> writtenPath = new AtomicReference<>();
        /**
         * Captures the policy path.
         */
        private final AtomicReference<Path> policyPath = new AtomicReference<>();
        /**
         * Captures the project path.
         */
        private final AtomicReference<Path> projectPath = new AtomicReference<>();

        /**
         * Records the provided paths and returns a self-recording executor.
         *
         * @param policyFile policy location
         * @param projectDirectory project location
         * @return executor stub
         * @since 0.0.1
         * @author Markus Paulsen
         */
        @Override
        public SecurityPolicyExecutor create(Path policyFile, Path projectDirectory) {
            policyPath.set(policyFile);
            projectPath.set(projectDirectory);
            return new SecurityPolicyExecutor() {
                /**
                 * Marks test case creation occurred.
                 *
                 * @return this executor
                 * @since 0.0.1
                 * @author Markus Paulsen
                 */
                @Override
                public SecurityPolicyExecutor createTestCases() {
                    return this;
                }

                /**
                 * Records the output directory written to.
                 *
                 * @param outputDirectory destination path
                 * @since 0.0.1
                 * @author Markus Paulsen
                 */
                @Override
                public void writeTestCases(Path outputDirectory) {
                    writeCalled.set(true);
                    writtenPath.set(outputDirectory);
                }
            };
        }
    }
}
