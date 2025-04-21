package de.sommerfeld.topspin.fx;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.sommerfeld.topspin.bootstrap.TopspinModule;
import de.sommerfeld.topspin.fx.controller.TopspinMetaController;
import de.sommerfeld.topspin.fx.view.ViewProvider;
import de.sommerfeld.topspin.logger.LogFacade;
import de.sommerfeld.topspin.logger.LogFacadeFactory;
import de.sommerfeld.topspin.persistence.guice.PersistenceModule;
import de.sommerfeld.topspin.updater.guice.UpdaterModule;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class TopspinApplication extends Application {

    private final Injector injector;
    private final LogFacade log = LogFacadeFactory.getLogger();

    public TopspinApplication() {
        this.injector = Guice.createInjector(new TopspinModule(), new UpdaterModule(), new PersistenceModule());
    }

    @Override
    public void start(Stage stage) {
        log.info("Starting Topspin application");
        ViewProvider viewProvider = injector.getInstance(ViewProvider.class);
        Parent root = viewProvider.requestView(TopspinMetaController.class).parent();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.getIcons().setAll(new Image("de/sommerfeld/topspin/fx/logo.png"));
        stage.setTitle("TopSpin");
        stage.show();
    }
}
