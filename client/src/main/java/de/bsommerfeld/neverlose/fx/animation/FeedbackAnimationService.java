package de.bsommerfeld.neverlose.fx.animation;

import javafx.scene.Node;

/**
 * Service interface for synesthetic feedback animations. Provides methods for applying visual feedback to UI elements.
 */
public interface FeedbackAnimationService extends AnimationService {

    /**
     * Apply synesthetic feedback for user actions
     *
     * @param element  The UI element
     * @param feedback The feedback type
     */
    void applySynestheticFeedback(Node element, FeedbackType feedback);

    /**
     * Feedback types for synesthetic feedback
     */
    enum FeedbackType {
        NONE, SUCCESS, ERROR
    }
}