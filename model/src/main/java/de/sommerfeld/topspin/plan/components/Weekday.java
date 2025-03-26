package de.sommerfeld.topspin.plan.components;

/**
 * Represents the days of the week as an enumeration.
 * Each constant corresponds to a day, with an associated display name.
 * Provides a textual representation of the day through the {@code toString} method.
 */
public enum Weekday {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    private final String displayName;

    Weekday(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
