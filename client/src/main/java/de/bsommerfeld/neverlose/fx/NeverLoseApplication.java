package de.bsommerfeld.neverlose.fx;

import de.bsommerfeld.neverlose.Main;
import de.bsommerfeld.neverlose.fx.controller.NeverLoseMetaController;
import de.bsommerfeld.neverlose.fx.ui.ModernUIController;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main JavaFX application class for Neverlose. Implements modern UI design elements for 2025/2026.
 */
public class NeverLoseApplication extends Application {

    private final LogFacade log = LogFacadeFactory.getLogger();
    private ModernUIController modernUIController;

    @Override
    public void start(Stage stage) {
        log.info("Starting Neverlose application with modern UI");
        ViewProvider viewProvider = Main.getInjector().getInstance(ViewProvider.class);
        Parent root = viewProvider.requestView(NeverLoseMetaController.class).parent();
        Scene scene = new Scene(root);

        // Centrally load the application stylesheets
        scene.getStylesheets().add("/de/bsommerfeld/neverlose/fx/css/style.css");
        scene.getStylesheets().add("/de/bsommerfeld/neverlose/fx/css/modern-ui.css");

        // Get the modern UI controller from the injector and initialize it with the scene
        modernUIController = Main.getInjector().getInstance(ModernUIController.class);
        modernUIController.initialize(scene);

        stage.setScene(scene);
        stage.getIcons().setAll(new Image("de/bsommerfeld/neverlose/fx/icon.png"));
        stage.setTitle("Neverlose " + Main.getNeverloseVersion());
        stage.show();

        // Apply breathing animation to important elements
        Platform.runLater(() -> {
            // Find and animate the new plan button
            scene.getRoot().lookupAll(".editor-action-button").forEach(
                    node -> modernUIController.applyBreathingAnimation(node, true)
            );

            // Log the number of elements found for debugging
            int count = scene.getRoot().lookupAll(".editor-action-button").size();
            log.debug("Found " + count + " elements with class .editor-action-button for breathing animation");
        });
    }

    @Override
    public void stop() {
        // Clean up resources
        if (modernUIController != null) {
            modernUIController.shutdown();
        }
    }

    /**
     * Get the modern UI controller
     *
     * @return The ModernUIController instance
     */
    public ModernUIController getModernUIController() {
        return modernUIController;
    }
}
