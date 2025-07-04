package de.bsommerfeld.neverlose.fx.controller;

import de.bsommerfeld.neverlose.fx.controller.base.AbstractCardController;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.persistence.model.ExerciseSummary;
import java.util.UUID;

/**
 * Controller for the exercise card view that displays a single training exercise template. Handles
 * selection and deletion of the template.
 */
@View
public class ExerciseCardController extends AbstractCardController<ExerciseSummary> {

  /**
   * Sets the template to display in this card.
   *
   * @param template the template summary to display
   */
  public void setTemplate(ExerciseSummary template) {
    setItem(template);
  }

  @Override
  protected String getItemName(ExerciseSummary item) {
    return item.name();
  }

  @Override
  protected UUID getItemId(ExerciseSummary item) {
    return item.identifier();
  }
}
