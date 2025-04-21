package de.sommerfeld.topspin.updater.model;

/**
 * Defines the possible state during an update process.
 */
public enum UpdateState {
    IDLE,
    CHECKING,
    DOWNLOADING,
    ERROR
}
