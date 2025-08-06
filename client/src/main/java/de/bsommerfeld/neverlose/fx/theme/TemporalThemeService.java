package de.bsommerfeld.neverlose.fx.theme;

import javafx.scene.Node;

/**
 * Service interface for temporal theme management. Provides methods for applying temporal layering to elements based on
 * their state.
 */
public interface TemporalThemeService extends ThemeService {

    /**
     * Apply temporal layering to elements based on their state
     *
     * @param element The UI element
     * @param state   The temporal state (past, present, future)
     */
    void applyTemporalState(Node element, TemporalState state);

    /**
     * Temporal states for UI elements
     */
    enum TemporalState {
        PAST, PRESENT, FUTURE
    }
}