package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.controller.TopBarController.Alignment;
import de.bsommerfeld.neverlose.fx.messages.Messages;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.fx.state.SearchState;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.model.PlanSummary;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Controller for the plan list view that displays all available training plans. */
@View
public class PlanListViewController implements ControlsProvider {

    private static final LogFacade log = LogFacadeFactory.getLogger();
    private final ViewProvider viewProvider;
    private final PlanStorageService planStorageService;
    private final SearchState searchState;
    private final NotificationService notificationService;
    // Cache for lazily loaded plan metadata (description, units)
    private final Map<UUID, PlanMeta> planMetaCache = new java.util.concurrent.ConcurrentHashMap<>();
    private Consumer<TrainingPlan> onPlanSelected;
    @FXML
    private ListView<PlanSummary> listView;

    private TextField searchTextField;
    private Label searchLabel;
    private HBox searchContainer;

    private List<PlanSummary> allPlans = new ArrayList<>();
    private ChangeListener<String> searchListener;
    private String activeSearchTerm = "";

    @Inject
    public PlanListViewController(
            ViewProvider viewProvider,
            PlanStorageService planStorageService,
            SearchState searchState,
            NotificationService notificationService) {
        this.viewProvider = viewProvider;
        this.planStorageService = planStorageService;
        this.searchState = searchState;
        this.notificationService = notificationService;
    }

    /**
     * Allows embedding layouts (like the combined view) to intercept a plan selection. If set, selecting a plan will
     * call this consumer instead of navigating to the editor view.
     */
    public void setOnPlanSelected(Consumer<TrainingPlan> onPlanSelected) {
        this.onPlanSelected = onPlanSelected;
    }

    @Override
    public Alignment alignment() {
        return Alignment.CENTER;
    }

    /**
     * Returns a list of control nodes to be displayed in the top bar. This implementation provides the search
     * components with CENTER alignment.
     */
    @Override
    public Node[] controls() {
        if (searchContainer != null) {
            log.debug("Providing search components for top bar with CENTER alignment");
            return new Node[]{searchContainer};
        }

        throw new IllegalStateException("SearchContainer cannot be null.");
    }

    @FXML
    private void initialize() {
        // Create search components first so header is ready
        createSearchComponents();

        // Configure compact list behavior
        configureListView();

        // Load data
        loadPlans();
    }

    private void configureListView() {
        // Custom cell showing name and a meta row: description (left, ellipsized) + units icon+count (right)
        listView.setCellFactory(lv -> new ListCell<>() {
            private final Label title = new Label();
            private final javafx.scene.shape.SVGPath exerciseIcon = new javafx.scene.shape.SVGPath();
            private final Label exerciseCountLabel = new Label();
            private final HBox exercisesBox = new HBox(exerciseIcon, exerciseCountLabel);
            private final javafx.scene.shape.SVGPath unitIcon = new javafx.scene.shape.SVGPath();
            private final Label unitCountLabel = new Label();
            private final HBox unitsBox = new HBox(unitIcon, unitCountLabel);
            private final HBox metaRow = new HBox(exercisesBox, unitsBox);
            private final VBox box = new VBox(title, metaRow);
            private final Region spacer = new Region();
            private UUID boundId;

            {
                title.getStyleClass().add("plan-list-title");

                metaRow.getChildren().addFirst(spacer);

                // Spacer pushes icons to the right
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                // Exercises icon and label (three horizontal bars)
                exerciseIcon.getStyleClass().add("plan-exercise-icon");
                exerciseIcon.setContent("M0 4 H10 V6 H0 Z M0 7 H10 V9 H0 Z M0 10 H10 V12 H0 Z");
                exerciseIcon.setScaleX(2.0);
                exerciseIcon.setScaleY(2.0);
                exerciseCountLabel.getStyleClass().add("plan-exercise-count");
                exercisesBox.getStyleClass().add("plan-exercises");
                exercisesBox.setSpacing(6);
                exercisesBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                // Reserve fixed width so column stays aligned across rows
                exercisesBox.setMinWidth(84);
                exercisesBox.setPrefWidth(84);
                exercisesBox.setMaxWidth(84);
                exercisesBox.setManaged(true);

                // Units icon and label (dumbbell)
                unitIcon.getStyleClass().add("plan-unit-icon");
                unitIcon.setContent("M0 5 H2 V9 H0 Z M2 6 H8 V8 H2 Z M8 5 H10 V9 H8 Z");
                unitIcon.setScaleX(2.0);
                unitIcon.setScaleY(2.0);
                unitCountLabel.getStyleClass().add("plan-unit-count");
                unitsBox.getStyleClass().add("plan-units");
                unitsBox.setSpacing(6);
                unitsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                // Reserve fixed width so column stays aligned across rows
                unitsBox.setMinWidth(72);
                unitsBox.setPrefWidth(72);
                unitsBox.setMaxWidth(72);
                unitsBox.setManaged(true);

                // Meta row container
                metaRow.getStyleClass().add("plan-list-meta");
                metaRow.setSpacing(10);
                metaRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                HBox.setHgrow(metaRow, javafx.scene.layout.Priority.ALWAYS);

                // Layout spacing between title and meta row
                box.setSpacing(2);
            }

            @Override
            protected void updateItem(PlanSummary item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null);
                    boundId = null;
                } else {
                    setText(null);
                    title.setText(item.name());
                    // Hide meta row until metadata arrives
                    exercisesBox.setVisible(false);
                    exercisesBox.setManaged(true); // keep space reserved
                    unitsBox.setVisible(false);
                    unitsBox.setManaged(true); // keep space reserved
                    metaRow.setVisible(false);
                    metaRow.setManaged(false);
                    setTooltip(null);
                    setGraphic(box);
                    boundId = item.identifier();

                    PlanMeta meta = planMetaCache.get(boundId);
                    if (meta != null) {
                        applyMeta(meta);
                    } else {
                        // Capture id for async task to avoid cell reuse issues
                        final UUID requestedId = boundId;
                        // Load metadata asynchronously to keep UI responsive
                        java.util.concurrent.CompletableFuture
                                .supplyAsync(() -> {
                                    try {
                                        java.util.Optional<TrainingPlan> opt = planStorageService.loadPlan(requestedId);
                                        return opt.orElse(null);
                                    } catch (IOException e) {
                                        log.error(Messages.getString("log.plan.loadFailed", requestedId), e);
                                        return null;
                                    }
                                })
                                .thenAccept(plan -> {
                                    if (plan == null) return;
                                    String desc = plan.getDescription() != null ? plan.getDescription() : "";
                                    int units = 0;
                                    int exercises = 0;
                                    try {
                                        var trainingUnits = (plan.getTrainingUnits() != null) ? plan.getTrainingUnits().getAll() : java.util.Collections.<de.bsommerfeld.neverlose.plan.components.TrainingUnit>emptyList();
                                        units = trainingUnits.size();
                                        for (de.bsommerfeld.neverlose.plan.components.TrainingUnit u : trainingUnits) {
                                            try {
                                                var exList = (u.getTrainingExercises() != null) ? u.getTrainingExercises().getAll() : java.util.Collections.<de.bsommerfeld.neverlose.plan.components.TrainingExercise>emptyList();
                                                exercises += exList.size();
                                            } catch (Exception ignoredInner) {
                                            }
                                        }
                                    } catch (Exception ignored) {
                                    }
                                    PlanMeta computed = new PlanMeta(desc, units, exercises);
                                    planMetaCache.put(requestedId, computed);
                                    Platform.runLater(() -> {
                                        if (getItem() != null && getItem().identifier().equals(requestedId)) {
                                            applyMeta(computed);
                                        }
                                    });
                                });
                    }
                }
            }

            private void applyMeta(PlanMeta meta) {
                String desc = meta.description != null ? meta.description.trim() : "";
                boolean hasUnits = meta.unitCount > 0;
                boolean hasExercises = meta.exerciseCount > 0;

                // Tooltip holds the full description (no inline text)
                setTooltip(!desc.isBlank() ? new Tooltip(desc) : null);

                // Exercises indicator
                if (hasExercises) {
                    exerciseCountLabel.setText(Integer.toString(meta.exerciseCount));
                    exercisesBox.setVisible(true);
                } else {
                    exercisesBox.setVisible(false);
                }
                exercisesBox.setManaged(true); // always reserve space for alignment

                // Units indicator
                if (hasUnits) {
                    unitCountLabel.setText(Integer.toString(meta.unitCount));
                    unitsBox.setVisible(true);
                } else {
                    unitsBox.setVisible(false);
                }
                unitsBox.setManaged(true); // always reserve space for alignment

                // Show meta row if any indicator is visible
                boolean showMeta = hasUnits || hasExercises;
                metaRow.setVisible(showMeta);
                metaRow.setManaged(showMeta);
            }
        });

        // Double-click to open
        listView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                openSelectedPlan();
            }
        });

        // Enter key to open
        listView.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                openSelectedPlan();
            }
        });

        // Default placeholder
        Label placeholder = new Label(Messages.getString("ui.label.noPlans"));
        placeholder.getStyleClass().add("search-results-placeholder");
        listView.setPlaceholder(placeholder);
    }

    /** Creates the search components that will be registered with the TopBar. */
    private void createSearchComponents() {
        // Create search container
        searchContainer = new HBox();
        searchContainer.setSpacing(5);
        searchContainer.setAlignment(javafx.geometry.Pos.CENTER);
        searchContainer.getStyleClass().add("search-container");
        // Allow the container to grow when parent grants space
        searchContainer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchContainer, javafx.scene.layout.Priority.ALWAYS);

        // Create search text field
        searchTextField = new TextField();
        searchTextField.setPromptText(Messages.getString("fxml.planListView.searchPrompt"));
        searchTextField.getStyleClass().add("search-field");
        // Let the text field take all available width inside the container
        HBox.setHgrow(searchTextField, javafx.scene.layout.Priority.ALWAYS);
        searchTextField.setMaxWidth(Double.MAX_VALUE);

        // Create search label
        searchLabel = new Label(Messages.getString("fxml.planListView.searchLabel"));
        searchLabel.getStyleClass().add("search-label");
        searchLabel.setOnMouseClicked(this::onSearch);

        // Add components to container
        searchContainer.getChildren().addAll(searchTextField, searchLabel);

        // Setup search functionality
        setupSearchListener();

        // Bind the search text field to the search state
        Bindings.bindBidirectional(searchTextField.textProperty(), searchState.searchTermProperty());
    }

    private void onSearch(MouseEvent mouseEvent) {
        performSearch();
    }

    private void performSearch() {
        String currentSearchTerm = searchTextField.getText();

        // If the search button shows "X" (we're already filtering with this term)
        if (Objects.equals(searchLabel.getText(), Messages.getString("ui.button.clear"))) {
            // Clear the search
            searchTextField.clear();
            searchLabel.setText(Messages.getString("ui.button.search"));
            activeSearchTerm = "";
            displayPlans(allPlans);
        } else {
            // Apply the search filter
            applySearchFilter(!currentSearchTerm.isBlank(), currentSearchTerm);
        }
    }

    private void applySearchFilter(boolean isNotBlank, String searchTerm) {
        if (isNotBlank) {
            activeSearchTerm = searchTerm;
            filterPlans(searchTerm);
            searchLabel.setText(Messages.getString("ui.button.clear"));
        }
    }

    /** Loads all available plans and displays them in the flow pane. */
    private void loadPlans() {
        try {
            allPlans = planStorageService.loadPlanSummaries();
            displayPlans(allPlans);
        } catch (IOException e) {
            log.error(Messages.getString("log.plan.loadFailed"), e);
            showErrorMessage(Messages.getString("error.plan.loadFailed.title"));
        }
    }

    /**
     * Displays the given plans in a compact ListView.
     *
     * @param plans the plans to display
     */
    private void displayPlans(List<PlanSummary> plans) {
        if (plans == null || plans.isEmpty()) {
            showNoPlansMessage();
            return;
        }
        // Populate list
        listView.getItems().setAll(plans);
    }

    private void openSelectedPlan() {
        PlanSummary selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openPlan(selected.identifier());
        }
    }

    /**
     * Opens the plan with the given ID in the editor.
     *
     * @param planId the ID of the plan to open
     */
    private void openPlan(UUID planId) {
        try {
            planStorageService
                    .loadPlan(planId)
                    .ifPresent(plan -> {
                        if (onPlanSelected != null) {
                            onPlanSelected.accept(plan);
                        } else {
                            viewProvider.triggerViewChange(
                                    TrainingPlanEditorController.class, t -> t.setTrainingPlan(plan));
                        }
                    });
        } catch (IOException e) {
            log.error(Messages.getString("log.plan.loadFailed", planId), e);
            showErrorMessage(Messages.getString("error.plan.loadSingleFailed.title"));
        }
    }

    /** Shows a message when no plans are available. */
    private void showNoPlansMessage() {
        listView.getItems().clear();
        Label placeholder = new Label(Messages.getString("ui.label.noPlans"));
        placeholder.getStyleClass().add("search-results-placeholder");
        listView.setPlaceholder(placeholder);
    }

    /**
     * Shows an error message in the list view.
     *
     * @param message the error message to show
     */
    private void showErrorMessage(String message) {
        listView.getItems().clear();
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("search-results-placeholder");
        listView.setPlaceholder(errorLabel);
    }

    /** Sets up the search listener to filter plans when the search term changes. */
    private void setupSearchListener() {
        // Remove existing listener if any
        if (searchListener != null) {
            searchState.searchTermProperty().removeListener(searchListener);
        }

        searchTextField.setOnKeyPressed(
                keyEvent -> {
                    KeyCode keyCode = keyEvent.getCode();
                    String currentSearchTerm = searchTextField.getText();
                    boolean isBlank = searchTextField.getText().isBlank();

                    if (keyCode == KeyCode.ENTER) {
                        applySearchFilter(!isBlank, currentSearchTerm);
                    }
                });

        // Create and add new listener
        searchListener =
                (observable, oldValue, newValue) -> {
                    // Update the search button text based on the search term
                    if (newValue != null && !newValue.isBlank()) {
                        if (Objects.equals(newValue, activeSearchTerm)) {
                            searchLabel.setText(Messages.getString("ui.button.clear"));
                        } else {
                            searchLabel.setText(Messages.getString("ui.button.search"));
                        }
                    } else {
                        searchLabel.setText(Messages.getString("ui.button.search"));
                        activeSearchTerm = "";
                    }
                };
        searchState.searchTermProperty().addListener(searchListener);

        // Initial filter with current search term
        Platform.runLater(
                () -> {
                    String currentTerm = searchState.getSearchTerm();
                    applySearchFilter(currentTerm != null && !currentTerm.isBlank(), currentTerm);
                });
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

    private static class PlanMeta {
        final String description;
        final int unitCount;
        final int exerciseCount;

        PlanMeta(String description, int unitCount, int exerciseCount) {
            this.description = description;
            this.unitCount = unitCount;
            this.exerciseCount = exerciseCount;
        }
    }
}
