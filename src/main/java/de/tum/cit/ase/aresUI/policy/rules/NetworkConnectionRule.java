package de.tum.cit.ase.aresUI.policy.rules;

/**
 * Represents a network connection rule for a specific host and port.
 */
public class NetworkConnectionRule {

    private final String host;
    private final int port;
    private final boolean openConnections;
    private final boolean sendData;
    private final boolean receiveData;

    /**
     * Creates a network connection rule.
     *
     * @param host destination host
     * @param port destination port (1..65535)
     * @param openConnections whether opening connections is permitted
     * @param sendData whether sending data is permitted
     * @param receiveData whether receiving data is permitted
     * @throws IllegalArgumentException if {@code host} is {@code null} or blank, or if {@code port} is out of range
     */
    public NetworkConnectionRule(String host,
                                 int port,
                                 boolean openConnections,
                                 boolean sendData,
                                 boolean receiveData) {
        this.host = requireNonBlank(host);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        this.port = port;
        this.openConnections = openConnections;
        this.sendData = sendData;
        this.receiveData = receiveData;
    }

    /**
     * Ensures the given string is non-blank and returns its trimmed value.
     *
     * @param s input string
     * @return trimmed string
     * @throws IllegalArgumentException if the string is {@code null} or blank
     */
    private static String requireNonBlank(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        return s.trim();
    }

    /**
     * Returns the destination host.
     *
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the destination port.
     *
     * @return port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Indicates whether opening connections is permitted.
     *
     * @return {@code true} if permitted
     */
    public boolean isOpenConnections() {
        return openConnections;
    }

    /**
     * Indicates whether sending data is permitted.
     *
     * @return {@code true} if permitted
     */
    public boolean isSendData() {
        return sendData;
    }

    /**
     * Indicates whether receiving data is permitted.
     *
     * @return {@code true} if permitted
     */
    public boolean isReceiveData() {
        return receiveData;
    }
}