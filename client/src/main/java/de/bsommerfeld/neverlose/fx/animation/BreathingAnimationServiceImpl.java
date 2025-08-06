package de.bsommerfeld.neverlose.fx.animation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of BreathingAnimationService that provides subtle scaling animations for UI elements.
 */
@Singleton
public class BreathingAnimationServiceImpl implements BreathingAnimationService {

    private static final LogFacade LOG = LogFacadeFactory.getLogger();
    private final Map<Node, Timeline> breathingAnimations = new WeakHashMap<>();

    @Inject
    public BreathingAnimationServiceImpl() {
        LOG.debug("BreathingAnimationService initialized");
    }

    @Override
    public void applyBreathingAnimation(Node element, boolean breathing) {
        // Stop any existing animation
        Timeline existingAnimation = breathingAnimations.get(element);
        if (existingAnimation != null) {
            existingAnimation.stop();
            breathingAnimations.remove(element);
        }

        // Remove CSS class if it was previously used
        element.getStyleClass().remove("breathing-animation");

        if (breathing) {
            // Create a new breathing animation using JavaFX Timeline
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(element.scaleXProperty(), 1.0),
                            new KeyValue(element.scaleYProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(1.5),
                            new KeyValue(element.scaleXProperty(), 1.05),
                            new KeyValue(element.scaleYProperty(), 1.05)),
                    new KeyFrame(Duration.seconds(4),
                            new KeyValue(element.scaleXProperty(), 1.0),
                            new KeyValue(element.scaleYProperty(), 1.0))
            );

            // Make the animation repeat indefinitely
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

            // Store the animation
            breathingAnimations.put(element, timeline);

            LOG.debug("Applied breathing animation to element");
        }
    }

    @Override
    public void shutdown() {
        // Stop all breathing animations
        for (Timeline animation : breathingAnimations.values()) {
            animation.stop();
        }
        breathingAnimations.clear();

        LOG.debug("BreathingAnimationService shutdown complete");
    }
}