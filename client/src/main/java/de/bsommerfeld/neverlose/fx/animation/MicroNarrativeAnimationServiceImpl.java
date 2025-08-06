package de.bsommerfeld.neverlose.fx.animation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Implementation of MicroNarrativeAnimationService that provides animations for adding and removing UI elements.
 */
@Singleton
public class MicroNarrativeAnimationServiceImpl implements MicroNarrativeAnimationService {

    private static final LogFacade LOG = LogFacadeFactory.getLogger();

    @Inject
    public MicroNarrativeAnimationServiceImpl() {
        LOG.debug("MicroNarrativeAnimationService initialized");
    }

    @Override
    public void applyAddAnimation(Node element) {
        element.getStyleClass().add("add-animation");

        // Create and play the animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), element);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition moveDown = new TranslateTransition(Duration.millis(500), element);
        moveDown.setFromY(-20);
        moveDown.setToY(0);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(500), element);
        scaleUp.setFromX(0.8);
        scaleUp.setFromY(0.8);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        // Play animations in parallel
        fadeIn.play();
        moveDown.play();
        scaleUp.play();

        // Remove the class after animation completes
        fadeIn.setOnFinished(event -> element.getStyleClass().remove("add-animation"));
    }

    @Override
    public void applyRemoveAnimation(Node element, Runnable onFinished) {
        element.getStyleClass().add("remove-animation");

        // Create and play the animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), element);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition moveUp = new TranslateTransition(Duration.millis(500), element);
        moveUp.setFromY(0);
        moveUp.setToY(20);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(500), element);
        scaleDown.setFromX(1.0);
        scaleDown.setFromY(1.0);
        scaleDown.setToX(0.8);
        scaleDown.setToY(0);

        // Play animations in parallel
        fadeOut.play();
        moveUp.play();
        scaleDown.play();

        // Execute callback after animation completes
        fadeOut.setOnFinished(event -> {
            element.getStyleClass().remove("remove-animation");
            if (onFinished != null) {
                onFinished.run();
            }
        });
    }

    @Override
    public void shutdown() {
        // No resources to clean up
    }
}