package de.bsommerfeld.neverlose.export.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;

/**
 * Manages PDF document operations such as page creation and content stream management.
 * This class encapsulates the logic for creating and managing pages in a PDF document.
 */
public class PdfDocumentManager {
    private final PDDocument document;
    private PDPage currentPage;
    private PDPageContentStream contentStream;
    private PdfTextRenderer textRenderer;
    private PdfContainerRenderer containerRenderer;
    private final PdfStyle footerStyle;

    /**
     * Creates a new PdfDocumentManager.
     *
     * @param document The PDF document to manage
     * @param footerStyle The style to use for the footer text
     */
    public PdfDocumentManager(PDDocument document, PdfStyle footerStyle) {
        this.document = document;
        this.footerStyle = footerStyle;
    }

    /**
     * Gets the current page.
     *
     * @return The current page
     */
    public PDPage getCurrentPage() {
        return currentPage;
    }

    /**
     * Gets the current content stream.
     *
     * @return The current content stream
     */
    public PDPageContentStream getContentStream() {
        return contentStream;
    }

    /**
     * Gets the text renderer for the current page.
     *
     * @return The text renderer
     */
    public PdfTextRenderer getTextRenderer() {
        return textRenderer;
    }

    /**
     * Gets the container renderer for the current page.
     *
     * @return The container renderer
     */
    public PdfContainerRenderer getContainerRenderer() {
        return containerRenderer;
    }

    /**
     * Creates a new page and initializes the content stream and renderers.
     *
     * @return The Y position at the top of the new page
     * @throws IOException If there's an error creating the page
     */
    public float startNewPage() throws IOException {
        closeCurrentContentStream();
        currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);
        contentStream = new PDPageContentStream(document, currentPage);
        
        float startY = currentPage.getMediaBox().getHeight() - PdfLayout.MARGIN;
        textRenderer = new PdfTextRenderer(contentStream, startY);
        containerRenderer = new PdfContainerRenderer(contentStream);
        
        return startY;
    }

    /**
     * Closes the current content stream after writing the footer.
     *
     * @throws IOException If there's an error closing the stream
     */
    public void closeCurrentContentStream() throws IOException {
        if (contentStream != null) {
            if (currentPage != null && containerRenderer != null) {
                containerRenderer.drawFooter(currentPage.getMediaBox().getWidth(), footerStyle);
            }
            contentStream.close();
            contentStream = null;
        }
    }

    /**
     * Saves the document to the specified file and closes all resources.
     *
     * @param targetFile The file to save to
     * @throws IOException If there's an error saving the document
     */
    public void saveAndClose(java.io.File targetFile) throws IOException {
        closeCurrentContentStream();
        document.save(targetFile);
    }
}