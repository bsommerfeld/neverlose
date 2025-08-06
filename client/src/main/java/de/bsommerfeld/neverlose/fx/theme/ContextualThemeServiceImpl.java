package de.bsommerfeld.neverlose.fx.theme;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.scene.Parent;

/**
 * Implementation of ContextualThemeService that provides stress response and task context styling for UI elements.
 */
@Singleton
public class ContextualThemeServiceImpl implements ContextualThemeService {

    private static final LogFacade LOG = LogFacadeFactory.getLogger();

    @Inject
    public ContextualThemeServiceImpl() {
        LOG.debug("ContextualThemeService initialized");
    }

    @Override
    public void applyStressResponse(Parent container, StressLevel stressLevel) {
        container.getStyleClass().removeAll(
                "stress-calm", "stress-moderate", "stress-high");

        switch (stressLevel) {
            case CALM:
                container.getStyleClass().add("stress-calm");
                break;
            case MODERATE:
                container.getStyleClass().add("stress-moderate");
                break;
            case HIGH:
                container.getStyleClass().add("stress-high");
                break;
        }
    }

    @Override
    public void applyTaskContext(Parent container, TaskContext context) {
        container.getStyleClass().removeAll(
                "metamorphic-planning", "metamorphic-exercise", "metamorphic-analysis");

        switch (context) {
            case PLANNING:
                container.getStyleClass().add("metamorphic-planning");
                break;
            case EXERCISE_DESIGN:
                container.getStyleClass().add("metamorphic-exercise");
                break;
            case ANALYSIS:
                container.getStyleClass().add("metamorphic-analysis");
                break;
        }
    }

    @Override
    public void shutdown() {
        // No resources to clean up
    }
}