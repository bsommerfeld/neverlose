package de.bsommerfeld.neverlose.fx;

import de.bsommerfeld.neverlose.Main;
import de.bsommerfeld.neverlose.fx.controller.NeverLoseMetaController;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main JavaFX application class for Neverlose. Implements modern UI design elements for 2025/2026.
 */
public class NeverLoseApplication extends Application {

    private final LogFacade log = LogFacadeFactory.getLogger();

    @Override
    public void start(Stage stage) {
        log.info("Starting Neverlose application with modern UI");
        ViewProvider viewProvider = Main.getInjector().getInstance(ViewProvider.class);
        Parent root = viewProvider.requestView(NeverLoseMetaController.class).parent();
        Scene scene = new Scene(root);

        // Centrally load the application stylesheets
        scene.getStylesheets().add("/de/bsommerfeld/neverlose/fx/css/style.css");
        scene.getStylesheets().add("/de/bsommerfeld/neverlose/fx/css/modern-ui.css");

        stage.setScene(scene);
        stage.getIcons().setAll(new Image("de/bsommerfeld/neverlose/fx/icon.png"));
        stage.setTitle("Neverlose " + Main.getNeverloseVersion());
        stage.show();
    }
}
