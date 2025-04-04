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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private HBox breadcrumbBox;

    @FXML
    private StackPane contentArea;
    @FXML
    private ScrollPane contentScrollPane;

    @FXML
    private VBox unitListContainer;
    @FXML
    private ListView<TrainingUnitViewModel> trainingUnitsListView;
    @FXML
    private Button addUnitButton;
    @FXML
    private Button removeUnitButton;

    @FXML
    private VBox unitDetailContainer;
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
    private VBox exerciseDetailContainer;
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

        trainingUnitsListView.setItems(null);
        if (viewModel.selectedTrainingUnitProperty().isBound()) {
            viewModel.selectedTrainingUnitProperty().removeListener(selectedUnitListener);
            viewModel.selectedTrainingUnitProperty().unbind();
        }
        if (removeUnitButton.disableProperty().isBound()) {
            removeUnitButton.disableProperty().unbind();
        }

        TrainingUnitViewModel lastSelectedUnit = trainingUnitsListView.getSelectionModel().getSelectedItem();
        if (lastSelectedUnit != null) {
            ExerciseViewModel lastSelectedExercise = trainingExercisesListView.getSelectionModel().getSelectedItem();
            unbindExerciseDetails(lastSelectedExercise);
            unbindUnitDetails(lastSelectedUnit);
        }

        trainingUnitsListView.getSelectionModel().clearSelection();
        trainingExercisesListView.getSelectionModel().clearSelection();

        planNameTextField.clear();
        planDescriptionTextArea.clear();
        clearAndHideAllContentViews();
        breadcrumbBox.getChildren().clear();

        addUnitButton.setOnAction(null);
        removeUnitButton.setOnAction(null);
        addExerciseButton.setOnAction(null);
        removeExerciseButton.setOnAction(null);
        exportPdfButton.setOnAction(null);

        System.out.println("Form Controller: UI Unbound.");
    }

    private void bindUI() {
        System.out.println("Form Controller: Binding UI...");
        Objects.requireNonNull(viewModel, "ViewModel cannot be null during bindUI in Form Controller");

        planNameTextField.textProperty().bindBidirectional(viewModel.planNameProperty());
        planDescriptionTextArea.textProperty().bindBidirectional(viewModel.planDescriptionProperty());

        trainingUnitsListView.setItems(viewModel.trainingUnitsProperty());

        viewModel.selectedTrainingUnitProperty().addListener(selectedUnitListener);
        viewModel.selectedTrainingUnitProperty().bind(trainingUnitsListView.getSelectionModel().selectedItemProperty());

        removeUnitButton.disableProperty().bind(viewModel.selectedTrainingUnitProperty().isNull());

        TrainingUnitViewModel initialUnit = viewModel.selectedTrainingUnitProperty().get();
        ExerciseViewModel initialExercise = null;
        if (initialUnit != null) {
            viewModel.selectedTrainingUnitProperty().unbind();
            trainingUnitsListView.getSelectionModel().select(initialUnit);
            viewModel.selectedTrainingUnitProperty().bind(trainingUnitsListView.getSelectionModel().selectedItemProperty()); // Rebind

            if (initialUnit.selectedExerciseProperty() != null) {
                initialExercise = initialUnit.selectedExerciseProperty().get();
                if (initialExercise != null) {
                    trainingExercisesListView.getSelectionModel().select(initialExercise);
                }
            }
            onSelectedUnitChanged(null, initialUnit);
        } else {
            showView(unitListContainer);
            updateBreadcrumb(null, null);
        }

        addUnitButton.setOnAction(event -> {
            viewModel.addTrainingUnit();
            selectAndScrollToLast(trainingUnitsListView, viewModel.trainingUnitsProperty());
        });
        removeUnitButton.setOnAction(event -> viewModel.removeSelectedTrainingUnit()); // Selection change handles view update

        addExerciseButton.setOnAction(event -> {
            viewModel.addExerciseToSelectedUnit();
            TrainingUnitViewModel currentUnit = viewModel.selectedTrainingUnitProperty().get();
            if (currentUnit != null) {
                selectAndScrollToLast(trainingExercisesListView, currentUnit.exercisesProperty());
            }
        });
        removeExerciseButton.setOnAction(event -> viewModel.removeSelectedExerciseFromSelectedUnit()); // Selection change handles view update

        exportPdfButton.setOnAction(event -> handleExportPdfAction());

        System.out.println("Form Controller: UI Bound.");
    }

    /**
     * Makes the specified view node visible and managed within the StackPane,
     * hiding and unmanaging the others.
     * Also resets scroll position.
     *
     * @param viewToShow The Node (VBox container) to display.
     */
    private void showView(Node viewToShow) {
        contentArea.getChildren().forEach(node -> {
            boolean isVisible = (node == viewToShow);
            node.setVisible(isVisible);
            node.setManaged(isVisible);
        });
        contentScrollPane.setVvalue(0.0);
    }

    /**
     * Clears and hides all dynamic content views
     */
    private void clearAndHideAllContentViews() {
        unitListContainer.setVisible(false);
        unitListContainer.setManaged(false);
        unitDetailContainer.setVisible(false);
        unitDetailContainer.setManaged(false);
        exerciseDetailContainer.setVisible(false);
        exerciseDetailContainer.setManaged(false);
    }

    // --- Breadcrumb Logic ---

    /**
     * Updates the breadcrumb navigation bar based on the current selection.
     *
     * @param selectedUnit     The currently selected TrainingUnitViewModel, or null.
     * @param selectedExercise The currently selected ExerciseViewModel, or null.
     */
    private void updateBreadcrumb(TrainingUnitViewModel selectedUnit, ExerciseViewModel selectedExercise) {
        breadcrumbBox.getChildren().clear();
        String planName = viewModel.planNameProperty().get();
        if (planName == null || planName.trim().isEmpty()) {
            planName = "Training Plan"; // Default if no name yet
        }

        if (selectedUnit == null) {
            Label planLabel = new Label(planName);
            planLabel.getStyleClass().add("breadcrumb-item-active");
            breadcrumbBox.getChildren().add(planLabel);
        } else {
            Hyperlink planLink = new Hyperlink(planName);
            planLink.setOnAction(e -> {
                trainingExercisesListView.getSelectionModel().clearSelection();
                trainingUnitsListView.getSelectionModel().clearSelection();
            });
            planLink.getStyleClass().add("breadcrumb-item");
            breadcrumbBox.getChildren().add(planLink);
        }

        if (selectedUnit != null) {
            breadcrumbBox.getChildren().add(createBreadcrumbSeparator());
            String unitName = selectedUnit.nameProperty().get();
            if (unitName == null || unitName.trim().isEmpty()) {
                unitName = "Selected Unit"; // Default
            }

            if (selectedExercise == null) {
                Label unitLabel = new Label(unitName);
                unitLabel.getStyleClass().add("breadcrumb-item-active");
                breadcrumbBox.getChildren().add(unitLabel);
            } else {
                Hyperlink unitLink = new Hyperlink(unitName);
                unitLink.setOnAction(e -> {
                    trainingExercisesListView.getSelectionModel().clearSelection();
                });
                unitLink.getStyleClass().add("breadcrumb-item");
                breadcrumbBox.getChildren().add(unitLink);
            }
        }

        if (selectedExercise != null) {
            breadcrumbBox.getChildren().add(createBreadcrumbSeparator());
            String exerciseName = selectedExercise.nameProperty().get();
            if (exerciseName == null || exerciseName.trim().isEmpty()) {
                exerciseName = "Selected Exercise"; // Default
            }
            Label exerciseLabel = new Label(exerciseName);
            exerciseLabel.getStyleClass().add("breadcrumb-item-active");
            breadcrumbBox.getChildren().add(exerciseLabel);
        }
    }

    /**
     * Helper to create a visual separator for the breadcrumb
     */
    private Node createBreadcrumbSeparator() {
        Label separator = new Label(">");
        separator.getStyleClass().add("breadcrumb-separator");
        return separator;
    }


    /**
     * Unbinds controls specific to a TrainingUnitViewModel
     */
    private void unbindUnitDetails(TrainingUnitViewModel unitVm) {
        if (unitVm == null) return;
        System.out.println("Form Controller: Unbinding Unit Details for: " + unitVm.nameProperty().get());
        unitNameTextField.textProperty().unbindBidirectional(unitVm.nameProperty());
        unitDescriptionTextArea.textProperty().unbindBidirectional(unitVm.descriptionProperty());
        unitWeekdayChoiceBox.valueProperty().unbindBidirectional(unitVm.weekdayProperty());

        if (trainingExercisesListView.itemsProperty().isBound()) {
            trainingExercisesListView.itemsProperty().unbind();
        }
        if (unitVm.selectedExerciseProperty().isBound()) {
            unitVm.selectedExerciseProperty().removeListener(selectedExerciseListener);
            unitVm.selectedExerciseProperty().unbind();
        }
        trainingExercisesListView.setItems(null);

        if (removeExerciseButton.disableProperty().isBound()) {
            removeExerciseButton.disableProperty().unbind();
        }
        removeExerciseButton.setDisable(true);
    }

    /**
     * Unbinds controls specific to an ExerciseViewModel
     */
    private void unbindExerciseDetails(ExerciseViewModel exerciseVm) {
        if (exerciseVm == null) return;
        System.out.println("Form Controller: Unbinding Exercise Details for: " + exerciseVm.nameProperty().get());
        exerciseNameTextField.textProperty().unbindBidirectional(exerciseVm.nameProperty());
        exerciseDescriptionTextArea.textProperty().unbindBidirectional(exerciseVm.descriptionProperty());
        exerciseDurationTextField.textProperty().unbindBidirectional(exerciseVm.durationProperty());
        exerciseBallBucketCheckBox.selectedProperty().unbindBidirectional(exerciseVm.ballBucketProperty());
        unbindSetsTextField();
    }

    private void onSelectedUnitChanged(TrainingUnitViewModel oldSelectedUnit, TrainingUnitViewModel newSelectedUnit) {
        System.out.println("Form Controller: Selected Unit Changed - New: " + (newSelectedUnit != null ? newSelectedUnit.nameProperty().get() : "null"));

        if (oldSelectedUnit != null) {
            ExerciseViewModel oldExercise = oldSelectedUnit.selectedExerciseProperty().get();
            if (oldExercise != null) {
                unbindExerciseDetails(oldExercise);
            }
            unbindUnitDetails(oldSelectedUnit);
        } else {
            unitNameTextField.clear();
            unitDescriptionTextArea.clear();
            unitWeekdayChoiceBox.setValue(null);
            trainingExercisesListView.setItems(null);
            removeExerciseButton.setDisable(true);
        }

        if (newSelectedUnit != null) {
            unitNameTextField.textProperty().bindBidirectional(newSelectedUnit.nameProperty());
            unitDescriptionTextArea.textProperty().bindBidirectional(newSelectedUnit.descriptionProperty());
            unitWeekdayChoiceBox.valueProperty().bindBidirectional(newSelectedUnit.weekdayProperty());

            trainingExercisesListView.setItems(newSelectedUnit.exercisesProperty());

            newSelectedUnit.selectedExerciseProperty().addListener(selectedExerciseListener);
            newSelectedUnit.selectedExerciseProperty().bind(trainingExercisesListView.getSelectionModel().selectedItemProperty());

            removeExerciseButton.disableProperty().bind(newSelectedUnit.selectedExerciseProperty().isNull());

            showView(unitDetailContainer);
            updateBreadcrumb(newSelectedUnit, null);

            onSelectedExerciseChanged(null, newSelectedUnit.selectedExerciseProperty().get());

        } else {
            showView(unitListContainer);
            updateBreadcrumb(null, null);
            unbindExerciseDetails(null);
            exerciseNameTextField.clear();
            exerciseDescriptionTextArea.clear();
            exerciseDurationTextField.clear();
            exerciseSetsTextField.clear();
            exerciseBallBucketCheckBox.setSelected(false);
        }
    }

    private void onSelectedExerciseChanged(ExerciseViewModel oldSelectedExercise, ExerciseViewModel newSelectedExercise) {
        System.out.println("Form Controller: Selected Exercise Changed - New: " + (newSelectedExercise != null ? newSelectedExercise.nameProperty().get() : "null"));
        TrainingUnitViewModel currentUnit = viewModel.selectedTrainingUnitProperty().get();

        if (oldSelectedExercise != null) {
            unbindExerciseDetails(oldSelectedExercise);
        } else {
            exerciseNameTextField.clear();
            exerciseDescriptionTextArea.clear();
            exerciseDurationTextField.clear();
            unbindSetsTextField();
            exerciseSetsTextField.clear();
            exerciseBallBucketCheckBox.setSelected(false);
        }

        if (newSelectedExercise != null) {
            exerciseNameTextField.textProperty().bindBidirectional(newSelectedExercise.nameProperty());
            exerciseDescriptionTextArea.textProperty().bindBidirectional(newSelectedExercise.descriptionProperty());
            exerciseDurationTextField.textProperty().bindBidirectional(newSelectedExercise.durationProperty());
            exerciseBallBucketCheckBox.selectedProperty().bindBidirectional(newSelectedExercise.ballBucketProperty());
            bindSetsTextField(newSelectedExercise);

            showView(exerciseDetailContainer);
            updateBreadcrumb(currentUnit, newSelectedExercise);

        } else {
            if (currentUnit != null) {
                showView(unitDetailContainer);
                updateBreadcrumb(currentUnit, null);
            } else {
                // Edge case: Unit was deselected *while* exercise was selected? Go back to list.
                showView(unitListContainer);
                updateBreadcrumb(null, null);
            }
            exerciseNameTextField.clear();
            exerciseDescriptionTextArea.clear();
            exerciseDurationTextField.clear();
            unbindSetsTextField();
            exerciseSetsTextField.clear();
            exerciseBallBucketCheckBox.setSelected(false);
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

    private void bindSetsTextField(ExerciseViewModel exerciseVm) {
        unbindSetsTextField();
        if (exerciseVm == null) {
            exerciseSetsTextField.clear();
            return;
        }

        currentlyBoundSetsProperty = exerciseVm.setsProperty();
        exerciseSetsTextField.setText(String.valueOf(currentlyBoundSetsProperty.get()));

        textToVmSetsListener = (obs, oldVal, newVal) -> {
            if (currentlyBoundSetsProperty == null) return; // Guard
            try {
                int sets = 0; // Default to 0 if empty or invalid
                if (newVal != null && !newVal.trim().isEmpty()) {
                    sets = Integer.parseInt(newVal.trim());
                }
                if (currentlyBoundSetsProperty.get() != sets) {
                    currentlyBoundSetsProperty.set(sets);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format for Sets: " + newVal + " - Setting VM to 0");
                final String vmValue = String.valueOf(currentlyBoundSetsProperty.get());
                if (!exerciseSetsTextField.getText().equals(vmValue)) {
                    exerciseSetsTextField.setText(vmValue);
                }
            }
        };
        exerciseSetsTextField.textProperty().addListener(textToVmSetsListener);

        vmToTextSetsListener = (obs, oldVal, newVal) -> {
            String currentText = exerciseSetsTextField.getText().trim();
            String newValueStr = newVal.toString();
            int currentTextParsed = 0;
            try {
                if (!currentText.isEmpty()) {
                    currentTextParsed = Integer.parseInt(currentText);
                }
            } catch (NumberFormatException e) { /* Ignore, comparison below handles it */ }

            if (currentTextParsed != newVal.intValue() || !currentText.equals(newValueStr)) {
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
     * f
     * Cleans up listeners when the view is potentially being destroyed.
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