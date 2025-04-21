package de.sommerfeld.topspin.updater.model;

import java.net.URL;
import java.util.Map;

/**
 * Contains relevant information about a release of a repository.
 *
 * @param latestVersion   the version of the release.
 * @param releaseNotesUrl URL to the release notes (optional).
 * @param assets          map for the asset identifier ("windows-x64", "macos-aarch64", etc) with their respective URL
 */
public record ReleaseInfo(AppVersion latestVersion, URL releaseNotesUrl, Map<String, AssetDetails> assets) {
}
