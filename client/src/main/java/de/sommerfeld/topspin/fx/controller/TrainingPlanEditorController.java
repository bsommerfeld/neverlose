package de.sommerfeld.topspin.fx.controller;

import de.sommerfeld.topspin.fx.view.View;
import de.sommerfeld.topspin.fx.viewmodel.ExerciseViewModel;
import de.sommerfeld.topspin.fx.viewmodel.TrainingPlanEditorViewModel;
import de.sommerfeld.topspin.fx.viewmodel.TrainingUnitViewModel;
import de.sommerfeld.topspin.plan.TrainingPlan;
import de.sommerfeld.topspin.plan.components.Weekday;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@View
public class TrainingPlanEditorController {

    private final Map<ObservableValue<?>, ChangeListener<?>> previewChangeListeners = new HashMap<>();
    private final Map<ObservableList<?>, ListChangeListener<?>> previewListChangeListeners = new HashMap<>();

    private TrainingPlanEditorViewModel viewModel;

    @FXML
    private TextField planNameTextField;
    @FXML
    private TextArea planDescriptionTextArea;

    @FXML
    private ListView<TrainingUnitViewModel> trainingUnitsListView;
    @FXML
    private Button addUnitButton;
    @FXML
    private Button removeUnitButton;

    @FXML
    private TitledPane selectedUnitPane;
    @FXML
    private TextField unitNameTextField;
    @FXML
    private TextArea unitDescriptionTextArea;
    @FXML
    private ChoiceBox<Weekday> unitWeekdayChoiceBox;

    @FXML
    private ListView<ExerciseViewModel> trainingExercisesListView;
    @FXML
    private Button addExerciseButton;
    @FXML
    private Button removeExerciseButton;

    @FXML
    private TitledPane selectedExercisePane;
    @FXML
    private TextField exerciseNameTextField;
    @FXML
    private TextArea exerciseDescriptionTextArea;
    @FXML
    private TextField exerciseDurationTextField;
    @FXML
    private TextField exerciseSetsTextField; // Maybe Spinner<Integer>
    @FXML
    private CheckBox exerciseBallBucketCheckBox;

    @FXML
    private Button exportPdfButton;

    @FXML
    private VBox previewVBox;
    @FXML
    private Label previewPlanNameLabel;
    @FXML
    private Text previewPlanDescriptionText;
    @FXML
    private VBox previewUnitsContainer;
    private ChangeListener<String> textToVmSetsListener = null;
    private ChangeListener<Number> vmToTextSetsListener = null;
    private IntegerProperty currentlyBoundSetsProperty = null;
    private final ChangeListener<ExerciseViewModel> selectedExerciseListener =
            (obs, oldExVm, newExVm) -> onSelectedExerciseChanged(oldExVm, newExVm);
    private final ChangeListener<TrainingUnitViewModel> selectedUnitListener =
            (obs, oldUnitVm, newUnitVm) -> onSelectedUnitChanged(oldUnitVm, newUnitVm);

    @FXML
    public void initialize() {
        this.viewModel = new TrainingPlanEditorViewModel();
        bindUI();
    }

    /**
     * Loads a new TrainingPlan into the editor.
     * It achieves this by unbinding the UI from the current ViewModel state,
     * telling the ViewModel to load the new plan's data, and then re-binding
     * the UI to the updated ViewModel state.
     *
     * @param plan The new TrainingPlan to load.
     */
    public void setPlan(TrainingPlan plan) {
        System.out.println("Controller: Starting setPlan...");
        unbindUI();

        Objects.requireNonNull(viewModel, "ViewModel must not be null when setting a plan.");
        viewModel.setTrainingPlan(plan);

        bindUI();
        System.out.println("Controller: setPlan complete.");
    }

    /**
     * Removes all bindings and listeners connecting UI elements to the ViewModel.
     * Resets UI elements to a default state.
     */
    private void unbindUI() {
        System.out.println("Controller: Unbinding UI...");

        planNameTextField.textProperty().unbindBidirectional(viewModel.planNameProperty());
        planDescriptionTextArea.textProperty().unbindBidirectional(viewModel.planDescriptionProperty());

        // Important: Unbind the ViewModel's selected property *before* clearing items/selection listener
        viewModel.selectedTrainingUnitProperty().unbind();
        trainingUnitsListView.setItems(null);

        removeUnitButton.disableProperty().unbind();
        selectedUnitPane.disableProperty().unbind();

        viewModel.selectedTrainingUnitProperty().removeListener(selectedUnitListener);

        cleanupDetailPanes();
        cleanupListeners();

        planNameTextField.clear();
        planDescriptionTextArea.clear();
        removeUnitButton.setDisable(true);
        selectedUnitPane.setDisable(true);
        selectedExercisePane.setDisable(true);
        previewPlanNameLabel.textProperty().unbind();
        previewPlanDescriptionText.textProperty().unbind();
        previewPlanNameLabel.setText("Plan Name"); // Default text
        previewPlanDescriptionText.setText("");
        previewUnitsContainer.getChildren().clear();

        System.out.println("Controller: UI Unbound.");
    }

    /**
     * Establishes all bindings and listeners connecting UI elements to the current ViewModel state.
     * Should mirror the setup logic from the original initialize method.
     */
    private void bindUI() {
        System.out.println("Controller: Binding UI...");
        Objects.requireNonNull(viewModel, "ViewModel cannot be null during bindUI");

        planNameTextField.textProperty().bindBidirectional(viewModel.planNameProperty());
        planDescriptionTextArea.textProperty().bindBidirectional(viewModel.planDescriptionProperty());

        trainingUnitsListView.setItems(viewModel.trainingUnitsProperty());
        viewModel.selectedTrainingUnitProperty().bind(trainingUnitsListView.getSelectionModel().selectedItemProperty());

        removeUnitButton.disableProperty().bind(viewModel.selectedTrainingUnitProperty().isNull());
        selectedUnitPane.disableProperty().bind(viewModel.selectedTrainingUnitProperty().isNull());

        viewModel.selectedTrainingUnitProperty().addListener(selectedUnitListener);

        onSelectedUnitChanged(null, viewModel.selectedTrainingUnitProperty().get());

        unitWeekdayChoiceBox.setItems(FXCollections.observableArrayList(Weekday.values()));

        addUnitButton.setOnAction(event -> {
            viewModel.addTrainingUnit();
            selectAndScrollToLast(trainingUnitsListView, viewModel.trainingUnitsProperty());
        });
        removeUnitButton.setOnAction(event -> viewModel.removeSelectedTrainingUnit());
        addExerciseButton.setOnAction(event -> {
            viewModel.addExerciseToSelectedUnit();
            TrainingUnitViewModel currentUnit = viewModel.selectedTrainingUnitProperty().get();
            if (currentUnit != null) {
                selectAndScrollToLast(trainingExercisesListView, currentUnit.exercisesProperty());
            }
        });
        removeExerciseButton.setOnAction(event -> viewModel.removeSelectedExerciseFromSelectedUnit());
        exportPdfButton.setOnAction(event -> handleExportPdfAction());

        previewVBox.getStyleClass().add("preview-container");
        previewPlanNameLabel.textProperty().bind(viewModel.planNameProperty());
        previewPlanNameLabel.getStyleClass().add("preview-title");
        previewPlanDescriptionText.textProperty().bind(viewModel.planDescriptionProperty());
        previewPlanDescriptionText.getStyleClass().add("preview-desc");
        setupPreviewListeners();

        updatePreview();

        System.out.println("Controller: UI Bound.");
    }

    /**
     * Helper method to select and scroll to the last item in a ListView
     */
    private <T> void selectAndScrollToLast(ListView<T> listView, ObservableList<T> items) {
        if (listView != null && items != null) {
            int lastIndex = items.size() - 1;
            if (lastIndex >= 0) {
                listView.getSelectionModel().select(lastIndex);
                listView.scrollTo(lastIndex);
            }
        }
    }

    /**
     * Manually cleans up bindings and state related to the Unit and Exercise detail panes.
     * Called during unbindUI.
     */
    private void cleanupDetailPanes() {
        // Get current selections *before* detaching listeners
        TrainingUnitViewModel currentUnit = viewModel.selectedTrainingUnitProperty().get();
        ExerciseViewModel currentExercise = null;
        if (currentUnit != null) {
            // Temporarily unbind the exercise selection from list view to read it
            currentUnit.selectedExerciseProperty().unbind();
            currentExercise = currentUnit.selectedExerciseProperty().get();
            // Remove exercise listener if it was attached
            currentUnit.selectedExerciseProperty().removeListener(selectedExerciseListener);
        }

        // Simulate unbinding exercise details
        onSelectedExerciseChanged(currentExercise, null);

        // Simulate unbinding unit details
        onSelectedUnitChanged(currentUnit, null);

        // Ensure detail list views are cleared
        trainingExercisesListView.setItems(null);

        // Explicitly disable exercise remove button (its binding is complex)
        removeExerciseButton.disableProperty().unbind(); // Make sure it's unbound
        removeExerciseButton.setDisable(true);
        selectedExercisePane.setDisable(true);
    }

    /**
     * Sets up the listeners needed for the preview pane to update dynamically.
     * Called during bindUI.
     */
    private void setupPreviewListeners() {
        addPreviewChangeListener(viewModel.planNameProperty());
        addPreviewChangeListener(viewModel.planDescriptionProperty());
        addPreviewListChangeListener(viewModel.trainingUnitsProperty()); // This recursively adds listeners for items

        // Note: Listeners for items *within* the lists (units, exercises) are handled
        // recursively by addPreviewListChangeListener / removePreviewListChangeListener
    }

    private void handleExportPdfAction() {
        if (viewModel == null || viewModel.getTrainingPlanModel() == null) {
            showError("Cannot Export", "No training plan data available to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Training Plan as PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf")
        );

        String planName = viewModel.planNameProperty().get();
        String initialFileName = "TrainingPlan.pdf"; // Default
        if (planName != null && !planName.trim().isEmpty()) {
            // Remove/replace characters potentially invalid in filenames
            initialFileName = planName.trim().replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + ".pdf";
        }
        fileChooser.setInitialFileName(initialFileName);

        Window ownerWindow = exportPdfButton.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(ownerWindow);

        if (selectedFile != null) {
            if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".pdf");
            }

            System.out.println("Attempting to export PDF to: " + selectedFile.getAbsolutePath());

            try {
                viewModel.exportPlanToPdf(selectedFile);
                showConfirmation("Export Successful", "Training plan exported successfully to:\n" + selectedFile.getName());

            } catch (IOException e) {
                System.err.println("Error exporting PDF: " + e.getMessage());
                e.printStackTrace();
                showError("Export Failed", "Could not export training plan to PDF.\nError: " + e.getMessage());

            } catch (Exception e) {
                System.err.println("Unexpected error during PDF export: " + e.getMessage());
                e.printStackTrace();
                showError("Export Failed", "An unexpected error occurred during PDF export.");
            }
        } else {
            System.out.println("PDF Export cancelled by user.");
        }
    }

    private void showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void onSelectedUnitChanged(TrainingUnitViewModel oldSelectedUnit, TrainingUnitViewModel newSelectedUnit) {
        if (oldSelectedUnit != null) {
            unitNameTextField.textProperty().unbindBidirectional(oldSelectedUnit.nameProperty());
            unitDescriptionTextArea.textProperty().unbindBidirectional(oldSelectedUnit.descriptionProperty());
            unitWeekdayChoiceBox.valueProperty().unbindBidirectional(oldSelectedUnit.weekdayProperty());
            if (trainingExercisesListView.itemsProperty().isBound()) {
                trainingExercisesListView.itemsProperty().unbind();
            }

            oldSelectedUnit.selectedExerciseProperty().unbind();
            oldSelectedUnit.selectedExerciseProperty().removeListener(selectedExerciseListener);
        } else {
            unitNameTextField.clear();
            unitDescriptionTextArea.clear();
            unitWeekdayChoiceBox.setValue(null);
            trainingExercisesListView.setItems(null);
        }

        if (newSelectedUnit != null) {
            unitNameTextField.textProperty().bindBidirectional(newSelectedUnit.nameProperty());
            unitDescriptionTextArea.textProperty().bindBidirectional(newSelectedUnit.descriptionProperty());
            unitWeekdayChoiceBox.valueProperty().bindBidirectional(newSelectedUnit.weekdayProperty());

            trainingExercisesListView.setItems(newSelectedUnit.exercisesProperty());
            newSelectedUnit.selectedExerciseProperty().bind(trainingExercisesListView.getSelectionModel().selectedItemProperty());

            newSelectedUnit.selectedExerciseProperty().addListener(selectedExerciseListener);
            // Trigger exercise listener manually for initial state
            onSelectedExerciseChanged(null, newSelectedUnit.selectedExerciseProperty().get());

            if (removeExerciseButton.disableProperty().isBound())
                removeExerciseButton.disableProperty().unbind(); // Unbind previous first
            removeExerciseButton.disableProperty().bind(newSelectedUnit.selectedExerciseProperty().isNull());

        } else {
            unitNameTextField.clear();
            unitDescriptionTextArea.clear();
            unitWeekdayChoiceBox.setValue(null);
            trainingExercisesListView.setItems(null);

            // Ensure exercise details are also cleared/disabled and unbound
            onSelectedExerciseChanged(null, null);
            if (removeExerciseButton.disableProperty().isBound()) removeExerciseButton.disableProperty().unbind();
            removeExerciseButton.setDisable(true);
        }
    }

    private void unbindSetsTextField() {
        if (textToVmSetsListener != null) {
            exerciseSetsTextField.textProperty().removeListener(textToVmSetsListener);
            textToVmSetsListener = null;
        }
        if (vmToTextSetsListener != null && currentlyBoundSetsProperty != null) {
            currentlyBoundSetsProperty.removeListener(vmToTextSetsListener);
            vmToTextSetsListener = null;
        }
        currentlyBoundSetsProperty = null;
    }

    private void onSelectedExerciseChanged(ExerciseViewModel oldSelectedExercise, ExerciseViewModel newSelectedExercise) {
        if (oldSelectedExercise != null) {
            exerciseNameTextField.textProperty().unbindBidirectional(oldSelectedExercise.nameProperty());
            exerciseDescriptionTextArea.textProperty().unbindBidirectional(oldSelectedExercise.descriptionProperty());
            exerciseDurationTextField.textProperty().unbindBidirectional(oldSelectedExercise.durationProperty());
            exerciseBallBucketCheckBox.selectedProperty().unbindBidirectional(oldSelectedExercise.ballBucketProperty());
            unbindSetsTextField();
        } else {
            exerciseNameTextField.clear();
            exerciseDescriptionTextArea.clear();
            exerciseDurationTextField.clear();
            exerciseBallBucketCheckBox.setSelected(false);
            unbindSetsTextField();
            exerciseSetsTextField.clear();
        }

        if (newSelectedExercise != null) {
            exerciseNameTextField.textProperty().bindBidirectional(newSelectedExercise.nameProperty());
            exerciseDescriptionTextArea.textProperty().bindBidirectional(newSelectedExercise.descriptionProperty());
            exerciseDurationTextField.textProperty().bindBidirectional(newSelectedExercise.durationProperty());
            exerciseBallBucketCheckBox.selectedProperty().bindBidirectional(newSelectedExercise.ballBucketProperty());
            bindSetsTextField(newSelectedExercise);
        } else {
            exerciseNameTextField.clear();
            exerciseDescriptionTextArea.clear();
            exerciseDurationTextField.clear();
            exerciseBallBucketCheckBox.setSelected(false);
            unbindSetsTextField();
            exerciseSetsTextField.clear();
        }

        selectedExercisePane.setDisable(newSelectedExercise == null);
    }

    private void bindSetsTextField(ExerciseViewModel exerciseVm) {
        unbindSetsTextField();

        if (exerciseVm == null) return;

        currentlyBoundSetsProperty = exerciseVm.setsProperty();

        // Initial value
        exerciseSetsTextField.setText(String.valueOf(currentlyBoundSetsProperty.get()));

        textToVmSetsListener = (obs, oldVal, newVal) -> {
            try {
                int sets = Integer.parseInt(newVal);
                if (currentlyBoundSetsProperty != null && currentlyBoundSetsProperty.get() != sets) {
                    currentlyBoundSetsProperty.set(sets);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid number for Sets: " + newVal);
                // Maybe add visual feedback for error
            }
        };
        exerciseSetsTextField.textProperty().addListener(textToVmSetsListener);

        vmToTextSetsListener = (obs, oldVal, newVal) -> {
            String currentText = exerciseSetsTextField.getText();
            String newValueStr = newVal.toString();
            if (!currentText.equals(newValueStr)) {
                exerciseSetsTextField.setText(newValueStr);
            }
        };
        currentlyBoundSetsProperty.addListener(vmToTextSetsListener);
    }

    private void updatePreview() {
        previewUnitsContainer.getChildren().clear();

        if (viewModel == null || viewModel.trainingUnitsProperty() == null) return;

        for (TrainingUnitViewModel unitVm : viewModel.trainingUnitsProperty()) {
            previewUnitsContainer.getChildren().add(createUnitPreviewNode(unitVm));
        }
    }

    private VBox createUnitPreviewNode(TrainingUnitViewModel unitVm) {
        VBox unitBox = new VBox(5.0);
        unitBox.getStyleClass().add("preview-unit-box");

        Label unitHeader = new Label(unitVm.nameProperty().get() + " - " + unitVm.weekdayProperty().get());
        unitHeader.getStyleClass().add("preview-unit-header");

        Text unitDesc = new Text(unitVm.descriptionProperty().get());
        unitDesc.getStyleClass().add("preview-unit-desc");
        unitDesc.setWrappingWidth(380);

        unitBox.getChildren().addAll(unitHeader, unitDesc);

        if (unitVm.exercisesProperty() != null && !unitVm.exercisesProperty().isEmpty()) {
            VBox exercisesContainer = new VBox(8.0);
            exercisesContainer.setPadding(new Insets(10, 0, 0, 15));
            exercisesContainer.getStyleClass().add("preview-exercises-container");

            Label exercisesTitle = new Label("Exercises:");
            exercisesTitle.getStyleClass().add("preview-exercises-title");

            exercisesContainer.getChildren().add(exercisesTitle);

            for (ExerciseViewModel exVm : unitVm.exercisesProperty()) {
                exercisesContainer.getChildren().add(createExercisePreviewNode(exVm));
            }
            unitBox.getChildren().add(exercisesContainer);
        }
        return unitBox;
    }

    private VBox createExercisePreviewNode(ExerciseViewModel exVm) {
        VBox exerciseBox = new VBox(2.0);
        exerciseBox.getStyleClass().add("preview-exercise-box");

        Label exName = new Label(exVm.nameProperty().get());
        exName.getStyleClass().add("preview-exercise-name");

        Text exDesc = new Text(exVm.descriptionProperty().get());
        exDesc.getStyleClass().add("preview-exercise-desc");

        Label exDetails = new Label(String.format("Duration: %s | Sets: %d | Ball Bucket: %s",
                exVm.durationProperty().get(),
                exVm.setsProperty().get(),
                exVm.ballBucketProperty().get() ? "Yes" : "No"));
        exDetails.getStyleClass().add("preview-exercise-details");

        exerciseBox.getChildren().addAll(exName, exDesc, exDetails);
        return exerciseBox;
    }

    /**
     * Call this method when the view associated with this controller is being closed or disposed of to prevent memory
     * leaks by removing all listeners.
     */
    public void cleanupListeners() {
        System.out.println("Controller: Cleaning up preview listeners...");
        removePreviewChangeListener(viewModel.planNameProperty());
        removePreviewChangeListener(viewModel.planDescriptionProperty());

        removePreviewListChangeListener(viewModel.trainingUnitsProperty());

        previewChangeListeners.clear();
        previewListChangeListeners.clear();

        // If selection listeners were added with weak references, they might clean up
        // automatically, otherwise manual removal might be needed if they cause leaks.
        // viewModel.selectedTrainingUnitProperty().removeListener(selectedUnitListener);
        // if (viewModel.selectedTrainingUnitProperty().get() != null) {
        //    viewModel.selectedTrainingUnitProperty().get().selectedExerciseProperty().removeListener(selectedExerciseListener);
        // }

        System.out.println("Controller: Listener cleanup complete.");
    }

    private void removeDeepListenersForPreview(TrainingUnitViewModel unitVm) {
        if (unitVm == null) return;
        removePreviewChangeListener(unitVm.nameProperty());
        removePreviewChangeListener(unitVm.descriptionProperty());
        removePreviewChangeListener(unitVm.weekdayProperty());

        removePreviewListChangeListener(unitVm.exercisesProperty());
    }

    private void removeDeepListenersForPreview(ExerciseViewModel exVm) {
        if (exVm == null) return;
        removePreviewChangeListener(exVm.nameProperty());
        removePreviewChangeListener(exVm.descriptionProperty());
        removePreviewChangeListener(exVm.durationProperty());
        removePreviewChangeListener(exVm.setsProperty());
        removePreviewChangeListener(exVm.ballBucketProperty());
    }

    private void addDeepListenersForPreview(TrainingUnitViewModel unitVm) {
        if (unitVm == null) return;
        addPreviewChangeListener(unitVm.nameProperty());
        addPreviewChangeListener(unitVm.descriptionProperty());
        addPreviewChangeListener(unitVm.weekdayProperty());

        addPreviewListChangeListener(unitVm.exercisesProperty());
    }

    private void addDeepListenersForPreview(ExerciseViewModel exVm) {
        if (exVm == null) return;
        addPreviewChangeListener(exVm.nameProperty());
        addPreviewChangeListener(exVm.descriptionProperty());
        addPreviewChangeListener(exVm.durationProperty());
        addPreviewChangeListener(exVm.setsProperty());
        addPreviewChangeListener(exVm.ballBucketProperty());
    }

    /**
     * Adds a standard ChangeListener to an ObservableValue that triggers updatePreview. Stores the listener in a map
     * for later removal. Avoids adding duplicate listeners.
     */
    private <T> void addPreviewChangeListener(ObservableValue<T> property) {
        if (property != null && !previewChangeListeners.containsKey(property)) {
            ChangeListener<T> listener = (obs, ov, nv) -> updatePreview();
            property.addListener(listener);
            previewChangeListeners.put(property, listener);
        }
    }

    /**
     * Removes the preview ChangeListener previously added to an ObservableValue.
     */
    private <T> void removePreviewChangeListener(ObservableValue<T> property) {
        if (property != null && previewChangeListeners.containsKey(property)) {
            @SuppressWarnings("unchecked")
            ChangeListener<T> listener = (ChangeListener<T>) previewChangeListeners.get(property);
            property.removeListener(listener);
            previewChangeListeners.remove(property);
        }
    }

    /**
     * Adds a ListChangeListener to an ObservableList that triggers updatePreview and recursively manages listeners for
     * added/removed items. Stores the listener in a map for later removal. Avoids adding duplicate listeners.
     */
    private <T> void addPreviewListChangeListener(ObservableList<T> list) {
        if (list != null && !previewListChangeListeners.containsKey(list)) {
            ListChangeListener<T> listener = change -> {
                while (change.next()) {
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
                    // TODO: Handle wasPermutated, wasUpdated if necessary for preview accuracy
                }
                updatePreview();
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
     * Removes the preview ListChangeListener previously added to an ObservableList. Also recursively removes listeners
     * from items *currently* in the list.
     */
    private <T> void removePreviewListChangeListener(ObservableList<T> list) {
        if (list != null && previewListChangeListeners.containsKey(list)) {
            @SuppressWarnings("unchecked")
            ListChangeListener<T> listener = (ListChangeListener<T>) previewListChangeListeners.get(list);
            list.removeListener(listener);
            previewListChangeListeners.remove(list);

            for (T item : list) {
                if (item instanceof TrainingUnitViewModel) {
                    removeDeepListenersForPreview((TrainingUnitViewModel) item);
                } else if (item instanceof ExerciseViewModel) {
                    removeDeepListenersForPreview((ExerciseViewModel) item);
                }
            }
        }
    }
}
