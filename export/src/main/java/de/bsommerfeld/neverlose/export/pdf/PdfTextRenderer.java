package de.bsommerfeld.neverlose.export.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Handles text rendering operations for PDF documents. This class encapsulates the logic for
 * calculating text dimensions, wrapping text, and rendering styled text to a PDF document.
 */
public class PdfTextRenderer {
  private final PDPageContentStream contentStream;
  private float currentY;

  /**
   * Creates a new PdfTextRenderer.
   *
   * @param contentStream The content stream to write to
   * @param startY The starting Y position for text rendering
   */
  public PdfTextRenderer(PDPageContentStream contentStream, float startY) {
    this.contentStream = contentStream;
    this.currentY = startY;
  }

  /**
   * Gets the current Y position.
   *
   * @return The current Y position
   */
  public float getCurrentY() {
    return currentY;
  }

  /**
   * Sets the current Y position.
   *
   * @param y The new Y position
   */
  public void setCurrentY(float y) {
    this.currentY = y;
  }

  /**
   * Adds vertical spacing.
   *
   * @param space The amount of space to add
   * @return true if spacing was added, false if a new page is needed
   */
  public boolean addSpacing(float space) {
    if (space <= 0) return true;
    if (currentY - space < PdfLayout.MARGIN) {
      return false; // Need new page
    } else {
      currentY -= space;
      return true;
    }
  }

  /**
   * Writes a single line of styled text.
   *
   * @param line The text to write
   * @param indent The indentation from the left margin
   * @param style The style to apply
   * @param extraLeading Extra leading to add after the line
   * @return true if text was written, false if a new page is needed
   * @throws IOException If there's an error writing to the PDF
   */
  public boolean writeStyledTextLine(String line, float indent, PdfStyle style, float extraLeading)
      throws IOException {
    float x = PdfLayout.MARGIN + indent;
    float availableWidth = PdfLayout.CONTENT_WIDTH - indent;
    float baseLeading = style.size() * PdfLayout.BASE_LINE_SPACING_FACTOR;
    float totalLeading = baseLeading + extraLeading;

    if (currentY - totalLeading < PdfLayout.MARGIN) {
      return false; // Need new page
    }

    float lineWidth = calculateTextWidth(line, style.font(), style.size());
    String lineToWrite = line;
    if (lineWidth > availableWidth) {
      lineToWrite = truncateText(line, style.font(), style.size(), availableWidth);
    }
    if (lineToWrite.isEmpty()) return true;

    contentStream.beginText();
    contentStream.setFont(style.font(), style.size());
    contentStream.setNonStrokingColor(style.color());
    contentStream.newLineAtOffset(x, currentY - style.size());
    contentStream.showText(lineToWrite);
    contentStream.endText();

    currentY -= totalLeading;
    return true;
  }

  /**
   * Writes a single line of styled text with default leading.
   *
   * @param line The text to write
   * @param indent The indentation from the left margin
   * @param style The style to apply
   * @return true if text was written, false if a new page is needed
   * @throws IOException If there's an error writing to the PDF
   */
  public boolean writeStyledTextLine(String line, float indent, PdfStyle style) throws IOException {
    return writeStyledTextLine(line, indent, style, 0f);
  }

  /**
   * Writes wrapped text with the specified style.
   *
   * @param text The text to write
   * @param indent The indentation from the left margin
   * @param style The style to apply
   * @param extraLeadingPerLine Extra leading to add after each line
   * @return true if all text was written, false if a new page is needed
   * @throws IOException If there's an error writing to the PDF
   */
  public boolean writeStyledWrappedText(
      String text, float indent, PdfStyle style, float extraLeadingPerLine) throws IOException {
    if (text == null || text.trim().isEmpty()) return true;

    float availableWidth;
    // Use double indentation for exercise content to be consistent with calculateExerciseHeight
    if (indent == PdfLayout.INDENT_EXERCISE_BLOCK || indent == PdfLayout.INDENT_EXERCISE_CONTENT) {
      availableWidth = PdfLayout.CONTENT_WIDTH - (2 * indent);
    } else if (indent == PdfLayout.INDENT_EXERCISE_BLOCK + PdfLayout.INDENT_EXERCISE_INTERNAL
        || indent == PdfLayout.INDENT_EXERCISE_CONTENT + PdfLayout.INDENT_EXERCISE_INTERNAL) {
      // For exercise content with internal padding
      availableWidth =
          PdfLayout.CONTENT_WIDTH
              - (2 * (indent - PdfLayout.INDENT_EXERCISE_INTERNAL))
              - (2 * PdfLayout.INDENT_EXERCISE_INTERNAL);
    } else {
      availableWidth = PdfLayout.CONTENT_WIDTH - indent;
    }

    List<String> lines = wrapText(text, style.font(), style.size(), availableWidth);

    for (String line : lines) {
      if (line.isEmpty()) {
        if (!addSpacing(style.size() * PdfLayout.BASE_LINE_SPACING_FACTOR + extraLeadingPerLine)) {
          return false; // Need new page
        }
      } else {
        if (!writeStyledTextLine(line, indent, style, extraLeadingPerLine)) {
          return false; // Need new page
        }
      }
    }
    return true;
  }

  /**
   * Writes wrapped text with the specified style and default leading.
   *
   * @param text The text to write
   * @param indent The indentation from the left margin
   * @param style The style to apply
   * @return true if all text was written, false if a new page is needed
   * @throws IOException If there's an error writing to the PDF
   */
  public boolean writeStyledWrappedText(String text, float indent, PdfStyle style)
      throws IOException {
    return writeStyledWrappedText(text, indent, style, 0f);
  }

  /**
   * Calculates the width of text with the specified font and size.
   *
   * @param text The text to measure
   * @param font The font to use
   * @param fontSize The font size
   * @return The width of the text in points
   * @throws IOException If there's an error calculating the width
   */
  public float calculateTextWidth(String text, PDFont font, float fontSize) throws IOException {
    return font.getStringWidth(text) / 1000f * fontSize;
  }

  /**
   * Truncates text to fit within the specified width.
   *
   * @param text The text to truncate
   * @param font The font to use
   * @param fontSize The font size
   * @param maxWidth The maximum width
   * @return The truncated text
   * @throws IOException If there's an error calculating the width
   */
  public String truncateText(String text, PDFont font, float fontSize, float maxWidth)
      throws IOException {
    String result = text;
    float width = calculateTextWidth(result, font, fontSize);
    while (width > maxWidth && !result.isEmpty()) {
      result = result.substring(0, result.length() - 1);
      width = calculateTextWidth(result, font, fontSize);
    }
    return result;
  }

  /**
   * Wraps text to fit within the specified width.
   *
   * @param text The text to wrap
   * @param font The font to use
   * @param fontSize The font size
   * @param maxWidth The maximum width
   * @return A list of wrapped lines
   * @throws IOException If there's an error calculating the width
   */
  public List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth)
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

  /**
   * Finds the index at which to break text to fit within the specified width.
   *
   * @param text The text to break
   * @param font The font to use
   * @param fontSize The font size
   * @param maxWidth The maximum width
   * @return The index at which to break the text
   * @throws IOException If there's an error calculating the width
   */
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
}
