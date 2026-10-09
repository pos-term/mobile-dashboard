package com.posterm.mobiledashboard.network;

public final class ApiConfiguration {
    private final String baseUrl;
    private final String bearerToken;
    private final boolean networkLoggingEnabled;

    public ApiConfiguration(String baseUrl, String bearerToken) {
        this(baseUrl, bearerToken, false);
    }

    public ApiConfiguration(
            String baseUrl,
            String bearerToken,
            boolean networkLoggingEnabled
    ) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.bearerToken = requireValue(bearerToken, "bearerToken");
        this.networkLoggingEnabled = networkLoggingEnabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public boolean isNetworkLoggingEnabled() {
        return networkLoggingEnabled;
    }

    private static String normalizeBaseUrl(String baseUrl) {
        String normalized = requireValue(baseUrl, "baseUrl");
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }

    private static String requireValue(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
