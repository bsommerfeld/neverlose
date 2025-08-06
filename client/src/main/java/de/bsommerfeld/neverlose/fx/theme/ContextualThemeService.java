package de.bsommerfeld.neverlose.fx.theme;

import javafx.scene.Parent;

/**
 * Service interface for contextual theme management. Provides methods for applying stress-responsive interfaces and
 * metamorphic layouts.
 */
public interface ContextualThemeService extends ThemeService {

    /**
     * Apply stress-responsive interface based on user behavior
     *
     * @param container   The container element
     * @param stressLevel The detected stress level
     */
    void applyStressResponse(Parent container, StressLevel stressLevel);

    /**
     * Apply metamorphic layout based on task context
     *
     * @param container The container element
     * @param context   The task context
     */
    void applyTaskContext(Parent container, TaskContext context);

    /**
     * Stress levels for responsive interfaces
     */
    enum StressLevel {
        CALM, MODERATE, HIGH
    }

    /**
     * Task contexts for metamorphic layouts
     */
    enum TaskContext {
        PLANNING, EXERCISE_DESIGN, ANALYSIS
    }
}