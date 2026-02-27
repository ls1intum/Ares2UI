package de.tum.cit.ase.aresUI.policy;

import de.tum.cit.ase.aresUI.policy.rules.*;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogModel;
import de.tum.cit.ase.aresUI.policy.yaml.PolicyYamlCreator;
import de.tum.cit.ase.aresUI.policy.yaml.PolicyYamlParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Round trip tests for {@link PolicyYamlCreator} and {@link PolicyYamlParser}.
 */
class PolicyYamlRoundTripTest {

    @TempDir
    Path tmp;

    @Test
    void createThenParseRoundTripsAllSections() throws Exception {
        PolicyDialogModel model = new PolicyDialogModel(
                "JAVA_USING_MAVEN_WALA_AND_ASPECTJ",
                "de.example",
                "Main",
                List.of("de.example.ExampleTest", "de.example.OtherTest"),
                List.of(new FileSystemRule("/tmp", true, false, true, true, false)),
                List.of(new NetworkConnectionRule("example.com", 443, true, true, false)),
                List.of(new CommandExecutionRule("echo", List.of("a", "b"))),
                List.of(new ThreadCreationRule(2, "java.lang.Thread")),
                List.of(new PackageImportRule("java.time")),
                List.of(new TimeoutRule(7))
        );

        Path yamlFile = tmp.resolve("security-policy.yaml");
        Path written = new PolicyYamlCreator().createPolicyYamlAt(yamlFile, model);

        PolicyDialogModel parsed = PolicyYamlParser.parse(written);

        assertThat(parsed.getProgrammingLanguageConfiguration()).isEqualTo(model.getProgrammingLanguageConfiguration());
        assertThat(parsed.getRootPackage()).isEqualTo(model.getRootPackage());
        assertThat(parsed.getMainClass()).isEqualTo(model.getMainClass());
        assertThat(parsed.getTestClasses()).containsExactlyElementsOf(model.getTestClasses());

        assertThat(parsed.getFileSystemRules()).hasSize(1);
        assertThat(parsed.getNetworkConnectionRules()).hasSize(1);
        assertThat(parsed.getCommandExecutionRules()).hasSize(1);
        assertThat(parsed.getThreadCreationRules()).hasSize(1);
        assertThat(parsed.getPackageImportRules()).hasSize(1);
        assertThat(parsed.getTimeoutRules()).hasSize(1);

        assertThat(parsed.getTimeoutRules().getFirst().getTimeoutSeconds()).isEqualTo(7);
    }

    @Test
    void parseStringRejectsEmpty() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> PolicyYamlParser.parseString("  "));
    }
}
