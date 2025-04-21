package de.sommerfeld.topspin.updater.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import de.sommerfeld.topspin.logger.LogFacade;
import de.sommerfeld.topspin.logger.LogFacadeFactory;
import de.sommerfeld.topspin.updater.model.AppVersion;
import de.sommerfeld.topspin.updater.model.AssetDetails;
import de.sommerfeld.topspin.updater.model.GitHubRepository;
import de.sommerfeld.topspin.updater.model.ReleaseInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of UpdateApiClient using Java's HttpClient
 * to fetch release information from the GitHub API.
 * Dependencies are injected via Guice.
 */
public class GitHubApiHttpClient implements UpdateApiClient {

    private static final LogFacade log = LogFacadeFactory.getLogger();

    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance with injected dependencies.
     *
     * @param httpClient   The HttpClient instance provided by Guice.
     * @param objectMapper The ObjectMapper instance provided by Guice.
     */
    @Inject
    public GitHubApiHttpClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public CompletableFuture<ReleaseInfo> fetchLatestReleaseInfo(GitHubRepository repository) {
        Objects.requireNonNull(repository, "repository must not be null");

        String apiUrl = String.format("%s/repos/%s/%s/releases/latest",
                GITHUB_API_BASE, repository.owner(), repository.repoName());

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(apiUrl))
                    .header("Accept", "application/vnd.github.v3+json") // Standard GitHub API v3 header
                    .header("User-Agent", "TopSpin-Updater/1.0")
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            log.error("Internal error: Invalid API URL generated: {}", apiUrl, e);
            return CompletableFuture.failedFuture(
                    new RuntimeException("Failed to create GitHub API request URI", e)
            );
        }

        log.debug("Fetching latest release info from: {}", apiUrl);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    log.trace("Received HTTP status: {}", response.statusCode());
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        String errorMsg = String.format("GitHub API request failed: Status %d, Body: %s",
                                response.statusCode(), response.body().substring(0, Math.min(500, response.body().length())));
                        log.error(errorMsg);
                        throw new RuntimeException("GitHub API request failed with status: " + response.statusCode());
                    }
                    return response.body();
                })
                .thenApply(jsonBody -> {
                    try {
                        log.trace("Parsing JSON response body");
                        GitHubReleaseDTO releaseDTO = objectMapper.readValue(jsonBody, GitHubReleaseDTO.class);
                        log.trace("Mapping DTO to ReleaseInfo");
                        return mapDtoToReleaseInfo(releaseDTO);
                    } catch (IOException e) {
                        String errorMsg = "Failed to parse GitHub API JSON response";
                        log.error(errorMsg, e);
                        throw new RuntimeException(errorMsg, e);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                })
                .exceptionally(ex -> {
                    String errorMsg = "Failed to fetch or process release info";
                    log.error(errorMsg, ex);
                    System.err.println(errorMsg + ": " + ex.getMessage());
                    throw new RuntimeException(errorMsg, ex);
                });
    }

    /**
     * Maps the raw DTO from GitHub API to our domain model ReleaseInfo.
     * Includes logic to find the appropriate download asset for the current platform.
     */
    private ReleaseInfo mapDtoToReleaseInfo(GitHubReleaseDTO dto) throws MalformedURLException, URISyntaxException {
        AppVersion version = new AppVersion(dto.tagName.startsWith("v") ? dto.tagName.substring(1) : dto.tagName);
        URL releaseNotesUrl = (dto.htmlUrl != null) ? new URI(dto.htmlUrl).toURL() : null;
        Map<String, AssetDetails> assets = findRelevantAssets(dto.assets);
        return new ReleaseInfo(version, releaseNotesUrl, assets);
    }

    /**
     * Finds the relevant download URLs and sizes from the list of assets for the current OS/Arch.
     *
     * @param assets List of asset DTOs from the GitHub API.
     * @return A Map where the key identifies the asset (e.g., "windows-x64")
     * and the value is an AssetDetails object containing URL and size.
     */
    private Map<String, AssetDetails> findRelevantAssets(List<GitHubAssetDTO> assets) {
        if (assets == null || assets.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, AssetDetails> relevantAssets = new HashMap<>();
        String currentOs = System.getProperty("os.name").toLowerCase();
        String currentArch = System.getProperty("os.arch").toLowerCase();

        String targetOsArch = null;
        String expectedSuffix = null;

        if (currentOs.contains("win")) {
            targetOsArch = "windows-x64";
            expectedSuffix = "-windows-x64.exe";
        } else if (currentOs.contains("mac")) {
            if (currentArch.contains("aarch64")) {
                targetOsArch = "macos-aarch64";
                expectedSuffix = "-macos-aarch64.dmg";
            } else { // Assume x64 otherwise for Mac
                targetOsArch = "macos-x64";
                expectedSuffix = "-macos-x64.dmg";
            }
        } else if (currentOs.contains("linux")) {
            if (currentArch.contains("amd64") || currentArch.contains("x86_64")) {
                targetOsArch = "linux-amd64";
                expectedSuffix = "_amd64.deb";
            }
        }


        if (targetOsArch != null && expectedSuffix != null) {
            log.debug("Looking for asset suffix: {}", expectedSuffix);
            for (GitHubAssetDTO asset : assets) {
                if (asset.name != null && asset.name.endsWith(expectedSuffix) && asset.browserDownloadUrl != null) {
                    try {
                        URL downloadUrl = new URI(asset.browserDownloadUrl).toURL();
                        long assetSize = asset.size; // <-- Größe hier holen!
                        AssetDetails details = new AssetDetails(downloadUrl, assetSize);
                        relevantAssets.put(targetOsArch, details);
                        log.info("Found matching asset for {}: {} ({} bytes)", targetOsArch, asset.name, assetSize);
                        break;
                    } catch (MalformedURLException | URISyntaxException e) {
                        log.warn("Invalid download URL for asset {}: {}", asset.name, asset.browserDownloadUrl, e);
                    }
                }
            }
        } else {
            log.warn("Could not determine target OS/Arch or expected asset suffix for os={}, arch={}", currentOs, currentArch);
        }

        if (relevantAssets.isEmpty()) {
            log.warn("No suitable asset found for the current platform (os={}, arch={}) in release.", currentOs, currentArch);
        }

        return Collections.unmodifiableMap(relevantAssets);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GitHubReleaseDTO {
        @JsonProperty("tag_name")
        public String tagName;

        @JsonProperty("html_url")
        public String htmlUrl;

        @JsonProperty("assets")
        public List<GitHubAssetDTO> assets;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GitHubAssetDTO {
        @JsonProperty("name")
        public String name;

        @JsonProperty("browser_download_url")
        public String browserDownloadUrl;

        @JsonProperty("size")
        public long size; // Useful for download progress
    }
}