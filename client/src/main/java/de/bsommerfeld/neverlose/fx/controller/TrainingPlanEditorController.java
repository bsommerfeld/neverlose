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
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import de.bsommerfeld.neverlose.plan.components.Weekday;
import de.bsommerfeld.neverlose.plan.components.collection.TrainingExercises;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
    TrainingUnitControl unitControl =
        new TrainingUnitControl(unit, planStorageService, this::saveUnitAsTemplate);
    trainingUnitsContainer.getChildren().add(unitControl);
  }

  /**
   * Saves a training unit as a template.
   *
   * @param unit the training unit to save as a template
   */
  private void saveUnitAsTemplate(TrainingUnit unit) {
    try {
      // Check if a unit with the same name already exists
      String unitName = unit.getName();
      Optional<UUID> existingUnitId = planStorageService.findUnitIdByName(unitName);

      if (existingUnitId.isPresent() && !existingUnitId.get().equals(unit.getId())) {
        // A unit with this name exists but has a different ID
        // Show confirmation dialog before overwriting
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Overwrite Template?");
        confirmDialog.setHeaderText("A Unit template with the name '" + unitName + "' already exists.");
        confirmDialog.setContentText("Do you really want to overwrite the existing template?");

        // Apply application stylesheet to the dialog
        DialogPane dialogPane = confirmDialog.getDialogPane();
        dialogPane.getStylesheets().addAll(rootPane.getScene().getStylesheets());

        // Wait for user response
        java.util.Optional<javafx.scene.control.ButtonType> result = confirmDialog.showAndWait();

        // If user confirmed, update the existing unit
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
          log.info("User confirmed overwriting unit template with name '{}'.", unitName);
          // Use the existing ID for the template unit
          unit = new TrainingUnit(
              existingUnitId.get(),
              unitName,
              unit.getDescription(),
              unit.getWeekday(),
              unit.getTrainingExercises());
        } else {
          // User canceled, abort save operation
          log.info("User canceled overwriting unit template with name '{}'.", unitName);
          return;
        }
      }

      // Create a new unit with the same ID to ensure it overwrites any existing template with the same ID
      TrainingUnit templateUnit = new TrainingUnit(
          unit.getId(),
          unit.getName(),
          unit.getDescription(),
          unit.getWeekday(),
          new TrainingExercises());

      // Copy all exercises from the original unit to the template unit
      for (TrainingExercise exercise : unit.getTrainingExercises().getAll()) {
        // Create a copy of each exercise
        TrainingExercise templateExercise = new TrainingExercise(
            exercise.getName(),
            exercise.getDescription(),
            exercise.getDuration(),
            exercise.getSets(),
            exercise.isBallBucket());

        // Add it to the template unit
        templateUnit.getTrainingExercises().add(templateExercise);
      }

      // Save the template unit
      planStorageService.saveUnit(templateUnit);
      log.info("Training unit saved as template successfully: {}", templateUnit.getName());

      // Show success message
      showStyledAlert(
          Alert.AlertType.INFORMATION,
          "Template saved",
          null,
          "The Training Unit was successfully saved as template.");
    } catch (Exception e) {
      log.error("Error saving training unit as template", e);

      // Show error message
      showStyledAlert(
          Alert.AlertType.ERROR,
          "Error while saving template",
          "Saving the template has failed.",
          "An error occurred: " + e.getMessage());
    }
  }

  /** Adds the "Add Unit" button to the container. */
  private void addAddUnitButton() {
    Button addButton = new Button("+");
    addButton.getStyleClass().add("add-unit-button");
    addButton.setOnAction(event -> handleAddUnit());

    Button addFromTemplate = new Button("From Template");
    addFromTemplate.getStyleClass().add("add-from-template-button");
    addFromTemplate.setOnAction(event -> handleAddFromTemplate());

    // Create an HBox to center the button
    HBox buttonContainer = new HBox(addButton, addFromTemplate);
    buttonContainer.setSpacing(10);
    buttonContainer.setAlignment(Pos.CENTER);

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

  /** Handles the action of adding a unit from a template. */
  @FXML
  private void handleAddFromTemplate() {
    try {
      // Create the controller instance with the required dependencies
      TemplateBrowserController controller = new TemplateBrowserController(planStorageService);

      // Load the template browser view
      javafx.fxml.FXMLLoader loader =
          new javafx.fxml.FXMLLoader(
              getClass()
                  .getResource("/de/bsommerfeld/neverlose/fx/controller/TemplateBrowser.fxml"));

      // Set the controller before loading
      loader.setController(controller);
      javafx.scene.Parent root = loader.load();

      // Set the callback
      controller.setTemplateSelectedCallback(this::addTemplateToTrainingPlan);

      // Create a new stage for the template browser
      Stage templateBrowserStage = new Stage();
      templateBrowserStage.setTitle("Training Unit Templates");
      templateBrowserStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
      templateBrowserStage.initOwner(rootPane.getScene().getWindow());

      // Set the scene and show the stage
      Scene scene = new Scene(root, 600, 400);
      scene.getStylesheets().addAll(rootPane.getScene().getStylesheets());
      templateBrowserStage.setScene(scene);
      templateBrowserStage.showAndWait();

    } catch (Exception e) {
      log.error("Error opening template browser", e);
      showStyledAlert(
          Alert.AlertType.ERROR,
          "Error Opening",
          "The template browser could not be opened.",
          "An error occurred: " + e.getMessage());
    }
  }

  /**
   * Adds a template unit to the training plan.
   *
   * @param templateUnit the template unit to add
   */
  private void addTemplateToTrainingPlan(TrainingUnit templateUnit) {
    // Create a new unit with a new ID
    TrainingUnit newUnit =
        new TrainingUnit(
            templateUnit.getName(), templateUnit.getDescription(), templateUnit.getWeekday());

    // Copy all exercises from the template to the new unit
    for (TrainingExercise exercise : templateUnit.getTrainingExercises().getAll()) {
      // Create a copy of each exercise
      TrainingExercise newExercise =
          new TrainingExercise(
              exercise.getName(),
              exercise.getDescription(),
              exercise.getDuration(),
              exercise.getSets(),
              exercise.isBallBucket());

      // Add it to the new unit
      newUnit.getTrainingExercises().add(newExercise);
    }

    // Add the new unit to the training plan
    trainingPlan.getTrainingUnits().add(newUnit);

    // Update the UI
    updateUIFromModel();

    log.info("Added template unit '{}' to training plan", templateUnit.getName());
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
        // Show confirmation dialog before overwriting
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Overwrite Plan?");
        confirmDialog.setHeaderText("A Plan with the name '" + planName + "' already exists.");
        confirmDialog.setContentText("Do you really want to overwrite the existing plan?");

        // Apply application stylesheet to the dialog
        DialogPane dialogPane = confirmDialog.getDialogPane();
        dialogPane.getStylesheets().addAll(rootPane.getScene().getStylesheets());

        // Wait for user response
        java.util.Optional<javafx.scene.control.ButtonType> result = confirmDialog.showAndWait();

        // If user confirmed, update the existing plan
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
          log.info("User confirmed overwriting plan with name '{}'.", planName);
          trainingPlan =
              new TrainingPlan(
                  existingPlanId,
                  planName,
                  trainingPlan.getDescription(),
                  trainingPlan.getTrainingUnits());
        } else {
          // User canceled, abort save operation
          log.info("User canceled overwriting plan with name '{}'.", planName);
          return;
        }
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
    dialogPane.getStylesheets().addAll(rootPane.getScene().getStylesheets());

    alert.showAndWait();
  }
}
