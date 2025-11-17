package de.tum.cit.ase.aresUI;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Model}.
 *
 * <p>Description: Verifies that the Model correctly stores and clears user selections.
 *
 * <p>Design Rationale: Ensures the simple data holder behaves predictably.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
class ModelTest {

    /**
     * Ensures getters/setters handle null and non-null values.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void storesDirectoryAndPolicy() {
        Model model = new Model();
        assertNull(model.getProjectDirectory());
        assertNull(model.getPolicyFile());

        model.setProjectDirectory("/tmp/project");
        model.setPolicyFile("/tmp/policy.yaml");

        assertEquals("/tmp/project", model.getProjectDirectory());
        assertEquals("/tmp/policy.yaml", model.getPolicyFile());

        model.setProjectDirectory(null);
        model.setPolicyFile(null);

        assertNull(model.getProjectDirectory());
        assertNull(model.getPolicyFile());
    }
}
