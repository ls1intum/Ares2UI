package de.tum.cit.ase.aresUI.policy;

import de.tum.cit.ase.aresUI.policy.rules.*;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link PolicyDialogModel}.
 */
class PolicyDialogModelBehaviorTest {

    @Test
    void constructorTrimsAndCleansRules() {
        PolicyDialogModel model = new PolicyDialogModel(
                "  JAVA_USING_MAVEN_WALA_AND_ASPECTJ ",
                "  de.example ",
                " Main ",
                List.of("de.example.ExampleTest"),
                List.of(
                        new FileSystemRule("  /tmp ", true, false, false, false)
                ),
                List.of(new NetworkConnectionRule("  example.com ", 443, true, false, true)),
                List.of(new CommandExecutionRule("  echo ", List.of(" a ", "", "b"))),
                List.of(new ThreadCreationRule(1, "  java.lang.Thread  ")),
                List.of(new PackageImportRule("  java.time  ")),
                List.of(new TimeoutRule(5))
        );

        assertThat(model.getProgrammingLanguageConfiguration()).isEqualTo("JAVA_USING_MAVEN_WALA_AND_ASPECTJ");
        assertThat(model.getRootPackage()).isEqualTo("de.example");
        assertThat(model.getMainClass()).isEqualTo("Main");

        assertThat(model.getFileSystemRules()).hasSize(1);
        assertThat(model.getFileSystemRules().getFirst().getPathAndBelow()).isEqualTo("/tmp");

        assertThat(model.getNetworkConnectionRules()).hasSize(1);
        assertThat(model.getNetworkConnectionRules().getFirst().getHost()).isEqualTo("example.com");

        assertThat(model.getCommandExecutionRules()).hasSize(1);
        assertThat(model.getCommandExecutionRules().getFirst().getCommand()).isEqualTo("echo");
        assertThat(model.getCommandExecutionRules().getFirst().getArguments()).containsExactly("a", "b");

        assertThat(model.getThreadCreationRules()).hasSize(1);
        assertThat(model.getThreadCreationRules().getFirst().getThreadClass()).isEqualTo("java.lang.Thread");

        assertThat(model.getPackageImportRules()).hasSize(1);
        assertThat(model.getPackageImportRules().getFirst().getPackageName()).isEqualTo("java.time");

        assertThat(model.getTimeoutRules()).hasSize(1);
        assertThat(model.getTimeoutRules().getFirst().getTimeoutSeconds()).isEqualTo(5);
    }

    @Test
    void constructorRejectsMissingRequiredFields() {
        assertThrows(IllegalArgumentException.class, () -> new PolicyDialogModel(
                " ",
                "de.example",
                "Main",
                List.of("Test"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        ));

        assertThrows(IllegalArgumentException.class, () -> new PolicyDialogModel(
                "JAVA_USING_MAVEN_WALA_AND_ASPECTJ",
                "de.example",
                "Main",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        ));
    }
}
