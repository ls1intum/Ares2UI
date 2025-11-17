package de.tum.cit.ase.aresUI.generation;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Production factory that creates {@link SecurityPolicyReaderAndDirector} instances.
 *
 * <p>Description: Bridges the UI layer to the real Ares implementation by producing concrete generators.
 *
 * <p>Design Rationale: Keeps creation logic encapsulated, enabling tests to supply lightweight factories.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public class DefaultSecurityPolicyGeneratorFactory implements SecurityPolicyGeneratorFactory {

    /**
     * Function that instantiates the delegate generator.
     */
    private final BiFunction<Path, Path, SecurityPolicyReaderAndDirector> delegateFactory;

    /**
     * Creates the default factory delegating to {@link SecurityPolicyReaderAndDirector}.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public DefaultSecurityPolicyGeneratorFactory() {
        this(SecurityPolicyReaderAndDirector::new);
    }

    /**
     * Visible-for-testing constructor that accepts a custom delegate factory.
     *
     * @param delegateFactory function that instantiates the delegate
     * @since 0.0.1
     * @author Markus Paulsen
     */
    DefaultSecurityPolicyGeneratorFactory(BiFunction<Path, Path, SecurityPolicyReaderAndDirector> delegateFactory) {
        this.delegateFactory = Objects.requireNonNull(delegateFactory, "delegateFactory");
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public SecurityPolicyExecutor create(Path policyFile, Path projectDirectory) {
        SecurityPolicyReaderAndDirector delegate = delegateFactory.apply(policyFile, projectDirectory);
        return new SecurityPolicyExecutor() {
            @Override
            public SecurityPolicyExecutor createTestCases() {
                delegate.createTestCases();
                return this;
            }

            @Override
            public void writeTestCases(Path outputDirectory) {
                delegate.writeTestCases(outputDirectory);
            }
        };
    }
}
