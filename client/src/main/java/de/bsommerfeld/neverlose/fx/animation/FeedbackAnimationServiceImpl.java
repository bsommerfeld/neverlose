package de.bsommerfeld.neverlose.fx.animation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.application.Platform;
import javafx.scene.Node;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation of FeedbackAnimationService that provides visual feedback animations for UI elements.
 */
@Singleton
public class FeedbackAnimationServiceImpl implements FeedbackAnimationService {

    private static final LogFacade LOG = LogFacadeFactory.getLogger();

    @Inject
    public FeedbackAnimationServiceImpl() {
        LOG.debug("FeedbackAnimationService initialized");
    }

    @Override
    public void applySynestheticFeedback(Node element, FeedbackType feedback) {
        element.getStyleClass().removeAll(
                "synesthetic-success", "synesthetic-error");

        switch (feedback) {
            case SUCCESS:
                element.getStyleClass().add("synesthetic-success");
                break;
            case ERROR:
                element.getStyleClass().add("synesthetic-error");
                break;
            default:
                // No feedback
                break;
        }

        // Remove feedback after a delay
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    element.getStyleClass().removeAll(
                            "synesthetic-success", "synesthetic-error");
                });
            }
        }, 2000); // Remove after 2 seconds
    }

    @Override
    public void shutdown() {
        // No resources to clean up
    }
}