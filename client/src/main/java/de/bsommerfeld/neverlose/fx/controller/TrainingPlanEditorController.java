package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.export.ExportService;
import de.bsommerfeld.neverlose.fx.components.TrainingUnitControl;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.model.PlanSummary;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import de.bsommerfeld.neverlose.plan.components.Weekday;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller for the TrainingPlan WYSIWYG editor view. This controller manages the editing of a
 * TrainingPlan object in a document-like interface.
 */
@View
public class TrainingPlanEditorController {

  private final LogFacade log = LogFacadeFactory.getLogger();

  private final PlanStorageService planStorageService;
  private final ExportService exportService;
  @FXML private BorderPane rootPane;
  @FXML private TextField planNameField;
  @FXML private TextField planDescriptionField;
  @FXML private VBox trainingUnitsContainer;
  private TrainingPlan trainingPlan;
  private NeverLoseMetaController metaController;

  /**
   * Constructor for Guice injection.
   *
   * @param planStorageService the service for saving and loading training plans
   * @param exportService the service for exporting training plans to PDF
   */
  @Inject
  public TrainingPlanEditorController(
      PlanStorageService planStorageService, ExportService exportService) {
    this.planStorageService = planStorageService;
    this.exportService = exportService;
  }

  /** Initializes the controller after FXML fields are injected. */
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
   * Sets the meta controller for navigation.
   *
   * @param metaController the meta controller
   */
  public void setMetaController(NeverLoseMetaController metaController) {
    this.metaController = metaController;
  }

  /** Updates the UI components with the current state of the training plan model. */
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

  /** Adds the "Add Unit" button to the container. */
  private void addAddUnitButton() {
    Button addButton = new Button("+");
    addButton.getStyleClass().add("add-unit-button");
    addButton.setOnAction(event -> handleAddUnit());

    // Create an HBox to center the button
    HBox buttonContainer = new HBox(addButton);
    buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);

    trainingUnitsContainer.getChildren().add(buttonContainer);
  }

  /** Handles the action of adding a new training unit. */
  @FXML
  private void handleAddUnit() {
    // Create a new training unit with default values
    TrainingUnit newUnit = new TrainingUnit("New Unit", "Description", Weekday.MONDAY);

    // Add it to the training plan
    trainingPlan.getTrainingUnits().add(newUnit);

    // Update the UI
    updateUIFromModel();
  }

  /** Handles the save button action. */
  @FXML
  private void handleSave() {
    updateModelFromUI();

    try {
      // Check if a plan with the same name already exists (but with a different ID)
      String planName = trainingPlan.getName();
      UUID existingPlanId = findExistingPlanByName(planName);

      if (existingPlanId != null && !existingPlanId.equals(trainingPlan.getId())) {
        // A plan with this name exists but has a different ID
        // Update the existing plan instead of creating a new one
        log.info("Plan with name '{}' already exists. Updating existing plan.", planName);
        trainingPlan = new TrainingPlan(
            existingPlanId, 
            planName, 
            trainingPlan.getDescription(), 
            trainingPlan.getTrainingUnits());
      }

      String identifier = planStorageService.savePlan(trainingPlan);
      log.info("Training plan saved successfully with identifier: {}", identifier);

      // Show success message
      showStyledAlert(
          Alert.AlertType.INFORMATION,
          "Plan Saved",
          null,
          "The training plan has been successfully saved.");

      // Navigate back to the plan list view
      if (metaController != null) {
        metaController.showPlanListView();
      } else {
        log.error("Meta controller not set, cannot navigate back to plan list view");
      }
    } catch (Exception e) {
      log.error("Error saving training plan", e);

      // Show error message
      showStyledAlert(
          Alert.AlertType.ERROR,
          "Save Error",
          "The save operation failed.",
          "An error occurred while saving the plan: " + e.getMessage());
    }
  }

  /**
   * Finds an existing plan by name.
   *
   * @param name the name to search for
   * @return the UUID of the existing plan, or null if no plan with that name exists
   */
  private UUID findExistingPlanByName(String name) {
    try {
      List<PlanSummary> summaries = planStorageService.loadPlanSummaries();
      return summaries.stream()
          .filter(summary -> summary.name().equals(name))
          .map(summary -> summary.identifier())
          .findFirst()
          .orElse(null);
    } catch (IOException e) {
      log.error("Error loading plan summaries", e);
      return null;
    }
  }

  /** Handles the export button action by showing a file chooser and exporting the plan. */
  @FXML
  private void handleExport() {
    updateModelFromUI();

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Export Training Plan as PDF");

    String initialFileName = trainingPlan.getName().replaceAll("\\s+", "_") + ".pdf";
    fileChooser.setInitialFileName(initialFileName);

    FileChooser.ExtensionFilter extFilter =
        new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf");
    fileChooser.getExtensionFilters().add(extFilter);

    Stage stage = (Stage) rootPane.getScene().getWindow();
    File file = fileChooser.showSaveDialog(stage);

    if (file != null) {
      try {
        exportService.export(trainingPlan, file);
        log.info("Training plan successfully exported to: {}", file.getAbsolutePath());

        showStyledAlert(
            Alert.AlertType.INFORMATION,
            "Export Successful",
            null,
            "The training plan has been successfully saved as a PDF.");

      } catch (Exception e) {
        log.error("Error exporting training plan", e);

        showStyledAlert(
            Alert.AlertType.ERROR,
            "Export Error",
            "The export failed.",
            "An error occurred while saving the PDF file: " + e.getMessage());
      }
    } else {
      log.info("Export canceled by user.");
    }
  }

  /** Updates the training plan model with the current state of the UI. */
  private void updateModelFromUI() {
    if (trainingPlan != null) {
      trainingPlan.setName(planNameField.getText());
      trainingPlan.setDescription(planDescriptionField.getText());

      // The training units are updated directly by their controls
    }
  }

  /**
   * Creates and shows an Alert with the application's stylesheet applied.
   *
   * @param alertType the type of the alert
   * @param title the title of the alert
   * @param headerText the header text (can be null)
   * @param contentText the content text
   */
  private void showStyledAlert(
      Alert.AlertType alertType, String title, String headerText, String contentText) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(headerText);
    alert.setContentText(contentText);

    DialogPane dialogPane = alert.getDialogPane();
    dialogPane.getStylesheets().addAll(rootPane.getScene().getRoot().getStylesheets());

    alert.showAndWait();
  }
}
