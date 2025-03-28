package de.sommerfeld.topspin.plan.messages;

import de.sommerfeld.topspin.logger.LogFacade;
import de.sommerfeld.topspin.logger.LogFacadeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Utility class to load and provide localized messages from a properties file
 * located in the classpath resources.
 * Properties are loaded once when the class is initialized.
 */
public final class Messages {

    private static final LogFacade log;
    private static final Properties PROPERTIES = new Properties();
    private static final String PROPERTIES_RESOURCE_PATH = "/messages.properties";

    static {
        try {
            log = LogFacadeFactory.getLogger(Messages.class);
        } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
            System.err.println("FATAL: Failed to initialize logger for Messages class: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Cannot initialize logger for Messages: " + e.getMessage());
        } catch (Throwable t) {
            System.err.println("FATAL: Unexpected error initializing logger for Messages class: " + t.getMessage());
            t.printStackTrace();
            throw new IllegalStateException("Unexpected error initializing logger for Messages: " + t.getMessage());
        }

        loadPropertiesFromResources();
    }

    private Messages() {
        throw new IllegalStateException("Utility class - cannot be instantiated.");
    }

    /**
     * Loads properties from the resource file defined by PROPERTIES_RESOURCE_PATH.
     * Called only once from the static initializer.
     */
    private static void loadPropertiesFromResources() {
        try (InputStream propertiesStream = Messages.class.getResourceAsStream(PROPERTIES_RESOURCE_PATH)) {

            if (propertiesStream == null) {
                log.error("FATAL ERROR: Resource file not found in classpath: {}", PROPERTIES_RESOURCE_PATH);
                throw new IllegalStateException("Resource file not found: " + PROPERTIES_RESOURCE_PATH);
            } else {
                PROPERTIES.load(propertiesStream);
                log.info("Successfully loaded messages from resource: {}", PROPERTIES_RESOURCE_PATH);
                log.debug("Loaded {} message keys.", PROPERTIES.size());
            }
        } catch (IOException e) {
            log.error("FATAL ERROR: Could not load properties from resource: {}", PROPERTIES_RESOURCE_PATH, e);
            throw new IllegalStateException("Could not load properties from resource: " + PROPERTIES_RESOURCE_PATH, e);
        } catch (Exception e) {
            log.error("FATAL ERROR: Unexpected error loading properties from resource: {}", PROPERTIES_RESOURCE_PATH, e);
            throw new IllegalStateException("Unexpected error loading properties from resource: " + PROPERTIES_RESOURCE_PATH, e);
        }
    }

    /**
     * Retrieves the message string for the given key from the loaded properties.
     *
     * @param key The property key for the message.
     * @return The message string associated with the key. If the key is not found,
     * returns a string like "!key!" to make missing keys visible in the UI.
     * Returns null if the input key is null.
     */
    public static String getString(String key) {
        if (key == null) {
            return null;
        }
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            log.warn("Missing message key requested: {}", key);
            return "!" + key + "!";
        }
        return value;
    }

    /**
     * Retrieves the message string for the given key and formats it with the provided arguments
     * using {@link java.text.MessageFormat}.
     * Example: getString("greeting.message", "World") with "greeting.message=Hello, {0}!" -> "Hello, World!"
     *
     * @param key       The property key for the message pattern.
     * @param arguments The arguments to be formatted into the message string.
     * @return The formatted message string. If the key is not found, returns "!key!".
     * If formatting fails, returns "!key!" and logs an error.
     */
    public static String getString(String key, Object... arguments) {
        String pattern = getString(key);

        if (pattern == null || (pattern.startsWith("!") && pattern.endsWith("!"))) {
            return pattern;
        }

        if (arguments == null || arguments.length == 0) {
            return pattern;
        }

        try {
            return MessageFormat.format(pattern, arguments);
        } catch (IllegalArgumentException e) {
            log.error("Failed to format message for key '{}' with pattern '{}' and arguments.", key, pattern, e);
            return "!" + key + "!";
        }
    }
}