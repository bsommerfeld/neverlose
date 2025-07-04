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

/**
 * Generates a PDF representation of a TrainingPlan, aiming for a clean, modern, and readable layout
 * suitable for screen and print.
 */
public class PdfExportService implements ExportService {

  // PDF Fonts
  private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
  private static final PDFont FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
  private static final PDFont FONT_ITALIC =
      new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

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
  private static final PdfStyle STYLE_FOOTER =
      new PdfStyle(FONT_REGULAR, 8f, Theme.Colors.TEXT_LIGHTER);

  @Override
  public void export(TrainingPlan trainingPlan, File targetFile) throws IOException {
    Objects.requireNonNull(trainingPlan, "Training plan cannot be null.");
    Objects.requireNonNull(targetFile, "Target file cannot be null.");

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
