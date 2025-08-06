package de.bsommerfeld.neverlose.fx.theme;

import javafx.scene.Node;

/**
 * Service interface for emotional theme management. Provides methods for applying emotional geometry based on context.
 */
public interface EmotionalThemeService extends ThemeService {

    /**
     * Apply emotional geometry based on context
     *
     * @param element The UI element
     * @param state   The emotional state
     */
    void applyEmotionalState(Node element, EmotionalState state);

    /**
     * Emotional states for UI geometry
     */
    enum EmotionalState {
        NEUTRAL, ENERGETIC, CALM
    }
}