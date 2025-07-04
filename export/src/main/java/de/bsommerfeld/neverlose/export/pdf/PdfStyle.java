package de.bsommerfeld.neverlose.export.pdf;

import java.awt.Color;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Represents a text style in a PDF document, combining font, size, and color.
 * This is used to maintain consistent styling throughout the PDF.
 */
public record PdfStyle(PDFont font, float size, Color color) {}