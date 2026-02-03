package de.tum.cit.ase.aresUI.policy.rules;

/**
 * PackageImportRule defines a rule for importing packages.
 */
public class PackageImportRule {

    private final String packageName;

    /**
     * Creates a package import rule.
     *
     * @param packageName package name to permit
     * @throws IllegalArgumentException if {@code packageName} is {@code null} or blank
     */
    public PackageImportRule(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            throw new IllegalArgumentException("packageName must not be blank");
        }
        this.packageName = packageName.trim();
    }

    /**
     * Returns the package name.
     *
     * @return package name
     */
    public String getPackageName() {
        return packageName;
    }
}
