package de.bsommerfeld.neverlose.export;

import de.bsommerfeld.neverlose.plan.TrainingPlan;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import de.bsommerfeld.neverlose.theme.Theme;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Generates a PDF representation of a TrainingPlan, aiming for a clean, modern, and readable layout
 * suitable for screen and print.
 */
public class PdfExportService implements ExportService {

  // PDF Fonts
  private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
  private static final PDFont FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
  private static final PDFont FONT_ITALIC = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

  // Styles using the centralized Theme
  private static final PdfStyle STYLE_PREVIEW_TITLE =
      new PdfStyle(FONT_BOLD, Theme.Fonts.SIZE_TITLE, Theme.Colors.TEXT_DARKEST);
  private static final PdfStyle STYLE_PREVIEW_DESC =
      new PdfStyle(FONT_REGULAR, Theme.Fonts.SIZE_SUBTITLE, Theme.Colors.TEXT_MEDIUM);
  private static final PdfStyle STYLE_UNIT_HEADER =
      new PdfStyle(FONT_BOLD, Theme.Fonts.SIZE_UNIT_HEADER, Theme.Colors.TEXT_DARKER);
  private static final PdfStyle STYLE_UNIT_WEEKDAY =
      new PdfStyle(FONT_REGULAR, Theme.Fonts.SIZE_UNIT_WEEKDAY, Theme.Colors.TEXT_LIGHTER);
  private static final PdfStyle STYLE_UNIT_DESC =
      new PdfStyle(FONT_REGULAR, Theme.Fonts.SIZE_UNIT_DESC, Theme.Colors.TEXT_LIGHT);
  private static final PdfStyle STYLE_EXERCISE_NAME =
      new PdfStyle(FONT_BOLD, Theme.Fonts.SIZE_EXERCISE_NAME, Theme.Colors.TEXT_DARK);
  private static final PdfStyle STYLE_EXERCISE_DESC =
      new PdfStyle(FONT_REGULAR, Theme.Fonts.SIZE_EXERCISE_DESC, Theme.Colors.TEXT_LIGHT);
  private static final PdfStyle STYLE_EXERCISE_DETAILS =
      new PdfStyle(FONT_REGULAR, Theme.Fonts.SIZE_EXERCISE_DETAILS, Theme.Colors.TEXT_LIGHTER);
  private static final PdfStyle STYLE_PLACEHOLDER =
      new PdfStyle(FONT_ITALIC, Theme.Fonts.SIZE_PLACEHOLDER, Theme.Colors.TEXT_LIGHT);
  private static final String DEFAULT_PLAN_NAME = "[Unnamed Plan]";
  private static final String DEFAULT_UNIT_NAME = "[Unnamed Unit]";
  private static final String DEFAULT_EXERCISE_NAME = "[Unnamed Exercise]";
  private static final String DEFAULT_WEEKDAY = "-";
  private static final String DEFAULT_DURATION = "-";
  private static final String PLACEHOLDER_NO_UNITS = "[No training units defined]";
  private static final String PLACEHOLDER_NO_EXERCISES = "[No exercises in this unit]";
  private PDDocument document;
  private PDPageContentStream contentStream;
  private PDPage currentPage;
  private float currentY;

  @Override
  public void export(TrainingPlan trainingPlan, File targetFile) throws IOException {
    Objects.requireNonNull(trainingPlan, "Training plan cannot be null.");
    Objects.requireNonNull(targetFile, "Target file cannot be null.");

    try (PDDocument doc = new PDDocument()) {
      this.document = doc;
      startNewPage();

      writePlanHeader(trainingPlan);
      drawSeparator();
      writeUnitsSection(trainingPlan);

      closeCurrentContentStream();
      document.save(targetFile);
    }
    resetState();
  }

  private void resetState() {
    this.document = null;
    this.contentStream = null;
    this.currentPage = null;
    this.currentY = 0;
  }

  private void writePlanHeader(TrainingPlan plan) throws IOException {
    String planName = Objects.toString(plan.getName(), DEFAULT_PLAN_NAME);
    writeStyledWrappedText(planName, Layout.INDENT_UNIT_LEVEL, STYLE_PREVIEW_TITLE);
    addSpacing(Layout.SPACING_AFTER_TITLE);

    String description = plan.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      writeStyledWrappedText(
          description, Layout.INDENT_UNIT_LEVEL, STYLE_PREVIEW_DESC, Layout.EXTRA_LINE_SPACING);
      addSpacing(Layout.SPACING_AFTER_PREVIEW_DESC);
    } else {
      addSpacing(Layout.SPACING_AFTER_TITLE);
    }
  }

  private void drawSeparator() throws IOException {
    float separatorHeight = 1f;
    float totalSpaceNeeded = Layout.SPACING_SEPARATOR + separatorHeight;

    if (currentY - totalSpaceNeeded < Layout.MARGIN) {
      startNewPage();
    }

    addSpacing(Layout.SPACING_SEPARATOR / 2);

    contentStream.setStrokingColor(Theme.Colors.SEPARATOR);
    contentStream.setLineWidth(0.75f);
    contentStream.moveTo(Layout.MARGIN, currentY);
    contentStream.lineTo(Layout.PAGE_WIDTH - Layout.MARGIN, currentY);
    contentStream.stroke();
    currentY -= separatorHeight;

    addSpacing(Layout.SPACING_SEPARATOR / 2);
  }

  private void writeUnitsSection(TrainingPlan plan) throws IOException {
    List<TrainingUnit> units =
        plan.getTrainingUnits() != null ? plan.getTrainingUnits().getAll() : List.of();

    if (units.isEmpty()) {
      addSpacing(Layout.SPACING_BETWEEN_UNITS);
      writeStyledTextLine(PLACEHOLDER_NO_UNITS, Layout.INDENT_UNIT_LEVEL, STYLE_PLACEHOLDER);
      return;
    }

    boolean firstUnit = true;
    for (TrainingUnit unit : units) {
      if (!firstUnit) {
        addSpacing(Layout.SPACING_BETWEEN_UNITS);
      }
      writeUnit(unit);
      firstUnit = false;
    }
  }

  /**
   * Calculates the height needed to render a training unit, including its header, description,
   * and all exercises.
   *
   * @param unit The training unit to measure
   * @return The total height in points needed to render the unit
   * @throws IOException If there's an error calculating text dimensions
   */
  private float calculateUnitHeight(TrainingUnit unit) throws IOException {
    float totalHeight = 0;

    // Unit name
    String unitName = Objects.toString(unit.getName(), DEFAULT_UNIT_NAME);
    List<String> nameLines = wrapText(unitName, STYLE_UNIT_HEADER.font(), 
        STYLE_UNIT_HEADER.size(), Layout.CONTENT_WIDTH - Layout.INDENT_UNIT_LEVEL);
    totalHeight += nameLines.size() * STYLE_UNIT_HEADER.size() * Layout.BASE_LINE_SPACING_FACTOR;
    totalHeight += Layout.SPACING_AFTER_UNIT_HEADER;

    // Weekday (on a separate line)
    String weekday = Objects.toString(unit.getWeekday(), DEFAULT_WEEKDAY);
    List<String> weekdayLines = wrapText(weekday, STYLE_UNIT_WEEKDAY.font(), 
        STYLE_UNIT_WEEKDAY.size(), Layout.CONTENT_WIDTH - Layout.INDENT_UNIT_LEVEL);
    totalHeight += weekdayLines.size() * STYLE_UNIT_WEEKDAY.size() * Layout.BASE_LINE_SPACING_FACTOR;
    totalHeight += Layout.SPACING_AFTER_UNIT_WEEKDAY;

    // Unit description
    String description = unit.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      List<String> descLines = wrapText(description, STYLE_UNIT_DESC.font(), 
          STYLE_UNIT_DESC.size(), Layout.CONTENT_WIDTH - Layout.INDENT_UNIT_LEVEL);

      for (String line : descLines) {
        if (line.isEmpty()) {
          totalHeight += STYLE_UNIT_DESC.size() * Layout.BASE_LINE_SPACING_FACTOR + Layout.EXTRA_LINE_SPACING;
        } else {
          totalHeight += STYLE_UNIT_DESC.size() * Layout.BASE_LINE_SPACING_FACTOR + Layout.EXTRA_LINE_SPACING;
        }
      }
      totalHeight += Layout.SPACING_AFTER_UNIT_DESC;
    }

    // Exercises section
    List<TrainingExercise> exercises =
        unit.getTrainingExercises() != null ? unit.getTrainingExercises().getAll() : List.of();

    if (exercises.isEmpty()) {
      List<String> placeholderLines = wrapText(PLACEHOLDER_NO_EXERCISES, STYLE_PLACEHOLDER.font(),
          STYLE_PLACEHOLDER.size(), Layout.CONTENT_WIDTH - (2 * Layout.INDENT_EXERCISE_CONTAINER) - (2 * Layout.INDENT_EXERCISE_INTERNAL)); // Adjusted width for consistency and internal padding
      totalHeight += placeholderLines.size() * STYLE_PLACEHOLDER.size() * Layout.BASE_LINE_SPACING_FACTOR;
      totalHeight += Layout.SPACING_BETWEEN_EXERCISES;
    } else {
      totalHeight += Layout.SPACING_BEFORE_EXERCISES;

      boolean firstExercise = true;
      for (TrainingExercise exercise : exercises) {
        if (!firstExercise) {
          totalHeight += Layout.SPACING_BETWEEN_EXERCISES;
        }
        totalHeight += calculateExerciseHeight(exercise);
        firstExercise = false;
      }
    }

    // Add padding for the container
    totalHeight += 2 * Layout.SPACING_AFTER_UNIT_DESC; // Top and bottom padding

    return totalHeight;
  }

  /**
   * Writes a training unit to the PDF, including a container background.
   *
   * @param unit The training unit to write
   * @throws IOException If there's an error writing to the PDF
   */
  private void writeUnit(TrainingUnit unit) throws IOException {
    // Calculate the height needed for this unit
    float unitHeight = calculateUnitHeight(unit);

    // Check if we need to start a new page
    if (currentY - unitHeight < Layout.MARGIN) {
      startNewPage();
    }

    // Save the current Y position to draw the container
    float containerStartY = currentY;

    // Draw the container background
    drawContainer(
        Layout.MARGIN, 
        currentY - unitHeight, 
        Layout.CONTENT_WIDTH, 
        unitHeight, 
        Layout.UNIT_BORDER_RADIUS, 
        Theme.Colors.TRAINING_UNIT_BG);

    // Add some padding at the top
    addSpacing(Layout.SPACING_AFTER_UNIT_DESC);

    // Write the unit content
    String unitName = Objects.toString(unit.getName(), DEFAULT_UNIT_NAME);
    String weekday = Objects.toString(unit.getWeekday(), DEFAULT_WEEKDAY);

    // Write unit name and weekday on separate lines
    writeStyledWrappedText(unitName, Layout.INDENT_UNIT_LEVEL, STYLE_UNIT_HEADER);
    addSpacing(Layout.SPACING_AFTER_UNIT_HEADER);
    writeStyledWrappedText(weekday, Layout.INDENT_UNIT_LEVEL, STYLE_UNIT_WEEKDAY);
    addSpacing(Layout.SPACING_AFTER_UNIT_WEEKDAY);

    String description = unit.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      writeStyledWrappedText(
          description, Layout.INDENT_UNIT_LEVEL, STYLE_UNIT_DESC, Layout.EXTRA_LINE_SPACING);
      addSpacing(Layout.SPACING_AFTER_UNIT_DESC);
    }

    writeExercisesSection(unit);
  }

  private void writeExercisesSection(TrainingUnit unit) throws IOException {
    List<TrainingExercise> exercises =
        unit.getTrainingExercises() != null ? unit.getTrainingExercises().getAll() : List.of();

    if (exercises.isEmpty()) {
      writeStyledWrappedText(
          PLACEHOLDER_NO_EXERCISES, Layout.INDENT_EXERCISE_CONTAINER + Layout.INDENT_EXERCISE_INTERNAL, STYLE_PLACEHOLDER);
      addSpacing(Layout.SPACING_BETWEEN_EXERCISES);
      return;
    }

    addSpacing(Layout.SPACING_BEFORE_EXERCISES);

    boolean firstExercise = true;
    for (TrainingExercise exercise : exercises) {
      if (!firstExercise) {
        addSpacing(Layout.SPACING_BETWEEN_EXERCISES);
      }
      writeExercise(exercise);
      firstExercise = false;
    }
  }

  /**
   * Calculates the height needed to render a training exercise, including its name, description,
   * and details.
   *
   * @param exercise The training exercise to measure
   * @return The total height in points needed to render the exercise
   * @throws IOException If there's an error calculating text dimensions
   */
  private float calculateExerciseHeight(TrainingExercise exercise) throws IOException {
    float totalHeight = 0;

    // Exercise name
    String exerciseName = Objects.toString(exercise.getName(), DEFAULT_EXERCISE_NAME);
    List<String> nameLines = wrapText(exerciseName, STYLE_EXERCISE_NAME.font(), 
        STYLE_EXERCISE_NAME.size(), Layout.CONTENT_WIDTH - (2 * Layout.INDENT_EXERCISE_BLOCK) - (2 * Layout.INDENT_EXERCISE_INTERNAL)); // Adjusted width for consistency and internal padding
    totalHeight += nameLines.size() * STYLE_EXERCISE_NAME.size() * Layout.BASE_LINE_SPACING_FACTOR;
    totalHeight += Layout.SPACING_AFTER_EXERCISE_NAME;

    // Exercise description
    String description = exercise.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      List<String> descLines = wrapText(description, STYLE_EXERCISE_DESC.font(), 
          STYLE_EXERCISE_DESC.size(), Layout.CONTENT_WIDTH - (2 * Layout.INDENT_EXERCISE_CONTENT) - (2 * Layout.INDENT_EXERCISE_INTERNAL)); // Adjusted width for consistency and internal padding

      for (String line : descLines) {
        if (line.isEmpty()) {
          totalHeight += STYLE_EXERCISE_DESC.size() * Layout.BASE_LINE_SPACING_FACTOR + Layout.EXTRA_LINE_SPACING;
        } else {
          totalHeight += STYLE_EXERCISE_DESC.size() * Layout.BASE_LINE_SPACING_FACTOR + Layout.EXTRA_LINE_SPACING;
        }
      }
      totalHeight += Layout.SPACING_AFTER_EXERCISE_DESC;
    }

    // Exercise details
    String duration = Objects.toString(exercise.getDuration(), DEFAULT_DURATION);
    String details = String.format(
        "Duration: %s  •  Sets: %d  •  Ball Bucket: %s",
        duration, exercise.getSets(), (exercise.isBallBucket() ? "Yes" : "No"));
    List<String> detailLines = wrapText(details, STYLE_EXERCISE_DETAILS.font(), 
        STYLE_EXERCISE_DETAILS.size(), Layout.CONTENT_WIDTH - (2 * Layout.INDENT_EXERCISE_CONTENT) - (2 * Layout.INDENT_EXERCISE_INTERNAL)); // Adjusted width for consistency and internal padding
    totalHeight += detailLines.size() * STYLE_EXERCISE_DETAILS.size() * Layout.BASE_LINE_SPACING_FACTOR;

    // Add padding for the container
    totalHeight += 2 * Layout.SPACING_AFTER_EXERCISE_NAME; // Top and bottom padding

    return totalHeight;
  }

  /**
   * Writes a training exercise to the PDF, including a container background.
   *
   * @param exercise The training exercise to write
   * @throws IOException If there's an error writing to the PDF
   */
  private void writeExercise(TrainingExercise exercise) throws IOException {
    // Calculate the height needed for this exercise
    float exerciseHeight = calculateExerciseHeight(exercise);

    // Check if we need to start a new page
    if (currentY - exerciseHeight < Layout.MARGIN) {
      startNewPage();
    }

    // Draw the container background
    drawContainer(
        Layout.MARGIN + Layout.INDENT_EXERCISE_CONTAINER, 
        currentY - exerciseHeight, 
        Layout.CONTENT_WIDTH - (2 * Layout.INDENT_EXERCISE_CONTAINER), // Adjusted width to prevent extending outside unit container
        exerciseHeight, 
        Layout.EXERCISE_BORDER_RADIUS, 
        Theme.Colors.EXERCISE_BG);

    // Add some padding at the top
    addSpacing(Layout.SPACING_AFTER_EXERCISE_NAME);

    // Write the exercise content
    String exerciseName = Objects.toString(exercise.getName(), DEFAULT_EXERCISE_NAME);
    writeStyledWrappedText(exerciseName, Layout.INDENT_EXERCISE_BLOCK + Layout.INDENT_EXERCISE_INTERNAL, STYLE_EXERCISE_NAME);
    addSpacing(Layout.SPACING_AFTER_EXERCISE_NAME);

    String description = exercise.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      writeStyledWrappedText(
          description,
          Layout.INDENT_EXERCISE_CONTENT + Layout.INDENT_EXERCISE_INTERNAL,
          STYLE_EXERCISE_DESC,
          Layout.EXTRA_LINE_SPACING);
      addSpacing(Layout.SPACING_AFTER_EXERCISE_DESC);
    }

    String duration = Objects.toString(exercise.getDuration(), DEFAULT_DURATION);
    String details =
        String.format(
            "Duration: %s  •  Sets: %d  •  Ball Bucket: %s",
            duration, exercise.getSets(), (exercise.isBallBucket() ? "Yes" : "No"));
    writeStyledWrappedText(details, Layout.INDENT_EXERCISE_CONTENT + Layout.INDENT_EXERCISE_INTERNAL, STYLE_EXERCISE_DETAILS);
  }

  /**
   * Draws a container with a background color and rounded corners.
   *
   * @param x The x-coordinate of the top-left corner
   * @param y The y-coordinate of the top-left corner
   * @param width The width of the container
   * @param height The height of the container
   * @param borderRadius The radius of the rounded corners
   * @param backgroundColor The background color of the container
   * @throws IOException If there's an error drawing to the PDF
   */
  private void drawContainer(float x, float y, float width, float height, float borderRadius, Color backgroundColor) 
      throws IOException {
    // Save the current graphics state
    contentStream.saveGraphicsState();

    // Set the fill color to the background color
    contentStream.setNonStrokingColor(backgroundColor);

    // Set the stroke color and width for the border
    contentStream.setStrokingColor(Theme.Colors.ACCENT_SILVER);
    contentStream.setLineWidth(Layout.BORDER_WIDTH);

    // Draw a rounded rectangle
    // Since PDFBox doesn't have built-in support for rounded rectangles,
    // we'll approximate it with a series of lines and curves

    // Start a new path
    contentStream.moveTo(x + borderRadius, y);

    // Top edge
    contentStream.lineTo(x + width - borderRadius, y);

    // Top-right corner
    contentStream.curveTo(
        x + width, y,
        x + width, y,
        x + width, y + borderRadius);

    // Right edge
    contentStream.lineTo(x + width, y + height - borderRadius);

    // Bottom-right corner
    contentStream.curveTo(
        x + width, y + height,
        x + width, y + height,
        x + width - borderRadius, y + height);

    // Bottom edge
    contentStream.lineTo(x + borderRadius, y + height);

    // Bottom-left corner
    contentStream.curveTo(
        x, y + height,
        x, y + height,
        x, y + height - borderRadius);

    // Left edge
    contentStream.lineTo(x, y + borderRadius);

    // Top-left corner
    contentStream.curveTo(
        x, y,
        x, y,
        x + borderRadius, y);

    // Fill and stroke the path
    contentStream.fillAndStroke();

    // Restore the graphics state
    contentStream.restoreGraphicsState();
  }

  private void startNewPage() throws IOException {
    closeCurrentContentStream();
    currentPage = new PDPage(PDRectangle.A4);
    document.addPage(currentPage);
    contentStream = new PDPageContentStream(document, currentPage);
    currentY = currentPage.getMediaBox().getHeight() - Layout.MARGIN;
  }

  private void closeCurrentContentStream() throws IOException {
    if (contentStream != null) {
      contentStream.close();
      contentStream = null;
    }
  }

  private void addSpacing(float space) throws IOException {
    if (space <= 0) return;
    if (currentY - space < Layout.MARGIN) {
      startNewPage();
    } else {
      currentY -= space;
    }
  }

  private void writeStyledTextLine(String line, float indent, PdfStyle style) throws IOException {
    writeStyledTextLine(line, indent, style, 0f);
  }

  private void writeStyledTextLine(String line, float indent, PdfStyle style, float extraLeading)
      throws IOException {
    float x = Layout.MARGIN + indent;
    float availableWidth = Layout.CONTENT_WIDTH - indent;
    float baseLeading = style.size() * Layout.BASE_LINE_SPACING_FACTOR;
    float totalLeading = baseLeading + extraLeading;

    if (currentY - totalLeading < Layout.MARGIN) {
      startNewPage();
    }

    float lineWidth = calculateTextWidth(line, style.font(), style.size());
    String lineToWrite = line;
    if (lineWidth > availableWidth) {
      lineToWrite = truncateText(line, style.font(), style.size(), availableWidth);
    }
    if (lineToWrite.isEmpty()) return;

    contentStream.beginText();
    contentStream.setFont(style.font(), style.size());
    contentStream.setNonStrokingColor(style.color());
    contentStream.newLineAtOffset(x, currentY - style.size());
    contentStream.showText(lineToWrite);
    contentStream.endText();

    currentY -= totalLeading;
  }

  private void writeStyledWrappedText(String text, float indent, PdfStyle style)
      throws IOException {
    writeStyledWrappedText(text, indent, style, 0f);
  }

  private void writeStyledWrappedText(
      String text, float indent, PdfStyle style, float extraLeadingPerLine) throws IOException {
    if (text == null || text.trim().isEmpty()) return;

    float availableWidth;
    // Use double indentation for exercise content to be consistent with calculateExerciseHeight
    if (indent == Layout.INDENT_EXERCISE_BLOCK || indent == Layout.INDENT_EXERCISE_CONTENT) {
      availableWidth = Layout.CONTENT_WIDTH - (2 * indent);
    } else if (indent == Layout.INDENT_EXERCISE_BLOCK + Layout.INDENT_EXERCISE_INTERNAL || 
               indent == Layout.INDENT_EXERCISE_CONTENT + Layout.INDENT_EXERCISE_INTERNAL) {
      // For exercise content with internal padding
      availableWidth = Layout.CONTENT_WIDTH - (2 * (indent - Layout.INDENT_EXERCISE_INTERNAL)) - (2 * Layout.INDENT_EXERCISE_INTERNAL);
    } else {
      availableWidth = Layout.CONTENT_WIDTH - indent;
    }

    List<String> lines = wrapText(text, style.font(), style.size(), availableWidth);

    for (String line : lines) {
      if (line.isEmpty()) {
        addSpacing(style.size() * Layout.BASE_LINE_SPACING_FACTOR + extraLeadingPerLine);
      } else {
        writeStyledTextLine(line, indent, style, extraLeadingPerLine);
      }
    }
  }

  private float calculateTextWidth(String text, PDFont font, float fontSize) throws IOException {
    return font.getStringWidth(text) / 1000f * fontSize;
  }

  private String truncateText(String text, PDFont font, float fontSize, float maxWidth)
      throws IOException {
    String result = text;
    float width = calculateTextWidth(result, font, fontSize);
    while (width > maxWidth && !result.isEmpty()) {
      result = result.substring(0, result.length() - 1);
      width = calculateTextWidth(result, font, fontSize);
    }
    return result;
  }

  private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth)
      throws IOException {
    List<String> lines = new ArrayList<>();
    String remainingText = text.trim().replaceAll("\r\n", "\n");
    String[] paragraphs = remainingText.split("\n", -1);

    for (String paragraph : paragraphs) {
      if (paragraph.isEmpty()) {
        lines.add("");
        continue;
      }
      String remainingParagraph = paragraph.trim();
      while (!remainingParagraph.isEmpty()) {
        int breakIndex = findBreakIndex(remainingParagraph, font, fontSize, maxWidth);
        lines.add(remainingParagraph.substring(0, breakIndex).trim());
        remainingParagraph = remainingParagraph.substring(breakIndex).trim();
      }
    }
    return lines;
  }

  private int findBreakIndex(String text, PDFont font, float fontSize, float maxWidth)
      throws IOException {
    int lastWhitespace = -1;
    float currentWidth = 0;

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      float charWidth = font.getStringWidth(String.valueOf(c)) / 1000f * fontSize;

      if (currentWidth + charWidth > maxWidth) {
        if (lastWhitespace != -1) {
          return lastWhitespace + 1;
        } else {
          return Math.max(1, i);
        }
      }
      currentWidth += charWidth;

      if (Character.isWhitespace(c)) {
        lastWhitespace = i;
      }
    }
    return text.length();
  }

  /**
   * Layout constants for PDF rendering, using values from the centralized Theme.
   */
  private static class Layout {
    // Page Layout
    static final float MARGIN = Theme.Layout.MARGIN;
    static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    static final float BASE_LINE_SPACING_FACTOR = Theme.Layout.BASE_LINE_SPACING_FACTOR;
    static final float EXTRA_LINE_SPACING = Theme.Layout.EXTRA_LINE_SPACING;

    // Indentation
    static final float INDENT_UNIT_LEVEL = Theme.Layout.INDENT_UNIT_LEVEL;
    static final float INDENT_EXERCISE_CONTAINER = Theme.Layout.INDENT_EXERCISE_CONTAINER;
    static final float INDENT_EXERCISE_INTERNAL = Theme.Layout.INDENT_EXERCISE_INTERNAL;
    static final float INDENT_EXERCISE_BLOCK = Theme.Layout.INDENT_EXERCISE_BLOCK;
    static final float INDENT_EXERCISE_CONTENT = Theme.Layout.INDENT_EXERCISE_CONTENT;

    // Spacing
    static final float SPACING_AFTER_TITLE = Theme.Layout.SPACING_AFTER_TITLE;
    static final float SPACING_AFTER_PREVIEW_DESC = Theme.Layout.SPACING_AFTER_PREVIEW_DESC;
    static final float SPACING_SEPARATOR = Theme.Layout.SPACING_SEPARATOR;
    static final float SPACING_BETWEEN_UNITS = Theme.Layout.SPACING_BETWEEN_UNITS;
    static final float SPACING_AFTER_UNIT_HEADER = Theme.Layout.SPACING_AFTER_UNIT_HEADER;
    static final float SPACING_AFTER_UNIT_WEEKDAY = Theme.Layout.SPACING_AFTER_UNIT_WEEKDAY;
    static final float SPACING_AFTER_UNIT_DESC = Theme.Layout.SPACING_AFTER_UNIT_DESC;
    static final float SPACING_BEFORE_EXERCISES = Theme.Layout.SPACING_BEFORE_EXERCISES;
    static final float SPACING_BETWEEN_EXERCISES = Theme.Layout.SPACING_BETWEEN_EXERCISES;
    static final float SPACING_AFTER_EXERCISE_NAME = Theme.Layout.SPACING_AFTER_EXERCISE_NAME;
    static final float SPACING_AFTER_EXERCISE_DESC = Theme.Layout.SPACING_AFTER_EXERCISE_DESC;

    // Component Styling
    static final float UNIT_BORDER_RADIUS = Theme.Layout.UNIT_BORDER_RADIUS;
    static final float EXERCISE_BORDER_RADIUS = Theme.Layout.EXERCISE_BORDER_RADIUS;
    static final float BORDER_WIDTH = Theme.Layout.BORDER_WIDTH;
  }

  private record PdfStyle(PDFont font, float size, Color color) {}
}
