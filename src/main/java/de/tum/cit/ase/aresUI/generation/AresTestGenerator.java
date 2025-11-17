package de.tum.cit.ase.aresUI.generation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Validates paths and delegates to {@link SecurityPolicyGeneratorFactory}.
 *
 * <p>Description: Coordinates validation of user-specified directories before invoking the Ares test generation pipeline.
 *
 * <p>Design Rationale: Separating validation from the UI keeps responsibilities focused and enables reuse.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public class AresTestGenerator {

    /**
     * Factory responsible for instantiating Ares-specific executors.
     */
    private final SecurityPolicyGeneratorFactory factory;

    /**
     * Creates a generator with the provided factory.
     *
     * @param factory provider for {@link SecurityPolicyGeneratorFactory.SecurityPolicyExecutor}
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public AresTestGenerator(SecurityPolicyGeneratorFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory must not be null");
    }

    /**
     * Validates inputs and triggers the executor to create and write tests.
     *
     * @param policyFile YAML policy file
     * @param projectDirectory project directory containing the Maven project
     * @param outputDirectory directory where generated tests should be written
     * @throws IOException when the output directory cannot be created
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public void generateTests(Path policyFile, Path projectDirectory, Path outputDirectory) throws IOException {
        validatePolicyFile(policyFile);
        validateProjectDirectory(projectDirectory);
        Path normalizedOutput = createOutputDirectory(outputDirectory);

        SecurityPolicyGeneratorFactory.SecurityPolicyExecutor executor =
                factory.create(policyFile.toAbsolutePath(), projectDirectory.toAbsolutePath());
        executor.createTestCases().writeTestCases(normalizedOutput);
    }

    /**
     * Ensures the provided policy file exists.
     *
     * @param policyFile file to validate
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void validatePolicyFile(Path policyFile) {
        Objects.requireNonNull(policyFile, "policyFile must not be null");
        if (!Files.exists(policyFile) || !Files.isRegularFile(policyFile)) {
            throw new IllegalArgumentException("Policy file does not exist: " + policyFile);
        }
    }

    /**
     * Ensures the provided project directory exists.
     *
     * @param projectDirectory directory to validate
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void validateProjectDirectory(Path projectDirectory) {
        Objects.requireNonNull(projectDirectory, "projectDirectory must not be null");
        if (!Files.exists(projectDirectory) || !Files.isDirectory(projectDirectory)) {
            throw new IllegalArgumentException("Project directory does not exist: " + projectDirectory);
        }
    }

    /**
     * Creates the output directory if necessary and returns its absolute path.
     *
     * @param outputDirectory directory to create
     * @return existing or newly created directory
     * @throws IOException when the directory cannot be created
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private Path createOutputDirectory(Path outputDirectory) throws IOException {
        Objects.requireNonNull(outputDirectory, "outputDirectory must not be null");
        Files.createDirectories(outputDirectory);
        return outputDirectory.toAbsolutePath();
    }
}
