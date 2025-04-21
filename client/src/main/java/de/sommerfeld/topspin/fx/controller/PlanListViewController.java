package de.sommerfeld.topspin.fx.controller;

import com.google.inject.Inject;
import de.sommerfeld.topspin.fx.state.SearchState;
import de.sommerfeld.topspin.fx.view.View;
import de.sommerfeld.topspin.fx.view.ViewProvider;
import de.sommerfeld.topspin.logger.LogFacade;
import de.sommerfeld.topspin.logger.LogFacadeFactory;
import de.sommerfeld.topspin.persistence.model.PlanSummary;
import de.sommerfeld.topspin.persistence.service.PlanStorageService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@View
public class PlanListViewController {

    private static final LogFacade log = LogFacadeFactory.getLogger();

    private final PlanStorageService planStorageService;
    private final SearchState searchState;
    private final ViewProvider viewProvider;
    private final ObservableList<PlanSummary> allPlans = FXCollections.observableArrayList();

    @FXML
    private FlowPane planFlowPane;
    private FilteredList<PlanSummary> filteredPlans;

    @Inject
    public PlanListViewController(PlanStorageService planStorageService, SearchState searchState, ViewProvider viewProvider) {
        this.planStorageService = planStorageService;
        this.searchState = searchState;
        this.viewProvider = viewProvider;
    }

    @FXML
    private void initialize() {
        log.debug("Initializing PlanListViewController...");
        filteredPlans = new FilteredList<>(allPlans, p -> true);

        searchState.searchTermProperty().addListener((obs, oldVal, newVal) -> {
            log.trace("Search term changed to: '{}'", newVal);
            updateFilterPredicate(newVal);
        });

        filteredPlans.addListener((ListChangeListener<PlanSummary>) c -> {
            log.trace("Filtered plan list changed, updating display.");
            displayPlans();
        });

        loadAndDisplayPlans();
    }

    /**
     * Loads plan summaries from storage and updates the main list.
     */
    private void loadAndDisplayPlans() {
        log.debug("Loading plan summaries...");
        try {
            List<PlanSummary> summaries = planStorageService.loadPlanSummaries();
            Platform.runLater(() -> {
                allPlans.setAll(summaries);
                log.info("Loaded {} plan summaries.", summaries.size());
                updateFilterPredicate(searchState.getSearchTerm());
            });
        } catch (IOException e) {
            log.error("Failed to load plan summaries from storage path: {}", planStorageService.getStoragePath(), e);
            // TODO: Benutzerfeedback geben (z.B. Dialog oder Label in der View)
            Platform.runLater(allPlans::clear);
        }
    }

    /**
     * Updates the predicate of the FilteredList based on the search term.
     */
    private void updateFilterPredicate(String searchTerm) {
        final String lowerCaseFilter = (searchTerm == null) ? "" : searchTerm.toLowerCase().trim();

        filteredPlans.setPredicate(planSummary -> {
            if (lowerCaseFilter.isEmpty()) {
                return true;
            }
            return planSummary.name().toLowerCase().contains(lowerCaseFilter);
        });
    }


    /**
     * Clears the FlowPane and repopulates it based on the current FilteredList.
     */
    private void displayPlans() {
        planFlowPane.getChildren().clear();

        if (filteredPlans.isEmpty()) {
            log.debug("No plans to display (filtered list is empty).");
            Label placeholder = new Label("No plans found.");
            placeholder.getStyleClass().add("search-results-placeholder");
            planFlowPane.getChildren().add(placeholder);
            return;
        }

        log.debug("Displaying {} filtered plans.", filteredPlans.size());
        for (PlanSummary summary : filteredPlans) {
            VBox planNode = createPlanNode(summary);
            planFlowPane.getChildren().add(planNode);
        }
    }

    /**
     * Creates a simple UI node (VBox with Label) to represent a PlanSummary.
     * Adds a click handler to navigate to the editor for that plan.
     */
    private VBox createPlanNode(PlanSummary summary) {
        VBox card = new VBox();
        card.getStyleClass().add("plan-card");
        card.setPadding(new Insets(10));
        card.setPrefSize(180, 100);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setCursor(Cursor.HAND);

        ImageView iconView = null;
        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/plan-icon.jpg")));
            iconView = new ImageView(icon);
            iconView.setFitHeight(32);
            iconView.setFitWidth(32);
            iconView.setPreserveRatio(true);
        } catch (Exception e) {
            log.error("Could not load plan card icon", e);
        }

        Label nameLabel = new Label(summary.name());
        nameLabel.getStyleClass().add("plan-card-title");
        nameLabel.setWrapText(true);

        Label idLabel = new Label("ID: " + summary.identifier().substring(0, Math.min(summary.identifier().length(), 15)) + "...");
        idLabel.getStyleClass().add("plan-card-subtitle");

        if (iconView != null) {
            card.getChildren().add(iconView);
            VBox.setMargin(iconView, new Insets(0, 0, 5, 0));
        }

        card.getChildren().addAll(nameLabel, idLabel);
        card.setOnMouseClicked(event -> handlePlanSelection(summary));

        return card;
    }

    /**
     * Handles the selection of a plan from the list view.
     * Loads the full plan and triggers a view change to the editor.
     */
    private void handlePlanSelection(PlanSummary summary) {
        log.info("Plan selected: {}", summary.identifier());
        // TODO: Evtl. Loading-Indikator anzeigen

        try {
            Optional<de.sommerfeld.topspin.plan.TrainingPlan> planOptional = planStorageService.loadPlan(summary.identifier());
            if (planOptional.isPresent()) {
                de.sommerfeld.topspin.plan.TrainingPlan loadedPlan = planOptional.get();
                log.debug("Plan '{}' loaded successfully, switching to editor.", loadedPlan.getName());
                viewProvider.triggerViewChange(TrainingPlanEditorMetaController.class, editor -> editor.setPlan(loadedPlan));
                searchState.setSearchTerm("");
            } else {
                log.error("Failed to load selected plan with identifier: {}. File might be missing or corrupted.", summary.identifier());
                // TODO: Benutzerfeedback (Dialog?)
            }
        } catch (IOException e) {
            log.error("IOException occurred while loading plan: {}", summary.identifier(), e);
            // TODO: Benutzerfeedback (Dialog?)
        }
    }
}