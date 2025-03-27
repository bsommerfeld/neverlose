package de.sommerfeld.topspin.fx.controller;

import de.sommerfeld.topspin.fx.components.SearchComponent;
import de.sommerfeld.topspin.plan.TrainingPlan;
import de.sommerfeld.topspin.plan.components.Weekday;
import de.sommerfeld.topspin.fx.view.View;
import de.sommerfeld.topspin.fx.viewmodel.ExerciseViewModel;
import de.sommerfeld.topspin.fx.viewmodel.TrainingPlanEditorViewModel;
import de.sommerfeld.topspin.fx.viewmodel.TrainingUnitViewModel;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.beans.value.ChangeListener;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// This class IS the controller for TrainingPlanEditor.fxml
@View
public class TrainingPlanEditorController {

    // --- Listener Management for Preview Updates ---
    private final Map<ObservableValue<?>, ChangeListener<?>> previewChangeListeners = new HashMap<>();
    private final Map<ObservableList<?>, ListChangeListener<?>> previewListChangeListeners = new HashMap<>();

    // --- ViewModel ---
    private TrainingPlanEditorViewModel viewModel;

    // --- FXML Injected Fields ---

    // Top Bar -- TODO: move this to a separate controller
    @FXML
    private HBox searchComponentPlaceholder;
    private SearchComponent<TrainingPlan> searchComponent;

    // Plan Details
    @FXML
    private TextField planNameTextField;
    @FXML
    private TextArea planDescriptionTextArea;

    // Unit Management
    @FXML
    private ListView<TrainingUnitViewModel> trainingUnitsListView;
    @FXML
    private Button addUnitButton;
    @FXML
    private Button removeUnitButton;

    // Selected Unit Details
    @FXML
    private TitledPane selectedUnitPane;
    @FXML
    private TextField unitNameTextField;
    @FXML
    private TextArea unitDescriptionTextArea;
    @FXML
    private ChoiceBox<Weekday> unitWeekdayChoiceBox;

    // Exercise Management (within selected unit)
    @FXML
    private ListView<ExerciseViewModel> trainingExercisesListView;
    @FXML
    private Button addExerciseButton;
    @FXML
    private Button removeExerciseButton;

    // Selected Exercise Details
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

    // Management Area
    @FXML
    private Button exportPdfButton;

    // Preview Pane
    @FXML
    private VBox previewVBox;
    @FXML
    private Label previewPlanNameLabel;
    @FXML
    private Text previewPlanDescriptionText;
    @FXML
    private VBox previewUnitsContainer;

    // Listeners for selection changes to manage nested bindings cleanly
    // Could use WeakChangeListener if necessary to prevent memory leaks in complex scenarios
    private final ChangeListener<TrainingUnitViewModel> selectedUnitListener =
            (obs, oldUnitVm, newUnitVm) -> onSelectedUnitChanged(oldUnitVm, newUnitVm);

    private final ChangeListener<ExerciseViewModel> selectedExerciseListener =
            (obs, oldExVm, newExVm) -> onSelectedExerciseChanged(oldExVm, newExVm);


    @FXML
    public void initialize() {
        this.viewModel = new TrainingPlanEditorViewModel();
        initializeSearchComponent();

        planNameTextField.textProperty().bindBidirectional(viewModel.planNameProperty());
        planDescriptionTextArea.textProperty().bindBidirectional(viewModel.planDescriptionProperty());

        trainingUnitsListView.setItems(viewModel.trainingUnitsProperty());
        viewModel.selectedTrainingUnitProperty().bind(trainingUnitsListView.getSelectionModel().selectedItemProperty());

        addUnitButton.setOnAction(event -> viewModel.addTrainingUnit());
        removeUnitButton.setOnAction(event -> viewModel.removeSelectedTrainingUnit());
        removeUnitButton.disableProperty().bind(viewModel.selectedTrainingUnitProperty().isNull());

        selectedUnitPane.disableProperty().bind(viewModel.selectedTrainingUnitProperty().isNull());

        unitWeekdayChoiceBox.setItems(FXCollections.observableArrayList(Weekday.values()));

        viewModel.selectedTrainingUnitProperty().addListener(selectedUnitListener);
        // Trigger manually once for initial state
        onSelectedUnitChanged(null, viewModel.selectedTrainingUnitProperty().get());

        viewModel.selectedTrainingUnitProperty().bind(trainingUnitsListView.getSelectionModel().selectedItemProperty());

        addUnitButton.setOnAction(event -> {
            viewModel.addTrainingUnit();

            int lastIndex = viewModel.trainingUnitsProperty().size() - 1;
            if (lastIndex >= 0) {
                trainingUnitsListView.getSelectionModel().select(lastIndex);
                trainingUnitsListView.scrollTo(lastIndex);
            }
        });
        removeUnitButton.setOnAction(event -> viewModel.removeSelectedTrainingUnit());
        removeUnitButton.disableProperty().bind(viewModel.selectedTrainingUnitProperty().isNull());

        addExerciseButton.setOnAction(event -> viewModel.addExerciseToSelectedUnit());
        removeExerciseButton.setOnAction(event -> viewModel.removeSelectedExerciseFromSelectedUnit());
        removeExerciseButton.setDisable(true); // Initial state

        addExerciseButton.setOnAction(event -> {
            viewModel.addExerciseToSelectedUnit();

            TrainingUnitViewModel currentUnit = viewModel.selectedTrainingUnitProperty().get();
            if (currentUnit != null) {
                int lastIndex = currentUnit.exercisesProperty().size() - 1;
                if (lastIndex >= 0) {
                    trainingExercisesListView.getSelectionModel().select(lastIndex);
                    trainingExercisesListView.scrollTo(lastIndex);
                }
            }
        });
        removeExerciseButton.setOnAction(event -> viewModel.removeSelectedExerciseFromSelectedUnit());

        selectedExercisePane.setDisable(true); // Initial state

        previewVBox.getStyleClass().add("preview-container");
        previewPlanNameLabel.textProperty().bind(viewModel.planNameProperty());
        previewPlanNameLabel.getStyleClass().add("preview-title");
        previewPlanDescriptionText.textProperty().bind(viewModel.planDescriptionProperty());
        previewPlanDescriptionText.getStyleClass().add("preview-desc");

        addPreviewChangeListener(viewModel.planNameProperty());
        addPreviewChangeListener(viewModel.planDescriptionProperty());

        addPreviewListChangeListener(viewModel.trainingUnitsProperty());

        exportPdfButton.setOnAction(event -> handleExportPdfAction());

        updatePreview(); // Initial preview generation
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

    private ObservableList<TrainingPlan> loadTrainingPlans() {
        return FXCollections.observableArrayList(
                new TrainingPlan("Plan A", "..."),
                new TrainingPlan("Plan B", "..."),
                new TrainingPlan("Sehr langer Plan C", "...")
        );
    }

    private void setTextFieldWidths(SearchComponent<?> component, double maxWidth, double prefWidth) {
        if (!component.getChildren().isEmpty() && component.getChildren().get(0) instanceof TextField) {
            ((TextField) component.getChildren().get(0)).setMaxWidth(maxWidth);
            ((TextField) component.getChildren().get(0)).setPrefWidth(prefWidth);
        }
    }

    private void loadPlanIntoView(TrainingPlan plan) {
        System.out.println("Loading plan:" + plan.getName() + " into view...");
        // TODO: mainBorderPane.setCenter(...);
    }

    // --- Action Handler for Export Button ---
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

    // --- Helper methods for Alerts ---
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

    // --- Listener Methods ---
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

    private ChangeListener<String> textToVmSetsListener = null;
    private ChangeListener<Number> vmToTextSetsListener = null;
    private IntegerProperty currentlyBoundSetsProperty = null;

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

    // --- Preview Update Logic ---
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

    // --- Preview Deep Listener Setup/Teardown ---
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

    // --- Helper methods for Managing Deep Preview Listeners ---

    /**
     * Adds a standard ChangeListener to an ObservableValue that triggers updatePreview. Stores the listener in a map
     * for later removal. Avoids adding duplicate listeners.
     */
    private <T> void addPreviewChangeListener(ObservableValue<T> property) {
        if (property != null && !previewChangeListeners.containsKey(property)) {
            // Create a NEW listener instance for this specific property
            ChangeListener<T> listener = (obs, ov, nv) -> updatePreview();
            property.addListener(listener);
            // Store the property and the SPECIFIC listener instance created for it
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
