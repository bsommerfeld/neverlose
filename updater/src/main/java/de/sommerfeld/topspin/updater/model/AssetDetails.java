package de.sommerfeld.topspin.updater.model;

import java.net.URL;

/**
 * Holds details about a downloadable release asset.
 */
public record AssetDetails(URL downloadUrl, long size) {
}
