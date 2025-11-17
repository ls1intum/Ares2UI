package de.tum.cit.ase.aresUI.generation;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link DefaultSecurityPolicyGeneratorFactory}.
 *
 * <p>Description: Verifies the factory delegates operations to the underlying reader.
 *
 * <p>Design Rationale: Ensures the injection seam behaves as expected.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
class DefaultSecurityPolicyGeneratorFactoryTest {

    /**
     * Confirms create/write calls reach the delegate.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void delegatesToReaderAndDirector() {
        Path policy = Path.of("policy");
        Path project = Path.of("project");
        Path output = Path.of("output");

        SecurityPolicyReaderAndDirector delegate = mock(SecurityPolicyReaderAndDirector.class);
        when(delegate.createTestCases()).thenReturn(delegate);
        DefaultSecurityPolicyGeneratorFactory factory = new DefaultSecurityPolicyGeneratorFactory((p, d) -> delegate);

        SecurityPolicyGeneratorFactory.SecurityPolicyExecutor executor = factory.create(policy, project);
        executor.createTestCases().writeTestCases(output);

        verify(delegate).createTestCases();
        verify(delegate).writeTestCases(output);
    }
}
