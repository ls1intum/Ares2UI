package de.tum.cit.ase.aresUI.policy;

import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogModel;
import de.tum.cit.ase.aresUI.policy.yaml.PolicyYamlParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ensures {@link PolicyYamlParser} can read older/partial policies and still produce a usable model
 * by filling missing parts with safe defaults.
 */
class PolicyYamlParserLegacyTest {

    @Test
    void parseString_toleratesMissingPermittedSectionAndFillsDefaults() {
        String legacy = """
                regardingTheSupervisedCode:
                  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_WALA_AND_ASPECTJ
                  theSupervisedCodeUsesTheFollowingPackage: \"de.example\"
                  theMainClassInsideThisPackageIs: \"Main\"
                  theFollowingClassesAreTestClasses:
                    - \"de.example.ExampleTest\"
                """;

        PolicyDialogModel model = PolicyYamlParser.parseString(legacy);

        assertThat(model.getProgrammingLanguageConfiguration()).isEqualTo("JAVA_USING_MAVEN_WALA_AND_ASPECTJ");
        assertThat(model.getRootPackage()).isEqualTo("de.example");
        assertThat(model.getMainClass()).isEqualTo("Main");
        assertThat(model.getTestClasses()).containsExactly("de.example.ExampleTest");

        // missing resource rules => empty lists
        assertThat(model.getFileSystemRules()).isEmpty();
        assertThat(model.getNetworkConnectionRules()).isEmpty();
        assertThat(model.getCommandExecutionRules()).isEmpty();
        assertThat(model.getThreadCreationRules()).isEmpty();
        assertThat(model.getPackageImportRules()).isEmpty();
        assertThat(model.getTimeoutRules()).isEmpty();
    }

    @Test
    void parseString_toleratesMissingTestClassesAndUsesPlaceholder() {
        String legacy = """
                regardingTheSupervisedCode:
                  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_WALA_AND_ASPECTJ
                  theSupervisedCodeUsesTheFollowingPackage: \"de.example\"
                  theMainClassInsideThisPackageIs: \"Main\"
                  theFollowingResourceAccessesArePermitted: { }
                """;

        PolicyDialogModel model = PolicyYamlParser.parseString(legacy);
        assertThat(model.getTestClasses()).isNotEmpty();
    }
}
