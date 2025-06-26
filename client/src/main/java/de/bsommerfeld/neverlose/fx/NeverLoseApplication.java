package de.bsommerfeld.neverlose.fx;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.bsommerfeld.neverlose.bootstrap.NeverLoseModule;
import de.bsommerfeld.neverlose.fx.controller.NeverLoseMetaController;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
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

    // Centrally load the application stylesheet
    scene.getStylesheets().add("/de/bsommerfeld/neverlose/fx/css/style.css");

    stage.setScene(scene);
    stage.getIcons().setAll(new Image("de/bsommerfeld/neverlose/fx/logo.png"));
    stage.setTitle("NeverLose");
    stage.show();
  }
}
