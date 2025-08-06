package de.bsommerfeld.neverlose.fx.theme;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.scene.Node;

/**
 * Implementation of TemporalThemeService that provides temporal state styling for UI elements.
 */
@Singleton
public class TemporalThemeServiceImpl implements TemporalThemeService {

    private static final LogFacade LOG = LogFacadeFactory.getLogger();

    @Inject
    public TemporalThemeServiceImpl() {
        LOG.debug("TemporalThemeService initialized");
    }

    @Override
    public void applyTemporalState(Node element, TemporalState state) {
        element.getStyleClass().removeAll(
                "temporal-past", "temporal-present", "temporal-future");

        switch (state) {
            case PAST:
                element.getStyleClass().add("temporal-past");
                break;
            case PRESENT:
                element.getStyleClass().add("temporal-present");
                break;
            case FUTURE:
                element.getStyleClass().add("temporal-future");
                break;
        }
    }

    @Override
    public void shutdown() {
        // No resources to clean up
    }
}