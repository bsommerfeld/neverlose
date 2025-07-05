package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.controller.base.AbstractBrowserController;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.persistence.model.ExerciseSummary;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for the exercise template browser overlay that displays available training exercise
 * templates. Allows users to select a template to add to their unit or delete existing templates.
 */
@View
public class ExerciseTemplateBrowserController
    extends AbstractBrowserController<ExerciseSummary, TrainingExercise, ExerciseCardController> {

  /**
   * Constructor for Guice injection.
   *
   * @param planStorageService the service for loading and managing templates
   * @param notificationService the service for displaying notifications
   */
  @Inject
  public ExerciseTemplateBrowserController(PlanStorageService planStorageService, NotificationService notificationService) {
    super(planStorageService, notificationService);
  }

  @Override
  protected String getNoTemplatesMessage() {
    return "No exercise templates available. Save exercises as templates to display them here.";
  }

  @Override
  protected String getCardFxmlPath() {
    return "/de/bsommerfeld/neverlose/fx/controller/ExerciseCard.fxml";
  }

  @Override
  protected String getDeleteDialogTitle() {
    return "Delete Exercise Template";
  }

  @Override
  protected String getTemplateName(ExerciseSummary template) {
    return template.name();
  }

  @Override
  protected String getItemName(TrainingExercise item) {
    return item.getName();
  }

  @Override
  protected List<ExerciseSummary> loadTemplateSummaries() throws IOException {
    return planStorageService.loadExerciseSummaries();
  }

  @Override
  protected Optional<TrainingExercise> loadTemplateItem(UUID templateId) throws IOException {
    return planStorageService.loadExercise(templateId);
  }

  @Override
  protected boolean deleteTemplate(UUID templateId) throws IOException {
    return planStorageService.deleteExercise(templateId);
  }

  @Override
  protected void setupCardController(
      ExerciseCardController cardController, ExerciseSummary template) {
    cardController.setTemplate(template);
    cardController.setOnDeleteAction(this::handleDeleteTemplate);
    cardController.setOnSelectAction(this::handleSelectTemplate);
  }
}
