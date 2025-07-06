package de.bsommerfeld.neverlose.fx.messages;

import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

/**
 * Utility class to load and provide localized messages from a properties file located in the
 * classpath resources. Properties are loaded once when the class is initialized.
 * <p>
 * This class provides various helper methods for retrieving different types of messages
 * (UI strings, error messages, log messages, etc.) with proper formatting.
 */
public final class Messages {

  private static final LogFacade log;
  private static final Properties PROPERTIES = new Properties();
  private static final String PROPERTIES_RESOURCE_PATH = "/messages.properties";

  static {
    try {
      log = LogFacadeFactory.getLogger(Messages.class);
    } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
      System.err.println(
          "FATAL: Failed to initialize logger for Messages class: " + e.getMessage());
      e.printStackTrace();
      throw new IllegalStateException("Cannot initialize logger for Messages: " + e.getMessage());
    } catch (Throwable t) {
      System.err.println(
          "FATAL: Unexpected error initializing logger for Messages class: " + t.getMessage());
      t.printStackTrace();
      throw new IllegalStateException(
          "Unexpected error initializing logger for Messages: " + t.getMessage());
    }

    loadPropertiesFromResources();
  }

  private Messages() {
    throw new IllegalStateException("Utility class - cannot be instantiated.");
  }

  /**
   * Loads properties from the resource file defined by PROPERTIES_RESOURCE_PATH. Called only once
   * from the static initializer.
   */
  private static void loadPropertiesFromResources() {
    try (InputStream propertiesStream =
        Messages.class.getResourceAsStream(PROPERTIES_RESOURCE_PATH)) {

      if (propertiesStream == null) {
        log.error(
            "FATAL ERROR: Resource file not found in classpath: {}", PROPERTIES_RESOURCE_PATH);
        throw new IllegalStateException("Resource file not found: " + PROPERTIES_RESOURCE_PATH);
      } else {
        PROPERTIES.load(new InputStreamReader(propertiesStream, StandardCharsets.UTF_8));
        log.info(getString("log.app.messagesLoaded", PROPERTIES_RESOURCE_PATH));
        log.debug(getString("log.app.messagesCount", PROPERTIES.size()));
      }
    } catch (IOException e) {
      log.error(
          "FATAL ERROR: Could not load properties from resource: {}", PROPERTIES_RESOURCE_PATH, e);
      throw new IllegalStateException(
          "Could not load properties from resource: " + PROPERTIES_RESOURCE_PATH, e);
    } catch (Exception e) {
      log.error(
          "FATAL ERROR: Unexpected error loading properties from resource: {}",
          PROPERTIES_RESOURCE_PATH,
          e);
      throw new IllegalStateException(
          "Unexpected error loading properties from resource: " + PROPERTIES_RESOURCE_PATH, e);
    }
  }

  /**
   * Retrieves the message string for the given key from the loaded properties.
   *
   * @param key The property key for the message.
   * @return The message string associated with the key. If the key is not found, returns a string
   *     like "!key!" to make missing keys visible in the UI. Returns null if the input key is null.
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
   * Retrieves the message string for the given key and formats it with the provided arguments using
   * {@link java.text.MessageFormat}. Example: getString("greeting.message", "World") with
   * "greeting.message=Hello, {0}!" -> "Hello, World!"
   *
   * @param key The property key for the message pattern.
   * @param arguments The arguments to be formatted into the message string.
   * @return The formatted message string. If the key is not found, returns "!key!". If formatting
   *     fails, returns "!key!" and logs an error.
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
      // Create a new array with the same length to avoid modifying the original arguments
      Object[] safeArgs = new Object[arguments.length];
      for (int i = 0; i < arguments.length; i++) {
        // Replace null arguments with the string "null" to prevent NullPointerException
        safeArgs[i] = arguments[i] != null ? arguments[i] : "null";
      }
      return MessageFormat.format(pattern, safeArgs);
    } catch (IllegalArgumentException e) {
      log.error(
          "Failed to format message for key '{}' with pattern '{}' and arguments.",
          key,
          pattern,
          e);
      return "!" + key + "!";
    }
  }

  /**
   * Retrieves a UI string for the given element type and name.
   * This is a convenience method for accessing UI-related strings.
   *
   * @param elementType The type of UI element (button, label, title, etc.)
   * @param name The specific name of the element
   * @return The localized string for the UI element
   */
  public static String getUIString(String elementType, String name) {
    return getString("ui." + elementType + "." + name);
  }

  /**
   * Retrieves a button label string.
   *
   * @param name The name of the button
   * @return The localized string for the button
   */
  public static String getButtonLabel(String name) {
    return getUIString("button", name);
  }

  /**
   * Retrieves a label text string.
   *
   * @param name The name of the label
   * @return The localized string for the label
   */
  public static String getLabelText(String name) {
    return getUIString("label", name);
  }

  /**
   * Retrieves a title string.
   *
   * @param name The name of the title
   * @return The localized string for the title
   */
  public static String getTitle(String name) {
    return getUIString("title", name);
  }

  /**
   * Retrieves an error message for the given error type and formats it with the provided arguments.
   *
   * @param errorType The type of error (general, template, plan, etc.)
   * @param name The specific name of the error
   * @param arguments The arguments to be formatted into the error message
   * @return The formatted error message
   */
  public static String getErrorMessage(String errorType, String name, Object... arguments) {
    return getString("error." + errorType + "." + name, arguments);
  }

  /**
   * Retrieves a formatted error message with exception details.
   *
   * @param errorKey The key for the error message (without the "error." prefix)
   * @param t The throwable containing the error details
   * @return The formatted error message including the exception message
   */
  public static String getFormattedError(String errorKey, Throwable t) {
    return getString("error." + errorKey, t.getMessage());
  }

  /**
   * Retrieves a log message for the given log type and formats it with the provided arguments.
   *
   * @param logType The type of log message (plan, template, debug, error, etc.)
   * @param name The specific name of the log message
   * @param arguments The arguments to be formatted into the log message
   * @return The formatted log message
   */
  public static String getLogMessage(String logType, String name, Object... arguments) {
    return getString("log." + logType + "." + name, arguments);
  }

  /**
   * Retrieves a debug log message and formats it with the provided arguments.
   *
   * @param name The name of the debug log message
   * @param arguments The arguments to be formatted into the log message
   * @return The formatted debug log message
   */
  public static String getDebugLog(String name, Object... arguments) {
    return getLogMessage("debug", name, arguments);
  }

  /**
   * Retrieves an error log message and formats it with the provided arguments.
   *
   * @param name The name of the error log message
   * @param arguments The arguments to be formatted into the log message
   * @return The formatted error log message
   */
  public static String getErrorLog(String name, Object... arguments) {
    return getLogMessage("error", name, arguments);
  }

  /**
   * Retrieves a persistence log message and formats it with the provided arguments.
   *
   * @param name The name of the persistence log message
   * @param arguments The arguments to be formatted into the log message
   * @return The formatted persistence log message
   */
  public static String getPersistenceLog(String name, Object... arguments) {
    return getLogMessage("persistence", name, arguments);
  }

  /**
   * Retrieves a dialog message for the given dialog type and formats it with the provided arguments.
   *
   * @param dialogType The type of dialog (delete, overwrite, etc.)
   * @param name The specific name of the dialog
   * @param arguments The arguments to be formatted into the dialog message
   * @return The formatted dialog message
   */
  public static String getDialogMessage(String dialogType, String name, Object... arguments) {
    return getString("dialog." + dialogType + "." + name, arguments);
  }

  /**
   * Formats a list of items and inserts it into a message.
   *
   * @param key The key for the message pattern
   * @param items The list of items to format
   * @return The formatted message with the list of items, or null if key is null
   */
  public static String getListFormatted(String key, List<?> items) {
    if (key == null) {
      log.warn("Null key provided to getListFormatted");
      return null;
    }

    if (items == null) {
      log.warn("Null items list provided to getListFormatted for key: {}", key);
      return getString(key, "");
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < items.size(); i++) {
      if (i > 0) sb.append(", ");
      Object item = items.get(i);
      sb.append(item != null ? item.toString() : "null");
    }
    return getString(key, sb.toString());
  }

  /**
   * Formats a list of items with a custom separator and inserts it into a message.
   *
   * @param key The key for the message pattern
   * @param items The list of items to format
   * @param separator The separator to use between items
   * @return The formatted message with the list of items, or null if key is null
   */
  public static String getListFormatted(String key, List<?> items, String separator) {
    if (key == null) {
      log.warn("Null key provided to getListFormatted with separator");
      return null;
    }

    if (items == null) {
      log.warn("Null items list provided to getListFormatted with separator for key: {}", key);
      return getString(key, "");
    }

    String actualSeparator = separator != null ? separator : ", ";

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < items.size(); i++) {
      if (i > 0) sb.append(actualSeparator);
      Object item = items.get(i);
      sb.append(item != null ? item.toString() : "null");
    }
    return getString(key, sb.toString());
  }

  /**
   * Retrieves a notification message for the given notification type and formats it with the provided arguments.
   *
   * @param notificationType The type of notification (template, plan, export, etc.)
   * @param name The specific name of the notification (title, text, etc.)
   * @param arguments The arguments to be formatted into the notification message
   * @return The formatted notification message
   */
  public static String getNotificationMessage(String notificationType, String name, Object... arguments) {
    return getString("notification." + notificationType + "." + name, arguments);
  }

  /**
   * Retrieves a resource path for the given resource type.
   *
   * @param resourceType The type of resource (fxml, css, etc.)
   * @return The path to the resource
   */
  public static String getResourcePath(String resourceType) {
    return getString("path." + resourceType + ".fxml");
  }
}
