package de.tum.cit.ase.aresUI;

import de.tum.cit.ase.aresUI.generation.AresTestGenerator;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Main}.
 *
 * <p>Description: Covers the CLI argument handling and generator invocation flows.
 *
 * <p>Design Rationale: Ensures the non-UI entry point behaves predictably.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
class MainTest {

    /**
     * Ensures missing arguments yield an error exit code.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void returnsErrorCodeWhenArgumentsMissing() throws Exception {
        AresTestGenerator generator = mock(AresTestGenerator.class);

        int exitCode = Main.runCli(new String[0], generator, new PrintStream(new ByteArrayOutputStream()), System.err);

        assertThat(exitCode).isEqualTo(1);
        verify(generator, never()).generateTests(any(), any(), any());
    }

    /**
     * Verifies successful execution prints confirmation.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void runsGeneratorAndPrintsMessage() throws Exception {
        AresTestGenerator generator = mock(AresTestGenerator.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int exitCode = Main.runCli(new String[]{"policy.yaml", "project"}, generator, new PrintStream(out), System.err);

        assertThat(exitCode).isEqualTo(0);
        verify(generator).generateTests(Path.of("policy.yaml").toAbsolutePath().normalize(),
                Path.of("project").toAbsolutePath().normalize(),
                Path.of("project").toAbsolutePath().normalize().resolve("src").resolve("test").resolve("java"));
        String output = out.toString();
        assertThat(output).contains("Tests generated");
    }

    /**
     * Ensures generator failures return exit code 2 with error output.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void returnsErrorCodeWhenGeneratorFails() throws Exception {
        AresTestGenerator generator = mock(AresTestGenerator.class);
        doThrow(new IllegalStateException("boom")).when(generator).generateTests(any(), any(), any());
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        int exitCode = Main.runCli(new String[]{"policy.yaml", "project"}, generator, System.out, new PrintStream(err));

        assertThat(exitCode).isEqualTo(2);
        String errors = err.toString();
        assertThat(errors).contains("Failed to generate tests");
    }

    /**
     * Ensures the main method can run without inputs.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void mainMethodHandlesMissingArgs() {
        Main.main(new String[0]);
    }

    /**
     * Covers the private constructor via reflection.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void privateConstructorAccessibleViaReflection() throws Exception {
        var constructor = Main.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }
}
