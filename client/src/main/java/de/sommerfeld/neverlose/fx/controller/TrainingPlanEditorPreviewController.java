package de.sommerfeld.neverlose.fx.controller;

import de.sommerfeld.neverlose.fx.view.View;
import de.sommerfeld.neverlose.fx.viewmodel.ExerciseViewModel;
import de.sommerfeld.neverlose.fx.viewmodel.TrainingPlanEditorViewModel;
import de.sommerfeld.neverlose.fx.viewmodel.TrainingUnitViewModel;
import de.sommerfeld.neverlose.logger.LogFacade;
import de.sommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@View
public class TrainingPlanEditorPreviewController {

    private final LogFacade log = LogFacadeFactory.getLogger();

    private final Map<ObservableValue<?>, ChangeListener<?>> previewChangeListeners = new HashMap<>();
    private final Map<ObservableList<?>, ListChangeListener<?>> previewListChangeListeners = new HashMap<>();
    private TrainingPlanEditorViewModel viewModel;

    @FXML
    private VBox previewVBox;
    @FXML
    private Label previewPlanNameLabel;
    @FXML
    private Text previewPlanDescriptionText;
    @FXML
    private Label unitsPreviewLabel;
    @FXML
    private VBox previewUnitsContainer;

    public void initialize() {
        log.info("Preview Controller initialized (pending ViewModel).");
        previewVBox.getStyleClass().add("preview-container");
    }

    /**
     * Injects the ViewModel and triggers UI binding/listener setup.
     * This method should be called by the MetaController after FXML loading.
     *
     * @param viewModel The shared ViewModel.
     */
    public void initViewModel(TrainingPlanEditorViewModel viewModel) {
        Objects.requireNonNull(viewModel, "ViewModel cannot be null for Preview Controller");
        if (this.viewModel != null) {
            System.err.println("Warning: ViewModel already set in Preview Controller.");
            cleanupListeners();
        }
        this.viewModel = viewModel;
        log.info("ViewModel received.");
        bindPreviewUI();
        setupPreviewListeners();
        updatePreview();
    }

    /**
     * Binds the top-level preview elements that directly map to ViewModel properties.
     */
    private void bindPreviewUI() {
        Objects.requireNonNull(viewModel, "ViewModel is required for binding preview UI");
        log.info("Binding direct UI elements...");

        previewPlanNameLabel.textProperty().bind(viewModel.planNameProperty());
        previewPlanNameLabel.getStyleClass().add("preview-title");

        previewPlanDescriptionText.textProperty().bind(viewModel.planDescriptionProperty());
        previewPlanDescriptionText.getStyleClass().add("preview-desc");

        unitsPreviewLabel.visibleProperty().bind(Bindings.isEmpty(previewUnitsContainer.getChildren()).not());

        log.info("Direct UI elements bound.");
    }

    /**
     * Unbinds the top-level preview elements.
     */
    private void unbindPreviewUI() {
        if (viewModel == null) return;
        log.info("Unbinding direct UI elements...");
        if (previewPlanNameLabel.textProperty().isBound()) {
            previewPlanNameLabel.textProperty().unbind();
        }
        if (previewPlanDescriptionText.textProperty().isBound()) {
            previewPlanDescriptionText.textProperty().unbind();
        }
        previewPlanNameLabel.setText("[Plan Name Preview]");
        previewPlanDescriptionText.setText("[Plan description preview text will appear here]");
        log.info("Direct UI elements unbound.");
    }

    /**
     * Sets up the listeners needed for the preview pane to update dynamically
     * when the ViewModel's data changes.
     */
    private void setupPreviewListeners() {
        Objects.requireNonNull(viewModel, "ViewModel is required for setting up preview listeners");
        log.info("Setting up dynamic listeners...");
        addPreviewListChangeListener(viewModel.trainingUnitsProperty());
        log.info("Dynamic listeners set up.");
    }

    /**
     * Main method to refresh the entire preview display based on the current ViewModel state.
     */
    private void updatePreview() {
        log.info("Updating Preview...");
        if (viewModel == null || previewUnitsContainer == null) {
            System.err.println("Cannot update preview, ViewModel or container is null.");
            return;
        }

        previewUnitsContainer.getChildren().clear();

        if (viewModel.trainingUnitsProperty() != null) {
            for (TrainingUnitViewModel unitVm : viewModel.trainingUnitsProperty()) {
                previewUnitsContainer.getChildren().add(createUnitPreviewNode(unitVm));
            }
        } else {
            System.err.println("Training units property list is null.");
        }
        log.info("Preview update complete.");
    }


    private VBox createUnitPreviewNode(TrainingUnitViewModel unitVm) {
        VBox unitBox = new VBox(5.0);
        unitBox.getStyleClass().add("preview-unit-box");

        Label unitHeader = new Label(unitVm.getModel().getName() + " - " + unitVm.getModel().getWeekday());
        unitHeader.getStyleClass().add("preview-unit-header");

        Text unitDesc = new Text(unitVm.getModel().getDescription());
        unitDesc.getStyleClass().add("preview-unit-desc");
        unitDesc.setWrappingWidth(475);

        unitBox.getChildren().addAll(unitHeader, unitDesc);

        ObservableList<ExerciseViewModel> exercises = unitVm.exercisesProperty();
        if (exercises != null && !exercises.isEmpty()) {
            VBox exercisesContainer = new VBox(8.0);
            exercisesContainer.setPadding(new Insets(10, 0, 0, 15));
            exercisesContainer.getStyleClass().add("preview-exercises-container");

            Label exercisesTitle = new Label("Exercises:");
            exercisesTitle.getStyleClass().add("preview-exercises-title");
            exercisesContainer.getChildren().add(exercisesTitle);

            for (ExerciseViewModel exVm : exercises) {
                exercisesContainer.getChildren().add(createExercisePreviewNode(exVm));
            }
            unitBox.getChildren().add(exercisesContainer);
        } else {
            Label noExercisesLabel = new Label("No exercises in this unit.");
            noExercisesLabel.setStyle("-fx-font-style: italic; -fx-padding: 0 0 0 15px;");
            unitBox.getChildren().add(noExercisesLabel);
        }

        return unitBox;
    }

    private VBox createExercisePreviewNode(ExerciseViewModel exVm) {
        VBox exerciseBox = new VBox(2.0);
        exerciseBox.getStyleClass().add("preview-exercise-box");

        Label exName = new Label(exVm.getModel().getName());
        exName.getStyleClass().add("preview-exercise-name");

        Text exDesc = new Text(exVm.getModel().getDescription());
        exDesc.setWrappingWidth(450);
        exDesc.getStyleClass().add("preview-exercise-desc");

        String details = String.format("Duration: %s | Sets: %d | Ball Bucket: %s",
                exVm.getModel().getDuration(),
                exVm.getModel().getSets(),
                exVm.getModel().isBallBucket() ? "Yes" : "No");
        Label exDetails = new Label(details);
        exDetails.getStyleClass().add("preview-exercise-details");

        exerciseBox.getChildren().addAll(exName, exDesc, exDetails);
        return exerciseBox;
    }

    /**
     * Call this method when the view associated with this controller is being closed
     * or disposed of to prevent memory leaks by removing all listeners.
     * Should be called by the MetaController.
     */
    public void cleanupListeners() {
        log.info("Cleaning up preview listeners...");

        unbindPreviewUI();

        if (viewModel != null && viewModel.trainingUnitsProperty() != null) {
            removePreviewListChangeListener(viewModel.trainingUnitsProperty());
        }

        previewChangeListeners.clear();
        previewListChangeListeners.clear();

        log.info("Listener cleanup complete.");
    }

    /**
     * Adds a ChangeListener that triggers updatePreview and stores it for removal.
     */
    private <T> void addPreviewChangeListener(ObservableValue<T> property) {
        if (property != null && !previewChangeListeners.containsKey(property)) {
            ChangeListener<T> listener = (obs, ov, nv) -> updatePreview();
            property.addListener(listener);
            previewChangeListeners.put(property, listener);
        }
    }

    /**
     * Removes a ChangeListener previously added.
     */
    private <T> void removePreviewChangeListener(ObservableValue<T> property) {
        if (property != null && previewChangeListeners.containsKey(property)) {
            @SuppressWarnings("unchecked")
            ChangeListener<T> listener = (ChangeListener<T>) previewChangeListeners.remove(property);
            property.removeListener(listener);
        }
    }

    /**
     * Adds a ListChangeListener that handles adds/removes recursively and triggers updatePreview.
     */
    private <T> void addPreviewListChangeListener(ObservableList<T> list) {
        if (list != null && !previewListChangeListeners.containsKey(list)) {
            ListChangeListener<T> listener = change -> {
                log.info("Preview List Change Detected: " + change);
                boolean changed = false;
                while (change.next()) {
                    changed = true;
                    if (change.wasAdded()) {
                        for (T addedItem : change.getAddedSubList()) {
                            if (addedItem instanceof TrainingUnitViewModel) {
                                addDeepListenersForPreview((TrainingUnitViewModel) addedItem);
                            } else if (addedItem instanceof ExerciseViewModel) {
                                addDeepListenersForPreview((ExerciseViewModel) addedItem);
                            }
                        }
                    }
                    if (change.wasRemoved()) {
                        for (T removedItem : change.getRemoved()) {
                            if (removedItem instanceof TrainingUnitViewModel) {
                                removeDeepListenersForPreview((TrainingUnitViewModel) removedItem);
                            } else if (removedItem instanceof ExerciseViewModel) {
                                removeDeepListenersForPreview((ExerciseViewModel) removedItem);
                            }
                        }
                    }
                }
                if (changed) {
                    updatePreview();
                }
            };
            list.addListener(listener);
            previewListChangeListeners.put(list, listener);

            for (T item : list) {
                if (item instanceof TrainingUnitViewModel) {
                    addDeepListenersForPreview((TrainingUnitViewModel) item);
                } else if (item instanceof ExerciseViewModel) {
                    addDeepListenersForPreview((ExerciseViewModel) item);
                }
            }
        }
    }

    /**
     * Removes a ListChangeListener and recursively removes listeners from current items.
     */
    private <T> void removePreviewListChangeListener(ObservableList<T> list) {
        if (list != null && previewListChangeListeners.containsKey(list)) {
            @SuppressWarnings("unchecked")
            ListChangeListener<T> listener = (ListChangeListener<T>) previewListChangeListeners.remove(list);
            list.removeListener(listener);

            for (T item : list) {
                if (item instanceof TrainingUnitViewModel) {
                    removeDeepListenersForPreview((TrainingUnitViewModel) item);
                } else if (item instanceof ExerciseViewModel) {
                    removeDeepListenersForPreview((ExerciseViewModel) item);
                }
            }
        }
    }

    private void addDeepListenersForPreview(TrainingUnitViewModel unitVm) {
        if (unitVm == null) return;
        addPreviewChangeListener(unitVm.nameProperty());
        addPreviewChangeListener(unitVm.descriptionProperty());
        addPreviewChangeListener(unitVm.weekdayProperty());
        addPreviewListChangeListener(unitVm.exercisesProperty());
    }

    private void removeDeepListenersForPreview(TrainingUnitViewModel unitVm) {
        if (unitVm == null) return;
        removePreviewChangeListener(unitVm.nameProperty());
        removePreviewChangeListener(unitVm.descriptionProperty());
        removePreviewChangeListener(unitVm.weekdayProperty());
        removePreviewListChangeListener(unitVm.exercisesProperty());
    }

    private void addDeepListenersForPreview(ExerciseViewModel exVm) {
        if (exVm == null) return;
        addPreviewChangeListener(exVm.nameProperty());
        addPreviewChangeListener(exVm.descriptionProperty());
        addPreviewChangeListener(exVm.durationProperty());
        addPreviewChangeListener(exVm.setsProperty());
        addPreviewChangeListener(exVm.ballBucketProperty());
    }

    private void removeDeepListenersForPreview(ExerciseViewModel exVm) {
        if (exVm == null) return;
        removePreviewChangeListener(exVm.nameProperty());
        removePreviewChangeListener(exVm.descriptionProperty());
        removePreviewChangeListener(exVm.durationProperty());
        removePreviewChangeListener(exVm.setsProperty());
        removePreviewChangeListener(exVm.ballBucketProperty());
    }

}