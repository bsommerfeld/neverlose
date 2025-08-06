package de.bsommerfeld.neverlose.fx.theme;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.scene.Node;

/**
 * Implementation of EmotionalThemeService that provides emotional state styling for UI elements.
 */
@Singleton
public class EmotionalThemeServiceImpl implements EmotionalThemeService {

    private static final LogFacade LOG = LogFacadeFactory.getLogger();

    @Inject
    public EmotionalThemeServiceImpl() {
        LOG.debug("EmotionalThemeService initialized");
    }

    @Override
    public void applyEmotionalState(Node element, EmotionalState state) {
        element.getStyleClass().removeAll(
                "energetic-geometry", "calm-geometry");

        switch (state) {
            case ENERGETIC:
                element.getStyleClass().add("energetic-geometry");
                break;
            case CALM:
                element.getStyleClass().add("calm-geometry");
                break;
            default:
                // Use default neutral geometry
                break;
        }
    }

    @Override
    public void shutdown() {
        // No resources to clean up
    }
}