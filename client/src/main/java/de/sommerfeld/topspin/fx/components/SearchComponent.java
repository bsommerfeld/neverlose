package de.sommerfeld.topspin.fx.components;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a search component as a custom UI element that combines a text field and a popup suggestion list for
 * dynamic and interactive searching.
 * <p>
 * The component enables text-based queries over a predefined list of items and displays relevant suggestions in a
 * special popup menu. Suggestions update dynamically as the user types and can be selected for further interaction.
 * <p>
 * Features include: - Filtering suggestions using a debounce mechanism to minimize unnecessary updates on rapid input
 * changes. - Dynamically displaying a filtered list of suggestions in a popup. - Selection handling for user-chosen
 * items from the suggestion list. - Configurable through the provided string converter for representing items as
 * string.
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

    private final PauseTransition debounceTimer;
    private final long debounceDelayMillis = 300;

    private final ReadOnlyObjectWrapper<T> selectedItem = new ReadOnlyObjectWrapper<>();

    /**
     * Constructs a SearchComponent that provides a searchable text field with dynamic suggestions. This component
     * allows for filtering a list of items based on user input and displaying matching results.
     *
     * @param initialItems    the initial list of items to be displayed and filtered by the search component
     * @param stringConverter a function that converts each item in the list to a string for comparison with the search
     *                        term
     *
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
        suggestionsListView.setPrefHeight(150);

        suggestionsPopup = new PopupControl();
        suggestionsPopup.setAutoHide(true);
        suggestionsPopup.setSkin(new javafx.scene.control.Skin<PopupControl>() {
            @Override
            public PopupControl getSkinnable() {
                return suggestionsPopup;
            }

            @Override
            public javafx.scene.Node getNode() {
                return suggestionsListView;
            }

            @Override
            public void dispose() {
            }
        });
        suggestionsPopup.getStyleClass().add("search-results-popup");

        filteredList = new FilteredList<>(allItems, p -> false); // Initial empty
        suggestionsListView.setItems(filteredList);

        debounceTimer = new PauseTransition(Duration.millis(debounceDelayMillis));
        debounceTimer.setOnFinished(event -> performSearch());

        suggestionsListView.prefWidthProperty().bind(textField.widthProperty());
        this.getChildren().add(textField);
        this.getStyleClass().add("search-component");

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // 1. Textänderung im TextField -> Debounce starten
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                hideSuggestions();
                debounceTimer.stop();
            } else {
                // Timer (neu) starten bei jeder Eingabe
                debounceTimer.playFromStart();
            }
        });

        // 2. Fokusverlust des TextFields -> Popup schließen (wenn Fokus nicht ins Popup geht)
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !suggestionsPopup.isFocused()) {
                hideSuggestions();
            }
        });

        // 3. Auswahl in der ListView -> Element setzen, Popup schließen
        suggestionsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedItem.set(newVal);
                // Optional: Text im TextField aktualisieren
                // Platform.runLater verhindert Probleme beim direkten Setzen während des Listener-Aufrufs
                Platform.runLater(() -> {
                    textField.setText(stringConverter.apply(newVal));
                    textField.positionCaret(textField.getText().length()); // Cursor ans Ende
                });
                hideSuggestions(); // Wichtig: Popup nach Auswahl schließen
                textField.requestFocus(); // Fokus zurück zum Textfeld
            }
        });

        // Optional: Enter im TextField könnte ersten Eintrag auswählen
        textField.setOnAction(event -> {
            if (!suggestionsListView.getItems().isEmpty()) {
                suggestionsListView.getSelectionModel().selectFirst();
                // Der Listener oben kümmert sich um den Rest
            }
        });

        // Optional: Pfeiltasten zur Navigation in die Liste (vereinfacht)
        textField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DOWN:
                    if (suggestionsPopup.isShowing() && !suggestionsListView.getItems().isEmpty()) {
                        Platform.runLater(() -> { // Fokus muss im Popup landen können
                            suggestionsListView.requestFocus();
                            suggestionsListView.getSelectionModel().selectFirst();
                        });
                        event.consume();
                    }
                    break;
                case UP:
                    // Ähnlich für Pfeil nach oben, wenn nötig
                    break;
                default:
                    break;
            }
        });

        // Fokusverlust des Popups selbst
        suggestionsPopup.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !textField.isFocused()) {
                hideSuggestions();
            }
        });
    }

    private void performSearch() {
        String searchTerm = textField.getText();
        if (searchTerm == null || searchTerm.isEmpty()) {
            filteredList.setPredicate(p -> false); // Nichts anzeigen
            hideSuggestions();
            return;
        }

        String lowerCaseSearchTerm = searchTerm.toLowerCase();

        // Prädikat für die FilteredList aktualisieren
        Predicate<T> predicate = item -> {
            String itemText = stringConverter.apply(item);
            return itemText != null && itemText.toLowerCase().contains(lowerCaseSearchTerm);
        };
        filteredList.setPredicate(predicate);

        // Popup anzeigen oder verstecken
        if (filteredList.isEmpty()) {
            hideSuggestions();
        } else {
            // Max Höhe für ListView setzen, falls gewünscht (besser via CSS)
            // suggestionsListView.setPrefHeight(Math.min(filteredList.size() * 28, 150)); // Beispiel: Dynamische Höhe
            showSuggestions();
        }
    }

    private void showSuggestions() {
        if (suggestionsPopup.isShowing()) {
            return;
        }

        Bounds tbScreenBounds = textField.localToScreen(textField.getBoundsInLocal());
        if (tbScreenBounds == null) {
            System.err.println("Couldn't show Popup: Screen Bounds not available for TextField.");
            return;
        }

        double popupX = tbScreenBounds.getMinX();
        double popupY = tbScreenBounds.getMaxY();

        suggestionsPopup.setX(popupX);
        suggestionsPopup.setY(popupY);

        Window owner = textField.getScene().getWindow();
        if (owner != null) {
            suggestionsPopup.show(owner);
        } else {
            System.err.println("Couldn't show Popup: No Owner-Window found.");
            // suggestionsPopup.show(textField.getScene().getRoot().getScene().getWindow()); // last try
        }
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
