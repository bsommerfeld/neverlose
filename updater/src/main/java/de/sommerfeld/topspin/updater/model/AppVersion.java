package de.sommerfeld.topspin.updater.model;

/**
 * Represents the application version.
 *
 * @implNote might later be extended by SemVer-Parsing/comparison.
 */
public record AppVersion(String value) implements Comparable<AppVersion> {

    /**
     * @return Returns an unknown/initial version, specifically "0.0.0".
     * @apiNote 0.0.0 is as a version invalid, therefore will never represent an actual version.
     */
    public static AppVersion unknown() {
        return new AppVersion("0.0.0");
    }

    @Override
    public int compareTo(AppVersion other) {
        return this.value.compareTo(other.value);
    }
}
