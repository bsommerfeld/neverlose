package de.bsommerfeld.neverlose.export.pdf;

import de.bsommerfeld.neverlose.theme.Theme;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Layout constants for PDF rendering, using values from the centralized Theme. This class
 * encapsulates all layout-related constants to maintain consistency across the PDF generation
 * process.
 */
public class PdfLayout {
  // Page Layout
  public static final float MARGIN = Theme.Layout.MARGIN;
  public static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
  public static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
  public static final float BASE_LINE_SPACING_FACTOR = Theme.Layout.BASE_LINE_SPACING_FACTOR;
  public static final float EXTRA_LINE_SPACING = Theme.Layout.EXTRA_LINE_SPACING;

  public static final float PADDING_UNIT_VERTICAL = 12f;
  public static final float PADDING_UNIT_HORIZONTAL = 15f;
  public static final float PADDING_EXERCISE_VERTICAL = 10f;
  public static final float PADDING_EXERCISE_HORIZONTAL = 10f;

  // Indentation
  public static final float INDENT_UNIT_LEVEL = PADDING_UNIT_HORIZONTAL;
  public static final float INDENT_EXERCISE_CONTAINER = Theme.Layout.INDENT_EXERCISE_CONTAINER;
  public static final float INDENT_EXERCISE_INTERNAL = PADDING_EXERCISE_HORIZONTAL;
  public static final float INDENT_EXERCISE_BLOCK = Theme.Layout.INDENT_EXERCISE_BLOCK;
  public static final float INDENT_EXERCISE_CONTENT = Theme.Layout.INDENT_EXERCISE_CONTENT;

  // Spacing
  public static final float SPACING_AFTER_TITLE = Theme.Layout.SPACING_AFTER_TITLE;
  public static final float SPACING_AFTER_PREVIEW_DESC = Theme.Layout.SPACING_AFTER_PREVIEW_DESC;
  public static final float SPACING_SEPARATOR = Theme.Layout.SPACING_SEPARATOR;
  public static final float SPACING_BETWEEN_UNITS = 20f;
  public static final float SPACING_AFTER_UNIT_HEADER = Theme.Layout.SPACING_AFTER_UNIT_HEADER;
  public static final float SPACING_AFTER_UNIT_WEEKDAY = Theme.Layout.SPACING_AFTER_UNIT_WEEKDAY;
  public static final float SPACING_AFTER_UNIT_DESC = Theme.Layout.SPACING_AFTER_UNIT_DESC;
  public static final float SPACING_BEFORE_EXERCISES = 15f;
  public static final float SPACING_BETWEEN_EXERCISES = 10f;
  public static final float SPACING_AFTER_EXERCISE_NAME = Theme.Layout.SPACING_AFTER_EXERCISE_NAME;
  public static final float SPACING_AFTER_EXERCISE_DESC = Theme.Layout.SPACING_AFTER_EXERCISE_DESC;

  // Component Styling
  public static final float UNIT_BORDER_RADIUS = Theme.Layout.UNIT_BORDER_RADIUS;
  public static final float EXERCISE_BORDER_RADIUS = Theme.Layout.EXERCISE_BORDER_RADIUS;
  public static final float BORDER_WIDTH = Theme.Layout.BORDER_WIDTH;
}
