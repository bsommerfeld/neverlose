package de.bsommerfeld.neverlose.export.pdf;

import de.bsommerfeld.neverlose.theme.Theme;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.Color;
import java.io.IOException;

/**
 * Handles container rendering operations for PDF documents.
 * This class encapsulates the logic for drawing containers, separators,
 * and other graphical elements in a PDF document.
 */
public class PdfContainerRenderer {
    private final PDPageContentStream contentStream;

    /**
     * Creates a new PdfContainerRenderer.
     *
     * @param contentStream The content stream to write to
     */
    public PdfContainerRenderer(PDPageContentStream contentStream) {
        this.contentStream = contentStream;
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
    public void drawContainer(
            float x, float y, float width, float height, float borderRadius, Color backgroundColor)
            throws IOException {
        // Save the current graphics state
        contentStream.saveGraphicsState();

        // Set the fill color to the background color
        contentStream.setNonStrokingColor(backgroundColor);

        // Set the stroke color and width for the border
        contentStream.setStrokingColor(Theme.Colors.ACCENT_SILVER);
        contentStream.setLineWidth(PdfLayout.BORDER_WIDTH);

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
                x + width, y + height, x + width, y + height, x + width - borderRadius, y + height);

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
        contentStream.curveTo(x, y, x, y, x + borderRadius, y);

        // Fill and stroke the path
        contentStream.fillAndStroke();

        // Restore the graphics state
        contentStream.restoreGraphicsState();
    }

    /**
     * Draws a horizontal separator line.
     *
     * @param y The y-coordinate of the separator
     * @return The height consumed by the separator
     * @throws IOException If there's an error drawing to the PDF
     */
    public float drawSeparator(float y) throws IOException {
        float separatorHeight = 1f;

        contentStream.setStrokingColor(Theme.Colors.SEPARATOR);
        contentStream.setLineWidth(0.75f);
        contentStream.moveTo(PdfLayout.MARGIN, y);
        contentStream.lineTo(PdfLayout.PAGE_WIDTH - PdfLayout.MARGIN, y);
        contentStream.stroke();

        return separatorHeight;
    }

    /**
     * Draws a footer at the bottom of the page.
     *
     * @param pageWidth The width of the page
     * @param style The style to use for the footer text
     * @throws IOException If there's an error drawing to the PDF
     */
    public void drawFooter(float pageWidth, PdfStyle style) throws IOException {
        String footerText = "Made with Neverlose";
        float textWidth = style.font().getStringWidth(footerText) / 1000f * style.size();
        float x = (pageWidth - textWidth) / 2; // Center horizontally
        float y = PdfLayout.MARGIN / 2; // Position at bottom of page

        contentStream.beginText();
        contentStream.setFont(style.font(), style.size());
        contentStream.setNonStrokingColor(style.color());
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(footerText);
        contentStream.endText();
    }
}