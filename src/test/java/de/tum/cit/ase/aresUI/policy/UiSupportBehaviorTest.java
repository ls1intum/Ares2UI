package de.tum.cit.ase.aresUI.policy;

import de.tum.cit.ase.aresUI.testing.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UiSupportBehaviorTest {

    @Test
    void splitArgsHandlesNullEmptyAndQuotes() {
        assertThat(UiSupport.splitArgs(null)).isEmpty();
        assertThat(UiSupport.splitArgs("  ")).isEmpty();

        assertThat(UiSupport.splitArgs("a b c")).containsExactly("a", "b", "c");
        assertThat(UiSupport.splitArgs("a \"b c\" d")).containsExactly("a", "b c", "d");
    }

    @Test
    void parseIntOrZeroHandlesInvalid() {
        assertThat(UiSupport.parseIntOrZero(null)).isZero();
        assertThat(UiSupport.parseIntOrZero(" ")).isZero();
        assertThat(UiSupport.parseIntOrZero("boom")).isZero();
        assertThat(UiSupport.parseIntOrZero(" 42 ")).isEqualTo(42);
    }

    @Test
    void javaFxFactoriesCreateNodes() {
        FxTestSupport.ensureToolkit();
        org.junit.jupiter.api.Assumptions.assumeTrue(FxTestSupport.isToolkitAvailable(), "JavaFX toolkit unavailable");

        FxTestSupport.runOnFx(() -> {
            Button b = UiSupport.createRemoveButton();
            Label l = UiSupport.boldLabel("X");

            assertThat(b.getText()).isEqualTo("");
            assertThat(l.getStyle()).contains("bold");
            assertThat(UiSupport.headerBottomPadding().getBottom()).isEqualTo(4.0);
        });
    }
}
