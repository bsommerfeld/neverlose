package de.bsommerfeld.neverlose.fx.theme;

import javafx.scene.Parent;

import java.time.LocalTime;

/**
 * Service interface for time-based theme management. Provides methods for applying themes based on the time of day.
 */
public interface TimeBasedThemeService extends ThemeService {

    /**
     * Initialize time-based theme switching
     *
     * @param root The root element of the scene
     */
    void initializeTimeBasedTheme(Parent root);

    /**
     * Apply theme based on time of day
     *
     * @param root The root element of the scene
     * @param time Current local time
     */
    void applyThemeBasedOnTime(Parent root, LocalTime time);
}