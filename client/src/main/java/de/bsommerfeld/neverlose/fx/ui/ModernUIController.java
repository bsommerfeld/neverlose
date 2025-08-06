package de.bsommerfeld.neverlose.fx.ui;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.animation.FeedbackAnimationService;
import de.bsommerfeld.neverlose.fx.theme.ContextualThemeService;
import de.bsommerfeld.neverlose.fx.theme.EmotionalThemeService;
import de.bsommerfeld.neverlose.fx.theme.TemporalThemeService;
import de.bsommerfeld.neverlose.fx.viewmodel.UIViewModel;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Controller for modern UI features that implements the futuristic design elements: - Adaptive Intelligence in Design -
 * Neurogene Farbsysteme (Responsive Colors) - Mikro-Narrative in der Navigation - Biometrische Anpassung - Temporale
 * Schichtung
 * <p>
 * This controller delegates to the UIViewModel, which coordinates the different UI services.
 */
public class ModernUIController {
    private static final LogFacade LOG = LogFacadeFactory.getLogger();

    private final UIViewModel uiViewModel;

    /**
     * Initialize the ModernUIController with the UIViewModel
     *
     * @param uiViewModel The UIViewModel that coordinates UI services
     */
    @Inject
    public ModernUIController(UIViewModel uiViewModel) {
        this.uiViewModel = uiViewModel;
        LOG.info("ModernUIController initialized");
    }

    /**
     * Initialize the UI services with the application scene
     *
     * @param scene The main application scene
     */
    public void initialize(Scene scene) {
        uiViewModel.initialize(scene);
    }

    /**
     * Track usage of UI elements to implement adaptive intelligence
     *
     * @param elementId Identifier for the UI element
     */
    public void trackElementUsage(String elementId) {
        uiViewModel.trackElementUsage(elementId);
    }

    /**
     * Apply micro-narrative animation when adding a new element
     *
     * @param element The element being added
     */
    public void applyAddAnimation(Node element) {
        uiViewModel.applyAddAnimation(element);
    }

    /**
     * Apply micro-narrative animation when removing an element
     *
     * @param element    The element being removed
     * @param onFinished Callback to execute after animation completes
     */
    public void applyRemoveAnimation(Node element, Runnable onFinished) {
        uiViewModel.applyRemoveAnimation(element, onFinished);
    }

    /**
     * Apply temporal layering to elements based on their state
     *
     * @param element The UI element
     * @param state   The temporal state (past, present, future)
     */
    public void applyTemporalState(Node element, TemporalThemeService.TemporalState state) {
        uiViewModel.applyTemporalState(element, state);
    }

    /**
     * Apply emotional geometry based on context
     *
     * @param element The UI element
     * @param state   The emotional state
     */
    public void applyEmotionalState(Node element, EmotionalThemeService.EmotionalState state) {
        uiViewModel.applyEmotionalState(element, state);
    }

    /**
     * Apply stress-responsive interface based on user behavior
     *
     * @param container   The container element
     * @param stressLevel The detected stress level
     */
    public void applyStressResponse(Parent container, ContextualThemeService.StressLevel stressLevel) {
        uiViewModel.applyStressResponse(container, stressLevel);
    }

    /**
     * Apply metamorphic layout based on task context
     *
     * @param container The container element
     * @param context   The task context
     */
    public void applyTaskContext(Parent container, ContextualThemeService.TaskContext context) {
        uiViewModel.applyTaskContext(container, context);
    }

    /**
     * Apply breathing animation to important elements
     *
     * @param element   The UI element
     * @param breathing Whether to apply breathing animation
     */
    public void applyBreathingAnimation(Node element, boolean breathing) {
        uiViewModel.applyBreathingAnimation(element, breathing);
    }

    /**
     * Apply synesthetic feedback for user actions
     *
     * @param element  The UI element
     * @param feedback The feedback type
     */
    public void applySynestheticFeedback(Node element, FeedbackAnimationService.FeedbackType feedback) {
        uiViewModel.applySynestheticFeedback(element, feedback);
    }

    /**
     * Clean up resources when the controller is no longer needed
     */
    public void shutdown() {
        uiViewModel.shutdown();
        LOG.debug("ModernUIController shutdown complete");
    }
}