package de.bsommerfeld.neverlose.export;

import de.bsommerfeld.neverlose.plan.TrainingPlan;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
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

  private static final PdfStyle STYLE_PREVIEW_TITLE =
      new PdfStyle(Fonts.BOLD, Fonts.SIZE_PREVIEW_TITLE, Colors.TEXT_DARKEST);
  private static final PdfStyle STYLE_PREVIEW_DESC =
      new PdfStyle(Fonts.REGULAR, Fonts.SIZE_PREVIEW_DESC, Colors.TEXT_MEDIUM);
  private static final PdfStyle STYLE_UNIT_HEADER =
      new PdfStyle(Fonts.BOLD, Fonts.SIZE_UNIT_HEADER, Colors.TEXT_DARKER);
  private static final PdfStyle STYLE_UNIT_DESC =
      new PdfStyle(Fonts.REGULAR, Fonts.SIZE_UNIT_DESC, Colors.TEXT_LIGHT);
  private static final PdfStyle STYLE_EXERCISE_NAME =
      new PdfStyle(Fonts.BOLD, Fonts.SIZE_EXERCISE_NAME, Colors.TEXT_DARK);
  private static final PdfStyle STYLE_EXERCISE_DESC =
      new PdfStyle(Fonts.REGULAR, Fonts.SIZE_EXERCISE_DESC, Colors.TEXT_LIGHT);
  private static final PdfStyle STYLE_EXERCISE_DETAILS =
      new PdfStyle(Fonts.REGULAR, Fonts.SIZE_EXERCISE_DETAILS, Colors.TEXT_LIGHTER);
  private static final PdfStyle STYLE_PLACEHOLDER =
      new PdfStyle(Fonts.ITALIC, Fonts.SIZE_PLACEHOLDER, Colors.TEXT_LIGHT);
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

    contentStream.setStrokingColor(Colors.SEPARATOR);
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

  private void writeUnit(TrainingUnit unit) throws IOException {
    String unitName = Objects.toString(unit.getName(), DEFAULT_UNIT_NAME);
    String weekday = Objects.toString(unit.getWeekday(), DEFAULT_WEEKDAY);
    String headerText = String.format("%s (%s)", unitName, weekday);

    writeStyledWrappedText(headerText, Layout.INDENT_UNIT_LEVEL, STYLE_UNIT_HEADER);
    addSpacing(Layout.SPACING_AFTER_UNIT_HEADER);

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
          PLACEHOLDER_NO_EXERCISES, Layout.INDENT_EXERCISE_CONTAINER, STYLE_PLACEHOLDER);
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

  private void writeExercise(TrainingExercise exercise) throws IOException {
    String exerciseName = Objects.toString(exercise.getName(), DEFAULT_EXERCISE_NAME);
    writeStyledWrappedText(exerciseName, Layout.INDENT_EXERCISE_BLOCK, STYLE_EXERCISE_NAME);
    addSpacing(Layout.SPACING_AFTER_EXERCISE_NAME);

    String description = exercise.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      writeStyledWrappedText(
          description,
          Layout.INDENT_EXERCISE_CONTENT,
          STYLE_EXERCISE_DESC,
          Layout.EXTRA_LINE_SPACING);
      addSpacing(Layout.SPACING_AFTER_EXERCISE_DESC);
    }

    String duration = Objects.toString(exercise.getDuration(), DEFAULT_DURATION);
    String details =
        String.format(
            "Duration: %s  •  Sets: %d  •  Ball Bucket: %s",
            duration, exercise.getSets(), (exercise.isBallBucket() ? "Yes" : "No"));
    writeStyledWrappedText(details, Layout.INDENT_EXERCISE_CONTENT, STYLE_EXERCISE_DETAILS);
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

    float availableWidth = Layout.CONTENT_WIDTH - indent;
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

  private static class Layout {
    static final float MARGIN = 50;
    static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    static final float BASE_LINE_SPACING_FACTOR = 1.3f;
    static final float EXTRA_LINE_SPACING = 1.5f; // Additional spacing for description text lines

    static final float INDENT_UNIT_LEVEL = 0;
    static final float INDENT_EXERCISE_CONTAINER = 15;
    static final float INDENT_EXERCISE_BLOCK = INDENT_EXERCISE_CONTAINER;
    static final float INDENT_EXERCISE_CONTENT = INDENT_EXERCISE_CONTAINER;

    static final float SPACING_AFTER_TITLE = 8;
    static final float SPACING_AFTER_PREVIEW_DESC = 12;
    static final float SPACING_SEPARATOR = 10; // Total space around separator
    static final float SPACING_BETWEEN_UNITS = 15;
    static final float SPACING_AFTER_UNIT_HEADER = 6;
    static final float SPACING_AFTER_UNIT_DESC = 10;
    static final float SPACING_BEFORE_EXERCISES = 8;
    static final float SPACING_BETWEEN_EXERCISES = 12;
    static final float SPACING_AFTER_EXERCISE_NAME = 2;
    static final float SPACING_AFTER_EXERCISE_DESC = 5;
  }

  private static class Fonts {
    static final PDFont BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    static final PDFont REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    static final PDFont ITALIC = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    static final float SIZE_PREVIEW_TITLE = 19;
    static final float SIZE_PREVIEW_DESC = 11;
    static final float SIZE_UNIT_HEADER = 15;
    static final float SIZE_UNIT_DESC = 11;
    static final float SIZE_EXERCISES_TITLE = 12;
    static final float SIZE_EXERCISE_NAME = 12;
    static final float SIZE_EXERCISE_DESC = 10.5f;
    static final float SIZE_EXERCISE_DETAILS = 10;
    static final float SIZE_PLACEHOLDER = 10;
  }

  private static class Colors {
    static final Color TEXT_DARKEST = hexToColor("#111111");
    static final Color TEXT_DARKER = hexToColor("#1A1A1A");
    static final Color TEXT_DARK = hexToColor("#222222");
    static final Color TEXT_MEDIUM = hexToColor("#555555");
    static final Color TEXT_LIGHT = hexToColor("#666666");
    static final Color TEXT_LIGHTER = hexToColor("#777777");
    static final Color SEPARATOR = hexToColor("#E0E0E0");

    static Color hexToColor(String hex) {
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

  private record PdfStyle(PDFont font, float size, Color color) {}
}
