package de.sommerfeld.neverlose;

import de.sommerfeld.neverlose.bootstrap.Bootstrap;
import de.sommerfeld.neverlose.bootstrap.LogDirectorySetup;
import de.sommerfeld.neverlose.bootstrap.NeverLoseBootstrap;

public class Main {

    public static void main(String[] args) {
        LogDirectorySetup.setupLogDirectory();

        Bootstrap topspinBootstrap = new NeverLoseBootstrap();
        topspinBootstrap.start();
    }
}