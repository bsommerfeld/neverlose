package de.bsommerfeld.neverlose.fx.messages;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * A ResourceBundle implementation that delegates to the Messages class. This allows the Messages
 * class to be used as a resource bundle in FXML files.
 * <p>
 * This implementation directly accesses the properties from the Messages class to avoid
 * loading the properties file twice.
 */
public class MessagesResourceBundle extends ResourceBundle {

  private static Properties properties;

  static {
    try {
      // Access the PROPERTIES field from Messages class using reflection
      Field propertiesField = Messages.class.getDeclaredField("PROPERTIES");
      propertiesField.setAccessible(true);
      properties = (Properties) propertiesField.get(null);
    } catch (NoSuchFieldException e) {
      System.err.println("Failed to access Messages.PROPERTIES: Field not found - " + e.getMessage());
      e.printStackTrace();
      // Create an empty properties object as fallback
      properties = new Properties();
    } catch (IllegalAccessException e) {
      System.err.println("Failed to access Messages.PROPERTIES: Access denied - " + e.getMessage());
      e.printStackTrace();
      // Create an empty properties object as fallback
      properties = new Properties();
    } catch (ClassCastException e) {
      System.err.println("Failed to access Messages.PROPERTIES: Invalid type - " + e.getMessage());
      e.printStackTrace();
      // Create an empty properties object as fallback
      properties = new Properties();
    } catch (Exception e) {
      System.err.println("Unexpected error accessing Messages.PROPERTIES: " + e.getMessage());
      e.printStackTrace();
      // Create an empty properties object as fallback
      properties = new Properties();
    }
  }

  /**
   * Returns the value for the given key from the Messages class.
   *
   * @param key the key for the desired string
   * @return the string for the given key, or null if key is null
   */
  @Override
  protected Object handleGetObject(String key) {
    // Check for null key
    if (key == null) {
      System.err.println("Warning: Null key provided to MessagesResourceBundle.handleGetObject()");
      return null;
    }

    // First try to get the value directly from our properties
    if (properties != null) {
      String value = properties.getProperty(key);
      if (value != null) {
        return value;
      }
    }
    // Fall back to Messages class if not found
    return Messages.getString(key);
  }

  /**
   * Returns an enumeration of the keys in this resource bundle.
   *
   * @return an enumeration of the keys in this resource bundle
   */
  @Override
  public Enumeration<String> getKeys() {
    return properties != null 
        ? Collections.enumeration(properties.stringPropertyNames())
        : Collections.emptyEnumeration();
  }
}
