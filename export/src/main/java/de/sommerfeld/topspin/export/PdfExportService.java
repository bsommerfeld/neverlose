package de.sommerfeld.topspin.export;

import de.sommerfeld.topspin.plan.TrainingPlan;
import de.sommerfeld.topspin.plan.components.TrainingExercise;
import de.sommerfeld.topspin.plan.components.TrainingUnit;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for exporting a TrainingPlan object to a PDF file using Apache PDFBox. Implements the
 * ExportService interface.
 */
public class PdfExportService implements ExportService {

    // --- Layout Constants ---
    private static final float MARGIN = 50;
    private static final float LEADING_FACTOR = 1.4f;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    // --- Font Sizes (approximating relative sizes from CSS preview) ---
    private static final float FONT_SIZE_TITLE = 15;
    private static final float FONT_SIZE_UNIT_HEADER = 13;
    private static final float FONT_SIZE_EXERCISE_HEADER = 11; // e.g., "Exercises:" title if used
    private static final float FONT_SIZE_EXERCISE_NAME = 10; // Base size, but bold
    private static final float FONT_SIZE_TEXT = 10;
    private static final float FONT_SIZE_DESC = 10;
    private static final float FONT_SIZE_DETAILS = 9;

    // --- Indentation (approximating visual structure) ---
    private static final float INDENT_UNIT = 0;
    private static final float INDENT_EXERCISE_CONTAINER = 15;
    private static final float INDENT_EXERCISE = INDENT_EXERCISE_CONTAINER;
    private static final float INDENT_EXERCISE_CHILD = INDENT_EXERCISE_CONTAINER + 10; // Details/Desc

    // --- Reusable Standard Fonts ---
    private static final PDFont FONT_TITLE = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont FONT_UNIT_HEADER = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont FONT_EXERCISE_HEADER = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont FONT_EXERCISE_NAME = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont FONT_TEXT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_TEXT_ITALIC = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    // --- State variables for page generation (instance fields) ---
    private PDDocument document;
    private PDPageContentStream contentStream;
    private PDPage currentPage;
    private float currentY;

    @Override
    public void export(TrainingPlan trainingPlan, File targetFile) { // Added throws IOException
        // --- Input Validation ---
        if (trainingPlan == null) {
            throw new IllegalArgumentException("Training plan cannot be null.");
        }

        // --- PDF Generation ---
        try (PDDocument doc = new PDDocument()) {
            this.document = doc;
            startNewPage(); // Initialize first page

            // --- Write Plan Header ---
            writeTextLine(trainingPlan.getName(), MARGIN + INDENT_UNIT, FONT_TITLE, FONT_SIZE_TITLE, CONTENT_WIDTH);
            addSpacing(FONT_SIZE_TITLE * (LEADING_FACTOR - 1.0f) * 0.5f);

            if (trainingPlan.getDescription() != null && !trainingPlan.getDescription().trim().isEmpty()) {
                writeWrappedText(trainingPlan.getDescription(), MARGIN + INDENT_UNIT, CONTENT_WIDTH, FONT_TEXT, FONT_SIZE_DESC);
            }
            addSpacing(FONT_SIZE_DESC * LEADING_FACTOR); // Space after description

            // --- Draw Separator ---
            drawSeparator();
            addSpacing(FONT_SIZE_UNIT_HEADER * LEADING_FACTOR * 0.8f); // Space after separator

            // --- Write Units and Exercises ---
            if (trainingPlan.getTrainingUnits() != null && !trainingPlan.getTrainingUnits().isEmpty()) {
                boolean firstUnit = true;
                for (TrainingUnit unit : trainingPlan.getTrainingUnits().getAll()) {
                    if (!firstUnit) {
                        addSpacing(FONT_SIZE_UNIT_HEADER * LEADING_FACTOR * 1.2f); // Space between units
                    }
                    firstUnit = false;

                    // Unit Header (Unit Name + Weekday)
                    String unitHeaderText = String.format("%s (%s)",
                            Objects.toString(unit.getName(), "[Unnamed Unit]"),
                            Objects.toString(unit.getWeekday(), "-"));
                    writeWrappedText(unitHeaderText, MARGIN + INDENT_UNIT, CONTENT_WIDTH, FONT_UNIT_HEADER, FONT_SIZE_UNIT_HEADER);
                    addSpacing(FONT_SIZE_UNIT_HEADER * (LEADING_FACTOR - 1.0f) * 0.5f);

                    // Exercises
                    if (unit.getTrainingExercises() != null && !unit.getTrainingExercises().isEmpty()) {
                        addSpacing(FONT_SIZE_TEXT * LEADING_FACTOR * 0.6f); // Space before exercises start

                        for (TrainingExercise exercise : unit.getTrainingExercises().getAll()) {
                            addSpacing(FONT_SIZE_TEXT * LEADING_FACTOR * 0.5f); // Space between exercises

                            // Exercise Name
                            writeWrappedText(Objects.toString(exercise.getName(), "[Unnamed Exercise]"),
                                    MARGIN + INDENT_EXERCISE, CONTENT_WIDTH - INDENT_EXERCISE, FONT_EXERCISE_NAME, FONT_SIZE_EXERCISE_NAME);
                            addSpacing(FONT_SIZE_EXERCISE_NAME * (LEADING_FACTOR - 1.0f) * 0.2f);

                            // Exercise Details
                            String details = String.format("Duration: %s  •  Sets: %d  •  Ball Bucket: %s",
                                    Objects.toString(exercise.getDuration(), "-"),
                                    exercise.getSets(),
                                    (exercise.isBallBucket() ? "Yes" : "No"));
                            writeWrappedText(details, MARGIN + INDENT_EXERCISE_CHILD, CONTENT_WIDTH - INDENT_EXERCISE_CHILD, FONT_TEXT_ITALIC, FONT_SIZE_DETAILS);
                            addSpacing(FONT_SIZE_DETAILS * (LEADING_FACTOR - 1.0f) * 0.2f);

                            // Exercise Description
                            if (exercise.getDescription() != null && !exercise.getDescription().trim().isEmpty()) {
                                writeWrappedText(exercise.getDescription(),
                                        MARGIN + INDENT_EXERCISE_CHILD, CONTENT_WIDTH - INDENT_EXERCISE_CHILD, FONT_TEXT, FONT_SIZE_DESC);
                            }
                        }
                    }
                }
            } else {
                // Handle case with no units
                addSpacing(FONT_SIZE_TEXT * LEADING_FACTOR);
                writeTextLine("[No training units defined]", MARGIN, FONT_TEXT_ITALIC, FONT_SIZE_TEXT, CONTENT_WIDTH);
            }

            // --- Finalize ---
            closeCurrentContentStream(); // Close the last stream
            document.save(targetFile); // Save to the temporary file

        } catch (IOException e) {
            // Clean up temporary file if save fails? Or let caller handle?
            // Re-throw for caller to handle.
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        } finally {
            // Clean up instance variables
            this.contentStream = null;
            this.document = null;
            this.currentPage = null;
        }
    }


    // --- Helper Methods (Copied and potentially adapted from previous version) ---

    private void startNewPage() throws IOException {
        closeCurrentContentStream();
        currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);
        contentStream = new PDPageContentStream(document, currentPage);
        currentY = currentPage.getMediaBox().getHeight() - MARGIN;
    }

    private void closeCurrentContentStream() throws IOException {
        if (contentStream != null) {
            contentStream.close();
            contentStream = null;
        }
    }

    private void addSpacing(float space) throws IOException {
        if (space <= 0) return;
        // Check if space fits BEFORE applying
        if (currentY - space < MARGIN) {
            startNewPage();
        } else {
            currentY -= space;
        }
    }

    private void drawSeparator() throws IOException {
        float separatorSpacing = 5f;
        addSpacing(separatorSpacing);

        // Re-check Y after spacing
        if (currentY <= MARGIN + separatorSpacing) {
            startNewPage();
            addSpacing(separatorSpacing); // Add space again on new page
        }

        contentStream.setStrokingColor(Color.LIGHT_GRAY);
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(MARGIN, currentY);
        contentStream.lineTo(PAGE_WIDTH - MARGIN, currentY);
        contentStream.stroke();

        addSpacing(separatorSpacing);
    }

    private void writeTextLine(String line, float x, PDFont font, float fontSize, float availableWidth) throws IOException {
        float leading = fontSize * LEADING_FACTOR;
        // Check for page break BEFORE writing
        if (currentY - leading < MARGIN) {
            startNewPage();
        }

        // Basic truncation if needed (though wrapping should handle most cases)
        float lineWidth = font.getStringWidth(line) / 1000f * fontSize;
        String lineToWrite = line;
        while (lineWidth > availableWidth && !lineToWrite.isEmpty()) {
            lineToWrite = lineToWrite.substring(0, lineToWrite.length() - 1);
            lineWidth = font.getStringWidth(lineToWrite) / 1000f * fontSize;
            // Consider adding "..." if truncated
        }
        if (lineToWrite.isEmpty()) return;

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, currentY - fontSize); // Position baseline
        contentStream.showText(lineToWrite);
        contentStream.endText();

        currentY -= leading; // Move Y down AFTER writing
    }

    private void writeWrappedText(String text, float x, float maxWidth, PDFont font, float fontSize) throws IOException {
        if (text == null || text.trim().isEmpty()) return;

        List<String> lines = new ArrayList<>();
        String remainingText = text.trim().replaceAll("\r\n", "\n"); // Normalize line breaks

        // Handle pre-existing newlines
        String[] paragraphs = remainingText.split("\n", -1);

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add(""); // Keep empty lines for paragraph spacing
                continue;
            }

            String remainingParagraph = paragraph;
            while (!remainingParagraph.isEmpty()) {
                int breakIndex = -1;
                int lastSpaceIndex = -1;

                // Find max fitting substring
                for (int i = 0; i <= remainingParagraph.length(); i++) {
                    String sub = remainingParagraph.substring(0, i);
                    float width = font.getStringWidth(sub) / 1000f * fontSize;

                    if (width > maxWidth) {
                        breakIndex = i > 0 ? i - 1 : 0; // Last fitting char index
                        break;
                    }
                    if (i > 0 && Character.isWhitespace(remainingParagraph.charAt(i - 1))) {
                        lastSpaceIndex = i - 1;
                    }
                    if (i == remainingParagraph.length()) {
                        breakIndex = i; // Fits completely
                    }
                }

                String line;
                if (breakIndex == remainingParagraph.length()) {
                    // Whole remaining paragraph fits
                    line = remainingParagraph;
                    remainingParagraph = "";
                } else if (lastSpaceIndex > 0 && breakIndex > lastSpaceIndex) {
                    // Break at last space before exceeding width
                    line = remainingParagraph.substring(0, lastSpaceIndex);
                    remainingParagraph = remainingParagraph.substring(lastSpaceIndex + 1); // Skip space
                } else {
                    // No suitable space or first word too long, force break
                    int forcedBreak = breakIndex > 0 ? breakIndex : 1; // Ensure progress even if first char too wide
                    if (breakIndex == 0 && remainingParagraph.length() > 1) { // If even 1 char > width, take 1
                        forcedBreak = 1;
                    }
                    line = remainingParagraph.substring(0, forcedBreak);
                    remainingParagraph = remainingParagraph.substring(forcedBreak);
                }
                lines.add(line.trim()); // Add calculated line
            }
        }

        // Write the calculated lines
        for (String line : lines) {
            if (line.isEmpty()) {
                // Handle paragraph spacing by adding vertical space for empty lines
                addSpacing(fontSize * LEADING_FACTOR);
            } else {
                writeTextLine(line, x, font, fontSize, maxWidth);
            }
        }
    }
}