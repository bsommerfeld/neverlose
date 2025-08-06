package de.bsommerfeld.neverlose.fx.viewmodel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.neverlose.fx.animation.BreathingAnimationService;
import de.bsommerfeld.neverlose.fx.animation.FeedbackAnimationService;
import de.bsommerfeld.neverlose.fx.animation.MicroNarrativeAnimationService;
import de.bsommerfeld.neverlose.fx.theme.ContextualThemeService;
import de.bsommerfeld.neverlose.fx.theme.EmotionalThemeService;
import de.bsommerfeld.neverlose.fx.theme.TemporalThemeService;
import de.bsommerfeld.neverlose.fx.theme.TimeBasedThemeService;
import de.bsommerfeld.neverlose.fx.tracking.UsageTrackingService;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * ViewModel for UI operations that coordinates different UI services. Provides a clean API for controllers to use
 * modern UI features.
 */
@Singleton
public class UIViewModel {

    private static final LogFacade LOG = LogFacadeFactory.getLogger();

    private final MicroNarrativeAnimationService microNarrativeAnimationService;
    private final BreathingAnimationService breathingAnimationService;
    private final FeedbackAnimationService feedbackAnimationService;
    private final TimeBasedThemeService timeBasedThemeService;
    private final EmotionalThemeService emotionalThemeService;
    private final TemporalThemeService temporalThemeService;
    private final ContextualThemeService contextualThemeService;
    private final UsageTrackingService usageTrackingService;

    @Inject
    public UIViewModel(
            MicroNarrativeAnimationService microNarrativeAnimationService,
            BreathingAnimationService breathingAnimationService,
            FeedbackAnimationService feedbackAnimationService,
            TimeBasedThemeService timeBasedThemeService,
            EmotionalThemeService emotionalThemeService,
            TemporalThemeService temporalThemeService,
            ContextualThemeService contextualThemeService,
            UsageTrackingService usageTrackingService) {
        this.microNarrativeAnimationService = microNarrativeAnimationService;
        this.breathingAnimationService = breathingAnimationService;
        this.feedbackAnimationService = feedbackAnimationService;
        this.timeBasedThemeService = timeBasedThemeService;
        this.emotionalThemeService = emotionalThemeService;
        this.temporalThemeService = temporalThemeService;
        this.contextualThemeService = contextualThemeService;
        this.usageTrackingService = usageTrackingService;

        LOG.debug("UIViewModel initialized");
    }

    /**
     * Initialize the UI services with the application scene
     *
     * @param scene The main application scene
     */
    public void initialize(Scene scene) {
        Parent root = scene.getRoot();

        // Initialize services that need the scene or root
        timeBasedThemeService.initializeTimeBasedTheme(root);
        usageTrackingService.initialize(scene);

        LOG.debug("UI services initialized with scene");
    }

    /**
     * Apply micro-narrative animation when adding a new element
     *
     * @param element The element being added
     */
    public void applyAddAnimation(Node element) {
        microNarrativeAnimationService.applyAddAnimation(element);
    }

    /**
     * Apply micro-narrative animation when removing an element
     *
     * @param element    The element being removed
     * @param onFinished Callback to execute after animation completes
     */
    public void applyRemoveAnimation(Node element, Runnable onFinished) {
        microNarrativeAnimationService.applyRemoveAnimation(element, onFinished);
    }

    /**
     * Apply breathing animation to important elements
     *
     * @param element   The UI element
     * @param breathing Whether to apply breathing animation
     */
    public void applyBreathingAnimation(Node element, boolean breathing) {
        breathingAnimationService.applyBreathingAnimation(element, breathing);
    }

    /**
     * Apply synesthetic feedback for user actions
     *
     * @param element  The UI element
     * @param feedback The feedback type
     */
    public void applySynestheticFeedback(Node element, FeedbackAnimationService.FeedbackType feedback) {
        feedbackAnimationService.applySynestheticFeedback(element, feedback);
    }

    /**
     * Apply temporal layering to elements based on their state
     *
     * @param element The UI element
     * @param state   The temporal state (past, present, future)
     */
    public void applyTemporalState(Node element, TemporalThemeService.TemporalState state) {
        temporalThemeService.applyTemporalState(element, state);
    }

    /**
     * Apply emotional geometry based on context
     *
     * @param element The UI element
     * @param state   The emotional state
     */
    public void applyEmotionalState(Node element, EmotionalThemeService.EmotionalState state) {
        emotionalThemeService.applyEmotionalState(element, state);
    }

    /**
     * Apply stress-responsive interface based on user behavior
     *
     * @param container   The container element
     * @param stressLevel The detected stress level
     */
    public void applyStressResponse(Parent container, ContextualThemeService.StressLevel stressLevel) {
        contextualThemeService.applyStressResponse(container, stressLevel);
    }

    /**
     * Apply metamorphic layout based on task context
     *
     * @param container The container element
     * @param context   The task context
     */
    public void applyTaskContext(Parent container, ContextualThemeService.TaskContext context) {
        contextualThemeService.applyTaskContext(container, context);
    }

    /**
     * Track usage of UI elements to implement adaptive intelligence
     *
     * @param elementId Identifier for the UI element
     */
    public void trackElementUsage(String elementId) {
        usageTrackingService.trackElementUsage(elementId);
    }

    /**
     * Clean up resources when the ViewModel is no longer needed
     */
    public void shutdown() {
        microNarrativeAnimationService.shutdown();
        breathingAnimationService.shutdown();
        feedbackAnimationService.shutdown();
        timeBasedThemeService.shutdown();
        emotionalThemeService.shutdown();
        temporalThemeService.shutdown();
        contextualThemeService.shutdown();
        usageTrackingService.shutdown();

        LOG.debug("UIViewModel shutdown complete");
    }
}