package de.tum.cit.ase.aresUI.policy.yaml;

import de.tum.cit.ase.aresUI.policy.rules.CommandExecutionRule;
import de.tum.cit.ase.aresUI.policy.rules.FileSystemRule;
import de.tum.cit.ase.aresUI.policy.rules.NetworkConnectionRule;
import de.tum.cit.ase.aresUI.policy.rules.PackageImportRule;
import de.tum.cit.ase.aresUI.policy.rules.ThreadCreationRule;
import de.tum.cit.ase.aresUI.policy.rules.TimeoutRule;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Lightweight parser for Ares security-policy YAML files.
 *
 * <p>This intentionally does not introduce a YAML dependency (like SnakeYAML). It supports the subset of YAML
 * produced by {@link PolicyYamlCreator} and the example policy format.
 */
public final class PolicyYamlParser {

    private PolicyYamlParser() {
    }

    /**
     * Parses a security policy YAML file into a {@link PolicyDialogModel}.
     *
     * @param yamlFile path to the YAML file
     * @return parsed policy model
     * @throws NullPointerException if {@code yamlFile} is {@code null}
     * @throws IOException if reading the file fails
     * @throws IllegalArgumentException if the file content is empty or does not match the expected schema
     */
    public static PolicyDialogModel parse(Path yamlFile) throws IOException {
        Objects.requireNonNull(yamlFile, "yamlFile");
        String content = Files.readString(yamlFile, StandardCharsets.UTF_8);
        return parseString(content);
    }

    /**
     * Parses a YAML string into a {@link PolicyDialogModel}.
     *
     * <p>This parser is designed for the subset of YAML produced by {@link PolicyYamlCreator}. Missing
     * sections are tolerated and replaced with safe defaults so older policies can still be opened.
     *
     * @param yaml YAML content
     * @return parsed policy model
     * @throws IllegalArgumentException if the input is {@code null}, blank, or not a supported YAML structure
     */
    public static PolicyDialogModel parseString(String yaml) {
        if (yaml == null || yaml.trim().isEmpty()) {
            throw new IllegalArgumentException("Selected file is empty.");
        }

        Object root = new MiniYaml().parse(yaml);
        if (!(root instanceof Map<?, ?> rootMap)) {
            throw new IllegalArgumentException("Invalid YAML structure (expected a mapping at root).");
        }

        Object rtsc = rootMap.get("regardingTheSupervisedCode");
        if (!(rtsc instanceof Map<?, ?> supervised)) {
            throw new IllegalArgumentException("Not a security policy: missing 'regardingTheSupervisedCode'.");
        }

        // Backwards compatibility: older policies might omit some fields.
        // We try best-effort parsing and fill missing data with safe defaults so the UI can still open.
        String config = getString(supervised, "theFollowingProgrammingLanguageConfigurationIsUsed", "JAVA_USING_MAVEN_WALA_AND_ASPECTJ");
        String rootPackage = getString(supervised, "theSupervisedCodeUsesTheFollowingPackage", "org.example");
        String mainClass = getString(supervised, "theMainClassInsideThisPackageIs", "Main");

        List<String> testClasses = asStringList(supervised.get("theFollowingClassesAreTestClasses"));
        if (testClasses.isEmpty()) {
            testClasses = List.of("org.example.ExampleTest");
        }

        Object permitted = supervised.get("theFollowingResourceAccessesArePermitted");
        Map<?, ?> permittedMap = permitted instanceof Map<?, ?> pm ? pm : Map.of();

        List<FileSystemRule> fsRules = parseFsRules(permittedMap.get("regardingFileSystemInteractions"));
        List<NetworkConnectionRule> networkRules = parseNetworkRules(permittedMap.get("regardingNetworkConnections"));
        List<CommandExecutionRule> commandRules = parseCommandRules(permittedMap.get("regardingCommandExecutions"));
        List<ThreadCreationRule> threadRules = parseThreadRules(permittedMap.get("regardingThreadCreations"));
        List<PackageImportRule> packageRules = parsePackageRules(permittedMap.get("regardingPackageImports"));
        List<TimeoutRule> timeoutRules = parseTimeoutRules(permittedMap.get("regardingTimeouts"));

        return new PolicyDialogModel(
                config,
                rootPackage,
                mainClass,
                testClasses,
                fsRules,
                networkRules,
                commandRules,
                threadRules,
                packageRules,
                timeoutRules
        );
    }

    /**
     * Parses the file system rules section.
     *
     * @param node YAML node representing the regardingFileSystemInteractions section
     * @return list of parsed rules
     * @throws IllegalArgumentException if a required field is missing
     */
    private static List<FileSystemRule> parseFsRules(Object node) {
        List<Map<String, Object>> items = asListOfMaps(node);
        if (items.isEmpty()) return List.of();

        List<FileSystemRule> rules = new ArrayList<>();
        for (Map<String, Object> item : items) {
            String path = requireString(item, "onThisPathAndAllPathsBelow");
            boolean read = getBool(item, "readAllFiles");
            boolean overwrite = getBool(item, "overwriteAllFiles");
            boolean create = getBool(item, "createAllFiles");
            boolean execute = getBool(item, "executeAllFiles");
            boolean delete = getBool(item, "deleteAllFiles");
            rules.add(new FileSystemRule(path, read, overwrite, create, execute, delete));
        }
        return rules;
    }

    /**
     * Parses the network connection rules section.
     *
     * @param node YAML node representing the regardingNetworkConnections section
     * @return list of parsed rules
     * @throws IllegalArgumentException if a required field is missing or a rule contains invalid data
     */
    private static List<NetworkConnectionRule> parseNetworkRules(Object node) {
        List<Map<String, Object>> items = asListOfMaps(node);
        if (items.isEmpty()) return List.of();

        List<NetworkConnectionRule> rules = new ArrayList<>();
        for (Map<String, Object> item : items) {
            String host = requireString(item, "onTheHost");
            int port = getInt(item, "onThePort", 0);
            boolean open = getBool(item, "openConnections");
            boolean send = getBool(item, "sendData");
            boolean receive = getBool(item, "receiveData");
            rules.add(new NetworkConnectionRule(host, port, open, send, receive));
        }
        return rules;
    }

    /**
     * Parses the command execution rules section.
     *
     * @param node YAML node representing the regardingCommandExecutions section
     * @return list of parsed rules
     * @throws IllegalArgumentException if a required field is missing
     */
    private static List<CommandExecutionRule> parseCommandRules(Object node) {
        List<Map<String, Object>> items = asListOfMaps(node);
        if (items.isEmpty()) return List.of();

        List<CommandExecutionRule> rules = new ArrayList<>();
        for (Map<String, Object> item : items) {
            String cmd = requireString(item, "executeTheCommand");
            List<String> args = asStringList(item.get("withTheseArguments"));
            rules.add(new CommandExecutionRule(cmd, args));
        }
        return rules;
    }

    /**
     * Parses the thread creation rules section.
     *
     * @param node YAML node representing the regardingThreadCreations section
     * @return list of parsed rules
     * @throws IllegalArgumentException if a required field is missing
     */
    private static List<ThreadCreationRule> parseThreadRules(Object node) {
        List<Map<String, Object>> items = asListOfMaps(node);
        if (items.isEmpty()) return List.of();

        List<ThreadCreationRule> rules = new ArrayList<>();
        for (Map<String, Object> item : items) {
            int n = getInt(item, "createTheFollowingNumberOfThreads", 0);
            String clazz = requireString(item, "ofThisClass");
            rules.add(new ThreadCreationRule(n, clazz));
        }
        return rules;
    }

    /**
     * Parses the package import rules section.
     *
     * @param node YAML node representing the regardingPackageImports section
     * @return list of parsed rules
     * @throws IllegalArgumentException if a required field is missing
     */
    private static List<PackageImportRule> parsePackageRules(Object node) {
        List<Map<String, Object>> items = asListOfMaps(node);
        if (items.isEmpty()) return List.of();

        List<PackageImportRule> rules = new ArrayList<>();
        for (Map<String, Object> item : items) {
            String pkg = requireString(item, "importTheFollowingPackage");
            rules.add(new PackageImportRule(pkg));
        }
        return rules;
    }

    /**
     * Parses the timeout section.
     *
     * <p>Per spec, this section contains either an empty list or a single mapping entry.
     *
     * @param node YAML node representing the regardingTimeouts section
     * @return a list containing at most one {@link TimeoutRule}
     */
    private static List<TimeoutRule> parseTimeoutRules(Object node) {
        // Spec: either [ ] or a single item "- timeout: <n>".
        // Backwards compatibility: tolerate invalid/missing values -> treat as "no timeout".
        List<Map<String, Object>> items = asListOfMaps(node);
        if (items.isEmpty()) return List.of();

        Map<String, Object> first = items.getFirst();
        int seconds = getInt(first, "timeout", 0);
        if (seconds <= 0) {
            return List.of();
        }
        return List.of(new TimeoutRule(seconds));
    }

    /**
     * Reads a required non-empty string field.
     *
     * @param map map node
     * @param key field name
     * @return string value
     * @throws IllegalArgumentException if the field is missing or blank
     */
    private static String requireString(Map<?, ?> map, String key) {
        Object v = map.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Missing required field: '" + key + "'.");
        }
        if (!(v instanceof String s) || s.trim().isEmpty()) {
            throw new IllegalArgumentException("Field '" + key + "' must be a non-empty string.");
        }
        return s;
    }

    /**
     * Reads a required list of strings.
     *
     * @param map map node
     * @param key field name
     * @return list of strings
     * @throws IllegalArgumentException if the list is missing or empty
     */
    private static List<String> requireStringList(Map<?, ?> map, String key) {
        List<String> l = asStringList(map.get(key));
        if (l.isEmpty()) {
            throw new IllegalArgumentException("Missing required list: '" + key + "'.");
        }
        return l;
    }

    /**
     * Coerces the given YAML node to a list of non-empty strings.
     *
     * @param node YAML node
     * @return list of strings, or an empty list if the node is absent or unsupported
     */
    private static List<String> asStringList(Object node) {
        if (node == null) return List.of();
        if (node instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof String s && !s.trim().isEmpty()) {
                    out.add(s);
                }
            }
            return out;
        }
        if (node instanceof String s) {
            String t = s.trim();
            return t.isEmpty() ? List.of() : List.of(t);
        }
        if (node instanceof Map<?, ?>) {
            // Not expected for our schema.
            return List.of();
        }
        return List.of();
    }

    /**
     * Reads a boolean value from a map.
     *
     * @param map source map
     * @param key field name
     * @return parsed boolean, or {@code false} when missing/unrecognized
     */
    private static boolean getBool(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s.trim());
        return false;
    }

    /**
     * Reads an integer value from a map.
     *
     * @param map source map
     * @param key field name
     * @param defaultValue value to return when the field is missing or unparsable
     * @return parsed integer or {@code defaultValue}
     */
    private static int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Reads a string value from a map and falls back to the given default.
     *
     * @param map source map
     * @param key field name
     * @param defaultValue fallback value
     * @return trimmed string or {@code defaultValue} when missing/blank
     */
    private static String getString(Map<?, ?> map, String key, String defaultValue) {
        Object v = map.get(key);
        if (v instanceof String s) {
            String t = s.trim();
            return t.isEmpty() ? defaultValue : t;
        }
        return defaultValue;
    }

    /**
     * Coerces a YAML node into a list of maps.
     *
     * <p>If the node is already a list, only map entries are retained. If the node is a single map, it is
     * wrapped into a singleton list.
     *
     * @param node YAML node
     * @return list of maps
     */
    private static List<Map<String, Object>> asListOfMaps(Object node) {
        if (node == null) return List.of();

        // Often empty list is represented as [] which MiniYaml parses as empty list.
        if (node instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    out.add((Map<String, Object>) m);
                }
            }
            return out;
        }

        // Sometimes people write a single item without a list; tolerate that by wrapping.
        if (node instanceof Map<?, ?> m) {
            return List.of((Map<String, Object>) m);
        }

        return List.of();
    }

    /**
        Minimal YAML subset parser.

        Supported:
        - indentation-based maps
        - lists using "-"
        - inline empty list "[ ]" or "[]"
        - strings (quoted or plain)
        - booleans, integers
     */
    private static final class MiniYaml {

        /**
         * Parses YAML content into a Java object tree consisting of maps, lists, and scalar values.
         *
         * @param yaml YAML content
         * @return parsed root node
         * @throws IllegalArgumentException if the input contains unsupported or invalid indentation
         */
        Object parse(String yaml) {
            List<String> lines = yaml.lines().toList();
            // strip BOM
            if (!lines.isEmpty() && lines.getFirst().startsWith("\uFEFF")) {
                lines = new ArrayList<>(lines);
                lines.set(0, lines.getFirst().substring(1));
            }

            Index idx = new Index();
            while (idx.pos < lines.size() && isIgnorable(lines.get(idx.pos))) idx.pos++;
            if (idx.pos >= lines.size()) return Map.of();

            int indent = indentOf(lines.get(idx.pos));
            if (trimmed(lines.get(idx.pos)).startsWith("-")) {
                return parseList(lines, idx, indent);
            }
            return parseMap(lines, idx, indent);
        }

        /**
         * Parses a mapping block at the given indentation level.
         *
         * @param lines input lines
         * @param idx mutable parse index
         * @param indent expected indentation
         * @return parsed map
         */
        private Map<String, Object> parseMap(List<String> lines, Index idx, int indent) {
            Map<String, Object> map = new LinkedHashMap<>();

            while (idx.pos < lines.size()) {
                String raw = lines.get(idx.pos);
                if (isIgnorable(raw)) {
                    idx.pos++;
                    continue;
                }

                int ind = indentOf(raw);
                if (ind < indent) {
                    break;
                }
                if (ind > indent) {
                    throw new IllegalArgumentException("Invalid indentation near: " + raw);
                }

                String t = trimmed(raw);
                if (t.startsWith("-")) {
                    // list where map expected
                    break;
                }

                int colon = t.indexOf(':');
                if (colon < 0) {
                    throw new IllegalArgumentException("Invalid mapping entry: " + t);
                }

                String key = t.substring(0, colon).trim();
                String rest = t.substring(colon + 1).trim();

                idx.pos++;

                Object value;
                if (rest.isEmpty()) {
                    // nested block
                    int childIndent = nextNonEmptyIndent(lines, idx);
                    if (childIndent <= ind) {
                        value = "";
                    } else {
                        String nextTrim = trimmed(lines.get(idx.pos));
                        if (nextTrim.startsWith("-")) {
                            value = parseList(lines, idx, childIndent);
                        } else {
                            value = parseMap(lines, idx, childIndent);
                        }
                    }
                } else {
                    value = parseScalar(rest);
                }

                map.put(key, value);
            }

            return map;
        }

        /**
         * Parses a list block at the given indentation level.
         *
         * @param lines input lines
         * @param idx mutable parse index
         * @param indent expected indentation
         * @return parsed list
         */
        private List<Object> parseList(List<String> lines, Index idx, int indent) {
            List<Object> list = new ArrayList<>();

            while (idx.pos < lines.size()) {
                String raw = lines.get(idx.pos);
                if (isIgnorable(raw)) {
                    idx.pos++;
                    continue;
                }

                int ind = indentOf(raw);
                if (ind < indent) {
                    break;
                }
                if (ind > indent) {
                    throw new IllegalArgumentException("Invalid indentation near: " + raw);
                }

                String t = trimmed(raw);
                if (!t.startsWith("-")) {
                    break;
                }

                String rest = t.substring(1).trim();
                idx.pos++;

                if (rest.isEmpty()) {
                    int childIndent = nextNonEmptyIndent(lines, idx);
                    if (childIndent <= ind) {
                        list.add("");
                    } else {
                        String nextTrim = trimmed(lines.get(idx.pos));
                        if (nextTrim.startsWith("-")) {
                            list.add(parseList(lines, idx, childIndent));
                        } else {
                            list.add(parseMap(lines, idx, childIndent));
                        }
                    }
                } else {
                    // list item can be "key: value" map inline
                    if (rest.contains(":")) {
                        // parse as single-entry map, then possibly consume nested map entries
                        int colon = rest.indexOf(':');
                        String key = rest.substring(0, colon).trim();
                        String after = rest.substring(colon + 1).trim();
                        Map<String, Object> m = new LinkedHashMap<>();
                        if (!after.isEmpty()) {
                            m.put(key, parseScalar(after));
                        } else {
                            m.put(key, "");
                        }

                        // If next line is more-indented, parse as map continuation
                        int childIndent = nextNonEmptyIndent(lines, idx);
                        if (childIndent > ind) {
                            Map<String, Object> tail = parseMap(lines, idx, childIndent);
                            m.putAll(tail);
                        }
                        list.add(m);
                    } else {
                        list.add(parseScalar(rest));
                    }
                }
            }

            return list;
        }

        /**
         * Parses a scalar value from its textual representation.
         *
         * @param raw raw scalar text
         * @return parsed value (boolean, integer, empty list, or string)
         */
        private static Object parseScalar(String raw) {
            String t = raw.trim();
            if (t.equals("[]") || t.equals("[ ]")) return Collections.emptyList();
            if (t.equalsIgnoreCase("true")) return Boolean.TRUE;
            if (t.equalsIgnoreCase("false")) return Boolean.FALSE;

            // quoted strings
            if ((t.startsWith("\"") && t.endsWith("\"")) || (t.startsWith("'") && t.endsWith("'"))) {
                return unquote(t);
            }

            // int
            try {
                return Integer.parseInt(t);
            } catch (NumberFormatException ignored) {
                // fall through
            }

            return t;
        }

        /**
         * Removes surrounding quotes and unescapes supported sequences.
         *
         * @param t quoted string
         * @return unescaped content
         */
        private static String unquote(String t) {
            if (t.length() < 2) return t;
            String inside = t.substring(1, t.length() - 1);
            return inside.replace("\\\"", "\"").replace("\\\\", "\\");
        }

        /**
         * Advances {@code idx} to the next non-ignorable line and returns its indentation.
         *
         * @param lines input lines
         * @param idx mutable parse index
         * @return indentation of the next non-empty line, or {@code -1} when none exists
         */
        private static int nextNonEmptyIndent(List<String> lines, Index idx) {
            int p = idx.pos;
            while (p < lines.size() && isIgnorable(lines.get(p))) p++;
            if (p >= lines.size()) return -1;
            idx.pos = p;
            return indentOf(lines.get(p));
        }

        /**
         * Checks whether a line is ignorable (empty or a comment).
         *
         * @param line input line
         * @return {@code true} if the line is blank or starts with '#'
         */
        private static boolean isIgnorable(String line) {
            if (line == null) return true;
            String t = trimmed(line);
            return t.isEmpty() || t.startsWith("#");
        }

        /**
         * Computes the leading-space indentation of the given line.
         *
         * @param line input line
         * @return indentation level in spaces
         */
        private static int indentOf(String line) {
            int i = 0;
            while (i < line.length() && line.charAt(i) == ' ') i++;
            return i;
        }

        /**
         * Returns the trimmed representation of the given line.
         *
         * @param line input line
         * @return trimmed string, or an empty string for {@code null}
         */
        private static String trimmed(String line) {
            return line == null ? "" : line.trim();
        }

        /**
         * Mutable parse position.
         */
        private static final class Index {
            int pos;
        }
    }
}
