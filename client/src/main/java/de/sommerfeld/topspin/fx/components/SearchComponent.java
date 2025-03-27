package de.sommerfeld.topspin.fx.components;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a search component as a custom UI element that combines a text field and a popup suggestion list for
 * dynamic and interactive searching.
 * <p>
 * The component enables text-based queries over a predefined list of items and displays relevant suggestions in a
 * special popup menu. Suggestions update dynamically as the user types, starting with a full sorted list when the
 * field gains focus, and can be selected for further interaction. The suggestion popup remains visible even if no items
 * match the search term.
 * <p>
 * Features include: - Filtering suggestions using a debounce mechanism to minimize unnecessary updates on rapid input
 * changes. - Dynamically displaying a filtered list of suggestions in a popup. - Showing all available items, sorted
 * alphabetically, when the search field gains focus and is empty. - Keeping the suggestion popup open even when no
 * results are found (showing a placeholder message). - Selection handling for user-chosen items from the suggestion
 * list. - Keyboard navigation support (Down, Enter, Escape). - Configurable through the provided string converter for
 * representing items as string.
 * <p>
 * Generic Type {@code <T>} corresponds to the type of objects in the source list and suggestion list.
 *
 * @param <T> The type of items managed and displayed by the SearchComponent.
 */
public class SearchComponent<T> extends VBox {

    private final TextField textField;
    private final ObservableList<T> allItems;
    private final Function<T, String> stringConverter;

    private final ListView<T> suggestionsListView;
    private final PopupControl suggestionsPopup;
    private final FilteredList<T> filteredList;
    private final SortedList<T> sortedList;

    private final PauseTransition debounceTimer;
    private final long debounceDelayMillis = 300;

    private final ReadOnlyObjectWrapper<T> selectedItem = new ReadOnlyObjectWrapper<>();

    private final Predicate<T> showAllPredicate = p -> true;

    /**
     * Constructs a SearchComponent that provides a searchable text field with dynamic suggestions. This component
     * allows for filtering a list of items based on user input and displaying matching results.
     *
     * @param initialItems    the initial list of items to be displayed and filtered by the search component
     * @param stringConverter a function that converts each item in the list to a string for display and comparison
     * @throws IllegalArgumentException if either initialItems or stringConverter is null
     */
    public SearchComponent(ObservableList<T> initialItems, Function<T, String> stringConverter) {
        if (initialItems == null || stringConverter == null) {
            throw new IllegalArgumentException("Initial items and string converter cannot be null.");
        }
        this.allItems = initialItems;
        this.stringConverter = stringConverter;

        textField = new TextField();
        textField.setPromptText("Suchen...");
        textField.getStyleClass().add("search-textfield");

        suggestionsListView = new ListView<>();
        suggestionsListView.getStyleClass().add("search-results-list");
        suggestionsListView.setPrefHeight(150); // Consider making this flexible

        Label placeholderLabel = new Label("No Results Found");
        placeholderLabel.setStyle("-fx-padding: 10px; -fx-text-fill: grey;");
        suggestionsListView.setPlaceholder(placeholderLabel);

        suggestionsPopup = new PopupControl();
        suggestionsPopup.setAutoHide(true);
        suggestionsPopup.setConsumeAutoHidingEvents(false);
        suggestionsPopup.setSkin(new Skin<PopupControl>() {
            @Override
            public PopupControl getSkinnable() {
                return suggestionsPopup;
            }

            @Override
            public Node getNode() {
                return suggestionsListView;
            }

            @Override
            public void dispose() {
            }
        });
        suggestionsPopup.getStyleClass().add("search-results-popup");

        sortedList = new SortedList<>(allItems, Comparator.comparing(stringConverter, String.CASE_INSENSITIVE_ORDER));
        filteredList = new FilteredList<>(sortedList, p -> false);
        suggestionsListView.setItems(filteredList);

        debounceTimer = new PauseTransition(Duration.millis(debounceDelayMillis));
        debounceTimer.setOnFinished(event -> performSearch());

        suggestionsListView.prefWidthProperty().bind(textField.widthProperty());

        this.getChildren().add(textField);
        this.getStyleClass().add("search-component");

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                debounceTimer.stop();
                filteredList.setPredicate(showAllPredicate);
                showSuggestions();
            } else {
                debounceTimer.playFromStart();
            }
        });

        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                String currentText = textField.getText();
                if (currentText == null || currentText.isEmpty()) {
                    filteredList.setPredicate(showAllPredicate);
                    showSuggestions();
                } else {
                    performSearch();
                }
            } else {
                if (!suggestionsPopup.isFocused()) {
                    hideSuggestions();
                }
            }
        });

        suggestionsPopup.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                if (!textField.isFocused()) {
                    hideSuggestions();
                }
            }
        });


        suggestionsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedItem.set(newVal);
                Platform.runLater(() -> {
                    textField.setText(stringConverter.apply(newVal));
                    textField.positionCaret(textField.getText().length());
                    hideSuggestions();
                });
            }
        });

        textField.setOnAction(event -> {
            if (suggestionsPopup.isShowing() && !suggestionsListView.getItems().isEmpty()) {
                T firstItem = suggestionsListView.getItems().get(0);
                selectedItem.set(firstItem);
                Platform.runLater(() -> {
                    textField.setText(stringConverter.apply(firstItem));
                    textField.positionCaret(textField.getText().length());
                    hideSuggestions();
                });
                event.consume();
            }
        });

        textField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DOWN:
                    if (suggestionsPopup.isShowing() && !suggestionsListView.getItems().isEmpty()) {
                        Platform.runLater(() -> {
                            suggestionsListView.requestFocus();
                            suggestionsListView.getSelectionModel().selectFirst();
                        });
                        event.consume();
                    }
                    break;
                case UP:
                    if (suggestionsPopup.isShowing()) {
                        event.consume();
                    }
                    break;
                case ESCAPE:
                    if (suggestionsPopup.isShowing()) {
                        hideSuggestions();
                        event.consume();
                    }
                    break;
                default:
                    break;
            }
        });

        suggestionsListView.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    T selected = suggestionsListView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        // Selection model listener handles the update and hide
                    }
                    event.consume();
                    break;
                case ESCAPE:
                    hideSuggestions();
                    textField.requestFocus();
                    event.consume();
                    break;
                case UP:
                    if (suggestionsListView.getSelectionModel().getSelectedIndex() == 0) {
                        // Optionally move focus back to text field
                        // Platform.runLater(textField::requestFocus);
                        // event.consume();
                    }
                    break;
                default:
                    break;
            }
        });
    }

    private void performSearch() {
        String searchTerm = textField.getText();
        if (searchTerm == null || searchTerm.isEmpty()) {
            if (textField.isFocused()) {
                filteredList.setPredicate(showAllPredicate);
                showSuggestions();
            }
            return;
        }

        String lowerCaseSearchTerm = searchTerm.toLowerCase();

        Predicate<T> predicate = item -> {
            String itemText = stringConverter.apply(item);
            return itemText != null && itemText.toLowerCase().contains(lowerCaseSearchTerm);
        };
        filteredList.setPredicate(predicate);

        showSuggestions();
    }


    private void showSuggestions() {
        if (suggestionsPopup.isShowing()) {
            return;
        }
        Window owner = getScene() != null ? getScene().getWindow() : null;
        if (owner == null || owner.getScene() == null) {
            return;
        }

        Bounds tbScreenBounds = textField.localToScreen(textField.getBoundsInLocal());
        if (tbScreenBounds == null) {
            System.err.println("Couldn't get TextField screen bounds.");
            return;
        }

        double popupX = tbScreenBounds.getMinX();
        double popupY = tbScreenBounds.getMaxY();

        // Basic check to prevent popup going off screen bottom
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        double estimatedPopupHeight = suggestionsListView.getPrefHeight();
        if (popupY + estimatedPopupHeight > screenHeight - 10) {
            popupY = tbScreenBounds.getMinY() - estimatedPopupHeight;
            // Consider more accurate height calculation if necessary
        }

        suggestionsPopup.setX(popupX);
        suggestionsPopup.setY(popupY);

        suggestionsPopup.show(owner);
        suggestionsListView.scrollTo(0);
    }

    private void hideSuggestions() {
        suggestionsPopup.hide();
    }

    /**
     * Retrieves the TextField used within the SearchComponent. The TextField provides the user interface element for
     * text input and interaction.
     *
     * @return the TextField instance associated with this SearchComponent.
     */
    public TextField getTextField() {
        return textField;
    }

    /**
     * Provides a read-only property for observing the currently selected item in the SearchComponent. This property
     * allows external components to listen for changes to the selection and respond accordingly.
     *
     * @return a ReadOnlyObjectProperty representing the currently selected item, which may be null if no item is
     * selected.
     */
    public ReadOnlyObjectProperty<T> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /**
     * Retrieves the currently selected item in the SearchComponent. If no item is selected, the method returns null.
     *
     * @return the currently selected item, or null if no item is selected.
     */
    public T getSelectedItem() {
        return selectedItem.get();
    }

    /**
     * Sets the prompt text that is displayed in the text field when it is empty. The prompt text serves as a hint to
     * the user about what kind of input is expected.
     *
     * @param text the prompt text to display in the text field.
     */
    public void setPromptText(String text) {
        textField.setPromptText(text);
    }
}