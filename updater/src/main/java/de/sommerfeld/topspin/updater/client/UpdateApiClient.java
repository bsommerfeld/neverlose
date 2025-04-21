package de.sommerfeld.topspin.updater.client;

import de.sommerfeld.topspin.updater.model.GitHubRepository;
import de.sommerfeld.topspin.updater.model.ReleaseInfo;

import java.util.concurrent.CompletableFuture;

/**
 * Client for requesting release information from an extern source.
 */
public interface UpdateApiClient {

    /**
     * Requests the information from the newest release for the given repository asynchronous.
     *
     * @param repository The information about the repository.
     * @return A CompletableFuture, which on success contains the ReleaseInfo. Throws an exception or error.
     */
    CompletableFuture<ReleaseInfo> fetchLatestReleaseInfo(GitHubRepository repository);
}
