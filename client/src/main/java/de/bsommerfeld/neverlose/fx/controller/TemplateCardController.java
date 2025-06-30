package de.bsommerfeld.neverlose.fx.controller;

import de.bsommerfeld.neverlose.fx.controller.base.AbstractCardController;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.persistence.model.UnitSummary;

import java.util.UUID;

/**
 * Controller for the template card view that displays a single training unit template.
 * Handles selection and deletion of the template.
 */
@View
public class TemplateCardController extends AbstractCardController<UnitSummary> {

    /**
     * Sets the template to display in this card.
     *
     * @param template the template summary to display
     */
    public void setTemplate(UnitSummary template) {
        setItem(template);
    }

    @Override
    protected String getItemName(UnitSummary item) {
        return item.name();
    }

    @Override
    protected UUID getItemId(UnitSummary item) {
        return item.identifier();
    }
}
