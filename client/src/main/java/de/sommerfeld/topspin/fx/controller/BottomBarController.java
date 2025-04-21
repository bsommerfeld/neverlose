package de.sommerfeld.topspin.fx.controller;

import com.google.inject.Inject;
import de.sommerfeld.topspin.fx.view.View;
import de.sommerfeld.topspin.fx.view.ViewProvider;

@View
public class BottomBarController {

    private final ViewProvider viewProvider;

    @Inject
    public BottomBarController(ViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }
}
