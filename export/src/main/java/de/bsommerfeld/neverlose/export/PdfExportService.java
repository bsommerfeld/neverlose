package de.bsommerfeld.neverlose.export;

import de.bsommerfeld.neverlose.export.pdf.PdfContentRenderer;
import de.bsommerfeld.neverlose.export.pdf.PdfDocumentManager;
import de.bsommerfeld.neverlose.export.pdf.PdfStyle;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import de.bsommerfeld.neverlose.theme.Theme;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a PDF representation of a TrainingPlan, aiming for a clean, modern, and readable layout suitable for screen
 * and print.
 */
public class PdfExportService implements ExportService {

    private static final Logger LOGGER = Logger.getLogger(PdfExportService.class.getName());

    // PDF Fonts - initialized safely to avoid font cache building errors
    private static PDFont FONT_BOLD;
    private static PDFont FONT_REGULAR;
    private static PDFont FONT_ITALIC;
    // Styles using the centralized Theme - initialized safely
    private static PdfStyle STYLE_PREVIEW_TITLE;
    private static PdfStyle STYLE_PREVIEW_DESC;
    private static PdfStyle STYLE_UNIT_HEADER;
    private static PdfStyle STYLE_UNIT_WEEKDAY;
    private static PdfStyle STYLE_UNIT_DESC;
    private static PdfStyle STYLE_EXERCISE_NAME;
    private static PdfStyle STYLE_EXERCISE_DESC;
    private static PdfStyle STYLE_EXERCISE_DETAILS;
    private static PdfStyle STYLE_PLACEHOLDER;
    private static PdfStyle STYLE_FOOTER;

    // Initialize fonts safely
    static {
        try {
            FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            FONT_ITALIC = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
        } catch (Exception e) {
            // Log the error but don't crash the application
            LOGGER.log(Level.WARNING, "Error initializing PDF fonts. PDF export may not work correctly.", e);

            // Set default fonts to null - we'll check for null before using them
            FONT_BOLD = null;
            FONT_REGULAR = null;
            FONT_ITALIC = null;
        }
    }

    // Initialize styles safely
    static {
        try {
            // Only initialize styles if fonts were loaded successfully
            if (FONT_BOLD != null && FONT_REGULAR != null && FONT_ITALIC != null) {
                STYLE_PREVIEW_TITLE = new PdfStyle(FONT_BOLD, Theme.Fonts.SIZE_TITLE, Theme.Colors.TEXT_DARKEST);
                STYLE_PREVIEW_DESC = new PdfStyle(FONT_REGULAR, Theme.Fonts.SIZE_SUBTITLE, Theme.Colors.TEXT_MEDIUM);
                STYLE_UNIT_HEADER = new PdfStyle(FONT_BOLD, Theme.Fonts.SIZE_UNIT_HEADER, Theme.Colors.TEXT_DARKER);
                STYLE_UNIT_WEEKDAY = new PdfStyle(FONT_REGULAR, Theme.Fonts.SIZE_UNIT_WEEKDAY, Theme.Colors.TEXT_LIGHTER);
                STYLE_UNIT_DESC = new PdfStyle(FONT_REGULAR, Theme.Fonts.SIZE_UNIT_DESC, Theme.Colors.TEXT_LIGHT);
                STYLE_EXERCISE_NAME = new PdfStyle(FONT_BOLD, Theme.Fonts.SIZE_EXERCISE_NAME, Theme.Colors.TEXT_DARK);
                STYLE_EXERCISE_DESC = new PdfStyle(FONT_REGULAR, Theme.Fonts.SIZE_EXERCISE_DESC, Theme.Colors.TEXT_LIGHT);
                STYLE_EXERCISE_DETAILS = new PdfStyle(FONT_REGULAR, Theme.Fonts.SIZE_EXERCISE_DETAILS, Theme.Colors.TEXT_LIGHTER);
                STYLE_PLACEHOLDER = new PdfStyle(FONT_ITALIC, Theme.Fonts.SIZE_PLACEHOLDER, Theme.Colors.TEXT_LIGHT);
                STYLE_FOOTER = new PdfStyle(FONT_REGULAR, 8f, Theme.Colors.TEXT_LIGHTER);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error initializing PDF styles. PDF export may not work correctly.", e);
        }
    }

    @Override
    public void export(TrainingPlan trainingPlan, File targetFile) throws IOException {
        Objects.requireNonNull(trainingPlan, "Training plan cannot be null.");
        Objects.requireNonNull(targetFile, "Target file cannot be null.");

        // Check if fonts and styles were initialized properly
        if (FONT_BOLD == null || FONT_REGULAR == null || FONT_ITALIC == null ||
                STYLE_PREVIEW_TITLE == null || STYLE_PREVIEW_DESC == null ||
                STYLE_UNIT_HEADER == null || STYLE_UNIT_WEEKDAY == null ||
                STYLE_UNIT_DESC == null || STYLE_EXERCISE_NAME == null ||
                STYLE_EXERCISE_DESC == null || STYLE_EXERCISE_DETAILS == null ||
                STYLE_PLACEHOLDER == null || STYLE_FOOTER == null) {

            LOGGER.log(Level.SEVERE, "Cannot export PDF: fonts or styles were not initialized properly.");
            throw new IOException("PDF export is not available due to font initialization issues.");
        }

        try (PDDocument doc = new PDDocument()) {
            PdfDocumentManager documentManager = new PdfDocumentManager(doc, STYLE_FOOTER);

            PdfContentRenderer contentRenderer = new PdfContentRenderer(
                    documentManager,
                    STYLE_PREVIEW_TITLE,
                    STYLE_PREVIEW_DESC,
                    STYLE_UNIT_HEADER,
                    STYLE_UNIT_WEEKDAY,
                    STYLE_UNIT_DESC,
                    STYLE_EXERCISE_NAME,
                    STYLE_EXERCISE_DESC,
                    STYLE_EXERCISE_DETAILS,
                    STYLE_PLACEHOLDER);

            contentRenderer.renderTrainingPlan(trainingPlan);
            documentManager.saveAndClose(targetFile);
        }
    }


}
