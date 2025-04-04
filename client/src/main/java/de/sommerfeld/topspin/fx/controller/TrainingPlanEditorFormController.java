package de.sommerfeld.topspin.fx.controller;

import de.sommerfeld.topspin.fx.viewmodel.ExerciseViewModel;
import de.sommerfeld.topspin.fx.viewmodel.TrainingPlanEditorViewModel;
import de.sommerfeld.topspin.fx.viewmodel.TrainingUnitViewModel;
import de.sommerfeld.topspin.plan.components.Weekday;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class TrainingPlanEditorFormController {

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

    private ChangeListener<String> textToVmSetsListener = null;
    private ChangeListener<Number> vmToTextSetsListener = null;
    private IntegerProperty currentlyBoundSetsProperty = null;
    private final ChangeListener<ExerciseViewModel> selectedExerciseListener =
            (obs, oldExVm, newExVm) -> onSelectedExerciseChanged(oldExVm, newExVm);
    private final ChangeListener<TrainingUnitViewModel> selectedUnitListener =
            (obs, oldUnitVm, newUnitVm) -> onSelectedUnitChanged(oldUnitVm, newUnitVm);


    public void initialize() {
        System.out.println("Form Controller initialized (pending ViewModel).");
        unitWeekdayChoiceBox.setItems(FXCollections.observableArrayList(Weekday.values()));
    }

    /**
     * Injects the ViewModel and triggers UI binding.
     * This method should be called by the MetaController after FXML loading.
     *
     * @param viewModel The shared ViewModel.
     */
    public void initViewModel(TrainingPlanEditorViewModel viewModel) {
        Objects.requireNonNull(viewModel, "ViewModel cannot be null for Form Controller");
        if (this.viewModel != null) {
            System.err.println("Warning: ViewModel already set in Form Controller.");
            unbindUI();
        }
        this.viewModel = viewModel;
        System.out.println("Form Controller: ViewModel received.");
        bindUI();
    }


    private void unbindUI() {
        if (viewModel == null) return;
        System.out.println("Form Controller: Unbinding UI...");

        planNameTextField.textProperty().unbindBidirectional(viewModel.planNameProperty());
        planDescriptionTextArea.textProperty().unbindBidirectional(viewModel.planDescriptionProperty());

        if (viewModel.selectedTrainingUnitProperty().isBound()) {
            viewModel.selectedTrainingUnitProperty().unbind();
        }
        trainingUnitsListView.setItems(null);
        viewModel.selectedTrainingUnitProperty().removeListener(selectedUnitListener);

        if (removeUnitButton.disableProperty().isBound()) {
            removeUnitButton.disableProperty().unbind();
        }
        if (selectedUnitPane.disableProperty().isBound()) {
            selectedUnitPane.disableProperty().unbind();
        }

        cleanupDetailPanes();

        planNameTextField.clear();
        planDescriptionTextArea.clear();
        removeUnitButton.setDisable(true);
        selectedUnitPane.setDisable(true);
        selectedExercisePane.setDisable(true);

        System.out.println("Form Controller: UI Unbound.");
    }


    private void bindUI() {
        System.out.println("Form Controller: Binding UI...");
        Objects.requireNonNull(viewModel, "ViewModel cannot be null during bindUI in Form Controller");

        planNameTextField.textProperty().bindBidirectional(viewModel.planNameProperty());
        planDescriptionTextArea.textProperty().bindBidirectional(viewModel.planDescriptionProperty());

        trainingUnitsListView.setItems(viewModel.trainingUnitsProperty());
        viewModel.selectedTrainingUnitProperty().bind(trainingUnitsListView.getSelectionModel().selectedItemProperty());
        viewModel.selectedTrainingUnitProperty().addListener(selectedUnitListener);

        removeUnitButton.disableProperty().bind(viewModel.selectedTrainingUnitProperty().isNull());
        selectedUnitPane.disableProperty().bind(viewModel.selectedTrainingUnitProperty().isNull());

        onSelectedUnitChanged(null, viewModel.selectedTrainingUnitProperty().get());

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

        System.out.println("Form Controller: UI Bound.");
    }

    /**
     * Manually cleans up bindings and state related to the Unit and Exercise detail panes.
     * Called during unbindUI.
     */
    private void cleanupDetailPanes() {
        if (viewModel == null) return;

        TrainingUnitViewModel currentUnit = viewModel.selectedTrainingUnitProperty().get();
        ExerciseViewModel currentExercise = null;
        if (currentUnit != null) {
            if (currentUnit.selectedExerciseProperty().isBound()) {
                currentUnit.selectedExerciseProperty().unbind();
            }
            currentExercise = currentUnit.selectedExerciseProperty().get();
            currentUnit.selectedExerciseProperty().removeListener(selectedExerciseListener);
        }

        onSelectedExerciseChanged(currentExercise, null);
        onSelectedUnitChanged(currentUnit, null);

        trainingExercisesListView.setItems(null);

        if (removeExerciseButton.disableProperty().isBound()) {
            removeExerciseButton.disableProperty().unbind();
        }
        removeExerciseButton.setDisable(true);
        selectedExercisePane.setDisable(true);
    }


    private void onSelectedUnitChanged(TrainingUnitViewModel oldSelectedUnit, TrainingUnitViewModel newSelectedUnit) {
        System.out.println("Form Controller: Selected Unit Changed - New: " + (newSelectedUnit != null ? newSelectedUnit.nameProperty().get() : "null"));
        if (oldSelectedUnit != null) {
            unitNameTextField.textProperty().unbindBidirectional(oldSelectedUnit.nameProperty());
            unitDescriptionTextArea.textProperty().unbindBidirectional(oldSelectedUnit.descriptionProperty());
            unitWeekdayChoiceBox.valueProperty().unbindBidirectional(oldSelectedUnit.weekdayProperty());

            if (trainingExercisesListView.itemsProperty().isBound()) {
                trainingExercisesListView.itemsProperty().unbind();
            }

            if (oldSelectedUnit.selectedExerciseProperty().isBound()) {
                oldSelectedUnit.selectedExerciseProperty().unbind();
            }
            oldSelectedUnit.selectedExerciseProperty().removeListener(selectedExerciseListener);

            trainingExercisesListView.setItems(null);
            onSelectedExerciseChanged(oldSelectedUnit.selectedExerciseProperty().get(), null);

        } else {
            unitNameTextField.clear();
            unitDescriptionTextArea.clear();
            unitWeekdayChoiceBox.setValue(null);
            trainingExercisesListView.setItems(null);
            onSelectedExerciseChanged(null, null);
        }

        if (newSelectedUnit != null) {
            unitNameTextField.textProperty().bindBidirectional(newSelectedUnit.nameProperty());
            unitDescriptionTextArea.textProperty().bindBidirectional(newSelectedUnit.descriptionProperty());
            unitWeekdayChoiceBox.valueProperty().bindBidirectional(newSelectedUnit.weekdayProperty());

            trainingExercisesListView.setItems(newSelectedUnit.exercisesProperty());
            newSelectedUnit.selectedExerciseProperty().bind(trainingExercisesListView.getSelectionModel().selectedItemProperty());
            newSelectedUnit.selectedExerciseProperty().addListener(selectedExerciseListener);
            onSelectedExerciseChanged(null, newSelectedUnit.selectedExerciseProperty().get());

            if (removeExerciseButton.disableProperty().isBound()) {
                removeExerciseButton.disableProperty().unbind();
            }
            removeExerciseButton.disableProperty().bind(newSelectedUnit.selectedExerciseProperty().isNull());

        } else {
            unitNameTextField.clear();
            unitDescriptionTextArea.clear();
            unitWeekdayChoiceBox.setValue(null);
            trainingExercisesListView.setItems(null);

            onSelectedExerciseChanged(null, null);
            if (removeExerciseButton.disableProperty().isBound()) {
                removeExerciseButton.disableProperty().unbind();
            }
            removeExerciseButton.setDisable(true);
        }
    }


    private void onSelectedExerciseChanged(ExerciseViewModel oldSelectedExercise, ExerciseViewModel newSelectedExercise) {
        System.out.println("Form Controller: Selected Exercise Changed - New: " + (newSelectedExercise != null ? newSelectedExercise.getModel().getName() : "null"));
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

    private void bindSetsTextField(ExerciseViewModel exerciseVm) {
        unbindSetsTextField();

        if (exerciseVm == null) return;

        currentlyBoundSetsProperty = exerciseVm.setsProperty();

        exerciseSetsTextField.setText(String.valueOf(currentlyBoundSetsProperty.get()));

        textToVmSetsListener = (obs, oldVal, newVal) -> {
            try {
                int sets = Integer.parseInt(newVal.trim());
                if (currentlyBoundSetsProperty != null && currentlyBoundSetsProperty.get() != sets) {
                    currentlyBoundSetsProperty.set(sets);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format for Sets: " + newVal);
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

    private <T> void selectAndScrollToLast(ListView<T> listView, ObservableList<T> items) {
        if (listView != null && items != null && !items.isEmpty()) {
            int lastIndex = items.size() - 1;
            listView.getSelectionModel().select(lastIndex);
            listView.scrollTo(lastIndex);
        }
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
            initialFileName = planName.trim().replaceAll("[^a-zA-Z0-9.\\-]", "_") + ".pdf";
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

    /**
     * Cleans up listeners when the view is potentially being destroyed.
     * Should be called by the MetaController.
     */
    public void cleanup() {
        System.out.println("Form Controller: Cleaning up...");
        unbindUI();
        if (viewModel != null) {
            viewModel.selectedTrainingUnitProperty().removeListener(selectedUnitListener);
            TrainingUnitViewModel currentUnit = viewModel.selectedTrainingUnitProperty().get();
            if (currentUnit != null) {
                currentUnit.selectedExerciseProperty().removeListener(selectedExerciseListener);
            }
        }
        System.out.println("Form Controller: Cleanup complete.");
    }
}