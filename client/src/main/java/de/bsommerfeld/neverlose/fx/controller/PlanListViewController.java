package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.state.SearchState;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.fx.view.ViewWrapper;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.model.PlanSummary;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;

/** Controller for the plan list view that displays all available training plans. */
@View
public class PlanListViewController {

  private static final LogFacade log = LogFacadeFactory.getLogger();

  private final ViewProvider viewProvider;
  private final PlanStorageService planStorageService;
  private final SearchState searchState;
  private NeverLoseMetaController metaController;

  @FXML private ScrollPane scrollPane;

  @FXML private FlowPane flowPane;

  @FXML private TextField searchTextField;

  private List<PlanSummary> allPlans = new ArrayList<>();
  private ChangeListener<String> searchListener;

  @Inject
  public PlanListViewController(
      ViewProvider viewProvider, PlanStorageService planStorageService, SearchState searchState) {
    this.viewProvider = viewProvider;
    this.planStorageService = planStorageService;
    this.searchState = searchState;
  }

  @FXML
  private void initialize() {
    loadPlans();
    setupSearchListener();
    // Bind the search text field to the search state
    javafx.beans.binding.Bindings.bindBidirectional(searchTextField.textProperty(), searchState.searchTermProperty());
  }

  /**
   * Sets a reference to the meta controller for navigation.
   *
   * @param metaController the meta controller
   */
  public void setMetaController(NeverLoseMetaController metaController) {
    this.metaController = metaController;
  }

  /** Loads all available plans and displays them in the flow pane. */
  private void loadPlans() {
    try {
      allPlans = planStorageService.loadPlanSummaries();
      displayPlans(allPlans);
    } catch (IOException e) {
      log.error("Failed to load plan summaries", e);
      showErrorMessage("Failed to load plans");
    }
  }

  /**
   * Displays the given plans in the flow pane.
   *
   * @param plans the plans to display
   */
  private void displayPlans(List<PlanSummary> plans) {
    flowPane.getChildren().clear();

    if (plans.isEmpty()) {
      showNoPlansMessage();
      return;
    }

    for (PlanSummary plan : plans) {
      try {
        Node planCard = createPlanCard(plan);
        flowPane.getChildren().add(planCard);
      } catch (IOException e) {
        log.error("Failed to create plan card for plan: {}", plan.name(), e);
      }
    }
  }

  /**
   * Creates a plan card for the given plan.
   *
   * @param plan the plan to create a card for
   * @return the plan card node
   * @throws IOException if loading the plan card view fails
   */
  private Node createPlanCard(PlanSummary plan) throws IOException {
    ViewWrapper<PlanCardController> viewWrapper =
        viewProvider.requestView(PlanCardController.class);
    PlanCardController controller = viewWrapper.controller();
    controller.setPlan(plan);
    controller.setPlanStorageService(planStorageService);
    controller.setParentController(this);

    // Add double-click event handler to open the plan
    Node planCard = viewWrapper.parent();
    planCard.addEventHandler(
        MouseEvent.MOUSE_CLICKED,
        event -> {
          if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            openPlan(plan.identifier());
          }
        });

    return planCard;
  }

  /**
   * Opens the plan with the given ID in the editor.
   *
   * @param planId the ID of the plan to open
   */
  private void openPlan(UUID planId) {
    if (metaController == null) {
      log.error("Meta controller not set, cannot open plan");
      return;
    }

    try {
      planStorageService
          .loadPlan(planId)
          .ifPresent(plan -> metaController.showTrainingPlanEditor(plan));
    } catch (IOException e) {
      log.error("Failed to load plan with ID: {}", planId, e);
      showErrorMessage("Failed to load plan");
    }
  }

  /** Shows a message when no plans are available. */
  private void showNoPlansMessage() {
    Label noPlansLabel = new Label("No plans found");
    noPlansLabel.getStyleClass().add("search-results-placeholder");
    flowPane.getChildren().add(noPlansLabel);
  }

  /**
   * Shows an error message in the flow pane.
   *
   * @param message the error message to show
   */
  private void showErrorMessage(String message) {
    Label errorLabel = new Label(message);
    errorLabel.getStyleClass().add("search-results-placeholder");
    flowPane.getChildren().clear();
    flowPane.getChildren().add(errorLabel);
  }

  /** Sets up the search listener to filter plans when the search term changes. */
  private void setupSearchListener() {
    // Remove existing listener if any
    if (searchListener != null) {
      searchState.searchTermProperty().removeListener(searchListener);
    }

    // Create and add new listener
    searchListener = (observable, oldValue, newValue) -> filterPlans(newValue);
    searchState.searchTermProperty().addListener(searchListener);

    // Initial filter with current search term
    Platform.runLater(() -> filterPlans(searchState.getSearchTerm()));
  }

  /**
   * Filters the displayed plans based on the given search query.
   *
   * @param query the search query
   */
  public void filterPlans(String query) {
    if (query == null || query.isBlank()) {
      displayPlans(allPlans);
      return;
    }

    String lowerQuery = query.toLowerCase();
    List<PlanSummary> filteredPlans =
        allPlans.stream()
            .filter(plan -> plan.name().toLowerCase().contains(lowerQuery))
            .collect(Collectors.toList());

    displayPlans(filteredPlans);
  }

  /** Refreshes the plan list. */
  public void refreshPlans() {
    loadPlans();
  }
}
