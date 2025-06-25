package de.sommerfeld.neverlose.fx.state;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Holds the shared state related to search functionality.
 * Managed as a Singleton by Guice.
 */
public class SearchState {

    private final StringProperty searchTerm = new SimpleStringProperty("");

    public StringProperty searchTermProperty() {
        return searchTerm;
    }

    public String getSearchTerm() {
        return searchTerm.get();
    }

    public void setSearchTerm(String value) {
        searchTerm.set(value);
    }
}
