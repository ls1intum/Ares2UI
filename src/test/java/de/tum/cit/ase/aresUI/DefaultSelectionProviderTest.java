package de.tum.cit.ase.aresUI;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import de.tum.cit.ase.aresUI.testing.FxTestSupport;
import org.junit.jupiter.api.Assumptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link DefaultSelectionProvider} chooser behavior.
 *
 * <p>Description: Validates that the provider returns Optional selections as expected.
 *
 * <p>Design Rationale: Ensures the injectable chooser strategy works in tests.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
class DefaultSelectionProviderTest {

    /**
     * Verifies directories are returned when provided.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void returnsSelectedDirectory() {
        File chosen = new File("project");
        DefaultSelectionProvider provider = new DefaultSelectionProvider(owner -> chosen, owner -> null);

        Optional<File> result = provider.selectProjectDirectory(null);

        assertThat(result).isPresent().contains(chosen);
    }

    /**
     * Verifies policy files are returned when provided.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void returnsSelectedPolicyFile() {
        File chosen = new File("Security.yaml");
        DefaultSelectionProvider provider = new DefaultSelectionProvider(owner -> null, owner -> chosen);

        Optional<File> result = provider.selectPolicyFile(null);

        assertThat(result).isPresent().contains(chosen);
    }

    /**
     * Ensures empty optionals are produced when selection is cancelled.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void returnsEmptyWhenCancelled() {
        DefaultSelectionProvider provider = new DefaultSelectionProvider(owner -> null, owner -> null);

        Optional<File> dir = provider.selectProjectDirectory(null);
        Optional<File> file = provider.selectPolicyFile(null);

        assertTrue(dir.isEmpty());
        assertTrue(file.isEmpty());
    }

    /**
     * Ensures default constructor executes without errors.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void defaultConstructorInitializesChoosers() {
        FxTestSupport.ensureToolkit();
        Assumptions.assumeTrue(FxTestSupport.isToolkitAvailable(), "JavaFX toolkit unavailable");
        DefaultSelectionProvider provider = FxTestSupport.callOnFx(DefaultSelectionProvider::new);
        assertThat(provider).isNotNull();
    }
}
