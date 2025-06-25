package de.sommerfeld.neverlose.fx;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.sommerfeld.neverlose.bootstrap.NeverLoseModule;
import de.sommerfeld.neverlose.fx.controller.NeverLoseMetaController;
import de.sommerfeld.neverlose.fx.view.ViewProvider;
import de.sommerfeld.neverlose.logger.LogFacade;
import de.sommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class NeverLoseApplication extends Application {

    private final Injector injector;
    private final LogFacade log = LogFacadeFactory.getLogger();

    public NeverLoseApplication() {
        this.injector = Guice.createInjector(new NeverLoseModule());
    }

    @Override
    public void start(Stage stage) {
        log.info("Starting Topspin application");
        ViewProvider viewProvider = injector.getInstance(ViewProvider.class);
        Parent root = viewProvider.requestView(NeverLoseMetaController.class).parent();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.getIcons().setAll(new Image("de/sommerfeld/neverlose/fx/logo.png"));
        stage.setTitle("NeverLose");
        stage.show();
    }
}
