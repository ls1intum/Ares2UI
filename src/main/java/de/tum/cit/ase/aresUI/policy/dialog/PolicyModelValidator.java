package de.tum.cit.ase.aresUI.policy.dialog;

import de.tum.cit.ase.aresUI.policy.rules.TimeoutRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validates a constructed {@link PolicyDialogModel}, producing user-friendly error messages
 * for constraints that are not enforced by the model constructor itself (e.g. at-most-one timeout).
 *
 * <p>This validator is called by {@link PolicyDialogViewModel} <em>after</em> the model has been
 * constructed via {@link PolicyDialogViewContract#collectToModel()}. Domain-level invariant checks
 * (e.g. non-negative thread count, non-blank host) remain in the rule constructors as a safety net.
 */
public final class PolicyModelValidator {

    private PolicyModelValidator() {
    }

    /**
     * Validates the given model and returns a list of user-facing error messages.
     *
     * @param model the constructed policy model
     * @return list of error messages; empty if the model is valid
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public static List<String> validate(PolicyDialogModel model) {
        Objects.requireNonNull(model, "model");
        List<String> errors = new ArrayList<>();

        validateTimeoutRules(model.getTimeoutRules(), errors);

        return errors;
    }

    private static void validateTimeoutRules(List<TimeoutRule> rules, List<String> errors) {
        if (rules == null || rules.isEmpty()) return;
        if (rules.size() > 1) {
            errors.add("Please provide only one timeout entry (or remove all of them).");
        }
    }
}
