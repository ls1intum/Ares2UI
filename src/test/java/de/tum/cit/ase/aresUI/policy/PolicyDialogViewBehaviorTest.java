package de.tum.cit.ase.aresUI.policy;

import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogModel;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogViewImpl;
import de.tum.cit.ase.aresUI.testing.FxTestSupport;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

/**
 * Minimal JavaFX lifecycle smoke test similar to {@code ViewModelTest#startInitializesScene}.
 */
class PolicyDialogViewBehaviorTest {

    @Test
    void dialogConstructsAndCollectLoadAreCallableOnFxThread() {
        FxTestSupport.ensureToolkit();
        org.junit.jupiter.api.Assumptions.assumeTrue(FxTestSupport.isToolkitAvailable(), "JavaFX toolkit unavailable");

        FxTestSupport.runOnFx(() -> {
            PolicyDialogViewImpl view = new PolicyDialogViewImpl(new Stage());

            PolicyDialogModel model = new PolicyDialogModel(
                    "JAVA_USING_MAVEN_WALA_AND_ASPECTJ",
                    "de.example",
                    "Main",
                    java.util.List.of("de.example.ExampleTest"),
                    java.util.List.of(),
                    java.util.List.of(),
                    java.util.List.of(),
                    java.util.List.of(),
                    java.util.List.of(),
                    java.util.List.of()
            );

            view.loadFromModel(model);
            view.collectToModel();

            // We don't call show() here to avoid blocking.
            view.close();
        });
    }
}
