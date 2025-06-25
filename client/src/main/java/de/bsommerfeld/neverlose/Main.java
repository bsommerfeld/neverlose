package de.bsommerfeld.neverlose;

import de.bsommerfeld.neverlose.bootstrap.Bootstrap;
import de.bsommerfeld.neverlose.bootstrap.LogDirectorySetup;
import de.bsommerfeld.neverlose.bootstrap.NeverLoseBootstrap;

public class Main {

  public static void main(String[] args) {
    LogDirectorySetup.setupLogDirectory();

    Bootstrap topspinBootstrap = new NeverLoseBootstrap();
    topspinBootstrap.start();
  }
}
