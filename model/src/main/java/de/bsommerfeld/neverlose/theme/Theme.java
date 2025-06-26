package de.bsommerfeld.neverlose.theme;

import java.awt.Color;

/**
 * Central theme configuration for the NeverLose application. This class defines style parameters
 * that are shared between the JavaFX UI and PDF exports, ensuring visual consistency across
 * different representations of the same data.
 */
public class Theme {
  // Color Palette
  public static class Colors {
    // Brand Colors
    public static final Color NEVER_BLUE =
        hexToColor("#1a3a8f"); // Deep royal blue for "Never" elements
    public static final Color LOSE_ORANGE =
        hexToColor("#ff7b29"); // Vibrant warm orange for "Lose" elements

    // Accent Colors
    public static final Color ACCENT_SILVER =
        hexToColor("#c0c0c0"); // Polished silver for interactive elements
    public static final Color ACCENT_GOLD = hexToColor("#d4af37"); // Matte gold for premium accents

    // Background Colors
    public static final Color BG_LIGHT = hexToColor("#f5f5f0"); // Light warm gray background
    public static final Color BG_DARK = hexToColor("#2a2a2a"); // Deep charcoal for dark mode

    // Text Colors
    public static final Color TEXT_PRIMARY = hexToColor("#333333"); // Primary text color
    public static final Color TEXT_SECONDARY = hexToColor("#666666"); // Secondary text color

    // Component-specific Colors
    public static final Color TRAINING_UNIT_BG =
        hexToColor("#f9f9f9"); // Light gray for training unit background
    public static final Color EXERCISE_BG = hexToColor("#ffffff"); // White for exercise background
    public static final Color SEPARATOR = hexToColor("#E0E0E0"); // Light gray for separators

    // Text Shades (for PDF)
    public static final Color TEXT_DARKEST = hexToColor("#111111");
    public static final Color TEXT_DARKER = hexToColor("#1A1A1A");
    public static final Color TEXT_DARK = hexToColor("#222222");
    public static final Color TEXT_MEDIUM = hexToColor("#555555");
    public static final Color TEXT_LIGHT = hexToColor("#666666");
    public static final Color TEXT_LIGHTER = hexToColor("#777777");

    /**
     * Converts a hex color string to a Color object.
     *
     * @param hex The hex color string (e.g., "#FF0000" for red)
     * @return The corresponding Color object
     */
    public static Color hexToColor(String hex) {
      hex = hex.replace("#", "");
      if (hex.length() != 6) return Color.BLACK;
      try {
        return new Color(
            Integer.valueOf(hex.substring(0, 2), 16),
            Integer.valueOf(hex.substring(2, 4), 16),
            Integer.valueOf(hex.substring(4, 6), 16));
      } catch (NumberFormatException e) {
        System.err.println("Warning: Invalid hex color format '" + hex + "'. Using black.");
        return Color.BLACK;
      }
    }
  }

  // Typography
  public static class Fonts {
    // Font Names (for JavaFX)
    public static final String HEADING_FONT = "Garamond, Baskerville, 'Times New Roman', serif";
    public static final String BODY_FONT = "Lato, 'Segoe UI', System";

    // Font Sizes (increased for better readability)
    public static final float SIZE_TITLE = 26;
    public static final float SIZE_SUBTITLE = 20;
    public static final float SIZE_UNIT_HEADER = 20;
    public static final float SIZE_UNIT_WEEKDAY = 14; // New size for weekday
    public static final float SIZE_UNIT_DESC = 16;
    public static final float SIZE_EXERCISE_NAME = 14;
    public static final float SIZE_EXERCISE_DESC = 12;
    public static final float SIZE_EXERCISE_DETAILS = 11;
    public static final float SIZE_PLACEHOLDER = 11;
  }

  // Layout Constants
  public static class Layout {
    // Page Layout
    public static final float MARGIN = 50;
    public static final float BASE_LINE_SPACING_FACTOR = 1.3f;
    public static final float EXTRA_LINE_SPACING = 1.5f;

    // Indentation
    public static final float INDENT_UNIT_LEVEL = 15; // Added padding to fix left alignment issue
    public static final float INDENT_EXERCISE_CONTAINER = 15;
    public static final float INDENT_EXERCISE_BLOCK = INDENT_EXERCISE_CONTAINER;
    public static final float INDENT_EXERCISE_CONTENT = INDENT_EXERCISE_CONTAINER;

    // Spacing (optimized for more structured layout)
    public static final float SPACING_AFTER_TITLE = 10;
    public static final float SPACING_AFTER_PREVIEW_DESC = 15;
    public static final float SPACING_SEPARATOR = 12;
    public static final float SPACING_BETWEEN_UNITS = 20;
    public static final float SPACING_AFTER_UNIT_HEADER = 4;
    public static final float SPACING_AFTER_UNIT_WEEKDAY = 8; // New spacing after weekday
    public static final float SPACING_AFTER_UNIT_DESC = 12;
    public static final float SPACING_BEFORE_EXERCISES = 10;
    public static final float SPACING_BETWEEN_EXERCISES = 15;
    public static final float SPACING_AFTER_EXERCISE_NAME = 3;
    public static final float SPACING_AFTER_EXERCISE_DESC = 6;

    // Component Styling
    public static final float UNIT_BORDER_RADIUS = 5;
    public static final float EXERCISE_BORDER_RADIUS = 3;
    public static final float BORDER_WIDTH = 1;
  }
}
