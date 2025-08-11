package de.bsommerfeld.neverlose.fx.controller;

import de.bsommerfeld.neverlose.fx.messages.Messages;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.awt.Desktop;
import java.net.URI;

/** Controller for the bottom bar of the application. Provides branding information. */
@View
public class BottomBarController {

    private static final LogFacade log = LogFacadeFactory.getLogger();
    private final DoubleProperty textWidth = new SimpleDoubleProperty(0.0);
    @FXML
    private Hyperlink githubCta;
    @FXML
    private HBox githubCtaText;
    private Timeline expandTl;
    private Timeline collapseTl;

    @FXML
    private void initialize() {
        // Start in collapsed state: width 0 and invisible
        Rectangle clip = new Rectangle();
        clip.heightProperty().bind(githubCtaText.heightProperty());
        clip.widthProperty().bind(textWidth);
        githubCtaText.setClip(clip);
        githubCtaText.maxWidthProperty().bind(textWidth);
        githubCtaText.prefWidthProperty().bind(textWidth);
        githubCtaText.setOpacity(0.0);

        Platform.runLater(() -> {
            try {
                // Temporarily unbind to measure natural width
                githubCtaText.maxWidthProperty().unbind();
                githubCtaText.prefWidthProperty().unbind();
                githubCtaText.setClip(null);
                githubCtaText.setOpacity(1.0);
                githubCtaText.setPrefWidth(Region.USE_COMPUTED_SIZE);
                githubCtaText.setMaxWidth(Region.USE_COMPUTED_SIZE);
                githubCtaText.applyCss();
                githubCtaText.layout();
                double target = Math.ceil(githubCtaText.prefWidth(-1));
                if (Double.isNaN(target) || target <= 0.0) {
                    target = Math.ceil(githubCtaText.getLayoutBounds().getWidth());
                }

                // Restore collapsed state and re-bind
                Rectangle clip2 = new Rectangle();
                clip2.heightProperty().bind(githubCtaText.heightProperty());
                clip2.widthProperty().bind(textWidth);
                githubCtaText.setClip(clip2);
                githubCtaText.maxWidthProperty().bind(textWidth);
                githubCtaText.prefWidthProperty().bind(textWidth);
                githubCtaText.setOpacity(0.0);
                textWidth.set(0.0);

                setupAnimations(target);

                // Wire hover handlers
                githubCta.setOnMouseEntered(e -> playExpand());
                githubCta.setOnMouseExited(e -> playCollapse());
            } catch (Exception ex) {
                log.error("Failed to initialize GitHub CTA animation: " + ex.getMessage(), ex);
            }
        });
    }

    private void setupAnimations(double expandedWidth) {
        expandTl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(textWidth, textWidth.get(), Interpolator.EASE_BOTH),
                        new KeyValue(githubCtaText.opacityProperty(), githubCtaText.getOpacity(), Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.millis(260),
                        new KeyValue(textWidth, expandedWidth, Interpolator.EASE_BOTH),
                        new KeyValue(githubCtaText.opacityProperty(), 1.0, Interpolator.EASE_BOTH)
                )
        );

        collapseTl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(textWidth, textWidth.get(), Interpolator.EASE_BOTH),
                        new KeyValue(githubCtaText.opacityProperty(), githubCtaText.getOpacity(), Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(textWidth, 0.0, Interpolator.EASE_BOTH),
                        new KeyValue(githubCtaText.opacityProperty(), 0.0, Interpolator.EASE_BOTH)
                )
        );
    }

    private void playExpand() {
        if (collapseTl != null) {
            collapseTl.stop();
        }
        if (expandTl != null) {
            expandTl.playFromStart();
        }
    }

    private void playCollapse() {
        if (expandTl != null) {
            expandTl.stop();
        }
        if (collapseTl != null) {
            collapseTl.playFromStart();
        }
    }

    @FXML
    private void handleSommerfeldLink() {
        try {
            String githubUrl = Messages.getString("url.github");
            // Use the appropriate method based on the platform
            if (Platform.isFxApplicationThread()) {
                // Try to use Desktop API first
                Desktop.getDesktop().browse(new URI(githubUrl));
            } else {
                // Fallback to Runtime exec (Linux)
                Runtime.getRuntime().exec("xdg-open " + githubUrl);
            }
        } catch (Exception e) {
            log.error(Messages.getString("error.url.openFailed", e.getMessage()), e);
        }
    }
}
