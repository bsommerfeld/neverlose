package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.export.ExportService;
import de.bsommerfeld.neverlose.fx.components.TrainingUnitControl;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import de.bsommerfeld.neverlose.plan.components.Weekday;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller for the TrainingPlan WYSIWYG editor view.
 * This controller manages the editing of a TrainingPlan object in a document-like interface.
 */
@View
public class TrainingPlanEditorController {

    private final PlanStorageService planStorageService;
    private final ExportService exportService;
    @FXML
    private TextField planNameField;
    @FXML
    private TextField planDescriptionField;
    @FXML
    private VBox trainingUnitsContainer;
    @FXML
    private Button saveButton;
    @FXML
    private Button exportButton;
    @FXML
    private Button addUnitButton;
    private TrainingPlan trainingPlan;

    /**
     * Constructor for Guice injection.
     * 
     * @param planStorageService the service for saving and loading training plans
     * @param exportService the service for exporting training plans to PDF
     */
    @Inject
    public TrainingPlanEditorController(PlanStorageService planStorageService, ExportService exportService) {
        this.planStorageService = planStorageService;
        this.exportService = exportService;
    }

    /**
     * Initializes the controller after FXML fields are injected.
     */
    @FXML
    private void initialize() {
        // Initialize with an empty training plan if none is set
        if (trainingPlan == null) {
            trainingPlan = new TrainingPlan("New Training Plan", "Description");
        }

        // Bind the training plan properties to the UI
        updateUIFromModel();
    }

    /**
     * Sets the training plan to be edited and updates the UI.
     *
     * @param trainingPlan the training plan to edit
     */
    public void setTrainingPlan(TrainingPlan trainingPlan) {
        this.trainingPlan = trainingPlan;
        updateUIFromModel();
    }

    /**
     * Updates the UI components with the current state of the training plan model.
     */
    private void updateUIFromModel() {
        if (trainingPlan != null) {
            planNameField.setText(trainingPlan.getName());
            planDescriptionField.setText(trainingPlan.getDescription());

            // Clear existing units
            trainingUnitsContainer.getChildren().clear();

            // Add each training unit to the container
            for (TrainingUnit unit : trainingPlan.getTrainingUnits().getAll()) {
                addTrainingUnitToUI(unit);
            }

            // Add the "Add Unit" button at the end
            addAddUnitButton();
        }
    }

    /**
     * Adds a training unit to the UI.
     *
     * @param unit the training unit to add
     */
    private void addTrainingUnitToUI(TrainingUnit unit) {
        TrainingUnitControl unitControl = new TrainingUnitControl(unit);
        trainingUnitsContainer.getChildren().add(unitControl);
    }

    /**
     * Adds the "Add Unit" button to the container.
     */
    private void addAddUnitButton() {
        Button addButton = new Button("+");
        addButton.getStyleClass().add("add-unit-button");
        addButton.setOnAction(event -> handleAddUnit());

        // Create an HBox to center the button
        javafx.scene.layout.HBox buttonContainer = new javafx.scene.layout.HBox(addButton);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);

        trainingUnitsContainer.getChildren().add(buttonContainer);
    }

    /**
     * Handles the action of adding a new training unit.
     */
    @FXML
    private void handleAddUnit() {
        // Create a new training unit with default values
        TrainingUnit newUnit = new TrainingUnit("New Unit", "Description", Weekday.MONDAY);

        // Add it to the training plan
        trainingPlan.getTrainingUnits().add(newUnit);

        // Update the UI
        updateUIFromModel();
    }

    /**
     * Handles the save button action.
     */
    @FXML
    private void handleSave() {
        // Update the model with the current UI state
        updateModelFromUI();

        // TODO: Implement saving logic
        System.out.println("Saving training plan: " + trainingPlan);
    }

    /**
     * Handles the export button action.
     */
    @FXML
    private void handleExport() {
        // Update the model with the current UI state
        updateModelFromUI();

        // TODO: Implement export logic
        System.out.println("Exporting training plan: " + trainingPlan);
    }

    /**
     * Updates the training plan model with the current state of the UI.
     */
    private void updateModelFromUI() {
        if (trainingPlan != null) {
            trainingPlan.setName(planNameField.getText());
            trainingPlan.setDescription(planDescriptionField.getText());

            // The training units are updated directly by their controls
        }
    }
}
