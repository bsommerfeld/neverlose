package de.sommerfeld.topspin.updater.model;

/**
 * Represents the information about the repository, in which the updater will be looking for the latest release.
 *
 * @param owner    the GitHub username or organisation name.
 * @param repoName the name of the repository.
 */
public record GitHubRepository(String owner, String repoName) {
}
