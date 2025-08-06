package de.bsommerfeld.neverlose.fx.animation;

import javafx.scene.Node;

/**
 * Service interface for breathing animations. Provides methods for applying subtle scaling animations to UI elements.
 */
public interface BreathingAnimationService extends AnimationService {

    /**
     * Apply breathing animation to important elements
     *
     * @param element   The UI element
     * @param breathing Whether to apply breathing animation
     */
    void applyBreathingAnimation(Node element, boolean breathing);
}