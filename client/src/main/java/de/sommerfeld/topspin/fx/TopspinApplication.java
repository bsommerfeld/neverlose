package de.sommerfeld.topspin.fx;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.sommerfeld.topspin.bootstrap.TopspinModule;
import de.sommerfeld.topspin.fx.controller.TopspinMetaController;
import de.sommerfeld.topspin.fx.controller.TrainingPlanEditorController;
import de.sommerfeld.topspin.fx.view.ViewProvider;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TopspinApplication extends Application {

    private final Injector injector;

    public TopspinApplication() {
        this.injector = Guice.createInjector(new TopspinModule());
    }

    @Override
    public void start(Stage stage) {
        ViewProvider viewProvider = injector.getInstance(ViewProvider.class);
        Parent root = viewProvider.requestView(TopspinMetaController.class).parent();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Topspin");
        stage.setOnCloseRequest(closeRequest -> {
            TrainingPlanEditorController controller = viewProvider.requestView(TrainingPlanEditorController.class).controller();
            controller.cleanupListeners();
        });
        stage.show();
    }
}
