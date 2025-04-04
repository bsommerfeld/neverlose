package de.sommerfeld.topspin.fx.controller;

import com.google.inject.Inject;
import de.sommerfeld.topspin.fx.components.SearchComponent;
import de.sommerfeld.topspin.fx.view.View;
import de.sommerfeld.topspin.fx.view.ViewProvider;
import de.sommerfeld.topspin.plan.TrainingPlan;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

@View
public class TopBarController {

    private final ViewProvider viewProvider;

    @FXML
    private HBox searchComponentPlaceholder;
    private SearchComponent<TrainingPlan> searchComponent;

    @Inject
    public TopBarController(ViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }

    @FXML
    private void initialize() {
        initializeSearchComponent();
    }

    private void initializeSearchComponent() {
        ObservableList<TrainingPlan> trainingPlans = loadTrainingPlans();

        searchComponent = new SearchComponent<>(trainingPlans, TrainingPlan::getName);
        searchComponent.setPromptText("Search for training plans...");

        setTextFieldWidths(searchComponent, 450.0, 400.0);

        searchComponent.getTextField().setMaxWidth(450.0);
        searchComponent.getTextField().setPrefWidth(400.0);

        searchComponentPlaceholder.getChildren().add(searchComponent);

        searchComponent.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Choosing over component: " + newVal.getName());
                loadPlanIntoView(newVal);
            }
        });
    }

    private void setTextFieldWidths(SearchComponent<?> component, double maxWidth, double prefWidth) {
        if (!component.getChildren().isEmpty() && component.getChildren().get(0) instanceof TextField) {
            ((TextField) component.getChildren().get(0)).setMaxWidth(maxWidth);
            ((TextField) component.getChildren().get(0)).setPrefWidth(prefWidth);
        }
    }

    private void loadPlanIntoView(TrainingPlan plan) {
        System.out.println("Loading plan:" + plan.getName() + " into view...");
        viewProvider.triggerViewChange(TrainingPlanEditorMetaController.class, planEditor -> planEditor.setPlan(plan));
    }

    private ObservableList<TrainingPlan> loadTrainingPlans() {
        return FXCollections.observableArrayList(
                new TrainingPlan("Plan A", "..."),
                new TrainingPlan("Plan B", "..."),
                new TrainingPlan("Sehr langer Plan C", "...")
        );
    }
}
