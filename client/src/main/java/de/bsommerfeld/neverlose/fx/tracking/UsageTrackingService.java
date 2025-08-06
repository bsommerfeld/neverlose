package de.bsommerfeld.neverlose.fx.tracking;

import javafx.scene.Scene;

/**
 * Service interface for tracking UI element usage. Provides methods for tracking element usage and highlighting popular
 * elements.
 */
public interface UsageTrackingService {

    /**
     * Initialize the usage tracking service with the application scene
     *
     * @param scene The main application scene
     */
    void initialize(Scene scene);

    /**
     * Track usage of UI elements to implement adaptive intelligence
     *
     * @param elementId Identifier for the UI element
     */
    void trackElementUsage(String elementId);

    /**
     * Cleans up resources used by the usage tracking service.
     */
    void shutdown();
}