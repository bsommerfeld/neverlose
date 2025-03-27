package de.sommerfeld.topspin.fx;

import de.sommerfeld.topspin.fx.controller.TrainingPlanEditorController;
import de.sommerfeld.topspin.fx.view.ViewProvider;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TopspinApplication extends Application {

    @Override
    public void start(Stage stage) {
        ViewProvider viewProvider = new ViewProvider();
        Parent root = viewProvider.requestView(TrainingPlanEditorController.class).parent();
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
