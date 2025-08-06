package de.bsommerfeld.neverlose.fx.tracking;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.scene.Node;
import javafx.scene.Scene;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of UsageTrackingService that provides element usage tracking and popular element highlighting.
 */
@Singleton
public class UsageTrackingServiceImpl implements UsageTrackingService {

    private static final LogFacade LOG = LogFacadeFactory.getLogger();
    private final Map<String, Integer> elementUsageCount = new HashMap<>();
    private Scene scene;

    @Inject
    public UsageTrackingServiceImpl() {
        LOG.debug("UsageTrackingService initialized");
    }

    @Override
    public void initialize(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void trackElementUsage(String elementId) {
        if (scene == null) {
            LOG.warn("Cannot track element usage: scene is not initialized");
            return;
        }

        elementUsageCount.put(elementId,
                elementUsageCount.getOrDefault(elementId, 0) + 1);

        // Apply collective intelligence highlighting based on usage
        if (elementUsageCount.get(elementId) > 10) {
            highlightPopularElement(elementId, true);
        }
    }

    /**
     * Highlight popular elements based on collective usage patterns
     *
     * @param elementId   Identifier for the UI element
     * @param veryPopular Whether the element is very popular (used frequently)
     */
    private void highlightPopularElement(String elementId, boolean veryPopular) {
        Node element = scene.lookup("#" + elementId);
        if (element != null) {
            element.getStyleClass().removeAll("collective-popular", "collective-very-popular");
            element.getStyleClass().add(veryPopular ? "collective-very-popular" : "collective-popular");
        }
    }

    @Override
    public void shutdown() {
        // No resources to clean up
    }
}