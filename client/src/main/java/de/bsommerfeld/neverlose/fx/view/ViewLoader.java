package de.bsommerfeld.neverlose.fx.view;

import com.google.inject.Inject;
import com.google.inject.Injector;
import de.bsommerfeld.neverlose.fx.messages.MessagesResourceBundle;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class ViewLoader {

  private final Injector injector;

  @Inject
  public ViewLoader(Injector injector) {
    this.injector = injector;
  }

  /**
   * Loads a view and its controller from an FXML file associated with the specified class.
   *
   * @param <T> the type of the controller
   * @param clazz the class of the controller for the corresponding FXML file
   * @return a {@link ViewWrapper} containing the loaded view and its controller
   * @throws IllegalStateException if the FXML file could not be found or loaded
   */
  public <T> ViewWrapper<T> loadView(Class<T> clazz) {
    FXMLLoader fxmlLoader = new FXMLLoader();

    String name = clazz.getSimpleName().replace("Controller", "");
    URL fxmlLocation = clazz.getResource(name + ".fxml");
    if (fxmlLocation == null) {
      throw new IllegalStateException(
          MessageFormat.format("FXML File not found for class: {0}", clazz));
    }

    fxmlLoader.setLocation(fxmlLocation);
    fxmlLoader.setControllerFactory(injector::getInstance);

    // Set the resource bundle for internationalization
    ResourceBundle resourceBundle = new MessagesResourceBundle();
    fxmlLoader.setResources(resourceBundle);

    try {
      Parent parent = fxmlLoader.load();
      T controller = fxmlLoader.getController();
      return new ViewWrapper<>(parent, controller);
    } catch (IOException e) {
      throw new IllegalStateException(
          MessageFormat.format("FXML for class: {0} could not be loaded.", clazz), e);
    }
  }
}
