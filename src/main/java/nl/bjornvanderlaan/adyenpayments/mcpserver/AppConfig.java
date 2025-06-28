package nl.bjornvanderlaan.adyenpayments.mcpserver;

import com.adyen.enums.Environment;

import java.util.Arrays;

/**
 * Don't copy this pattern. I just try to make it easy to run this server even without environment variables.
 */
public class AppConfig {
    private static final String API_KEY_ENV_VAR = "ADYEN_API_KEY";
    private static final String API_ENVIRONMENT_ENV_VAR = "ADYEN_ENVIRONMENT";
    private final String apiKey;
    private final String apiEnvironment;

    public AppConfig() {
        this.apiKey = loadApiKey();
        this.apiEnvironment = loadApiEnvironment();
    }
    public static AppConfig get() {
        return new AppConfig();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiEnvironment() {
        return apiEnvironment;
    }

    private String loadApiKey() {
        String apiKey = System.getenv(API_KEY_ENV_VAR);
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Warning: API key not found in environment variables.");
            return "NOT A REAL KEY";
        }
        return apiKey;
    }

    private String loadApiEnvironment() {
        String apiEnvironment = System.getenv(API_ENVIRONMENT_ENV_VAR);
        if (apiEnvironment == null || apiEnvironment.isEmpty() || !isValidApiEnvironment(apiEnvironment)) {
            System.err.println("API environment not found in environment variables. Using TEST environment.");
            return Environment.TEST.name();
        }

        return apiEnvironment;
    }

    private boolean isValidApiEnvironment(String apiEnvironment) {
        return Arrays.stream(Environment.values())
                .anyMatch(env -> env.name().equals(apiEnvironment));
    }
}
