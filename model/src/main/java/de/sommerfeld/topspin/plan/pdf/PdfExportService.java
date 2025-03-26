package de.sommerfeld.topspin.plan.pdf;

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

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Service responsible for exporting a TrainingPlan object to a PDF file using Apache PDFBox. Note: Typically, services
 * belong in their own 'service' package.
 */
public class PdfExportService {

    // --- Layout Constants ---
    private static final float MARGIN = 50; // Page margin on all sides
    private static final float LEADING_FACTOR = 1.5f; // Line spacing multiplier (e.g., 1.5 for 1.5x line height)

    // --- Font Sizes ---
    private static final float FONT_SIZE_TITLE = 18;
    private static final float FONT_SIZE_HEADING = 14;
    private static final float FONT_SIZE_TEXT = 10;
    private static final float FONT_SIZE_SMALL_TEXT = 9;

    // --- Indentation ---
    private static final float INDENT_UNIT = 0; // Relative to margin
    private static final float INDENT_EXERCISE = 20;
    private static final float INDENT_DETAILS = 30;
    private static final float INDENT_DESCRIPTION = 30;

    // --- Reusable Standard Fonts ---
    private static final PDFont FONT_TITLE = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont FONT_HEADING = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    // Let's use a slightly different bold font for text for variety, though HELVETICA_BOLD is fine
    private static final PDFont FONT_TEXT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont FONT_TEXT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    // --- State variables for page generation ---
    private PDDocument document;
    private PDPageContentStream contentStream;
    private PDPage currentPage;
    private float currentY; // Current Y position for writing (starts high, moves down)

    /**
     * Exports the given TrainingPlan to a PDF file at the specified location.
     *
     * @param plan The TrainingPlan to export. Must not be null.
     * @param file The File where the PDF should be saved. Must not be null.
     *
     * @throws IOException              If an error occurs during PDF generation or file writing.
     * @throws IllegalArgumentException If plan or file is null.
     */
    public void exportPlan(TrainingPlan plan, File file) throws IOException {
        // --- Input Validation ---
        if (plan == null) {
            throw new IllegalArgumentException("Training plan cannot be null.");
        }
        if (file == null) {
            throw new IllegalArgumentException("Output file cannot be null.");
        }

        // --- PDF Generation ---
        try (PDDocument doc = new PDDocument()) {
            this.document = doc;
            startNewPage();

            // --- Write Plan Header ---
            writeTextLine(plan.getName(), MARGIN + INDENT_UNIT, FONT_TITLE, FONT_SIZE_TITLE);
            if (plan.getDescription() != null && !plan.getDescription().trim().isEmpty()) {
                // Note: No line wrapping implemented for description, assumes reasonable length
                writeTextLine(plan.getDescription(), MARGIN + INDENT_UNIT, FONT_TEXT, FONT_SIZE_TEXT);
            }
            addSpacing(FONT_SIZE_HEADING * LEADING_FACTOR);

            // --- Write Units and Exercises ---
            if (plan.getTrainingUnits() != null) {
                for (TrainingUnit unit : plan.getTrainingUnits().getAll()) {
                    addSpacing(FONT_SIZE_TEXT); // Space before unit heading
                    writeTextLine("Unit: " + Objects.toString(unit.getName(), "[Unnamed Unit]"),
                            MARGIN + INDENT_UNIT, FONT_HEADING, FONT_SIZE_HEADING);

                    if (unit.getTrainingExercises() != null) {
                        for (TrainingExercise exercise : unit.getTrainingExercises().getAll()) {
                            addSpacing(FONT_SIZE_TEXT / 2f); // Little space before exercise name
                            writeTextLine(Objects.toString(exercise.getName(), "[Unnamed Exercise]"),
                                    MARGIN + INDENT_EXERCISE, FONT_TEXT_BOLD, FONT_SIZE_TEXT);

                            // Format exercise details
                            String details = String.format("Duration: %s  •  Sets: %d  •  Ball Bucket: %s",
                                    Objects.toString(exercise.getDuration(), "-"),
                                    exercise.getSets(),
                                    (exercise.isBallBucket() ? "Yes" : "No"));
                            writeTextLine(details, MARGIN + INDENT_DETAILS, FONT_TEXT, FONT_SIZE_SMALL_TEXT);

                            // Write exercise description if present
                            if (exercise.getDescription() != null && !exercise.getDescription().trim().isEmpty()) {
                                // Note: No line wrapping implemented here. Long descriptions might overflow.
                                writeTextLine("Desc: " + exercise.getDescription(),
                                        MARGIN + INDENT_DESCRIPTION, FONT_TEXT, FONT_SIZE_SMALL_TEXT);
                            }
                        }
                    }
                    addSpacing(FONT_SIZE_TEXT);
                }
            }

            closeCurrentContentStream();
            document.save(file);

        } finally {
            // Clean up state variables, although try-with-resources handles the document
            this.contentStream = null;
            this.document = null;
            this.currentPage = null;
        }
    }

    /**
     * Closes the current content stream if it's open and starts a new A4 page. Initializes the content stream and
     * resets the current Y position.
     *
     * @throws IOException If an error occurs closing the stream or creating the new page/stream.
     */
    private void startNewPage() throws IOException {
        closeCurrentContentStream(); // Ensure previous stream is closed

        currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);
        contentStream = new PDPageContentStream(document, currentPage);
        // Reset Y position to top margin for the new page
        currentY = currentPage.getMediaBox().getHeight() - MARGIN;
    }

    /**
     * Closes the current PDPageContentStream if it is not null.
     *
     * @throws IOException If an error occurs during closing.
     */
    private void closeCurrentContentStream() throws IOException {
        if (contentStream != null) {
            contentStream.close();
            contentStream = null; // Ensure it's marked as closed
        }
    }

    /**
     * Adds vertical spacing, potentially triggering a page break if needed.
     *
     * @param space The amount of vertical space to add (moves currentY down).
     *
     * @throws IOException If starting a new page fails.
     */
    private void addSpacing(float space) throws IOException {
        if (currentY - space < MARGIN) {
            startNewPage(); // Not enough space, create a new page first
        } else {
            currentY -= space; // Enough space, just move the Y position down
        }
    }

    /**
     * Writes a single line of text at the specified X position and the current Y position, then moves the Y position
     * down. Handles page breaks if the line doesn't fit. Does nothing if the text is null or effectively empty.
     *
     * @param text     The text to write.
     * @param x        The X coordinate (horizontal position) relative to the left edge.
     * @param font     The PDFont to use.
     * @param fontSize The font size.
     *
     * @throws IOException If writing text or starting a new page fails.
     */
    private void writeTextLine(String text, float x, PDFont font, float fontSize) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        float leading = LEADING_FACTOR * fontSize; // Calculate line spacing

        // Check if the line fits on the current page based on leading
        if (currentY - leading < MARGIN) {
            startNewPage(); // Not enough space, trigger new page
        }

        // --- Write the text ---
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        // PDFBox's coordinate system starts at the bottom-left (0,0)
        // newLineAtOffset positions the start of the *next* line, so we set it based on currentY
        contentStream.newLineAtOffset(x, currentY - fontSize); // Adjust Y for font baseline
        contentStream.showText(text);
        contentStream.endText();
        // --- Move Y position down for the next line ---
        currentY -= leading;
    }
}
