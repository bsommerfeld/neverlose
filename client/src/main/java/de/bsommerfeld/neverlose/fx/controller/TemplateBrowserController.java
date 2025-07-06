package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.controller.base.AbstractBrowserController;
import de.bsommerfeld.neverlose.fx.messages.Messages;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.persistence.model.UnitSummary;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for the template browser overlay that displays available training unit templates.
 * Allows users to select a template to add to their plan or delete existing templates.
 */
@View
public class TemplateBrowserController
    extends AbstractBrowserController<UnitSummary, TrainingUnit, TemplateCardController> {

  /**
   * Constructor for Guice injection.
   *
   * @param planStorageService the service for loading and managing templates
   * @param notificationService the service for displaying notifications
   */
  @Inject
  public TemplateBrowserController(PlanStorageService planStorageService, NotificationService notificationService) {
    super(planStorageService, notificationService);
  }

  @Override
  protected String getNoTemplatesMessage() {
    return Messages.getString("template.noTemplatesAvailable");
  }

  @Override
  protected String getCardFxmlPath() {
    return Messages.getString("path.templateCard.fxml");
  }

  @Override
  protected String getDeleteDialogTitle() {
    return Messages.getString("template.deleteButtonText");
  }

  @Override
  protected String getTemplateName(UnitSummary template) {
    return template.name();
  }

  @Override
  protected String getItemName(TrainingUnit item) {
    return item.getName();
  }

  @Override
  protected List<UnitSummary> loadTemplateSummaries() throws IOException {
    return planStorageService.loadUnitSummaries();
  }

  @Override
  protected Optional<TrainingUnit> loadTemplateItem(UUID templateId) throws IOException {
    return planStorageService.loadUnit(templateId);
  }

  @Override
  protected boolean deleteTemplate(UUID templateId) throws IOException {
    return planStorageService.deleteUnit(templateId);
  }

  @Override
  protected void setupCardController(TemplateCardController cardController, UnitSummary template) {
    cardController.setTemplate(template);
    cardController.setOnDeleteAction(this::handleDeleteTemplate);
    cardController.setOnSelectAction(this::handleSelectTemplate);
  }
}
