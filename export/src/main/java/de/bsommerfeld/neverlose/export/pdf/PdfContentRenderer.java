package de.bsommerfeld.neverlose.export.pdf;

import de.bsommerfeld.neverlose.plan.TrainingPlan;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import de.bsommerfeld.neverlose.theme.Theme;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Handles rendering of training plan content to PDF.
 * This class encapsulates the logic for rendering training plans, units, and exercises.
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

    /**
     * Creates a new PdfContentRenderer with the specified styles and document manager.
     *
     * @param documentManager The document manager to use
     * @param stylePlanTitle The style for plan titles
     * @param stylePlanDesc The style for plan descriptions
     * @param styleUnitHeader The style for unit headers
     * @param styleUnitWeekday The style for unit weekdays
     * @param styleUnitDesc The style for unit descriptions
     * @param styleExerciseName The style for exercise names
     * @param styleExerciseDesc The style for exercise descriptions
     * @param styleExerciseDetails The style for exercise details
     * @param stylePlaceholder The style for placeholders
     */
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

    /**
     * Renders a training plan to the PDF document.
     *
     * @param plan The training plan to render
     * @throws IOException If there's an error rendering the plan
     */
    public void renderTrainingPlan(TrainingPlan plan) throws IOException {
        documentManager.startNewPage();
        renderPlanHeader(plan);
        renderSeparator();
        renderUnitsSection(plan);

        // Add spacing at the end of the document to ensure proper spacing to the last page
        PdfTextRenderer textRenderer = documentManager.getTextRenderer();
        textRenderer.addSpacing(PdfLayout.SPACING_BETWEEN_UNITS);
    }

    /**
     * Renders the header section of a training plan.
     *
     * @param plan The training plan to render
     * @throws IOException If there's an error rendering the header
     */
    private void renderPlanHeader(TrainingPlan plan) throws IOException {
        PdfTextRenderer textRenderer = documentManager.getTextRenderer();

        String planName = Objects.toString(plan.getName(), DEFAULT_PLAN_NAME);
        textRenderer.writeStyledWrappedText(planName, PdfLayout.INDENT_UNIT_LEVEL, stylePlanTitle);
        textRenderer.addSpacing(PdfLayout.SPACING_AFTER_TITLE);

        String description = plan.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            textRenderer.writeStyledWrappedText(
                    description, PdfLayout.INDENT_UNIT_LEVEL, stylePlanDesc, PdfLayout.EXTRA_LINE_SPACING);
            textRenderer.addSpacing(PdfLayout.SPACING_AFTER_PREVIEW_DESC);
        } else {
            textRenderer.addSpacing(PdfLayout.SPACING_AFTER_TITLE);
        }
    }

    /**
     * Renders a separator line.
     *
     * @throws IOException If there's an error rendering the separator
     */
    private void renderSeparator() throws IOException {
        PdfTextRenderer textRenderer = documentManager.getTextRenderer();
        PdfContainerRenderer containerRenderer = documentManager.getContainerRenderer();

        float separatorHeight = 1f;
        float totalSpaceNeeded = PdfLayout.SPACING_SEPARATOR + separatorHeight;

        if (textRenderer.getCurrentY() - totalSpaceNeeded < PdfLayout.MARGIN) {
            documentManager.startNewPage();
            textRenderer = documentManager.getTextRenderer();
            containerRenderer = documentManager.getContainerRenderer();
        }

        textRenderer.addSpacing(PdfLayout.SPACING_SEPARATOR / 2);
        containerRenderer.drawSeparator(textRenderer.getCurrentY());
        textRenderer.setCurrentY(textRenderer.getCurrentY() - separatorHeight);
        textRenderer.addSpacing(PdfLayout.SPACING_SEPARATOR / 2);
    }

    /**
     * Renders the units section of a training plan.
     *
     * @param plan The training plan to render
     * @throws IOException If there's an error rendering the units
     */
    private void renderUnitsSection(TrainingPlan plan) throws IOException {
        PdfTextRenderer textRenderer = documentManager.getTextRenderer();

        List<TrainingUnit> units =
                plan.getTrainingUnits() != null ? plan.getTrainingUnits().getAll() : List.of();

        if (units.isEmpty()) {
            textRenderer.addSpacing(PdfLayout.SPACING_BETWEEN_UNITS);
            textRenderer.writeStyledTextLine(PLACEHOLDER_NO_UNITS, PdfLayout.INDENT_UNIT_LEVEL, stylePlaceholder);
            return;
        }

        boolean firstUnit = true;
        for (TrainingUnit unit : units) {
            if (!firstUnit) {
                textRenderer.addSpacing(PdfLayout.SPACING_BETWEEN_UNITS);
            }
            renderUnit(unit);
            firstUnit = false;
        }
    }

    /**
     * Renders a training unit.
     *
     * @param unit The training unit to render
     * @throws IOException If there's an error rendering the unit
     */
    private void renderUnit(TrainingUnit unit) throws IOException {
        PdfTextRenderer textRenderer = documentManager.getTextRenderer();
        PdfContainerRenderer containerRenderer = documentManager.getContainerRenderer();

        // Add some padding at the top
        textRenderer.addSpacing(PdfLayout.SPACING_AFTER_UNIT_DESC);

        // Draw the container background for the current visible portion
        float topY = textRenderer.getCurrentY();
        float visibleHeight = topY - PdfLayout.MARGIN;
        containerRenderer.drawContainer(
                PdfLayout.MARGIN,
                topY - visibleHeight,
                PdfLayout.CONTENT_WIDTH,
                visibleHeight,
                PdfLayout.UNIT_BORDER_RADIUS,
                Theme.Colors.TRAINING_UNIT_BG);

        // Write the unit content
        String unitName = Objects.toString(unit.getName(), DEFAULT_UNIT_NAME);
        String weekday = Objects.toString(unit.getWeekday(), DEFAULT_WEEKDAY);

        // Write unit name and weekday on separate lines
        if (!textRenderer.writeStyledWrappedText(unitName, PdfLayout.INDENT_UNIT_LEVEL, styleUnitHeader)) {
            // Need a new page
            float newPageY = documentManager.startNewPage();
            textRenderer = documentManager.getTextRenderer();
            containerRenderer = documentManager.getContainerRenderer();

            // Draw container on the new page
            visibleHeight = newPageY - PdfLayout.MARGIN;
            containerRenderer.drawContainer(
                    PdfLayout.MARGIN,
                    newPageY - visibleHeight,
                    PdfLayout.CONTENT_WIDTH,
                    visibleHeight,
                    PdfLayout.UNIT_BORDER_RADIUS,
                    Theme.Colors.TRAINING_UNIT_BG);

            // Try again on the new page
            textRenderer.writeStyledWrappedText(unitName, PdfLayout.INDENT_UNIT_LEVEL, styleUnitHeader);
        }

        textRenderer.addSpacing(PdfLayout.SPACING_AFTER_UNIT_HEADER);

        if (!textRenderer.writeStyledWrappedText(weekday, PdfLayout.INDENT_UNIT_LEVEL, styleUnitWeekday)) {
            // Need a new page
            float newPageY = documentManager.startNewPage();
            textRenderer = documentManager.getTextRenderer();
            containerRenderer = documentManager.getContainerRenderer();

            // Draw container on the new page
            visibleHeight = newPageY - PdfLayout.MARGIN;
            containerRenderer.drawContainer(
                    PdfLayout.MARGIN,
                    newPageY - visibleHeight,
                    PdfLayout.CONTENT_WIDTH,
                    visibleHeight,
                    PdfLayout.UNIT_BORDER_RADIUS,
                    Theme.Colors.TRAINING_UNIT_BG);

            // Try again on the new page
            textRenderer.writeStyledWrappedText(weekday, PdfLayout.INDENT_UNIT_LEVEL, styleUnitWeekday);
        }

        textRenderer.addSpacing(PdfLayout.SPACING_AFTER_UNIT_WEEKDAY);

        String description = unit.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            if (!textRenderer.writeStyledWrappedText(
                    description, PdfLayout.INDENT_UNIT_LEVEL, styleUnitDesc, PdfLayout.EXTRA_LINE_SPACING)) {
                // Need a new page
                float newPageY = documentManager.startNewPage();
                textRenderer = documentManager.getTextRenderer();
                containerRenderer = documentManager.getContainerRenderer();

                // Draw container on the new page
                visibleHeight = newPageY - PdfLayout.MARGIN;
                containerRenderer.drawContainer(
                        PdfLayout.MARGIN,
                        newPageY - visibleHeight,
                        PdfLayout.CONTENT_WIDTH,
                        visibleHeight,
                        PdfLayout.UNIT_BORDER_RADIUS,
                        Theme.Colors.TRAINING_UNIT_BG);

                // Try again on the new page
                textRenderer.writeStyledWrappedText(
                        description, PdfLayout.INDENT_UNIT_LEVEL, styleUnitDesc, PdfLayout.EXTRA_LINE_SPACING);
            }
            textRenderer.addSpacing(PdfLayout.SPACING_AFTER_UNIT_DESC);
        }

        renderExercisesSection(unit);
    }

    /**
     * Renders the exercises section of a training unit.
     *
     * @param unit The training unit containing the exercises
     * @throws IOException If there's an error rendering the exercises
     */
    private void renderExercisesSection(TrainingUnit unit) throws IOException {
        PdfTextRenderer textRenderer = documentManager.getTextRenderer();

        List<TrainingExercise> exercises =
                unit.getTrainingExercises() != null ? unit.getTrainingExercises().getAll() : List.of();

        if (exercises.isEmpty()) {
            textRenderer.writeStyledWrappedText(
                    PLACEHOLDER_NO_EXERCISES,
                    PdfLayout.INDENT_EXERCISE_CONTAINER + PdfLayout.INDENT_EXERCISE_INTERNAL,
                    stylePlaceholder);
            textRenderer.addSpacing(PdfLayout.SPACING_BETWEEN_EXERCISES);
            return;
        }

        textRenderer.addSpacing(PdfLayout.SPACING_BEFORE_EXERCISES);

        boolean firstExercise = true;
        for (TrainingExercise exercise : exercises) {
            if (!firstExercise) {
                // Add spacing between exercises, but check if we have enough space
                // If not enough space, the spacing will be added after page break in renderExercise
                if (textRenderer.getCurrentY() - PdfLayout.SPACING_BETWEEN_EXERCISES >= PdfLayout.MARGIN) {
                    textRenderer.addSpacing(PdfLayout.SPACING_BETWEEN_EXERCISES);
                }
            }
            renderExercise(exercise);
            firstExercise = false;
        }
    }

    /**
     * Renders a training exercise.
     *
     * @param exercise The training exercise to render
     * @throws IOException If there's an error rendering the exercise
     */
    private void renderExercise(TrainingExercise exercise) throws IOException {
        // Calculate the height needed for this exercise
        float exerciseHeight = calculateExerciseHeight(exercise);
        PdfTextRenderer textRenderer = documentManager.getTextRenderer();
        PdfContainerRenderer containerRenderer = documentManager.getContainerRenderer();

        // Check if we need to start a new page
        if (textRenderer.getCurrentY() - exerciseHeight < PdfLayout.MARGIN) {
            float newPageY = documentManager.startNewPage();
            textRenderer = documentManager.getTextRenderer();
            containerRenderer = documentManager.getContainerRenderer();

            // Draw unit container on the new page
            float visibleHeight = newPageY - PdfLayout.MARGIN;
            containerRenderer.drawContainer(
                    PdfLayout.MARGIN,
                    newPageY - visibleHeight,
                    PdfLayout.CONTENT_WIDTH,
                    visibleHeight,
                    PdfLayout.UNIT_BORDER_RADIUS,
                    Theme.Colors.TRAINING_UNIT_BG);

            // Add spacing before the first exercise on the new page
            textRenderer.addSpacing(PdfLayout.SPACING_BEFORE_EXERCISES);

            // Add spacing between exercises on the new page
            textRenderer.addSpacing(PdfLayout.SPACING_BETWEEN_EXERCISES);
        }

        // Draw the container background
        containerRenderer.drawContainer(
                PdfLayout.MARGIN + PdfLayout.INDENT_EXERCISE_CONTAINER,
                textRenderer.getCurrentY() - exerciseHeight,
                PdfLayout.CONTENT_WIDTH - (2 * PdfLayout.INDENT_EXERCISE_CONTAINER),
                exerciseHeight,
                PdfLayout.EXERCISE_BORDER_RADIUS,
                Theme.Colors.EXERCISE_BG);

        // Add some padding at the top
        textRenderer.addSpacing(PdfLayout.SPACING_AFTER_EXERCISE_NAME);

        // Write the exercise content
        String exerciseName = Objects.toString(exercise.getName(), DEFAULT_EXERCISE_NAME);
        textRenderer.writeStyledWrappedText(
                exerciseName,
                PdfLayout.INDENT_EXERCISE_BLOCK + PdfLayout.INDENT_EXERCISE_INTERNAL,
                styleExerciseName);
        textRenderer.addSpacing(PdfLayout.SPACING_AFTER_EXERCISE_NAME);

        String description = exercise.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            textRenderer.writeStyledWrappedText(
                    description,
                    PdfLayout.INDENT_EXERCISE_CONTENT + PdfLayout.INDENT_EXERCISE_INTERNAL,
                    styleExerciseDesc,
                    PdfLayout.EXTRA_LINE_SPACING);
            textRenderer.addSpacing(PdfLayout.SPACING_AFTER_EXERCISE_DESC);
        }

        String duration = Objects.toString(exercise.getDuration(), DEFAULT_DURATION);
        String details =
                String.format(
                        "Duration: %s  •  Sets: %d  •  Ball Bucket: %s",
                        duration, exercise.getSets(), (exercise.isBallBucket() ? "Yes" : "No"));
        textRenderer.writeStyledWrappedText(
                details,
                PdfLayout.INDENT_EXERCISE_CONTENT + PdfLayout.INDENT_EXERCISE_INTERNAL,
                styleExerciseDetails);
    }

    /**
     * Calculates the height needed to render a training unit.
     *
     * @param unit The training unit to measure
     * @return The total height in points needed to render the unit
     * @throws IOException If there's an error calculating text dimensions
     */
    private float calculateUnitHeight(TrainingUnit unit) throws IOException {
        PdfTextRenderer textRenderer = documentManager.getTextRenderer();
        float totalHeight = 0;

        // Unit name
        String unitName = Objects.toString(unit.getName(), DEFAULT_UNIT_NAME);
        List<String> nameLines =
                textRenderer.wrapText(
                        unitName,
                        styleUnitHeader.font(),
                        styleUnitHeader.size(),
                        PdfLayout.CONTENT_WIDTH - PdfLayout.INDENT_UNIT_LEVEL);
        totalHeight += nameLines.size() * styleUnitHeader.size() * PdfLayout.BASE_LINE_SPACING_FACTOR;
        totalHeight += PdfLayout.SPACING_AFTER_UNIT_HEADER;

        // Weekday (on a separate line)
        String weekday = Objects.toString(unit.getWeekday(), DEFAULT_WEEKDAY);
        List<String> weekdayLines =
                textRenderer.wrapText(
                        weekday,
                        styleUnitWeekday.font(),
                        styleUnitWeekday.size(),
                        PdfLayout.CONTENT_WIDTH - PdfLayout.INDENT_UNIT_LEVEL);
        totalHeight +=
                weekdayLines.size() * styleUnitWeekday.size() * PdfLayout.BASE_LINE_SPACING_FACTOR;
        totalHeight += PdfLayout.SPACING_AFTER_UNIT_WEEKDAY;

        // Unit description
        String description = unit.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            List<String> descLines =
                    textRenderer.wrapText(
                            description,
                            styleUnitDesc.font(),
                            styleUnitDesc.size(),
                            PdfLayout.CONTENT_WIDTH - PdfLayout.INDENT_UNIT_LEVEL);

            for (String line : descLines) {
                totalHeight +=
                        styleUnitDesc.size() * PdfLayout.BASE_LINE_SPACING_FACTOR + PdfLayout.EXTRA_LINE_SPACING;
            }
            totalHeight += PdfLayout.SPACING_AFTER_UNIT_DESC;
        }

        // Exercises section
        List<TrainingExercise> exercises =
                unit.getTrainingExercises() != null ? unit.getTrainingExercises().getAll() : List.of();

        if (exercises.isEmpty()) {
            List<String> placeholderLines =
                    textRenderer.wrapText(
                            PLACEHOLDER_NO_EXERCISES,
                            stylePlaceholder.font(),
                            stylePlaceholder.size(),
                            PdfLayout.CONTENT_WIDTH
                                    - (2 * PdfLayout.INDENT_EXERCISE_CONTAINER)
                                    - (2 * PdfLayout.INDENT_EXERCISE_INTERNAL));
            totalHeight +=
                    placeholderLines.size() * stylePlaceholder.size() * PdfLayout.BASE_LINE_SPACING_FACTOR;
            totalHeight += PdfLayout.SPACING_BETWEEN_EXERCISES;
        } else {
            totalHeight += PdfLayout.SPACING_BEFORE_EXERCISES;

            boolean firstExercise = true;
            for (TrainingExercise exercise : exercises) {
                if (!firstExercise) {
                    totalHeight += PdfLayout.SPACING_BETWEEN_EXERCISES;
                }
                totalHeight += calculateExerciseHeight(exercise);
                firstExercise = false;
            }
        }

        // Add padding for the container
        totalHeight += 2 * PdfLayout.SPACING_AFTER_UNIT_DESC; // Top and bottom padding

        return totalHeight;
    }

    /**
     * Calculates the height needed to render a training exercise.
     *
     * @param exercise The training exercise to measure
     * @return The total height in points needed to render the exercise
     * @throws IOException If there's an error calculating text dimensions
     */
    private float calculateExerciseHeight(TrainingExercise exercise) throws IOException {
        PdfTextRenderer textRenderer = documentManager.getTextRenderer();
        float totalHeight = 0;

        // Exercise name
        String exerciseName = Objects.toString(exercise.getName(), DEFAULT_EXERCISE_NAME);
        List<String> nameLines =
                textRenderer.wrapText(
                        exerciseName,
                        styleExerciseName.font(),
                        styleExerciseName.size(),
                        PdfLayout.CONTENT_WIDTH
                                - (2 * PdfLayout.INDENT_EXERCISE_BLOCK)
                                - (2 * PdfLayout.INDENT_EXERCISE_INTERNAL));
        totalHeight += nameLines.size() * styleExerciseName.size() * PdfLayout.BASE_LINE_SPACING_FACTOR;
        totalHeight += PdfLayout.SPACING_AFTER_EXERCISE_NAME;

        // Exercise description
        String description = exercise.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            List<String> descLines =
                    textRenderer.wrapText(
                            description,
                            styleExerciseDesc.font(),
                            styleExerciseDesc.size(),
                            PdfLayout.CONTENT_WIDTH
                                    - (2 * PdfLayout.INDENT_EXERCISE_CONTENT)
                                    - (2 * PdfLayout.INDENT_EXERCISE_INTERNAL));

            for (String line : descLines) {
                totalHeight +=
                        styleExerciseDesc.size() * PdfLayout.BASE_LINE_SPACING_FACTOR
                                + PdfLayout.EXTRA_LINE_SPACING;
            }
            totalHeight += PdfLayout.SPACING_AFTER_EXERCISE_DESC;
        }

        // Exercise details
        String duration = Objects.toString(exercise.getDuration(), DEFAULT_DURATION);
        String details =
                String.format(
                        "Duration: %s  •  Sets: %d  •  Ball Bucket: %s",
                        duration, exercise.getSets(), (exercise.isBallBucket() ? "Yes" : "No"));
        List<String> detailLines =
                textRenderer.wrapText(
                        details,
                        styleExerciseDetails.font(),
                        styleExerciseDetails.size(),
                        PdfLayout.CONTENT_WIDTH
                                - (2 * PdfLayout.INDENT_EXERCISE_CONTENT)
                                - (2 * PdfLayout.INDENT_EXERCISE_INTERNAL));
        totalHeight +=
                detailLines.size() * styleExerciseDetails.size() * PdfLayout.BASE_LINE_SPACING_FACTOR;

        // Add padding for the container
        totalHeight += 2 * PdfLayout.SPACING_AFTER_EXERCISE_NAME; // Top and bottom padding

        return totalHeight;
    }
}
