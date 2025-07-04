package de.bsommerfeld.neverlose.export.pdf;

import de.bsommerfeld.neverlose.plan.TrainingPlan;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import de.bsommerfeld.neverlose.theme.Theme;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Handles rendering of training plan content to PDF. This class encapsulates the logic for
 * rendering training plans, units, and exercises.
 */
public class PdfContentRenderer {
  // Default values for empty fields
  private static final String DEFAULT_PLAN_NAME = "[Unnamed Plan]";
  private static final String DEFAULT_UNIT_NAME = "[Unnamed Unit]";
  private static final String DEFAULT_EXERCISE_NAME = "[Unnamed Exercise]";
  private static final String DEFAULT_WEEKDAY = "-";
  private static final String DEFAULT_DURATION = "-";
  private static final String PLACEHOLDER_NO_UNITS = "[No training units defined]";
  private static final String PLACEHOLDER_NO_EXERCISES = "[No exercises in this unit]";

  // Styles
  private final PdfStyle stylePlanTitle;
  private final PdfStyle stylePlanDesc;
  private final PdfStyle styleUnitHeader;
  private final PdfStyle styleUnitWeekday;
  private final PdfStyle styleUnitDesc;
  private final PdfStyle styleExerciseName;
  private final PdfStyle styleExerciseDesc;
  private final PdfStyle styleExerciseDetails;
  private final PdfStyle stylePlaceholder;

  private final PdfDocumentManager documentManager;
  private PdfTextRenderer textRenderer; // Keep a reference to the current text renderer

  public PdfContentRenderer(
      PdfDocumentManager documentManager,
      PdfStyle stylePlanTitle,
      PdfStyle stylePlanDesc,
      PdfStyle styleUnitHeader,
      PdfStyle styleUnitWeekday,
      PdfStyle styleUnitDesc,
      PdfStyle styleExerciseName,
      PdfStyle styleExerciseDesc,
      PdfStyle styleExerciseDetails,
      PdfStyle stylePlaceholder) {
    this.documentManager = documentManager;
    this.textRenderer = documentManager.getTextRenderer(); // Initialize
    this.stylePlanTitle = stylePlanTitle;
    this.stylePlanDesc = stylePlanDesc;
    this.styleUnitHeader = styleUnitHeader;
    this.styleUnitWeekday = styleUnitWeekday;
    this.styleUnitDesc = styleUnitDesc;
    this.styleExerciseName = styleExerciseName;
    this.styleExerciseDesc = styleExerciseDesc;
    this.styleExerciseDetails = styleExerciseDetails;
    this.stylePlaceholder = stylePlaceholder;
  }

  public void renderTrainingPlan(TrainingPlan plan) throws IOException {
    documentManager.startNewPage();
    this.textRenderer = documentManager.getTextRenderer(); // Reset renderer on new page
    renderPlanHeader(plan);
    renderSeparator();
    renderUnitsSection(plan);

    // No need for extra spacing at the end, handled by margins.
  }

  private void renderPlanHeader(TrainingPlan plan) throws IOException {
    String planName = Objects.toString(plan.getName(), DEFAULT_PLAN_NAME);
    textRenderer.writeStyledWrappedText(planName, PdfLayout.INDENT_UNIT_LEVEL, stylePlanTitle);
    textRenderer.addSpacing(PdfLayout.SPACING_AFTER_TITLE);

    String description = plan.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      textRenderer.writeStyledWrappedText(
          description, PdfLayout.INDENT_UNIT_LEVEL, stylePlanDesc, PdfLayout.EXTRA_LINE_SPACING);
      textRenderer.addSpacing(PdfLayout.SPACING_AFTER_PREVIEW_DESC);
    }
  }

  /**
   * Checks if a page break is needed for a unit's content. If so, it creates a new page and draws
   * the continuing unit background on it before content is added.
   *
   * @param neededHeight The height of the content that needs to be rendered next.
   * @throws IOException if writing to the PDF fails.
   */
  private void handleUnitPageBreak(float neededHeight) throws IOException {
    if (textRenderer.getCurrentY() - neededHeight < PdfLayout.MARGIN) {
      // Seitenumbruch ist nötig.
      float newPageY = documentManager.startNewPage();
      this.textRenderer = documentManager.getTextRenderer();

      // Zeichne den Hintergrund auf der kompletten neuen Seite.
      documentManager
          .getContainerRenderer()
          .drawContainer(
              PdfLayout.MARGIN,
              PdfLayout.MARGIN,
              PdfLayout.CONTENT_WIDTH,
              newPageY - PdfLayout.MARGIN, // Füllt die gesamte neue Seite
              PdfLayout.UNIT_BORDER_RADIUS,
              Theme.Colors.TRAINING_UNIT_BG);

      // Füge einen Innenabstand oben auf der neuen Seite hinzu.
      textRenderer.addSpacing(PdfLayout.PADDING_UNIT_VERTICAL);
    }
  }

  private void renderSeparator() throws IOException {
    float separatorHeight = 1f;
    float totalSpaceNeeded = PdfLayout.SPACING_SEPARATOR + separatorHeight;
    if (textRenderer.getCurrentY() - totalSpaceNeeded < PdfLayout.MARGIN) {
      documentManager.startNewPage();
      this.textRenderer = documentManager.getTextRenderer();
    }
    textRenderer.addSpacing(PdfLayout.SPACING_SEPARATOR / 2);
    documentManager.getContainerRenderer().drawSeparator(textRenderer.getCurrentY());
    textRenderer.setCurrentY(textRenderer.getCurrentY() - separatorHeight);
    textRenderer.addSpacing(PdfLayout.SPACING_SEPARATOR / 2);
  }

  private void renderUnitsSection(TrainingPlan plan) throws IOException {
    List<TrainingUnit> units =
        plan.getTrainingUnits() != null ? plan.getTrainingUnits().getAll() : List.of();

    if (units.isEmpty()) {
      textRenderer.addSpacing(PdfLayout.SPACING_BETWEEN_UNITS);
      textRenderer.writeStyledTextLine(
          PLACEHOLDER_NO_UNITS, PdfLayout.INDENT_UNIT_LEVEL, stylePlaceholder);
      return;
    }

    boolean firstUnit = true;
    for (TrainingUnit unit : units) {
      if (!firstUnit) {

        float minHeightForNextUnit = calculateMinimumUnitHeight(unit);

        if (textRenderer.getCurrentY() - (PdfLayout.SPACING_BETWEEN_UNITS + minHeightForNextUnit)
            < PdfLayout.MARGIN) {
          documentManager.startNewPage();
          this.textRenderer = documentManager.getTextRenderer();
        } else {
          textRenderer.addSpacing(PdfLayout.SPACING_BETWEEN_UNITS);
        }
      }

      renderUnit(unit);
      firstUnit = false;
    }
  }

  /**
   * Calculates the minimum height required to start rendering a unit in a visually appealing way.
   * This is defined as the height of the unit's header plus its first exercise. This prevents the
   * unit header from being orphaned at the bottom of a page.
   *
   * @param unit The training unit to measure.
   * @return The minimum required height in points.
   * @throws IOException If there is an error calculating text dimensions.
   */
  private float calculateMinimumUnitHeight(TrainingUnit unit) throws IOException {
    float minHeight = calculateUnitHeaderHeight(unit);

    List<TrainingExercise> exercises =
        unit.getTrainingExercises() != null ? unit.getTrainingExercises().getAll() : List.of();

    if (!exercises.isEmpty()) {
      minHeight += PdfLayout.SPACING_BEFORE_EXERCISES;
      minHeight += calculateExerciseHeight(exercises.getFirst());
    }

    return minHeight;
  }

  /**
   * Calculates the TOTAL height of a given training unit, including its header, description, all
   * exercises, and all vertical spacing and padding.
   *
   * @param unit The TrainingUnit to measure.
   * @return The total height in points.
   * @throws IOException If text measurement fails.
   */
  private float calculateUnitHeight(TrainingUnit unit) throws IOException {
    float totalHeight = PdfLayout.PADDING_UNIT_VERTICAL;

    totalHeight += calculateUnitHeaderHeight(unit);

    List<TrainingExercise> exercises =
        unit.getTrainingExercises() != null ? unit.getTrainingExercises().getAll() : List.of();

    if (!exercises.isEmpty()) {
      totalHeight += PdfLayout.SPACING_BEFORE_EXERCISES;

      boolean firstExercise = true;
      for (TrainingExercise exercise : exercises) {
        if (!firstExercise) {
          totalHeight += PdfLayout.SPACING_BETWEEN_EXERCISES;
        }
        totalHeight += calculateExerciseHeight(exercise);
        firstExercise = false;
      }
    } else {
      totalHeight +=
          textRenderer.calculateWrappedTextHeight(
              PLACEHOLDER_NO_EXERCISES,
              stylePlaceholder,
              PdfLayout.CONTENT_WIDTH
                  - (2
                      * (PdfLayout.INDENT_EXERCISE_CONTAINER
                          + PdfLayout.INDENT_EXERCISE_INTERNAL)));
    }

    totalHeight += PdfLayout.PADDING_UNIT_VERTICAL;

    return totalHeight;
  }

  private void renderUnit(TrainingUnit unit) throws IOException {
    float totalUnitHeight = calculateUnitHeight(unit);
    PdfContainerRenderer containerRenderer = documentManager.getContainerRenderer();

    // Fall A: The whole unit fits into the page
    if (textRenderer.getCurrentY() - totalUnitHeight >= PdfLayout.MARGIN) {
      // Zeichne einen einzigen, passgenauen Container.
      containerRenderer.drawContainer(
          PdfLayout.MARGIN,
          textRenderer.getCurrentY() - totalUnitHeight,
          PdfLayout.CONTENT_WIDTH,
          totalUnitHeight,
          PdfLayout.UNIT_BORDER_RADIUS,
          Theme.Colors.TRAINING_UNIT_BG);

      renderUnitContent(unit);

    } else { // Fall B: The unit is too big and will break the page
      float availableHeight = textRenderer.getCurrentY() - PdfLayout.MARGIN;
      containerRenderer.drawContainer(
          PdfLayout.MARGIN,
          PdfLayout.MARGIN,
          PdfLayout.CONTENT_WIDTH,
          availableHeight,
          PdfLayout.UNIT_BORDER_RADIUS,
          Theme.Colors.TRAINING_UNIT_BG);

      renderUnitContent(unit);
    }
  }

  /**
   * Renders the actual content (text and exercises) of a unit, assuming the background container
   * has already been drawn. It handles internal page breaks for exercises.
   *
   * @param unit The unit whose content should be rendered.
   * @throws IOException If writing to the PDF fails.
   */
  private void renderUnitContent(TrainingUnit unit) throws IOException {
    textRenderer.addSpacing(PdfLayout.PADDING_UNIT_VERTICAL);

    String unitName = Objects.toString(unit.getName(), DEFAULT_UNIT_NAME);
    textRenderer.writeStyledWrappedText(unitName, PdfLayout.INDENT_UNIT_LEVEL, styleUnitHeader);
    textRenderer.addSpacing(PdfLayout.SPACING_AFTER_UNIT_HEADER);

    String weekday = Objects.toString(unit.getWeekday(), DEFAULT_WEEKDAY);
    textRenderer.writeStyledWrappedText(weekday, PdfLayout.INDENT_UNIT_LEVEL, styleUnitWeekday);
    textRenderer.addSpacing(PdfLayout.SPACING_AFTER_UNIT_WEEKDAY);

    String description = unit.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      textRenderer.writeStyledWrappedText(
          description, PdfLayout.INDENT_UNIT_LEVEL, styleUnitDesc, PdfLayout.EXTRA_LINE_SPACING);
      textRenderer.addSpacing(PdfLayout.SPACING_AFTER_UNIT_DESC);
    }

    renderExercisesSection(unit);

    textRenderer.addSpacing(PdfLayout.PADDING_UNIT_VERTICAL);
  }

  private void renderExercisesSection(TrainingUnit unit) throws IOException {
    List<TrainingExercise> exercises =
        unit.getTrainingExercises() != null ? unit.getTrainingExercises().getAll() : List.of();

    if (exercises.isEmpty()) {
      handleUnitPageBreak(stylePlaceholder.size() * 2);
      textRenderer.writeStyledWrappedText(
          PLACEHOLDER_NO_EXERCISES,
          PdfLayout.INDENT_EXERCISE_CONTAINER + PdfLayout.INDENT_EXERCISE_INTERNAL,
          stylePlaceholder);
      return;
    }

    textRenderer.addSpacing(PdfLayout.SPACING_BEFORE_EXERCISES);

    boolean firstExercise = true;
    for (TrainingExercise exercise : exercises) {
      float exerciseHeight = calculateExerciseHeight(exercise);
      float spaceNeeded = exerciseHeight;

      if (!firstExercise) {
        spaceNeeded += PdfLayout.SPACING_BETWEEN_EXERCISES;
      }

      handleUnitPageBreak(spaceNeeded);

      if (!firstExercise) {
        textRenderer.addSpacing(PdfLayout.SPACING_BETWEEN_EXERCISES);
      }

      renderExercise(exercise, exerciseHeight);
      firstExercise = false;
    }
  }

  private void renderExercise(TrainingExercise exercise, float exerciseHeight) throws IOException {
    float exerciseStartY = textRenderer.getCurrentY();

    // Draw the container background
    documentManager
        .getContainerRenderer()
        .drawContainer(
            PdfLayout.MARGIN + PdfLayout.INDENT_EXERCISE_CONTAINER,
            exerciseStartY - exerciseHeight,
            PdfLayout.CONTENT_WIDTH - (2 * PdfLayout.INDENT_EXERCISE_CONTAINER),
            exerciseHeight,
            PdfLayout.EXERCISE_BORDER_RADIUS,
            Theme.Colors.EXERCISE_BG);

    // Add top padding
    textRenderer.addSpacing(PdfLayout.PADDING_EXERCISE_VERTICAL);

    // Write the exercise content
    float indent = PdfLayout.INDENT_EXERCISE_CONTAINER + PdfLayout.PADDING_EXERCISE_HORIZONTAL;

    String exerciseName = Objects.toString(exercise.getName(), DEFAULT_EXERCISE_NAME);
    textRenderer.writeStyledWrappedText(exerciseName, indent, styleExerciseName);
    textRenderer.addSpacing(PdfLayout.SPACING_AFTER_EXERCISE_NAME);

    String description = exercise.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      textRenderer.writeStyledWrappedText(
          description, indent, styleExerciseDesc, PdfLayout.EXTRA_LINE_SPACING);
      textRenderer.addSpacing(PdfLayout.SPACING_AFTER_EXERCISE_DESC);
    }

    String duration = Objects.toString(exercise.getDuration(), DEFAULT_DURATION);
    String details =
        String.format(
            "Duration: %s  •  Sets: %d  •  Ball Bucket: %s",
            duration, exercise.getSets(), (exercise.isBallBucket() ? "Yes" : "No"));
    textRenderer.writeStyledWrappedText(details, indent, styleExerciseDetails);

    // Set cursor to the correct position after rendering content within the pre-calculated box
    textRenderer.setCurrentY(exerciseStartY - exerciseHeight);
  }

  private float calculateUnitHeaderHeight(TrainingUnit unit) throws IOException {
    float totalHeight = 0;
    totalHeight += PdfLayout.PADDING_UNIT_VERTICAL; // Top padding

    String unitName = Objects.toString(unit.getName(), DEFAULT_UNIT_NAME);
    totalHeight +=
        textRenderer.calculateWrappedTextHeight(
            unitName, styleUnitHeader, PdfLayout.CONTENT_WIDTH - (2 * PdfLayout.INDENT_UNIT_LEVEL));
    totalHeight += PdfLayout.SPACING_AFTER_UNIT_HEADER;

    String weekday = Objects.toString(unit.getWeekday(), DEFAULT_WEEKDAY);
    totalHeight +=
        textRenderer.calculateWrappedTextHeight(
            weekday, styleUnitWeekday, PdfLayout.CONTENT_WIDTH - (2 * PdfLayout.INDENT_UNIT_LEVEL));
    totalHeight += PdfLayout.SPACING_AFTER_UNIT_WEEKDAY;

    String description = unit.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      totalHeight +=
          textRenderer.calculateWrappedTextHeight(
              description,
              styleUnitDesc,
              PdfLayout.CONTENT_WIDTH - (2 * PdfLayout.INDENT_UNIT_LEVEL),
              PdfLayout.EXTRA_LINE_SPACING);
      totalHeight += PdfLayout.SPACING_AFTER_UNIT_DESC;
    }

    return totalHeight;
  }

  private float calculateExerciseHeight(TrainingExercise exercise) throws IOException {
    float totalHeight = 0;
    float contentWidth =
        PdfLayout.CONTENT_WIDTH
            - (2 * PdfLayout.INDENT_EXERCISE_CONTAINER)
            - (2 * PdfLayout.PADDING_EXERCISE_HORIZONTAL);

    totalHeight += PdfLayout.PADDING_EXERCISE_VERTICAL; // Top padding

    String exerciseName = Objects.toString(exercise.getName(), DEFAULT_EXERCISE_NAME);
    totalHeight +=
        textRenderer.calculateWrappedTextHeight(exerciseName, styleExerciseName, contentWidth);
    totalHeight += PdfLayout.SPACING_AFTER_EXERCISE_NAME;

    String description = exercise.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      totalHeight +=
          textRenderer.calculateWrappedTextHeight(
              description, styleExerciseDesc, contentWidth, PdfLayout.EXTRA_LINE_SPACING);
      totalHeight += PdfLayout.SPACING_AFTER_EXERCISE_DESC;
    }

    String duration = Objects.toString(exercise.getDuration(), DEFAULT_DURATION);
    String details =
        String.format(
            "Duration: %s  •  Sets: %d  •  Ball Bucket: %s",
            duration, exercise.getSets(), (exercise.isBallBucket() ? "Yes" : "No"));
    totalHeight +=
        textRenderer.calculateWrappedTextHeight(details, styleExerciseDetails, contentWidth);

    totalHeight += PdfLayout.PADDING_EXERCISE_VERTICAL; // Bottom padding
    return totalHeight;
  }
}
