package de.bsommerfeld.neverlose.fx.animation;

import javafx.scene.Node;

/**
 * Service interface for micro-narrative animations. Provides methods for applying add and remove animations to UI
 * elements.
 */
public interface MicroNarrativeAnimationService extends AnimationService {

    /**
     * Apply micro-narrative animation when adding a new element
     *
     * @param element The element being added
     */
    void applyAddAnimation(Node element);

    /**
     * Apply micro-narrative animation when removing an element
     *
     * @param element    The element being removed
     * @param onFinished Callback to execute after animation completes
     */
    void applyRemoveAnimation(Node element, Runnable onFinished);
}